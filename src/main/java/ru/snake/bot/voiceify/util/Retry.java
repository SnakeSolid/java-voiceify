package ru.snake.bot.voiceify.util;

import java.util.function.Predicate;

public class Retry {

	public static <T> T times(RetryCallback<T> callback, Predicate<T> validator, int retryTimes) throws Exception {
		Exception error = null;

		for (int iteration = 0; iteration < retryTimes; iteration += 1) {
			try {
				T result = callback.execute();

				if (validator.test(result)) {
					return result;
				}
			} catch (Exception e) {
				error = e;
			}
		}

		if (error != null) {
			throw error;
		}

		return null;
	}

	@FunctionalInterface
	public interface RetryCallback<T> {

		public T execute() throws Exception;

	}

	private Retry() {
		// Hide public constructor.
	}

}
