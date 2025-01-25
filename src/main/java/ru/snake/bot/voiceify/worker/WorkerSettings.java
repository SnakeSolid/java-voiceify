package ru.snake.bot.voiceify.worker;

import java.io.File;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

public class WorkerSettings {

	private static final String DEFAULT_URI = "http://localhost:11434/";

	private static final String MODEL_NAME = "gemma2";

	private static final long DEFAULT_TIMEOUT = 120;

	private String ollamaUri;

	private String modelName;

	private long timeout;

	private CommandSettings ttsCommand;

	private WorkerSettings(
		final String ollamaUri,
		final String modelName,
		final long timeout,
		CommandSettings ttsCommand
	) {
		this.ollamaUri = ollamaUri;
		this.modelName = modelName;
		this.timeout = timeout;
		this.ttsCommand = ttsCommand;
	}

	public String getOllamaUri() {
		return ollamaUri;
	}

	public WorkerSettings withOllamaUri(String ollamaUri) {
		this.ollamaUri = ollamaUri;

		return this;
	}

	public String getModelName() {
		return modelName;
	}

	public WorkerSettings withModelName(String modelName) {
		this.modelName = modelName;

		return this;
	}

	public long getTimeout() {
		return timeout;
	}

	public WorkerSettings withTimeout(long timeout) {
		this.timeout = timeout;

		return this;
	}

	public CommandSettings getTtsCommand() {
		return ttsCommand;
	}

	@Override
	public String toString() {
		return "WorkerSettings [ollamaUri=" + ollamaUri + ", modelName=" + modelName + ", timeout=" + timeout
				+ ", ttsCommand=" + ttsCommand + "]";
	}

	public static WorkerSettings fromFile(final File configuration) throws ConfigurateException {
		HoconConfigurationLoader loader = HoconConfigurationLoader.builder().file(configuration).build();
		CommentedConfigurationNode root = loader.load();
		CommandSettings ttsCommand = CommandSettings.fromNode(root.node("tts"));

		return new WorkerSettings(
			root.node("uri").getString(DEFAULT_URI),
			root.node("model").getString(MODEL_NAME),
			root.node("timeout").getLong(DEFAULT_TIMEOUT),
			ttsCommand
		);
	}

}
