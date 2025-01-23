package ru.snake.bot.voiceify.consume.callback;

import ru.snake.bot.voiceify.consume.Context;

@FunctionalInterface
public interface MessageAction {

	public void consume(final Context context, final String text) throws Exception;

}
