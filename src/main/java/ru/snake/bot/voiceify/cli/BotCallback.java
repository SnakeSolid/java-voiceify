package ru.snake.bot.voiceify.cli;

import java.io.File;
import java.util.Set;

@FunctionalInterface
public interface BotCallback {

	public void
			execute(final File configFile, final File databaseFile, final String botToken, final Set<Long> allowUsers);

}
