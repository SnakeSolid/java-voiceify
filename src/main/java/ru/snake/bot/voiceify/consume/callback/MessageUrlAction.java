package ru.snake.bot.voiceify.consume.callback;

import java.util.List;

import ru.snake.bot.voiceify.consume.Context;

@FunctionalInterface
public interface MessageUrlAction {

	public void consume(final Context context, final String text, List<String> uriStrings) throws Exception;

}
