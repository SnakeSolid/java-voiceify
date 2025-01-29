package ru.snake.bot.voiceify.worker;

import java.io.File;

@FunctionalInterface
public interface CallbackSuccess {

	public void call(final long chatId, final int messageId, final String caption, final File path);

}
