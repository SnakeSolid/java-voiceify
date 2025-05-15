package ru.snake.bot.voiceify.worker.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.snake.bot.voiceify.settings.Settings;
import ru.snake.bot.voiceify.util.Retry;
import ru.snake.bot.voiceify.worker.FileCallback;
import ru.snake.bot.voiceify.worker.data.SubtitlesResult;
import ru.snake.bot.voiceify.ytdlp.YtDlp;
import ru.snake.bot.voiceify.ytdlp.data.SubtitleRow;

public class YtService {

	private static final Logger LOG = LoggerFactory.getLogger(YtService.class);

	private final File cacheDirectory;

	private final YtDlp ytDlp;

	private YtService(final File cacheDirectory, final YtDlp ytDlp) {
		this.cacheDirectory = cacheDirectory;
		this.ytDlp = ytDlp;
	}

	@Override
	public String toString() {
		return "YtService [cacheDirectory=" + cacheDirectory + ", ytDlp=" + ytDlp + "]";
	}

	public static YtService create(Settings settings, File cacheDirectory) {
		String ytDlpPath = settings.getYtDlpPath();
		YtDlp ytDlp = new YtDlp(ytDlpPath, cacheDirectory);

		return new YtService(cacheDirectory, ytDlp);
	}

	public SubtitlesResult videoSubtitles(String uri) throws Exception {
		LOG.info("Load subs for: {}", uri);

		List<SubtitleRow> allSubs = ytDlp.listSubs(uri);

		if (allSubs.isEmpty()) {
			throw new Exception("Subtitles/closed captions unavailable");
		}

		String title = ytDlp.title(uri);
		SubtitleRow originalSubs = findBestSubs(allSubs);

		// Sometimes yt-dlp does not save subtitles to file.
		// Try to retry download 3 times before continue.
		File subsPath = Retry.times(() -> ytDlp.loadSubs(uri, originalSubs.getLanguage(), "json3"), f -> f != null, 3);

		if (subsPath == null) {
			return SubtitlesResult.fail();
		}

		String text = removeFile(subsPath, this::subsToText);

		return SubtitlesResult.success(title, text);
	}

	private String subsToText(File subsPath) throws IOException {
		StringBuilder result = new StringBuilder();

		try (FileReader reader = new FileReader(subsPath)) {
			JSONTokener tokener = new JSONTokener(reader);
			JSONObject jsonObject = new JSONObject(tokener);
			JSONArray events = jsonObject.getJSONArray("events");

			for (int i = 0; i < events.length(); i++) {
				JSONObject event = events.getJSONObject(i);

				if (!event.has("segs")) {
					continue;
				}

				JSONArray segments = event.getJSONArray("segs");

				for (int j = 0; j < segments.length(); j++) {
					JSONObject segment = segments.getJSONObject(j);
					String text = segment.getString("utf8");
					result.append(text);
				}
			}
		}

		return result.toString();
	}

	private <T> T removeFile(File file, FileCallback<T> callback) throws Exception {
		try {
			return callback.call(file);
		} finally {
			file.delete();
		}
	}

	private SubtitleRow findBestSubs(List<SubtitleRow> allSubs) {
		SubtitleRow source = null;

		for (SubtitleRow subs : allSubs) {
			if (subs.isOriginal()) {
				return subs;
			} else if (!subs.isTranslation()) {
				source = subs;
			}
		}

		if (source != null) {
			return source;
		}

		return allSubs.get(0);
	}

}
