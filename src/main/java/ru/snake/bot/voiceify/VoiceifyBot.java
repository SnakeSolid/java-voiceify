package ru.snake.bot.voiceify;

import java.io.File;
import java.io.IOException;
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
import ru.snake.bot.voiceify.text.Replacer;
import ru.snake.bot.voiceify.worker.Worker;
import ru.snake.bot.voiceify.worker.data.ArticleResult;
import ru.snake.bot.voiceify.worker.data.CaptionResult;
import ru.snake.bot.voiceify.worker.data.TextToSpeechResult;

public class VoiceifyBot extends BotClientConsumer implements LongPollingSingleThreadUpdateConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(VoiceifyBot.class);

	private static final String CALLBACK_TEXT = ":text";

	private static final String CALLBACK_LINK = ":link";

	private final Database database;

	private final Worker worker;

	public VoiceifyBot(final String botToken, final Set<Long> whiteList, final Database database, final Worker worker) {
		super(botToken, whiteList);

		this.database = database;
		this.worker = worker;

		onMessage(this::processMessage);
		onMessage(this::processMessageUrls);
		onCommand("/start", this::commandStart);
		onCommand("/help", this::commandHelp);
		onCommand(this::commandInvalid);
		onCallback(CALLBACK_TEXT, this::readAsText);
		onCallback(CALLBACK_LINK, this::readAsLink);
		onCallback(this::callbackInvalid);
		onAccessDenied(this::accessDenied);
	}

	public void readAsText(final Context context, final String queryId, final String command) throws Exception {
		sendCallbackAnswer(queryId);

		ChatState state = database.getChatState(context.getChatId());
		readText(context, state.getText());
	}

	public void readAsLink(final Context context, final String queryId, final String command) throws Exception {
		sendCallbackAnswer(queryId);
		ChatState state = database.getChatState(context.getChatId());
		readLinks(context, state.getUriStrings());
	}

	private void processMessage(final Context context, final String text) throws Exception {
		readText(context, text);
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
			readLinks(context, urlStrings);
		} else {
			ChatState state = ChatState.create(text, urlStrings);
			database.setChatState(context.getChatId(), state);

			sendMessage(context.getChatId(), Resource.asText("texts/message_links.txt"), massageTypeMenu());
		}
	}

	private ReplyKeyboard massageTypeMenu() {
		InlineKeyboardRow rowTwo = new InlineKeyboardRow();
		rowTwo.add(InlineKeyboardButton.builder().text("Прочитат текст").callbackData(CALLBACK_TEXT).build());
		rowTwo.add(InlineKeyboardButton.builder().text("Озвучить ссылки").callbackData(CALLBACK_LINK).build());
		ReplyKeyboard keyboard = InlineKeyboardMarkup.builder().keyboardRow(rowTwo).build();

		return keyboard;
	}

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

	// Read functions

	private void readText(final Context context, final String text)
			throws IOException, InterruptedException, OllamaBaseException {
		TextToSpeechResult resultTts = worker.textToSpeech(text);
		CaptionResult resultCaption = worker.writeCaption(text);

		if (resultTts.isSuccess()) {
			File speechPath = resultTts.getSpeechPath();

			replyVoice(context.getChatId(), context.getMessageId(), resultCaption.getCaption(), speechPath);

			speechPath.delete();
		} else {
			sendMessage(context.getChatId(), "Failed to convert text: " + resultTts.getMessage());
		}
	}

	private void readLinks(final Context context, final List<String> urlStrings)
			throws IOException, InterruptedException {
		for (String uri : urlStrings) {
			ArticleResult resultArticle = worker.articleText(uri);
			TextToSpeechResult resultTts = worker.textToSpeech(resultArticle.getText());

			if (resultTts.isSuccess()) {
				File speechPath = resultTts.getSpeechPath();

				replyVoice(context.getChatId(), context.getMessageId(), resultArticle.getTitle(), speechPath);

				speechPath.delete();
			} else {
				sendMessage(context.getChatId(), "Failed to convert article: " + resultTts.getMessage());
			}
		}
	}

	// Basic commands.

	private void commandStart(final Context context, final String command) throws IOException {
		sendMessage(context.getChatId(), Resource.asText("texts/command_start.txt"));
	}

	private void commandHelp(final Context context, final String command) throws IOException {
		sendMessage(context.getChatId(), Resource.asText("texts/command_help.txt"));
	}

	private void commandInvalid(final Context context, final String command) throws IOException {
		LOG.warn("Unknown bot command: {}", command);
	}

}
