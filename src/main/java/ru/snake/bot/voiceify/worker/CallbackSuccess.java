package ru.snake.bot.voiceify.worker;

import java.io.File;

@FunctionalInterface
public interface CallbackSuccess {

	public void call(final long chatId, final int messageId, final String text, final File path) throws Exception;

}
