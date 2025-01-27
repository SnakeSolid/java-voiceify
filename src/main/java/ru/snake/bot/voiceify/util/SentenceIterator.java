package ru.snake.bot.voiceify.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SentenceIterator implements Iterable<String>, Iterator<String> {

	private final String text;

	private int offset;

	public SentenceIterator(String text) {
		this.text = text;
		this.offset = 0;
	}

	@Override
	public boolean hasNext() {
		return offset < text.length();
	}

	@Override
	public String next() {
		if (offset == text.length()) {
			throw new NoSuchElementException();
		}

		int indexSeparator = separatorIndex('\n', '.', '!', '?');
		int index = offset;

		if (indexSeparator != -1) {
			offset = indexSeparator + 1;
		} else {
			offset = text.length();
		}

		return text.substring(index, offset);
	}

	private int separatorIndex(char... chars) {
		int index = -1;

		for (char ch : chars) {
			int charIndex = text.indexOf(ch, offset);

			if (charIndex != -1 && index == -1) {
				index = charIndex;
			} else if (charIndex != -1 && charIndex < index) {
				index = charIndex;
			}
		}

		return index;
	}

	@Override
	public Iterator<String> iterator() {
		return this;
	}

	@Override
	public String toString() {
		return "SentenceIterator [text=" + text + ", offset=" + offset + "]";
	}

}
