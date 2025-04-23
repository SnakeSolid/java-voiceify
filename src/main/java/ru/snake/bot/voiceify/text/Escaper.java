package ru.snake.bot.voiceify.text;

import java.util.Set;

public class Escaper {

	private static final Set<Character> ESCAPE_CHARACTERS = Set
		.of('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!');

	public static String escapeMarkdown(final String value) {
		StringBuilder builder = new StringBuilder();

		for (char ch : value.toCharArray()) {
			if (ESCAPE_CHARACTERS.contains(ch)) {
				builder.append('\\');
				builder.append(ch);
			} else {
				builder.append(ch);
			}
		}

		return builder.toString();
	}

	private Escaper() {
		// Hide public constructor.
	}

}
