package ru.snake.bot.voiceify.consume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import ru.snake.bot.voiceify.consume.callback.AccessDeniedAction;
import ru.snake.bot.voiceify.consume.callback.Callback;
import ru.snake.bot.voiceify.consume.callback.CallbackAction;
import ru.snake.bot.voiceify.consume.callback.CommandAction;
import ru.snake.bot.voiceify.consume.callback.DocumentAction;
import ru.snake.bot.voiceify.consume.callback.MessageAction;
import ru.snake.bot.voiceify.consume.callback.MessageUrlAction;

public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(UpdateConsumer.class);

	private final Set<Long> whiteList;

	private final Map<String, CommandAction> commands;

	private final Map<String, CallbackAction> callbacks;

	private CommandAction unknownCommand;

	private CallbackAction unknownCallback;

	private MessageUrlAction messageUrlAction;

	private MessageAction messageAction;

	private DocumentAction documentAction;

	private AccessDeniedAction accessDeniedAction;

	public UpdateConsumer(final Set<Long> whiteList) {
		this.whiteList = whiteList;
		this.commands = new HashMap<>();
		this.callbacks = new HashMap<>();
		this.unknownCommand = null;
		this.unknownCallback = null;
		this.messageAction = null;
		this.documentAction = null;
		this.accessDeniedAction = null;
	}

	public UpdateConsumer onCommand(final String command, final CommandAction callback) {
		commands.put(command, callback);

		return this;
	}

	public UpdateConsumer onCommand(final CommandAction callback) {
		unknownCommand = callback;

		return this;
	}

	public UpdateConsumer onCallback(final String command, final CallbackAction callback) {
		callbacks.put(command, callback);

		return this;
	}

	public UpdateConsumer onCallback(final CallbackAction callback) {
		unknownCallback = callback;

		return this;
	}

	public UpdateConsumer onMessage(final MessageAction callback) {
		messageAction = callback;

		return this;
	}

	public UpdateConsumer onDocument(final DocumentAction callback) {
		documentAction = callback;

		return this;
	}

	public UpdateConsumer onMessage(final MessageUrlAction callback) {
		messageUrlAction = callback;

		return this;
	}

	public UpdateConsumer onAccessDenied(AccessDeniedAction callback) {
		this.accessDeniedAction = callback;

		return this;
	}

	@Override
	public void consume(Update update) {
		if (update.hasMessage()) {
			Message message = update.getMessage();

			consumeMessage(message);
		} else if (update.hasEditedMessage()) {
			Message message = update.getEditedMessage();

			consumeMessage(message);
		} else if (update.hasCallbackQuery()) {
			CallbackQuery query = update.getCallbackQuery();

			consumeCallback(query);
		}
	}

	private void consumeCallback(CallbackQuery query) {
		long userId = query.getFrom().getId();
		long chatId = query.getMessage().getChatId();
		int messageId = query.getMessage().getMessageId();
		Context context = Context.from(userId, chatId, messageId);

		if (!whiteList.contains(userId)) {
			consume(accessDeniedAction, action -> action.consume(context));

			LOG.warn("Access denied for user ID = {}.", userId);

			return;
		}

		String callback = query.getData();
		String queryId = query.getId();

		consume(
			callbacks.getOrDefault(callback, unknownCallback),
			command -> command.consume(context, queryId, callback)
		);
	}

	private void consumeMessage(Message message) {
		long userId = message.getFrom().getId();
		long chatId = message.getChatId();
		int messageId = message.getMessageId();
		Context context = Context.from(userId, chatId, messageId);

		if (!whiteList.contains(userId)) {
			consume(accessDeniedAction, action -> action.consume(context));

			LOG.warn("Access denied for user ID = {}.", userId);

			return;
		}

		List<MessageEntity> entities = get(message, Message::hasEntities, Message::getEntities);
		List<MessageEntity> linkEntities = getLinks(entities);
		List<MessageEntity> botCommands = getBotCommands(entities);
		String text = get(message, Message::hasText, Message::getText);
		Document document = get(message, Message::hasDocument, Message::getDocument);

		if (!linkEntities.isEmpty() && text != null) {
			consume(messageUrlAction, action -> action.consume(context, text, linkEntities));
		} else if (!botCommands.isEmpty()) {
			for (MessageEntity entity : botCommands) {
				String botCommand = entity.getText();

				consume(
					commands.getOrDefault(botCommand, unknownCommand),
					command -> command.consume(context, botCommand)
				);
			}
		} else if (document != null) {
			consume(documentAction, action -> action.consume(context, document.getFileId(), document.getMimeType()));
		} else if (text != null) {
			consume(messageAction, action -> action.consume(context, text));
		}
	}

	private List<MessageEntity> getLinks(List<MessageEntity> entities) {
		if (entities == null || entities.isEmpty()) {
			return Collections.emptyList();
		}

		List<MessageEntity> result = new ArrayList<>();

		for (MessageEntity entity : entities) {
			if (Objects.equals(EntityType.URL, entity.getType())) {
				result.add(entity);
			} else if (Objects.equals(EntityType.TEXTLINK, entity.getType())) {
				result.add(entity);
			}
		}

		return result;
	}

	private List<MessageEntity> getBotCommands(List<MessageEntity> entities) {
		if (entities == null || entities.isEmpty()) {
			return Collections.emptyList();
		}

		List<MessageEntity> result = new ArrayList<>();

		for (MessageEntity entity : entities) {
			if (Objects.equals(EntityType.BOTCOMMAND, entity.getType())) {
				result.add(entity);
			}
		}

		return result;
	}

	private static <T> void consume(T object, Callback<T> action) {
		if (object != null) {
			try {
				action.call(object);
			} catch (Exception e) {
				LOG.error("Failed to process request.", e);
			} catch (Error e) {
				LOG.error("Failed to process request.", e);

				// Required to stop application if runtime error occurred.
				System.exit(0);
			}
		}
	}

	private static <T> T get(Message message, Predicate<Message> predicate, Function<Message, T> mapper) {
		if (predicate.test(message)) {
			return mapper.apply(message);
		}

		return null;
	}

	@Override
	public String toString() {
		return "UpdateConsumer [whiteList=" + whiteList + ", commands=" + commands + ", callbacks=" + callbacks
				+ ", unknownCommand=" + unknownCommand + ", unknownCallback=" + unknownCallback + ", messageUrlAction="
				+ messageUrlAction + ", messageAction=" + messageAction + ", documentAction=" + documentAction
				+ ", accessDeniedAction=" + accessDeniedAction + "]";
	}

}
