package ru.snake.bot.voiceify;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resource {

	private static final Logger LOG = LoggerFactory.getLogger(Resource.class);

	public static String asText(String path) throws IOException {
		LOG.info("Read resource: {}", path);

		try (InputStream stream = ClassLoader.getSystemResourceAsStream(path)) {
			byte[] bytes = stream.readAllBytes();
			String text = new String(bytes);

			return text;
		}
	}

	private Resource() {
		// Hide constructor for utility class.
	}

}
