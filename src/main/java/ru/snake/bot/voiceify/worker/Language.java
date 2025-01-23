package ru.snake.bot.voiceify.worker;

import java.lang.Character.UnicodeBlock;

public class Language {

	public static final Language ENGLISH = new Language("English", "English", UnicodeBlock.BASIC_LATIN);

	public static final Language RUSSIAN = new Language("Russian", "Русский", UnicodeBlock.CYRILLIC);

	private final String name;

	private final String display;

	private final UnicodeBlock unicodeBlock;

	private Language(String name, String display, UnicodeBlock unicodeBlock) {
		this.name = name;
		this.display = display;
		this.unicodeBlock = unicodeBlock;
	}

	public String getName() {
		return name;
	}

	public String getDisplay() {
		return display;
	}

	public boolean isTranslated(String text) {
		boolean translated = false;

		for (char ch : text.toCharArray()) {
			if (UnicodeBlock.of(ch) == unicodeBlock) {
				return true;
			}
		}

		return translated;
	}

	@Override
	public String toString() {
		return "Language [name=" + name + ", display=" + display + ", unicodeBlock=" + unicodeBlock + "]";
	}

}
