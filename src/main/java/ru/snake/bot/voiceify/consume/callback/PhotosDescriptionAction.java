package ru.snake.bot.voiceify.consume.callback;

import java.util.List;

import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import ru.snake.bot.voiceify.consume.Context;

@FunctionalInterface
public interface PhotosDescriptionAction {

	public void consume(final Context context, final List<PhotoSize> photos, final String descriptyion)
			throws Exception;

}
