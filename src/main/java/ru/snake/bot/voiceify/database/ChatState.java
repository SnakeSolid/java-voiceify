package ru.snake.bot.voiceify.database;

import java.util.Collections;
import java.util.List;

public class ChatState {

	private final String text;

	private final List<String> uriStrings;

	private ChatState(String text, List<String> uriStrings) {
		this.text = text;
		this.uriStrings = uriStrings;
	}

	public String getText() {
		return text;
	}

	public List<String> getUriStrings() {
		return uriStrings;
	}

	@Override
	public String toString() {
		return "ChatState [text=" + text + ", uriStrings=" + uriStrings + "]";
	}

	public static ChatState create(final String text, final List<String> uriStrings) {
		return new ChatState(text, uriStrings);
	}

	public static ChatState empty() {
		return new ChatState("", Collections.emptyList());
	}

}
