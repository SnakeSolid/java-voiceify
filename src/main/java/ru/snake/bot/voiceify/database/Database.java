package ru.snake.bot.voiceify.database;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;

public interface Database {

	public void setUserSettings(final long chatId, final UserSettings settings);

	public UserSettings getUserSettings(final long chatId, final UserSettings defaultSettings);

	public void setChatState(final long chatId, final ChatState state);

	public ChatState getChatState(final long chatId);

	public static Database onDisk(File databasePath) {
		DB db = DBMaker.fileDB(databasePath).make();

		return MapDBDatabase.from(db);
	}

	public static Database inMemory() {
		DB db = DBMaker.memoryDB().make();

		return MapDBDatabase.from(db);
	}

}
