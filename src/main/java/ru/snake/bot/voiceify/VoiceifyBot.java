package ru.snake.bot.voiceify;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;

import ru.snake.bot.voiceify.consume.BotClientConsumer;
import ru.snake.bot.voiceify.consume.Context;
import ru.snake.bot.voiceify.database.ChatState;
import ru.snake.bot.voiceify.database.Database;
import ru.snake.bot.voiceify.text.Replacer;
import ru.snake.bot.voiceify.worker.Worker;

public class VoiceifyBot extends BotClientConsumer implements LongPollingSingleThreadUpdateConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(VoiceifyBot.class);

	private static final String CALLBACK_SETTINGS = ":settings";

	private static final String CALLBACK_TEST = ":text";

	private static final String CALLBACK_ARTICLE = ":article";

	private static final String CALLBACK_VIDEO = ":video";

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
		onCallback(this::callbackInvalid);
		onAccessDenied(this::accessDenied);
	}

	private void processMessage(final Context context, final String text) throws Exception {
		ChatState chatState = database.getChatState(context.getChatId());

		if (chatState == ChatState.DEFAULT) {
			sendMessage(context.getChatId(), text);
		} else {
			unknownState(context.getChatId());
		}
	}

	private void processMessageUrls(final Context context, final String text, final List<String> urlStrings)
			throws Exception {
		ChatState chatState = database.getChatState(context.getChatId());

		if (chatState == ChatState.DEFAULT) {
			sendMessage(context.getChatId(), urlStrings.toString());
		} else {
			unknownState(context.getChatId());
		}
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

	private void commandStart(final Context context, final String command) throws IOException {
		sendMessage(context.getChatId(), Resource.asText("texts/command_start.txt"));
	}

	private void commandHelp(final Context context, final String command) throws IOException {
		sendMessage(context.getChatId(), Resource.asText("texts/command_help.txt"));
	}

	private void commandInvalid(final Context context, final String command) throws IOException {
		LOG.warn("Unknown bot command: {}", command);
	}

	private void unknownState(long chatId) throws IOException {
		sendMessage(chatId, Resource.asText("texts/unknown_state.txt"));
	}

}
