package ru.snake.bot.voiceify.worker.backend;

import ru.snake.bot.voiceify.settings.Backend;
import ru.snake.bot.voiceify.settings.Settings;

public interface LlmBackend {

	public String chat(final String modelName, final String... messages) throws LlmBackendException;

	public static LlmBackend from(Settings settings) {
		Backend backend = settings.getBackend();

		switch (backend) {
		case Ollama:
			return OllamaBackend.from(settings);

		case OpenAi:
			return OpenAiBackend.from(settings);

		default:
			throw new RuntimeException("Unsupported backend type: " + backend);
		}
	}

}
