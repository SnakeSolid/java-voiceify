package ru.snake.bot.voiceify.worker;

public enum JobType {

	/**
	 * Read text message.
	 */
	TEXT,

	/**
	 * Read text as subtitles (fix text style and remove sounds).
	 */
	SUBTITLES,

	/**
	 * Read article by URI.
	 */
	ARTICLE,

	/**
	 * Read video by URI.
	 */
	VIDEO,

}
