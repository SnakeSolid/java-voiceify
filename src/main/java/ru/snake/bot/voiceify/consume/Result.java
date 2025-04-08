package ru.snake.bot.voiceify.consume;

public class Result<T> {

	private final T value;

	private final boolean success;

	private final String message;

	private Result(T value, boolean success, String message) {
		this.value = value;
		this.success = success;
		this.message = message;
	}

	public T getValue() {
		return value;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "Result [value=" + value + ", success=" + success + ", message=" + message + "]";
	}

	public static <T> Result<T> success(final T value) {
		return new Result<T>(value, true, null);
	}

	public static <T> Result<T> error(final String message) {
		return new Result<T>(null, false, message);
	}

}
