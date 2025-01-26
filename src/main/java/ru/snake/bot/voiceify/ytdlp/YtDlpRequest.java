package ru.snake.bot.voiceify.ytdlp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class YtDlpRequest {

	private File workingDirectory;

	private String url;

	private Map<String, String> options;

	public YtDlpRequest(String url, File workingDirectory) {
		this.url = url;
		this.workingDirectory = workingDirectory;
		this.options = new HashMap<>();
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getOption() {
		return options;
	}

	public void setOption(String key) {
		options.put(key, null);
	}

	public void setOption(String key, String value) {
		options.put(key, value);
	}

	public void setOption(String key, int value) {
		options.put(key, String.valueOf(value));
	}

	public YtDlpRequest() {
	}

	public YtDlpRequest(String url) {
		this.url = url;
	}

	protected List<String> buildOptions() {
		List<String> result = new ArrayList<>();

		if (url != null) {
			result.add(url);
		}

		for (Entry<String, String> entry : options.entrySet()) {
			String option = entry.getKey();
			String value = entry.getValue();

			result.add(String.format("--%s", option));

			if (value != null) {
				result.add(value);
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return "YtDlpRequest [workingDirectory=" + workingDirectory + ", url=" + url + ", options=" + options + "]";
	}

}
