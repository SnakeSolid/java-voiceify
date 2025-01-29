package ru.snake.bot.voiceify.worker;

@FunctionalInterface
public interface CallbackError {

	public void call(final long chatId, final int messageId, final String uri, final String message) throws Exception;

}
