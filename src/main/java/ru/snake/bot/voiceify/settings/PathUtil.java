package ru.snake.bot.voiceify.settings;

import java.io.File;

public class PathUtil {

	private PathUtil() {
	}

	public static String absolutePath(final String path) {
		File file = new File(path);

		if (file.isAbsolute()) {
			return path;
		}

		File absolute = new File(System.getProperty("user.dir"), path);

		return absolute.getAbsolutePath();
	}

}
