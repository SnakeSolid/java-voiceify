package ru.snake.bot.voiceify.consume.callback;

import ru.snake.bot.voiceify.consume.Context;

@FunctionalInterface
public interface AccessDeniedAction {

	public void consume(final Context context) throws Exception;

}
