package ru.snake.bot.voiceify.settings;

import java.io.File;
import java.util.List;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

public class Settings {

	private static final String DEFAULT_URI = "http://localhost:11434/";

	private static final String MODEL_NAME = "gemma2";

	private static final int CONTEXT_LENGTH = 8192;

	private static final long DEFAULT_TIMEOUT = 120;

	private static final String YTDLP_PATH = "yt-dlp";

	private final Backend backend;

	private final String baseUri;

	private final String apiKey;

	private final String modelName;

	private final int contextLength;

	private final long timeout;

	private final List<String> videoHosts;

	private final CommandSettings ttsCommand;

	private final String ytDlpPath;

	private Settings(
		final Backend backend,
		final String baseUri,
		final String apiKey,
		final String modelName,
		final int contextLength,
		final long timeout,
		final String ytDlpPath,
		final List<String> videoHosts,
		final CommandSettings ttsCommand
	) {
		this.backend = backend;
		this.baseUri = baseUri;
		this.apiKey = apiKey;
		this.modelName = modelName;
		this.contextLength = contextLength;
		this.timeout = timeout;
		this.ytDlpPath = ytDlpPath;
		this.videoHosts = videoHosts;
		this.ttsCommand = ttsCommand;
	}

	public Backend getBackend() {
		return backend;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getModelName() {
		return modelName;
	}

	public int getContextLength() {
		return contextLength;
	}

	public long getTimeout() {
		return timeout;
	}

	public String getYtDlpPath() {
		return ytDlpPath;
	}

	public List<String> getVideoHosts() {
		return videoHosts;
	}

	public CommandSettings getTtsCommand() {
		return ttsCommand;
	}

	@Override
	public String toString() {
		return "Settings [backend=" + backend + ", baseUri=" + baseUri + ", apiKey=" + apiKey + ", modelName="
				+ modelName + ", contextLength=" + contextLength + ", timeout=" + timeout + ", videoHosts=" + videoHosts
				+ ", ttsCommand=" + ttsCommand + ", ytDlpPath=" + ytDlpPath + "]";
	}

	public static Settings fromFile(final File configuration) throws ConfigurateException {
		HoconConfigurationLoader loader = HoconConfigurationLoader.builder().file(configuration).build();
		CommentedConfigurationNode root = loader.load();
		CommandSettings ttsCommand = CommandSettings.fromNode(root.node("tts"));
		List<String> videoHosts = root.node("hosts", "video").getList(String.class);

		return new Settings(
			root.node("backend").get(Backend.class),
			root.node("base_uri").getString(DEFAULT_URI),
			root.node("api_key").getString(),
			root.node("model_name").getString(MODEL_NAME),
			root.node("context_length").getInt(CONTEXT_LENGTH),
			root.node("timeout").getLong(DEFAULT_TIMEOUT),
			PathUtil.absolutePath(root.node("ytdlp").getString(YTDLP_PATH)),
			videoHosts,
			ttsCommand
		);
	}

}
