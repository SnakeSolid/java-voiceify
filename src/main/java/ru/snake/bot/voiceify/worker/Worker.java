package ru.snake.bot.voiceify.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
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
import net.dankito.readability4j.extended.Readability4JExtended;
import ru.snake.bot.voiceify.Resource;
import ru.snake.bot.voiceify.settings.CommandSettings;
import ru.snake.bot.voiceify.settings.Settings;
import ru.snake.bot.voiceify.text.Replacer;
import ru.snake.bot.voiceify.worker.data.ArticleResult;
import ru.snake.bot.voiceify.worker.data.CaptionResult;
import ru.snake.bot.voiceify.worker.data.SubtitlesResult;
import ru.snake.bot.voiceify.worker.data.TextToSpeechResult;
import ru.snake.bot.voiceify.ytdlp.YtDlp;
import ru.snake.bot.voiceify.ytdlp.data.SubtitleRow;

public class Worker {

	private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

	private final File cacheDirectory;

	private final OllamaAPI ollamaApi;

	private final String modelName;

	private final int contextLength;

	private final CommandSettings ttsCommand;

	private final YtDlp ytDlp;

	public Worker(
		final File cacheDirectory,
		final OllamaAPI ollamaApi,
		final String modelName,
		final int contextLength,
		final CommandSettings ttsCommand,
		final YtDlp ytDlp
	) {
		this.cacheDirectory = cacheDirectory;
		this.ollamaApi = ollamaApi;
		this.modelName = modelName;
		this.contextLength = contextLength;
		this.ttsCommand = ttsCommand;
		this.ytDlp = ytDlp;
	}

	public synchronized TextToSpeechResult textToSpeech(String text) throws IOException, InterruptedException {
		LOG.info("Syntesing voice for `{}`", text);

		File tempDirectory = new File(cacheDirectory, "temp");
		File outputPath = new File(cacheDirectory, "output.mp3");

		if (outputPath.exists()) {
			outputPath.delete();
		}

		Map<String, String> parameters = Map.ofEntries(
			Map.entry("output", outputPath.getAbsolutePath()),
			Map.entry("temp", tempDirectory.getAbsolutePath())
		);
		Process process = startProcess(ttsCommand, parameters, false);
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
		Readability4J readability4J = new Readability4JExtended(uri, html);
		Article article = readability4J.parse();
		String title = article.getTitle();
		String text = article.getTextContent();

		return ArticleResult.from(title, text);
	}

	public SubtitlesResult videoSubtitles(String uri) throws Exception {
		String videoUrl = uri;
		String title = ytDlp.title(videoUrl);
		List<SubtitleRow> allSubs = ytDlp.listSubs(videoUrl);
		SubtitleRow originalSubs = findBestSubs(allSubs);
		File subsPath = ytDlp.loadSubs(uri, originalSubs.getLanguage(), "json3");
		String text = removeFile(subsPath, this::subsToText);

		return SubtitlesResult.success(title, text);
	}

	private SubtitleRow findBestSubs(List<SubtitleRow> allSubs) {
		SubtitleRow source = null;

		for (SubtitleRow subs : allSubs) {
			if (subs.isOriginal()) {
				return subs;
			} else if (!subs.isTranslation()) {
				source = subs;
			}
		}

		if (source != null) {
			return source;
		}

		return allSubs.get(0);
	}

	public String subsToArticle(String text) throws IOException, OllamaBaseException, InterruptedException {
		String fullText = Resource.asText("prompts/text_to_article.txt") + text;

		if (fullText.length() <= contextLength) {
			String result = textQuery(fullText);

			return result;
		} else {
			String prompt = Resource.asText("prompts/page_to_article.txt");
			StringBuilder builder = new StringBuilder();
			StringBuilder result = new StringBuilder();

			try (StringReader stringReader = new StringReader(text);
					BufferedReader reader = new BufferedReader(stringReader)) {
				String line = reader.readLine();

				while (line != null) {
					if (prompt.length() + builder.length() + line.length() >= contextLength) {
						String page = textQuery(prompt + builder.toString());

						result.append(page);
						builder.setLength(0);
					}

					builder.append(line);
					builder.append('\n');
					line = reader.readLine();
				}
			}

			if (builder.length() > 0) {
				String page = textQuery(Replacer.replace(prompt, Map.of("text", builder.toString())));

				result.append(page);
			}

			return result.toString();
		}
	}

	private <T> T removeFile(File file, FileCallback<T> callback) throws Exception {
		try {
			return callback.call(file);
		} finally {
			file.delete();
		}
	}

	private String subsToText(File subsPath) throws IOException {
		StringBuilder result = new StringBuilder();

		try (FileReader reader = new FileReader(subsPath)) {
			JSONTokener tokener = new JSONTokener(reader);
			JSONObject jsonObject = new JSONObject(tokener);
			JSONArray events = jsonObject.getJSONArray("events");

			for (int i = 0; i < events.length(); i++) {
				JSONObject event = events.getJSONObject(i);

				if (!event.has("segs")) {
					continue;
				}

				JSONArray segments = event.getJSONArray("segs");

				for (int j = 0; j < segments.length(); j++) {
					JSONObject segment = segments.getJSONObject(j);
					String text = segment.getString("utf8");
					result.append(text);
				}
			}
		}

		return result.toString();
	}

	private Process
			startProcess(final CommandSettings command, final Map<String, String> parameters, boolean pipeStdOut)
					throws IOException {
		ProcessBuilder builder = new ProcessBuilder(command.getCommand()).redirectInput(Redirect.PIPE)
			.redirectOutput(pipeStdOut ? Redirect.PIPE : Redirect.DISCARD)
			.redirectError(Redirect.DISCARD);

		for (String argument : command.getArguments()) {
			String value = Replacer.replace(argument, parameters);

			builder.command().add(value);
		}

		for (Entry<String, String> entry : command.getEnvironment().entrySet()) {
			String variable = entry.getKey();
			String value = entry.getValue();

			builder.environment().put(variable, value);
		}

		return builder.start();
	}

	@Override
	public String toString() {
		return "Worker [cacheDirectory=" + cacheDirectory + ", ollamaApi=" + ollamaApi + ", modelName=" + modelName
				+ ", ttsCommand=" + ttsCommand + ", ytDlp=" + ytDlp + "]";
	}

	public static Worker create(Settings settings) throws IOException {
		File cacheDirectory = Files.createTempDirectory("voiceify_").toFile();
		cacheDirectory.deleteOnExit();

		OllamaAPI ollamaApi = new OllamaAPI(settings.getOllamaUri());
		ollamaApi.setRequestTimeoutSeconds(settings.getTimeout());
		ollamaApi.setVerbose(false);

		String ytDlpPath = settings.getYtDlpPath();
		YtDlp ytDlp = new YtDlp(ytDlpPath, cacheDirectory);

		return new Worker(
			cacheDirectory,
			ollamaApi,
			settings.getModelName(),
			settings.getContextLength(),
			settings.getTtsCommand(),
			ytDlp
		);
	}

}
