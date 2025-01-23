package ru.snake.bot.voiceify.worker;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResult;
import ru.snake.bot.voiceify.Resource;
import ru.snake.bot.voiceify.text.Replacer;

public class Worker {

	private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

	private final OllamaAPI ollamaApi;

	private final String modelName;

	public Worker(final OllamaAPI ollamaApi, final String modelName) {
		this.ollamaApi = ollamaApi;
		this.modelName = modelName;
	}

	//	public synchronized ProfileResult profileDescription(String text, Language language)
	//			throws OllamaBaseException, IOException, InterruptedException {
	//		String description = textQuery(
	//			Replacer.replace(Resource.asText("prompts/create_profile.txt"), Map.of("text", text))
	//		);
	//		String result = translate(description, language);
	//		List<ProfileDescription> descriptions = new HeaderTextSplitter("##", "**").split(result);
	//
	//		return new ProfileResult(descriptions);
	//	}
	//
	//	public synchronized OpenersResult writeOpeners(File file, Language language)
	//			throws OllamaBaseException, IOException, InterruptedException {
	//		LOG.info("Generation openers for {}", file);
	//
	//		checkFiles(file);
	//
	//		String imageDescription = imageQuery(file, Resource.asText("prompts/image_description.txt"));
	//		String imageObjects = imageQuery(file, Resource.asText("prompts/image_objects.txt"));
	//		String initialPhrases = textQuery(
	//			Replacer.replace(
	//				Resource.asText("prompts/text_openers.txt"),
	//				Map.of(
	//					"image_description",
	//					imageDescription,
	//					"image_objects",
	//					imageObjects,
	//					"language",
	//					language.getName()
	//				)
	//			)
	//		);
	//		String translatedPhrases = translate(initialPhrases, language);
	//		List<TextList> openers = new ListTextSplitter(Set.of("##", "**"), Set.of("*")).split(translatedPhrases);
	//
	//		return OpenersResult.from(imageDescription, imageObjects, initialPhrases, translatedPhrases, openers);
	//	}
	//
	//	public synchronized OpenersResult writeOpeners(File file, String description, Language language)
	//			throws OllamaBaseException, IOException, InterruptedException {
	//		LOG.info("Generation openers for {}", file);
	//
	//		checkFiles(file);
	//
	//		String imageDescription = imageQuery(file, Resource.asText("prompts/image_description.txt"));
	//		String imageObjects = imageQuery(file, Resource.asText("prompts/image_objects.txt"));
	//		String initialPhrases = textQuery(
	//			Replacer.replace(
	//				Resource.asText("prompts/text_openers_description.txt"),
	//				Map.of(
	//					"image_description",
	//					imageDescription,
	//					"image_objects",
	//					imageObjects,
	//					"profile_description",
	//					description,
	//					"language",
	//					language.getName()
	//				)
	//			)
	//		);
	//		String translatedPhrases = translate(initialPhrases, language);
	//		List<TextList> openers = new ListTextSplitter(Set.of("##", "**"), Set.of("*")).split(translatedPhrases);
	//
	//		return OpenersResult.from(imageDescription, imageObjects, initialPhrases, translatedPhrases, openers);
	//	}
	//
	//	public synchronized ConverationResult continueConveration(String text, Language language)
	//			throws OllamaBaseException, IOException, InterruptedException {
	//		String alternatives = textQuery(
	//			Replacer.replace(Resource.asText("prompts/continue_converation.txt"), Map.of("text", text))
	//		);
	//		String result = translate(alternatives, language);
	//		List<TextList> descriptions = new ListTextSplitter(
	//			Set.of("##", "**"),
	//			Set.of("*", "1", "2", "3", "4", "5", "6", "7", "8", "9")
	//		).split(result);
	//
	//		return ConverationResult.from(descriptions);
	//	}
	//
	//	private String translate(String text, Language language)
	//			throws OllamaBaseException, IOException, InterruptedException {
	//		if (language.isTranslated(text)) {
	//			return text;
	//		}
	//
	//		return textQuery(
	//			Replacer.replace(
	//				Resource.asText("prompts/text_translate.txt"),
	//				Map.of("text", text, "language", language.getName())
	//			)
	//		);
	//	}
	//
	//	private String textQuery(String... messages) throws OllamaBaseException, IOException, InterruptedException {
	//		if (LOG.isInfoEnabled()) {
	//			LOG.info("Execute text query: {}", Arrays.asList(messages));
	//		}
	//
	//		OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(textModelName);
	//
	//		for (String message : messages) {
	//			builder.withMessage(OllamaChatMessageRole.USER, message);
	//		}
	//
	//		OllamaChatRequestModel request = builder.build();
	//		OllamaChatResult chat = ollama.chat(request);
	//		String result = chat.getResponse();
	//
	//		LOG.info("Query result: {}", result);
	//
	//		return result;
	//	}
	//
	//	private String imageQuery(File file, String... messages)
	//			throws OllamaBaseException, IOException, InterruptedException {
	//		if (messages.length == 0) {
	//			return "";
	//		}
	//
	//		if (LOG.isInfoEnabled()) {
	//			LOG.info("Execute image query: {}", Arrays.asList(messages));
	//		}
	//
	//		StringBuilder builder = new StringBuilder();
	//		OllamaChatRequestModel request = OllamaChatRequestBuilder.getInstance(imageModelName)
	//			.withMessage(OllamaChatMessageRole.USER, messages[0], List.of(file))
	//			.build();
	//		OllamaChatResult chat = imageApi.chat(request);
	//
	//		builder.append(chat.getResponse());
	//
	//		for (int index = 1; index < messages.length; index += 1) {
	//			request = OllamaChatRequestBuilder.getInstance(imageModelName)
	//				.withMessages(chat.getChatHistory())
	//				.withMessage(OllamaChatMessageRole.USER, messages[index])
	//				.build();
	//			chat = imageApi.chat(request);
	//
	//			builder.append("\n\n");
	//			builder.append(chat.getResponse());
	//		}
	//
	//		LOG.info("Query result: {}", builder);
	//
	//		return builder.toString();
	//	}
	//
	//	private static void checkFiles(File... files) {
	//		for (File file : files) {
	//			if (!file.exists()) {
	//				throw new RuntimeException(String.format("Path %s does not exists.", file));
	//			}
	//
	//			if (!file.isFile()) {
	//				throw new RuntimeException(String.format("Path %s is not regular file.", file));
	//			}
	//
	//			if (!file.canRead()) {
	//				throw new RuntimeException(String.format("Path %s is not readable.", file));
	//			}
	//		}
	// }

	@Override
	public String toString() {
		return "Worker [ollamaApi=" + ollamaApi + ", modelName=" + modelName + "]";
	}

	public static Worker create(WorkerSettings settings) {
		OllamaAPI ollamaApi = new OllamaAPI(settings.getOllamaUri());
		ollamaApi.setRequestTimeoutSeconds(settings.getTimeout());
		ollamaApi.setVerbose(false);

		return new Worker(ollamaApi, settings.getModelName());
	}

}
