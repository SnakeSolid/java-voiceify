package ru.snake.bot.voiceify.consume.callback;

import ru.snake.bot.voiceify.consume.Context;

@FunctionalInterface
public interface DocumentAction {

	public void consume(final Context context, final String document, final String mimeType) throws Exception;

}
