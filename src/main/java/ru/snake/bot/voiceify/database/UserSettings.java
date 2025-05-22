package ru.snake.bot.voiceify.database;

public class UserSettings {

	private final Language language;

	private final boolean shorten;

	private UserSettings(final Language language, final boolean shorten) {
		this.language = language;
		this.shorten = shorten;
	}

	public Language getLanguage() {
		return language;
	}

	public boolean isShorten() {
		return shorten;
	}

	public UserSettings withLanguage(final Language language) {
		return new UserSettings(language, shorten);
	}

	public UserSettings withShorten(final boolean shorten) {
		return new UserSettings(language, shorten);
	}

	@Override
	public String toString() {
		return "UserSettings [language=" + language + ", shorten=" + shorten + "]";
	}

	public static UserSettings create(final Language language, final boolean shorten) {
		return new UserSettings(language, shorten);
	}

}
