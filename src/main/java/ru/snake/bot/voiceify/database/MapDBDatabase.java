package ru.snake.bot.voiceify.database;

import java.util.Map;

import org.mapdb.DB;
import org.mapdb.Serializer;

public class MapDBDatabase implements Database {

	private final DB db;

	private final Map<Long, UserSettings> userSettings;

	private final Map<Long, ChatState> chatStates;

	private MapDBDatabase(
		final DB db,
		final Map<Long, UserSettings> userSettings,
		final Map<Long, ChatState> chatStates
	) {
		this.db = db;
		this.userSettings = userSettings;
		this.chatStates = chatStates;
	}

	@Override
	public void setUserSettings(long chatId, UserSettings settings) {
		userSettings.put(chatId, settings);
		db.commit();
	}

	@Override
	public UserSettings getUserSettings(long chatId, UserSettings defaultSettings) {
		return userSettings.getOrDefault(chatId, defaultSettings);
	}

	@Override
	public void setChatState(long chatId, ChatState state) {
		chatStates.put(chatId, state);
		db.commit();
	}

	@Override
	public ChatState getChatState(long chatId) {
		return chatStates.getOrDefault(chatId, ChatState.empty());
	}

	@Override
	public String toString() {
		return "MapDBDatabase [db=" + db + ", userSettings=" + userSettings + ", chatStates=" + chatStates + "]";
	}

	public static Database from(DB db) {
		Map<Long, UserSettings> UserSettings = db
			.hashMap("userSettings", Serializer.LONG, UserSettingsSerializer.instance())
			.createOrOpen();
		Map<Long, ChatState> chatStates = db.hashMap("chatStates", Serializer.LONG, ChatStateSerializer.instance())
			.createOrOpen();
		db.commit();

		return new MapDBDatabase(db, UserSettings, chatStates);
	}

}
