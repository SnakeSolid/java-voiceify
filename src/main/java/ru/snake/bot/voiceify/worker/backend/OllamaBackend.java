package ru.snake.bot.voiceify.worker.backend;

import java.io.IOException;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.exceptions.ToolInvocationException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResult;
import ru.snake.bot.voiceify.settings.Settings;

public class OllamaBackend implements LlmBackend {

	private final OllamaAPI ollamaApi;

	private OllamaBackend(final OllamaAPI ollamaApi) {
		this.ollamaApi = ollamaApi;
	}

	@Override
	public String chat(final String modelName, final String... messages) throws LlmBackendException {
		OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(modelName);

		for (String message : messages) {
			builder.withMessage(OllamaChatMessageRole.USER, message);
		}

		OllamaChatRequest request = builder.build();
		OllamaChatResult chat;

		try {
			chat = ollamaApi.chat(request);
		} catch (OllamaBaseException | IOException | InterruptedException | ToolInvocationException e) {
			throw new LlmBackendException(e);
		}

		String result = chat.getResponseModel().getMessage().getContent();

		return result;
	}

	@Override
	public String toString() {
		return "OllamaBackend [ollamaApi=" + ollamaApi + "]";
	}

	public static OllamaBackend from(Settings settings) {
		OllamaAPI ollamaApi = new OllamaAPI(settings.getBaseUri());
		ollamaApi.setRequestTimeoutSeconds(settings.getTimeout());
		ollamaApi.setVerbose(false);

		return new OllamaBackend(ollamaApi);
	}

}
