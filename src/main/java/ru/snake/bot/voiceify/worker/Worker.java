package ru.snake.bot.voiceify.worker;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResult;
import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;
import ru.snake.bot.voiceify.Resource;
import ru.snake.bot.voiceify.text.Replacer;
import ru.snake.bot.voiceify.worker.data.ArticleResult;
import ru.snake.bot.voiceify.worker.data.CaptionResult;
import ru.snake.bot.voiceify.worker.data.TextToSpeechResult;

public class Worker {

	private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

	private static final Set<String> SKIP_TAGS = Set.of("table", "code", "pre");

	private final File cacheDirectory;

	private final OllamaAPI ollamaApi;

	private final String modelName;

	private final CommandSettings ttsCommand;

	public Worker(
		final File cacheDirectory,
		final OllamaAPI ollamaApi,
		final String modelName,
		final CommandSettings ttsCommand
	) {
		this.cacheDirectory = cacheDirectory;
		this.ollamaApi = ollamaApi;
		this.modelName = modelName;
		this.ttsCommand = ttsCommand;
	}

	public synchronized TextToSpeechResult textToSpeech(String text) throws IOException, InterruptedException {
		LOG.info("Syntesing voice for `{}`", text);

		File tempDirectory = new File(cacheDirectory, "temp");
		File outputPath = new File(cacheDirectory, "output.mp3");

		if (outputPath.exists()) {
			outputPath.delete();
		}

		Map<String, Object> parameters = Map.ofEntries(
			Map.entry("output", outputPath.getAbsolutePath()),
			Map.entry("temp", tempDirectory.getAbsolutePath())
		);
		ProcessBuilder builder = new ProcessBuilder(ttsCommand.getCommand()).redirectInput(Redirect.PIPE)
			.redirectOutput(Redirect.DISCARD)
			.redirectError(Redirect.DISCARD);

		for (String argument : ttsCommand.getArguments()) {
			String value = Replacer.replace(argument, parameters);

			builder.command().add(value);
		}

		for (Entry<String, String> entry : ttsCommand.getEnvironment().entrySet()) {
			String variable = entry.getKey();
			String value = entry.getValue();

			builder.environment().put(variable, value);
		}

		Process process = builder.start();
		process.getOutputStream().write(text.getBytes());
		process.getOutputStream().close();
		int exitCode = process.waitFor();

		FileUtils.deleteDirectory(tempDirectory);

		if (exitCode != 0) {
			return TextToSpeechResult.fail(String.format("TTS exit code: %d", exitCode));
		}

		return TextToSpeechResult.success(outputPath);
	}

	public synchronized CaptionResult writeCaption(String text)
			throws OllamaBaseException, IOException, InterruptedException {
		LOG.info("Write caption for `{}`", text);

		String caption = textQuery(Replacer.replace(Resource.asText("prompts/text_caption.txt"), Map.of("text", text)));

		return CaptionResult.from(caption);
	}

	private String textQuery(String... messages) throws OllamaBaseException, IOException, InterruptedException {
		if (LOG.isInfoEnabled()) {
			LOG.info("Execute text query: {}", Arrays.asList(messages));
		}

		OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(modelName);

		for (String message : messages) {
			builder.withMessage(OllamaChatMessageRole.USER, message);
		}

		OllamaChatRequest request = builder.build();
		OllamaChatResult chat = ollamaApi.chat(request);
		String result = chat.getResponseModel().getMessage().getContent();

		LOG.info("Query result: {}", result);

		return result;
	}

	public ArticleResult articleText(String uri) throws IOException {
		Document document = Jsoup.connect(uri).timeout(60 * 1000).get();
		document.select("table, code, pre").forEach(e -> e.remove());

		String html = document.html();
		Readability4J readability4J = new Readability4J(uri, html);
		Article article = readability4J.parse();
		String title = article.getTitle();
		String text = article.getTextContent();

		return ArticleResult.from(title, text);
	}

	@Override
	public String toString() {
		return "Worker [ollamaApi=" + ollamaApi + ", modelName=" + modelName + "]";
	}

	public static Worker create(WorkerSettings settings) throws IOException {
		File cacheDirectory = Files.createTempDirectory("voiceify_").toFile();
		cacheDirectory.deleteOnExit();

		OllamaAPI ollamaApi = new OllamaAPI(settings.getOllamaUri());
		ollamaApi.setRequestTimeoutSeconds(settings.getTimeout());
		ollamaApi.setVerbose(false);

		return new Worker(cacheDirectory, ollamaApi, settings.getModelName(), settings.getTtsCommand());
	}

}
