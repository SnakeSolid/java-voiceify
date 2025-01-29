package ru.snake.bot.voiceify.worker.service;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.snake.bot.voiceify.settings.CommandSettings;
import ru.snake.bot.voiceify.settings.Settings;
import ru.snake.bot.voiceify.text.Replacer;
import ru.snake.bot.voiceify.worker.Translation;
import ru.snake.bot.voiceify.worker.data.TextToSpeechResult;

public class TtsService {

	private static final Logger LOG = LoggerFactory.getLogger(TtsService.class);

	private final File cacheDirectory;

	private final CommandSettings ttsCommand;

	public TtsService(File cacheDirectory, CommandSettings ttsCommand) {
		this.cacheDirectory = cacheDirectory;
		this.ttsCommand = ttsCommand;
	}

	public TextToSpeechResult textToSpeech(String text) throws IOException, InterruptedException {
		LOG.info("Synthesizing voice for `{}`", text);

		File tempDirectory = new File(cacheDirectory, "temp");
		File outputPath = new File(cacheDirectory, "output.mp3");

		if (outputPath.exists()) {
			outputPath.delete();
		}

		Map<String, String> parameters = Map.ofEntries(
			Map.entry("language", Translation.languageCode(text)),
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
		return "TtsService [cacheDirectory=" + cacheDirectory + ", ttsCommand=" + ttsCommand + "]";
	}

	public static TtsService create(Settings settings, File cacheDirectory) {
		CommandSettings ttsCommand = settings.getTtsCommand();

		return new TtsService(cacheDirectory, ttsCommand);
	}

}
