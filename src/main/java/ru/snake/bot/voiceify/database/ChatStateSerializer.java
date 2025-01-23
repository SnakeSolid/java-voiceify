package ru.snake.bot.voiceify.database;

import java.io.IOException;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class ChatStateSerializer extends GroupSerializerObjectArray<ChatState> {

	private static final ChatStateSerializer INSTANCE = new ChatStateSerializer();

	@Override
	public void serialize(DataOutput2 out, ChatState value) throws IOException {
		out.writeInt(value.ordinal());
	}

	@Override
	public ChatState deserialize(DataInput2 input, int available) throws IOException {
		int index = input.readInt();

		return ChatState.values()[index];
	}

	@Override
	public int fixedSize() {
		return Integer.BYTES;
	}

	@Override
	public boolean isTrusted() {
		return true;
	}

	public static Serializer<ChatState> instance() {
		return INSTANCE;
	}

}
