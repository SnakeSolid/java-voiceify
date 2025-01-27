package ru.snake.bot.voiceify.database;

public enum Language {
	/**
	 * Ignore language of original text. Do not translate even if text
	 * unreadable.
	 */
	IGNORE,

	/**
	 * Translate all non English texts to English. Do nothing id text is in
	 * English.
	 */
	ENGLISH,

	/**
	 * Translate all non Russian texts to Russian. Do nothing id text is in
	 * Russian.
	 */
	RUSSIAN;

}
