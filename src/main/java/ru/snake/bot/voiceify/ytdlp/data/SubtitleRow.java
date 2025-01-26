package ru.snake.bot.voiceify.ytdlp.data;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubtitleRow {

	private static final Pattern ROW_PATTERN = Pattern
		.compile("^(\\w{2,3}(-\\w+)?)\\s+([- ()\\w]+)\\s([0-9a-z]+(, [0-9a-z]+)*)", Pattern.UNICODE_CHARACTER_CLASS);

	private static final String ORIGINAL = "-orig";

	private final String language;

	private final boolean original;

	private final String name;

	private final List<String> formats;

	public SubtitleRow(final String language, final boolean original, final String name, final List<String> formats) {
		this.language = language;
		this.original = original;
		this.name = name;
		this.formats = formats;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isOriginal() {
		return original;
	}

	public String getName() {
		return name;
	}

	public List<String> getFormats() {
		return formats;
	}

	@Override
	public String toString() {
		return "SubtitleRow [language=" + language + ", original=" + original + ", name=" + name + ", formats="
				+ formats + "]";
	}

	public static SubtitleRow from(String line) {
		Matcher matcher = ROW_PATTERN.matcher(line);

		if (matcher.matches()) {
			String language = matcher.group(1);
			String postfix = matcher.group(2);
			String name = matcher.group(3).stripTrailing();
			String formats = matcher.group(4);

			return new SubtitleRow(language, ORIGINAL.equals(postfix), name, Arrays.asList(formats.split(", ")));
		} else {
			return null;
		}
	}

}
