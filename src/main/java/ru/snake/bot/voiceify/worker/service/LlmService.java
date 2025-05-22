package ru.snake.bot.voiceify.worker.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.snake.bot.voiceify.Resource;
import ru.snake.bot.voiceify.database.Language;
import ru.snake.bot.voiceify.settings.Settings;
import ru.snake.bot.voiceify.text.Replacer;
import ru.snake.bot.voiceify.util.SentenceIterator;
import ru.snake.bot.voiceify.util.TextUtil;
import ru.snake.bot.voiceify.worker.Translation;
import ru.snake.bot.voiceify.worker.backend.LlmBackend;
import ru.snake.bot.voiceify.worker.backend.LlmBackendException;

public class LlmService {

	private static final Logger LOG = LoggerFactory.getLogger(LlmService.class);

	private final LlmBackend backend;

	private final String modelName;

	private final int contextLength;

	private final int maxFragmentChars;

	public LlmService(
		final LlmBackend backend,
		final String modelName,
		final int contextLength,
		final int maxFragmentChars
	) {
		this.backend = backend;
		this.modelName = modelName;
		this.contextLength = contextLength;
		this.maxFragmentChars = maxFragmentChars;
	}

	public String translateText(String text, Language language) throws IOException, LlmBackendException {
		if (!Translation.isNeedTranslation(text, language)) {
			return text;
		}

		String languageName = Translation.languageName(language);
		String prompt = Replacer
			.replace(Resource.asText("prompts/text_translate.txt"), Map.of("language", languageName));
		String result = executeQuery(text, prompt);

		return result;
	}

	public String writeCaption(String text) throws LlmBackendException, IOException {
		LOG.info("Write caption for `{}`", TextUtil.trimText(text, 256));

		String message = Resource.asText("prompts/text_caption.txt");
		String head = TextUtil.trimText(text, contextLength - message.length());
		String caption = textQuery(Replacer.replace(message, Map.of("text", head)));

		return TextUtil.unquoteText(caption);
	}

	public String subsToArticle(String text) throws IOException, LlmBackendException {
		LOG.info("Convert subs to text: `{}`", TextUtil.trimText(text, 256));

		String prompt = Resource.asText("prompts/text_article.txt");
		String result = executeQuery(text, prompt);

		return result;
	}

	public String shortenArticle(String text) throws IOException, LlmBackendException {
		if (text.length() < maxFragmentChars) {
			return text;
		}

		LOG.info("Shorten text: `{}`", TextUtil.trimText(text, 256));

		String prompt = Resource.asText("prompts/text_shorten.txt");
		String result = text;

		while (result.length() >= maxFragmentChars) {
			result = executeQuery(result, prompt);
		}

		return result;
	}

	private String executeQuery(String text, String prompt) throws LlmBackendException {
		if (prompt.length() + text.length() <= contextLength) {
			String result = textQuery(prompt + text);

			return result;
		} else {
			StringBuilder builder = new StringBuilder();
			StringBuilder result = new StringBuilder();

			for (String line : new SentenceIterator(text)) {
				if (prompt.length() + builder.length() + line.length() >= contextLength) {
					String page = textQuery(prompt + builder.toString());

					result.append(page);
					builder.setLength(0);
				}

				builder.append(line);
			}

			if (builder.length() > 0) {
				String page = textQuery(prompt + builder.toString());

				result.append(page);
			}

			return result.toString();
		}
	}

	private String textQuery(String... messages) throws LlmBackendException {
		if (LOG.isInfoEnabled()) {
			LOG.info("Execute text query: {}", Arrays.asList(messages));
		}

		String result = backend.chat(modelName, messages);

		LOG.info("Query result: {}", result);

		return result;
	}

	@Override
	public String toString() {
		return "LlmService [backend=" + backend + ", modelName=" + modelName + ", contextLength=" + contextLength
				+ ", maxFragmentChars=" + maxFragmentChars + "]";
	}

	public static LlmService create(Settings settings) {
		return new LlmService(
			LlmBackend.from(settings),
			settings.getModelName(),
			settings.getContextLength(),
			settings.getMaxFragmentChars()
		);
	}

}
