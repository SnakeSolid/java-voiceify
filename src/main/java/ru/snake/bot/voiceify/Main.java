package ru.snake.bot.voiceify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
		Database database = createDatabase(databaseFile);
		File cacheDirectory;

		try {
			cacheDirectory = createCacheDirectory();
		} catch (IOException e) {
			LOG.error("Failed to create temporary directory.", e);

			return;
		}

		Worker worker = Worker.create(cacheDirectory, settings);
		VoiceifyBot bot = new VoiceifyBot(botToken, allowUsers, settings, database, worker);
		worker.setCallbackSuccess(bot::sendVoiceMessage);
		worker.setCallbackError(bot::logError);
		worker.start();

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

	private static File createCacheDirectory() throws IOException {
		File cacheDirectory = Files.createTempDirectory("voiceify_").toFile();
		cacheDirectory.deleteOnExit();

		return cacheDirectory;
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

}
