package ru.snake.bot.voiceify.ytdlp.data;

import java.util.Arrays;
import java.util.List;

public class SubtitleRow {

	private static final String ORIGINAL = "-orig";

	private static final String TRANSLATION = " from ";

	private final String language;

	private final boolean original;

	private final boolean translation;

	private final String name;

	private final List<String> formats;

	public SubtitleRow(
		final String language,
		final boolean original,
		final boolean translation,
		final String name,
		final List<String> formats
	) {
		this.language = language;
		this.original = original;
		this.translation = translation;
		this.name = name;
		this.formats = formats;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isOriginal() {
		return original;
	}

	public boolean isTranslation() {
		return translation;
	}

	public String getName() {
		return name;
	}

	public List<String> getFormats() {
		return formats;
	}

	@Override
	public String toString() {
		return "SubtitleRow [language=" + language + ", original=" + original + ", translation=" + translation
				+ ", name=" + name + ", formats=" + formats + "]";
	}

	public static SubtitleRow from(String line, int indexName, int indexFormats) {
		if (0 < indexName && indexName < indexFormats) {
			String language = line.substring(0, indexName).trim();
			String name = line.substring(indexName, indexFormats).trim();
			String formats = line.substring(indexFormats).trim();

			return new SubtitleRow(
				language,
				language.endsWith(ORIGINAL),
				name.contains(TRANSLATION),
				name,
				Arrays.asList(formats.split(", "))
			);
		} else {
			return null;
		}
	}

}
