package ru.snake.bot.voiceify.worker;

import java.io.File;

@FunctionalInterface
public interface FileCallback<T> {

	public T call(File file) throws Exception;

}
