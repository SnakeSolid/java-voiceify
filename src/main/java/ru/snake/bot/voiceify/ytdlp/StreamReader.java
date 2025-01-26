package ru.snake.bot.voiceify.ytdlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class StreamReader extends Thread {

	private final InputStream stream;

	private final StringBuilder buffer;

	public StreamReader(final InputStream stream) {
		this.stream = stream;
		this.buffer = new StringBuilder();

		start();
	}

	public StringBuilder getBuffer() {
		return buffer;
	}

	@Override
	public void run() {
		try {
			try (Reader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
					BufferedReader in = new BufferedReader(streamReader)) {
				String line = in.readLine();

				while (line != null) {
					buffer.append(line);
					buffer.append(System.lineSeparator());

					line = in.readLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "StreamGobbler [stream=" + stream + ", buffer=" + buffer + "]";
	}

}
