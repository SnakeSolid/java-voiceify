package ru.snake.bot.voiceify.consume.callback;

import ru.snake.bot.voiceify.consume.Context;

@FunctionalInterface
public interface CallbackAction {

	public void consume(final Context context, final String queryId, final String command) throws Exception;

}
