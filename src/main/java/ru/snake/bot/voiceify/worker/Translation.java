package ru.snake.bot.voiceify.worker;

import java.lang.Character.UnicodeBlock;

import ru.snake.bot.voiceify.database.Language;

public class Translation {

	/**
	 * Determines whether the given text needs translation based on its
	 * language.
	 *
	 * @param text
	 *            the text to be checked for translation
	 * @param language
	 *            the language of the text
	 * @return true if the text does not contain at least 50% characters in the
	 *         specified language, false otherwise
	 */
	public static boolean isNeedTranslation(final String text, final Language language) {
		UnicodeBlock unicodeBlock;

		switch (language) {
		case IGNORE:
			return false;
		case ENGLISH:
			unicodeBlock = UnicodeBlock.BASIC_LATIN;
			break;
		case RUSSIAN:
			unicodeBlock = UnicodeBlock.CYRILLIC;
			break;
		default:
			throw new RuntimeException(String.format("Unsupported language: %s", language));
		}

		int n = 0;

		for (char ch : text.toCharArray()) {
			if (Character.isWhitespace(ch) || Character.isDigit(ch) || UnicodeBlock.of(ch) == unicodeBlock) {
				n += 1;
			}
		}

		return 2 * n < text.length();
	}

	/**
	 * Returns the name of the given language.
	 *
	 * @param language
	 *            the language to get the name for
	 * @return the name of the language
	 */
	public static String languageName(Language language) {
		switch (language) {
		case ENGLISH:
			return "English";

		case RUSSIAN:
			return "Russian";

		default:
			throw new RuntimeException(String.format("Unsupported language: %s", language));
		}
	}

	public static String languageCode(String text) {
		int nLatin = 0;
		int nCyrillic = 0;

		for (char ch : text.toCharArray()) {
			UnicodeBlock unicodeBlock = UnicodeBlock.of(ch);
			if (unicodeBlock == UnicodeBlock.BASIC_LATIN) {
				nLatin += 1;
			} else if (unicodeBlock == UnicodeBlock.CYRILLIC) {
				nCyrillic += 1;
			}
		}

		if (nLatin <= nCyrillic) {
			return "ru";
		} else {
			return "en";
		}
	}
}
