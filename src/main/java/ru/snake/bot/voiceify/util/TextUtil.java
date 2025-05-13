package ru.snake.bot.voiceify.util;

import java.util.ArrayList;
import java.util.Collections;
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

	public static List<String> split(String content, int maxFragmentChars) {
		if (content.length() < maxFragmentChars) {
			return Collections.singletonList(content);
		}

		int nFragments = (content.length() + maxFragmentChars - 1) / maxFragmentChars;
		int fragmentLength = content.length() / nFragments;
		List<String> result = new ArrayList<>();
		StringBuilder builder = new StringBuilder();

		for (String sentence : new SentenceIterator(content)) {
			if (builder.length() > fragmentLength) {
				result.add(builder.toString());
				builder.setLength(0);
			}

			builder.append(sentence);
		}

		if (builder.length() > 0) {
			result.add(builder.toString());
		}

		return result;
	}

}
