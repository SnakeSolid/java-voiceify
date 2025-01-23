package ru.snake.bot.voiceify.consume.callback;

import ru.snake.bot.voiceify.consume.Context;

@FunctionalInterface
public interface CommandAction {

	public void consume(final Context context, final String command) throws Exception;

}
