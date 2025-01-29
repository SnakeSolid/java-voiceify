package ru.snake.bot.voiceify.worker;

@FunctionalInterface
public interface CallbackError {

	public void call(final long chatId, final int messageId, final String message);

}
