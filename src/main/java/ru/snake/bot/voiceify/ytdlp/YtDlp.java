package ru.snake.bot.voiceify.ytdlp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ru.snake.bot.voiceify.util.SentenceIterator;
import ru.snake.bot.voiceify.ytdlp.data.SubtitleRow;

public class YtDlp {

	private static final String SUBS_LANGUAGE = "Language";

	private static final String SUBS_NAME = "Name";

	private static final String SUBS_FORMATS = "Formats";

	private static final String DESTINATION = "[download] Destination: ";

	private static final String WRITING = "[info] Writing video subtitles to: ";

	private final String executablePath;

	private final File workingDirectory;

	public YtDlp(final String executablePath, final File workingDirectory) {
		this.executablePath = executablePath;
		this.workingDirectory = workingDirectory;
	}

	protected List<String> buildCommand(List<String> arguments) {
		List<String> command = new ArrayList<>();
		command.add(executablePath);
		command.addAll(arguments);

		return command;
	}

	public String title(String videoUrl) throws YtDlpException {
		YtDlpRequest request = new YtDlpRequest(videoUrl, workingDirectory);
		request.setOption("quiet");
		request.setOption("no-progress");
		request.setOption("simulate");
		request.setOption("print", "%(title)s");
		request.setOption("retries", 3);

		YtDlpResponse response = execute(request);

		return response.getStdOut().trim();
	}

	public List<SubtitleRow> listSubs(String videoUrl) throws YtDlpException {
		YtDlpRequest request = new YtDlpRequest(videoUrl, workingDirectory);
		request.setOption("quiet");
		request.setOption("no-progress");
		request.setOption("simulate");
		request.setOption("list-subs");
		request.setOption("retries", 3);

		YtDlpResponse response = execute(request);
		String stdout = response.getStdOut();
		List<SubtitleRow> result = new ArrayList<>();
		int indexName = 0;
		int indexFormats = 0;

		for (String line : new SentenceIterator(stdout, '\n')) {
			if (line.startsWith(SUBS_LANGUAGE)) {
				indexName = line.indexOf(SUBS_NAME);
				indexFormats = line.indexOf(SUBS_FORMATS);
			} else {
				SubtitleRow row = SubtitleRow.from(line, indexName, indexFormats);

				if (row != null) {
					result.add(row);
				}
			}
		}

		return result;
	}

	public File loadSubs(String videoUrl, String language, String format) throws YtDlpException {
		YtDlpRequest request = new YtDlpRequest(videoUrl, workingDirectory);
		request.setOption("no-progress");
		request.setOption("write-subs");
		request.setOption("write-auto-subs");
		request.setOption("sub-langs", language);
		request.setOption("sub-format", format);
		request.setOption("skip-download");
		request.setOption("retries", 3);

		YtDlpResponse response = execute(request);
		String stdout = response.getStdOut();

		for (String line : new SentenceIterator(stdout, '\n')) {
			if (line.startsWith(DESTINATION)) {
				String filename = line.substring(DESTINATION.length()).trim();
				File result = new File(workingDirectory, filename);

				return result;
			} else if (line.startsWith(WRITING)) {
				String filename = line.substring(WRITING.length()).trim();
				File result = new File(workingDirectory, filename);

				return result;
			}
		}

		return null;
	}

	public YtDlpResponse execute(YtDlpRequest request) throws YtDlpException {
		List<String> command = buildCommand(request.buildOptions());
		File directory = request.getWorkingDirectory();
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process process;
		int exitCode;

		if (directory != null) {
			processBuilder.directory(directory);
		}

		try {
			process = processBuilder.start();
		} catch (IOException e) {
			throw new YtDlpException(e);
		}

		InputStream outStream = process.getInputStream();
		InputStream errStream = process.getErrorStream();
		StreamReader stdOutProcessor = new StreamReader(outStream);
		StreamReader stdErrProcessor = new StreamReader(errStream);

		try {
			stdOutProcessor.join();
			stdErrProcessor.join();
			exitCode = process.waitFor();
		} catch (InterruptedException e) {
			throw new YtDlpException(e);
		}

		String stdout = stdOutProcessor.getBuffer().toString();
		String stderr = stdErrProcessor.getBuffer().toString();

		if (exitCode > 0 && stdout.isEmpty()) {
			throw new YtDlpException(stderr);
		}

		YtDlpResponse response = new YtDlpResponse(exitCode, stdout, stderr);

		return response;
	}

	@Override
	public String toString() {
		return "YtDlp [executablePath=" + executablePath + ", workingDirectory=" + workingDirectory + "]";
	}

}
