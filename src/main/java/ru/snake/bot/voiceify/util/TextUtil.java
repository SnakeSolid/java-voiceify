package ru.snake.bot.voiceify.util;

import java.util.List;

public class TextUtil {

	private static final List<Quotes> QUOTES = List.of(
		Quotes.from("'"),
		Quotes.from("\""),
		Quotes.from("«", "»"),
		Quotes.from("‹", "›"),
		Quotes.from("‘", "’"),
		Quotes.from("‚", "’"),
		Quotes.from("“", "”"),
		Quotes.from("„", "“"),
		Quotes.from("〞", "〟")
	);

	private TextUtil() {
		// Hide public constructor.
	}

	public static String trimText(String text, int maxLength) {
		if (text.length() < maxLength) {
			return text;
		} else {
			return text.substring(0, maxLength) + "...";
		}
	}

	public static String unquoteText(String text) {
		String result = text.trim();

		for (Quotes quotes : QUOTES) {
			String left = quotes.getLeft();
			String right = quotes.getRight();

			if (result.startsWith(left) && result.endsWith(right)) {
				return result.substring(left.length(), result.length() - right.length()).trim();
			}
		}

		return result;
	}

}
