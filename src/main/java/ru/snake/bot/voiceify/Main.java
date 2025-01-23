package ru.snake.bot.voiceify;

import java.io.File;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurateException;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import picocli.CommandLine;
import ru.snake.bot.voiceify.cli.BotCommand;
import ru.snake.bot.voiceify.database.Database;
import ru.snake.bot.voiceify.worker.Worker;
import ru.snake.bot.voiceify.worker.WorkerSettings;

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
		Worker worker = createWorker(configFile);
		Database database = createDatabase(databaseFile);
		VoiceifyBot bot = new VoiceifyBot(botToken, allowUsers, database, worker);

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

	private static Database createDatabase(File databaseFile) {
		if (databaseFile != null) {
			return Database.onDisk(databaseFile);
		} else {
			return Database.inMemory();
		}
	}

	private static Worker createWorker(final File configFile) {
		WorkerSettings settings;

		if (configFile == null) {
			settings = WorkerSettings.create();
		} else {
			try {
				settings = WorkerSettings.fromFile(configFile);
			} catch (ConfigurateException e) {
				LOG.error("Failed to load configuration.", e);

				System.exit(1);

				return null;
			}
		}

		return Worker.create(settings);
	}

}
