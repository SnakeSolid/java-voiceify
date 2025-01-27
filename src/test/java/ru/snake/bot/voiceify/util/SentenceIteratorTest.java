package ru.snake.bot.voiceify.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class SentenceIteratorTest {

	@Test
	void testHasNextOnEmptyText() {
		SentenceIterator iterator = new SentenceIterator("");

		assertFalse(iterator.hasNext());
	}

	@Test
	void testHasNextOnNonEmptyText() {
		SentenceIterator iterator = new SentenceIterator("Hello world.");

		assertTrue(iterator.hasNext());
	}

	@Test
	void testNextOnSingleSentence() {
		SentenceIterator iterator = new SentenceIterator("Hello world.");

		assertEquals("Hello world.", iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	void testNextOnMultipleSentences() {
		SentenceIterator iterator = new SentenceIterator("Hello world.\nHow are you?");

		assertEquals("Hello world.", iterator.next());
		assertEquals("\n", iterator.next());
		assertEquals("How are you?", iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	void testNextOnTextWithOnlyNewLines() {
		SentenceIterator iterator = new SentenceIterator("\n\n\n");

		assertEquals("\n", iterator.next());
		assertEquals("\n", iterator.next());
		assertEquals("\n", iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	void testNextOnTextWithOnlyDots() {
		SentenceIterator iterator = new SentenceIterator("...");

		assertEquals(".", iterator.next());
		assertEquals(".", iterator.next());
		assertEquals(".", iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	void testNextOnMixedText() {
		SentenceIterator iterator = new SentenceIterator("Hello.\nHow are you?\nI'm fine.");

		assertEquals("Hello.", iterator.next());
		assertEquals("\n", iterator.next());
		assertEquals("How are you?", iterator.next());
		assertEquals("\n", iterator.next());
		assertEquals("I'm fine.", iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	void testNextThrowsExceptionWhenNoMoreElements() {
		SentenceIterator iterator = new SentenceIterator("Hello world.");
		iterator.next();

		assertThrows(NoSuchElementException.class, iterator::next);
	}

	@Test
	void testIteratorMethod() {
		SentenceIterator iterator = new SentenceIterator("Hello world.");
		Iterator<String> it = iterator.iterator();

		assertTrue(it.hasNext());
		assertEquals("Hello world.", it.next());
		assertFalse(it.hasNext());
	}

}
