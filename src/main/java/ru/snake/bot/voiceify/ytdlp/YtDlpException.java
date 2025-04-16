package ru.snake.bot.voiceify.ytdlp;

public class YtDlpException extends Exception {

	private static final long serialVersionUID = -164962716154629321L;

	private String message;

	/**
	 * Construct YtDlpException with a message
	 *
	 * @param message
	 */
	public YtDlpException(String message) {
		this.message = message;
	}

	/**
	 * Construct YtDlpException from another exception
	 *
	 * @param e
	 *            Any exception
	 */
	public YtDlpException(Exception e) {
		message = e.getMessage();
	}

	/**
	 * Get exception message
	 *
	 * @return exception message
	 */
	@Override
	public String getMessage() {
		return message;
	}
}
