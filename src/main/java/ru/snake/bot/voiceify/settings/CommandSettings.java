package ru.snake.bot.voiceify.settings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class CommandSettings {

	private final String command;

	private final List<String> arguments;

	private final Map<String, String> environment;

	public CommandSettings(final String command, final List<String> arguments, final Map<String, String> environment) {
		this.command = command;
		this.arguments = arguments;
		this.environment = environment;
	}

	public String getCommand() {
		return command;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	@Override
	public String toString() {
		return "CommandSettings [command=" + command + ", arguments=" + arguments + ", environment=" + environment
				+ "]";
	}

	public static CommandSettings fromNode(CommentedConfigurationNode node) throws SerializationException {
		String command = PathUtil.absolutePath(node.node("command").getString());
		List<String> arguments = node.node("arguments").getList(String.class);
		Map<String, String> environment = new LinkedHashMap<>();

		for (Entry<Object, CommentedConfigurationNode> row : node.node("environment").childrenMap().entrySet()) {
			String key = row.getKey().toString();
			String value = row.getValue().getString();

			environment.put(key, value);
		}

		return new CommandSettings(command, arguments, environment);
	}

}
