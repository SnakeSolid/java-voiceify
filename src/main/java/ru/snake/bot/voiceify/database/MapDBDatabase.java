package ru.snake.bot.voiceify.database;

import java.util.Map;

import org.mapdb.DB;
import org.mapdb.Serializer;

public class MapDBDatabase implements Database {

	private final DB db;

	private final Map<Long, Language> languages;

	private final Map<Long, ChatState> chatStates;

	private MapDBDatabase(final DB db, final Map<Long, Language> languages, final Map<Long, ChatState> chatStates) {
		this.db = db;
		this.languages = languages;
		this.chatStates = chatStates;
	}

	@Override
	public void setLanguage(long chatId, Language language) {
		languages.put(chatId, language);
		db.commit();
	}

	@Override
	public Language getLanguage(long chatId, Language defaultLanguage) {
		return languages.getOrDefault(chatId, defaultLanguage);
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
		return "MapDBDatabase [db=" + db + ", languages=" + languages + ", chatStates=" + chatStates + "]";
	}

	public static Database from(DB db) {
		Map<Long, Language> languages = db.hashMap("languages", Serializer.LONG, LanguageSerializer.instance())
			.createOrOpen();
		Map<Long, ChatState> chatStates = db.hashMap("chatStates", Serializer.LONG, ChatStateSerializer.instance())
			.createOrOpen();

		return new MapDBDatabase(db, languages, chatStates);
	}

}
