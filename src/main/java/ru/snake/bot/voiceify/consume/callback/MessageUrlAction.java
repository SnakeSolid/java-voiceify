package ru.snake.bot.voiceify.consume.callback;

import java.util.List;

import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import ru.snake.bot.voiceify.consume.Context;

@FunctionalInterface
public interface MessageUrlAction {

	public void consume(final Context context, final String text, List<MessageEntity> linkEntities) throws Exception;

}
