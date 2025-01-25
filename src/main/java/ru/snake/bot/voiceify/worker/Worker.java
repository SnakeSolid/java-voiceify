package ru.snake.bot.voiceify.worker;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.ollama4j.OllamaAPI;
import ru.snake.bot.voiceify.text.Replacer;

public class Worker {

	private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

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

	public synchronized TextToSpeechResult textToSpeech(String text) {
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

		try {
			Process process = builder.start();
			process.getOutputStream().write(text.getBytes());
			process.getOutputStream().close();
			int exitCode = process.waitFor();

			FileUtils.deleteDirectory(tempDirectory);

			if (exitCode != 0) {
				return TextToSpeechResult.fail(String.format("TTS exit code: %d", exitCode));
			}
		} catch (IOException | InterruptedException e) {
			return TextToSpeechResult.fail(e.getMessage());
		}

		return TextToSpeechResult.success(outputPath);
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
