package ru.snake.bot.voiceify.worker;

@FunctionalInterface
public interface Processor<T> {

	public T process() throws Exception;

}
