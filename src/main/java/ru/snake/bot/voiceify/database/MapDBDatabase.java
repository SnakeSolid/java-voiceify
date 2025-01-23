package ru.snake.bot.voiceify.database;

import java.util.Map;

import org.mapdb.DB;
import org.mapdb.Serializer;

public class MapDBDatabase implements Database {

	private final Map<Long, ChatState> chatStates;

	private MapDBDatabase(final Map<Long, ChatState> chatStates) {
		this.chatStates = chatStates;
	}

	@Override
	public void setChatState(long chatId, ChatState state) {
		chatStates.put(chatId, state);
	}

	@Override
	public ChatState getChatState(long chatId) {
		return chatStates.getOrDefault(chatId, ChatState.DEFAULT);
	}

	@Override
	public String toString() {
		return "MapDBDatabase [chatStates=" + chatStates + "]";
	}

	public static Database from(DB db) {
		Map<Long, ChatState> chatStates = db.hashMap("chatStates", Serializer.LONG, ChatStateSerializer.instance())
			.createOrOpen();

		return new MapDBDatabase(chatStates);
	}

}
