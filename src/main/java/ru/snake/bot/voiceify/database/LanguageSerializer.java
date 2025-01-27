package ru.snake.bot.voiceify.database;

import java.io.IOException;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class LanguageSerializer extends GroupSerializerObjectArray<Language> {

	private static final LanguageSerializer INSTANCE = new LanguageSerializer();

	@Override
	public void serialize(DataOutput2 out, Language value) throws IOException {
		out.writeInt(value.ordinal());
	}

	@Override
	public Language deserialize(DataInput2 input, int available) throws IOException {
		int index = input.readInt();

		return Language.values()[index];
	}

	@Override
	public int fixedSize() {
		return Integer.BYTES;
	}

	@Override
	public boolean isTrusted() {
		return true;
	}

	public static Serializer<Language> instance() {
		return INSTANCE;
	}

}
