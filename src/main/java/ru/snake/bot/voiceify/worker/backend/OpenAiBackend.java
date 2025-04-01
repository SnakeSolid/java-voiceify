package ru.snake.bot.voiceify.worker.backend;

import java.time.Duration;
import java.util.Optional;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletion.Choice;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionCreateParams.Builder;

import ru.snake.bot.voiceify.settings.Settings;

public class OpenAiBackend implements LlmBackend {

	private final OpenAIClient client;

	private OpenAiBackend(final OpenAIClient client) {
		this.client = client;
	}

	@Override
	public String chat(String modelName, String... messages) throws LlmBackendException {
		Builder paramsBuilder = ChatCompletionCreateParams.builder().model(modelName);

		for (String message : messages) {
			paramsBuilder.addUserMessage(message);
		}

		ChatCompletion completion = client.chat().completions().create(paramsBuilder.build());
		StringBuilder builder = new StringBuilder();

		for (Choice chose : completion.choices()) {
			Optional<String> content = chose.message().content();
			content.ifPresent(builder::append);
		}

		return builder.toString();
	}

	@Override
	public String toString() {
		return "OpenAiBackend [client=" + client + "]";
	}

	static LlmBackend from(Settings settings) {
		OpenAIClient client = OpenAIOkHttpClient.builder()
			.baseUrl(settings.getBaseUri())
			.apiKey(settings.getApiKey())
			.timeout(Duration.ofSeconds(settings.getTimeout()))
			.build();

		return new OpenAiBackend(client);
	}

}
