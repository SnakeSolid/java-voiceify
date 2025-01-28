package ru.snake.bot.voiceify.util;

public class DomainMatcher {

	private static final String STAR = "*";

	public static boolean match(final String name, final String... masks) {
		for (String mask : masks) {
			if (match(name, mask)) {
				return true;
			}
		}

		return false;
	}

	public static boolean match(final String name, final Iterable<String> masks) {
		for (String mask : masks) {
			if (match(name, mask)) {
				return true;
			}
		}

		return false;
	}

	public static boolean match(final String name, final String mask) {
		String nameParts[] = name.split("\\.");
		String maskParts[] = mask.split("\\.");

		if (nameParts.length != maskParts.length) {
			return false;
		}

		for (int index = 0; index < maskParts.length; index += 1) {
			String namePart = nameParts[index];
			String maskPart = maskParts[index];

			if (maskPart.equals(STAR)) {
				continue;
			} else if (!maskPart.equals(namePart)) {
				return false;
			}
		}

		return true;
	}

	private DomainMatcher() {
		// Hide public constructor.
	}

}
