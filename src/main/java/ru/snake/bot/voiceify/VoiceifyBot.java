package ru.snake.bot.voiceify;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import io.github.ollama4j.exceptions.OllamaBaseException;
import ru.snake.bot.voiceify.consume.BotClientConsumer;
import ru.snake.bot.voiceify.consume.Context;
import ru.snake.bot.voiceify.database.ChatState;
import ru.snake.bot.voiceify.database.Database;
import ru.snake.bot.voiceify.database.Language;
import ru.snake.bot.voiceify.settings.Settings;
import ru.snake.bot.voiceify.text.Replacer;
import ru.snake.bot.voiceify.util.DomainMatcher;
import ru.snake.bot.voiceify.worker.Worker;

public class VoiceifyBot extends BotClientConsumer implements LongPollingSingleThreadUpdateConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(VoiceifyBot.class);

	private static final String CALLBACK_TEXT = ":text";

	private static final String CALLBACK_LINK = ":link";

	private static final String CALLBACK_IGNORE = ":language_ignore";

	private static final String CALLBACK_ENGLISH = ":language_en";

	private static final String CALLBACK_RUSSIAN = ":language_ru";

	private static final Map<String, Language> LANGUAGE_MAP = Map.ofEntries(
		Map.entry(CALLBACK_IGNORE, Language.IGNORE),
		Map.entry(CALLBACK_ENGLISH, Language.ENGLISH),
		Map.entry(CALLBACK_RUSSIAN, Language.RUSSIAN)
	);

	private static final Language DEAFULT_LANGUAGE = Language.IGNORE;

	private final Settings settings;

	private final Database database;

	private final Worker worker;

	public VoiceifyBot(
		final String botToken,
		final Set<Long> whiteList,
		final Settings settings,
		final Database database,
		final Worker worker
	) {
		super(botToken, whiteList);

		this.settings = settings;
		this.database = database;
		this.worker = worker;

		onMessage(this::processMessage);
		onMessage(this::processMessageUrls);
		onCommand("/start", this::commandStart);
		onCommand("/status", this::commandStatus);
		onCommand("/settings", this::commandSettings);
		onCommand("/help", this::commandHelp);
		onCommand(this::commandInvalid);
		onCallback(CALLBACK_TEXT, this::readAsText);
		onCallback(CALLBACK_LINK, this::readAsLink);
		onCallback(CALLBACK_IGNORE, this::setLanguage);
		onCallback(CALLBACK_ENGLISH, this::setLanguage);
		onCallback(CALLBACK_RUSSIAN, this::setLanguage);
		onCallback(this::callbackInvalid);
		onAccessDenied(this::accessDenied);
	}

	public void sendVoiceMessage(final long chatId, final int messageId, final String caption, final File path) {
		replyVoice(chatId, messageId, caption, path);
	}

	public void logError(final long chatId, final int messageId, final String uri, final String message)
			throws IOException {
		if (uri != null) {
			Map<String, Object> params = Map.of("uri", uri, "message", message);

			sendMessage(chatId, Replacer.replace(Resource.asText("texts/error_uri.txt"), params));
		} else {
			Map<String, Object> params = Map.of("message", message);

			sendMessage(chatId, Replacer.replace(Resource.asText("texts/error_text.txt"), params));
		}
	}

	// Message handlers

	private void processMessage(final Context context, final String text) throws Exception {
		readText(context.getChatId(), context.getMessageId(), text);
	}

	private void processMessageUrls(final Context context, final String text, final List<MessageEntity> linkEntities)
			throws Exception {
		List<String> urlStrings = new ArrayList<>();

		for (MessageEntity entity : linkEntities) {
			if (entity.getUrl() != null) {
				urlStrings.add(entity.getUrl());
			} else if (entity.getText() != null) {
				urlStrings.add(entity.getText());
			}
		}

		if (linkEntities.size() == 1 && text.length() <= linkEntities.get(0).getLength()) {
			readLinks(context.getChatId(), context.getMessageId(), urlStrings);
		} else {
			ChatState state = ChatState.create(context.getMessageId(), text, urlStrings);
			database.setChatState(context.getChatId(), state);

			sendMessage(context.getChatId(), Resource.asText("texts/message_links.txt"), massageTypeMenu());
		}
	}

	// Bot commands.

	private void commandStart(final Context context, final String command) throws IOException {
		sendMessage(context.getChatId(), Resource.asText("texts/command_start.txt"));
	}

	private void commandStatus(final Context context, final String command) throws IOException {
		Map<String, Integer> params = Map.of("length", worker.getQueueLength());
		boolean processing = worker.isProcessing();

		if (processing) {
			sendMessage(context.getChatId(), Replacer.replace(Resource.asText("texts/status_processing.txt"), params));
		} else {
			sendMessage(context.getChatId(), Replacer.replace(Resource.asText("texts/status_size.txt"), params));
		}
	}

	private void commandSettings(final Context context, final String command) throws IOException {
		Language language = database.getLanguage(context.getChatId(), DEAFULT_LANGUAGE);

		sendMessage(
			context.getChatId(),
			Replacer
				.replace(Resource.asText("texts/command_settings.txt"), Map.of("language", getDisplayName(language))),
			settingsMenu()
		);
	}

	private void commandHelp(final Context context, final String command) throws IOException {
		sendMessage(context.getChatId(), Resource.asText("texts/command_help.txt"));
	}

	private void commandInvalid(final Context context, final String command) throws IOException {
		sendMessage(context.getChatId(), Resource.asText("texts/unknown_command.txt"));
	}

	// Callback actions

	private void readAsText(final Context context, final String queryId, final String command) throws Exception {
		sendCallbackAnswer(queryId);

		ChatState state = database.getChatState(context.getChatId());
		readText(context.getChatId(), state.getMessageId(), state.getText());
	}

	private void readAsLink(final Context context, final String queryId, final String command) throws Exception {
		sendCallbackAnswer(queryId);

		ChatState state = database.getChatState(context.getChatId());
		readLinks(context.getChatId(), state.getMessageId(), state.getUriStrings());
	}

	private void setLanguage(final Context context, final String queryId, final String command) throws Exception {
		sendCallbackAnswer(queryId);

		Language language = LANGUAGE_MAP.getOrDefault(command, DEAFULT_LANGUAGE);
		database.setLanguage(context.getChatId(), language);

		sendMessage(
			context.getChatId(),
			Replacer.replace(Resource.asText("texts/language_set.txt"), Map.of("language", getDisplayName(language)))
		);
	}

	// Basic commands

	private void accessDenied(final Context context) throws IOException {
		sendMessage(
			context.getChatId(),
			Replacer.replace("Access denied. User ID = {user_id}.", Map.of("user_id", context.getUserId()))
		);
	}

	private void callbackInvalid(final Context context, final String queryId, final String callback)
			throws IOException {
		sendCallbackAnswer(queryId);

		LOG.warn("Unknown callback action: {}", callback);
	}

	// Menu buttons

	private ReplyKeyboard massageTypeMenu() {
		InlineKeyboardRow rowTwo = new InlineKeyboardRow();
		rowTwo.add(InlineKeyboardButton.builder().text("Прочитать текст").callbackData(CALLBACK_TEXT).build());
		rowTwo.add(InlineKeyboardButton.builder().text("Озвучить ссылки").callbackData(CALLBACK_LINK).build());
		ReplyKeyboard keyboard = InlineKeyboardMarkup.builder().keyboardRow(rowTwo).build();

		return keyboard;
	}

	private ReplyKeyboard settingsMenu() {
		InlineKeyboardRow rowTwo = new InlineKeyboardRow();
		rowTwo.add(InlineKeyboardButton.builder().text("Оригинал").callbackData(CALLBACK_IGNORE).build());
		rowTwo.add(InlineKeyboardButton.builder().text("English").callbackData(CALLBACK_ENGLISH).build());
		rowTwo.add(InlineKeyboardButton.builder().text("Русский").callbackData(CALLBACK_RUSSIAN).build());
		ReplyKeyboard keyboard = InlineKeyboardMarkup.builder().keyboardRow(rowTwo).build();

		return keyboard;
	}

	// Utility functions

	private void readText(final long chatId, final int massageId, final String text)
			throws IOException, InterruptedException, OllamaBaseException {
		Language language = database.getLanguage(chatId, DEAFULT_LANGUAGE);
		worker.sendText(chatId, massageId, text, language);

		sendQueueStatus(chatId);
	}

	private void readLinks(final long chatId, final int massageId, final List<String> urlStrings) throws Exception {
		Language language = database.getLanguage(chatId, DEAFULT_LANGUAGE);

		for (String uri : urlStrings) {
			String host = URI.create(uri).getHost();

			if (DomainMatcher.match(host, settings.getVideoHosts())) {
				worker.queueVideo(chatId, massageId, uri, language);
			} else {
				worker.queueArticle(chatId, massageId, uri, language);
			}
		}

		sendQueueStatus(chatId);
	}

	private void sendQueueStatus(long chatId) throws IOException {
		Map<String, Integer> params = Map.of("length", worker.getQueueLength());
		boolean processing = worker.isProcessing();

		if (processing) {
			sendMessage(chatId, Replacer.replace(Resource.asText("texts/queue_processing.txt"), params));
		} else {
			sendMessage(chatId, Replacer.replace(Resource.asText("texts/queue_size.txt"), params));
		}
	}

	private String getDisplayName(Language language) {
		switch (language) {
		case IGNORE:
			return "Не переводить";

		case ENGLISH:
			return "Английский";

		case RUSSIAN:
			return "Русский";

		default:
			return "Неизвестно";
		}
	}

}
