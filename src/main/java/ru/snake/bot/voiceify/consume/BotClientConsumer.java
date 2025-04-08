package ru.snake.bot.voiceify.consume;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import ru.snake.bot.voiceify.text.Escaper;

public class BotClientConsumer extends UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(BotClientConsumer.class);

	private final String botToken;

	private final OkHttpTelegramClient telegramClient;

	public BotClientConsumer(String botToken, Set<Long> whiteList) {
		super(whiteList);

		this.botToken = botToken;
		this.telegramClient = new OkHttpTelegramClient(botToken);
	}

	protected Result<String> getFileContent(String fileId) {
		GetFile getFile = GetFile.builder().fileId(fileId).build();

		try {
			String uri = telegramClient.execute(getFile).getFileUrl(botToken);
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			String content = response.body();

			return Result.success(content);
		} catch (TelegramApiException e) {
			LOG.warn("Failed get file URI.", e);

			return Result.error(e.getMessage());
		} catch (IOException | InterruptedException e) {
			LOG.warn("Failed read file content.", e);

			return Result.error(e.getMessage());
		}
	}

	protected void sendVoice(long chatId, String text, File audioPath) {
		InputFile audioFile = new InputFile(audioPath);
		SendVoice voice = SendVoice.builder()
			.chatId(chatId)
			.parseMode(ParseMode.MARKDOWNV2)
			.caption(Escaper.escapeMarkdown(text))
			.voice(audioFile)
			.build();

		try {
			telegramClient.execute(voice);
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send voice message.", e);
		}
	}

	protected void replyVoice(long chatId, int messageId, String text, File audioPath) {
		InputFile audioFile = new InputFile(audioPath);
		SendVoice voice = SendVoice.builder()
			.chatId(chatId)
			.replyToMessageId(messageId)
			.parseMode(ParseMode.MARKDOWNV2)
			.caption(text)
			.voice(audioFile)
			.build();

		try {
			telegramClient.execute(voice);
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send voice message.", e);
		}
	}

	protected void sendMessage(long chatId, String text) {
		SendMessage message = SendMessage.builder()
			.chatId(chatId)
			.parseMode(ParseMode.MARKDOWNV2)
			.text(Escaper.escapeMarkdown(text))
			.build();

		try {
			telegramClient.execute(message);
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send message.", e);
		}
	}

	protected void sendMessage(long chatId, final String text, final ReplyKeyboard keyboard) {
		SendMessage message = SendMessage.builder()
			.chatId(chatId)
			.parseMode(ParseMode.MARKDOWNV2)
			.text(Escaper.escapeMarkdown(text))
			.replyMarkup(keyboard)
			.build();

		try {
			telegramClient.execute(message);
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send message.", e);
		}
	}

	protected void sendCallbackAnswer(String queryId) {
		AnswerCallbackQuery answer = AnswerCallbackQuery.builder().callbackQueryId(queryId).build();

		try {
			telegramClient.execute(answer);
		} catch (TelegramApiException e) {
			LOG.warn("Failed to send answer.", e);
		}
	}

	@Override
	public String toString() {
		return "BotClientConsumer [telegramClient=" + telegramClient + "]";
	}

}
