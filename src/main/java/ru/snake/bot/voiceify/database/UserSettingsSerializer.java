package ru.snake.bot.voiceify.database;

import java.io.IOException;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class UserSettingsSerializer extends GroupSerializerObjectArray<UserSettings> {

	private static final UserSettingsSerializer INSTANCE = new UserSettingsSerializer();

	@Override
	public void serialize(DataOutput2 out, UserSettings value) throws IOException {
		out.writeInt(value.getLanguage().ordinal());
		out.writeBoolean(value.isShorten());
	}

	@Override
	public UserSettings deserialize(DataInput2 input, int available) throws IOException {
		Language language = Language.values()[input.readInt()];
		boolean shorten = input.readBoolean();

		return UserSettings.create(language, shorten);
	}

	@Override
	public int fixedSize() {
		return Integer.BYTES;
	}

	@Override
	public boolean isTrusted() {
		return true;
	}

	public static Serializer<UserSettings> instance() {
		return INSTANCE;
	}

}
