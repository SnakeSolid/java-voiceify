package ru.snake.bot.voiceify;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurateException;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import picocli.CommandLine;
import ru.snake.bot.voiceify.cli.BotCommand;
import ru.snake.bot.voiceify.database.Database;
import ru.snake.bot.voiceify.settings.Settings;
import ru.snake.bot.voiceify.worker.Worker;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		int exitCode = new CommandLine(new BotCommand(Main::startBot)).execute(args);

		System.exit(exitCode);
	}

	private static void startBot(
		final File configFile,
		final File databaseFile,
		final String botToken,
		final Set<Long> allowUsers
	) {
		Settings settings = createSettings(configFile);
		Worker worker = createWorker(settings);
		Database database = createDatabase(databaseFile);
		VoiceifyBot bot = new VoiceifyBot(botToken, allowUsers, settings, database, worker);

		try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
			botsApplication.registerBot(botToken, bot);
			Thread.currentThread().join();
		} catch (TelegramApiException e) {
			LOG.error("Telegramm API error.", e);
		} catch (InterruptedException e) {
			LOG.error("Thread was interrupted.", e);
		} catch (Exception e) {
			LOG.error("Unknown error.", e);
		}
	}

	private static Settings createSettings(File configFile) {
		try {
			return Settings.fromFile(configFile);
		} catch (ConfigurateException e) {
			LOG.error("Failed to load configuration.", e);

			System.exit(1);

			return null;
		}
	}

	private static Database createDatabase(File databaseFile) {
		if (databaseFile != null) {
			return Database.onDisk(databaseFile);
		} else {
			return Database.inMemory();
		}
	}

	private static Worker createWorker(final Settings settings) {
		try {
			return Worker.create(settings);
		} catch (IOException e) {
			LOG.error("Failed to create worker.", e);

			System.exit(1);

			return null;
		}
	}

}
