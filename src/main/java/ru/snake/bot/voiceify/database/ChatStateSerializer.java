package ru.snake.bot.voiceify.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class ChatStateSerializer extends GroupSerializerObjectArray<ChatState> {

	private static final ChatStateSerializer INSTANCE = new ChatStateSerializer();

	@Override
	public void serialize(DataOutput2 out, ChatState value) throws IOException {
		List<String> uriStrings = value.getUriStrings();

		out.writeUTF(value.getText());
		out.writeInt(uriStrings.size());

		for (String uriString : uriStrings) {
			out.writeUTF(uriString);
		}
	}

	@Override
	public ChatState deserialize(DataInput2 input, int available) throws IOException {
		String text = input.readUTF();
		List<String> uriStrings = new ArrayList<>();
		int nUriStrings = input.readInt();

		for (int index = 0; index < nUriStrings; index += 1) {
			uriStrings.add(input.readUTF());
		}

		return ChatState.create(text, uriStrings);
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
