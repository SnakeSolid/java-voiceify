package ru.snake.bot.voiceify.ytdlp;

public class YtDlpResponse {

	private final int exitCode;

	private final String stdOut;

	private final String stdErr;

	public YtDlpResponse(final int exitCode, final String stdOut, final String stdErr) {
		this.exitCode = exitCode;
		this.stdOut = stdOut;
		this.stdErr = stdErr;
	}

	public int getExitCode() {
		return exitCode;
	}

	public String getStdOut() {
		return stdOut;
	}

	public String getStdErr() {
		return stdErr;
	}

	@Override
	public String toString() {
		return "YtDlpResponse [exitCode=" + exitCode + ", stdOut=" + stdOut + ", stdErr=" + stdErr + "]";
	}

}
