package ru.snake.bot.voiceify.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TextUtilTest {

	@Test
	public void testTrimText_ShortString() {
		String shortText = "Hello";
		int maxLength = 10;

		assertEquals("Hello", TextUtil.trimText(shortText, maxLength));
	}

	@Test
	public void testTrimText_LongString() {
		String longText = "This is a longer text that needs to be trimmed.";
		int maxLength = 10;

		assertEquals("This is...", TextUtil.trimText(longText, maxLength));
	}

	@Test
	public void testTrimText_TwoChars() {
		String longText = "Lorem ipsum.";
		int maxLength = 2;

		assertEquals("Lo", TextUtil.trimText(longText, maxLength));
	}

	@Test
	public void testUnquoteText_NoQuotes() {
		String text = "Hello World";

		assertEquals("Hello World", TextUtil.unquoteText(text));
	}

	@Test
	public void testUnquoteText_SingleQuotedString() {
		String text = "'Hello World'";

		assertEquals("Hello World", TextUtil.unquoteText(text));
	}

	@Test
	public void testUnquoteText_DoubleQuotedString() {
		String text = "\"Hello World\"";

		assertEquals("Hello World", TextUtil.unquoteText(text));
	}

	@Test
	public void testUnquoteText_LeftRightQuotes() {
		String text = "«Hello World»";

		assertEquals("Hello World", TextUtil.unquoteText(text));
	}

	@Test
	public void testUnquoteText_ComplexString() {
		String text = "‘This is a quoted text’ with multiple quotes.";

		assertEquals("‘This is a quoted text’ with multiple quotes.", TextUtil.unquoteText(text));
	}

	@Test
	void contentShorterThanMax_ReturnsSingleFragment() {
		List<String> result = TextUtil.split("test", 5);
		assertEquals(1, result.size());
		assertEquals("test", result.get(0));
	}

	@Test
	void contentExactMax_ReturnsSingleFragment() {
		String content = "12345";
		List<String> result = TextUtil.split(content, 5);
		assertEquals(1, result.size());
		assertEquals(content, result.get(0));
	}

	@Test
	void addsRemainingBuilder() {
		String content = "a.b.";
		List<String> fragments = TextUtil.split(content, 3);
		assertEquals(1, fragments.size());
		assertEquals("a.b.", fragments.get(0));
	}

	@Test
	void handlesEmptyContent() {
		List<String> result = TextUtil.split("", 10);
		assertEquals(1, result.size());
		assertEquals("", result.get(0));
	}

	@Test
	void throwsExceptionWhenMaxIsZero() {
		assertThrows(ArithmeticException.class, () -> TextUtil.split("content", 0));
	}

	@Test
	void fragmentExceedsMaxFragmentChars() {
		String content = "longlong";
		List<String> fragments = TextUtil.split(content, 5);

		assertEquals(1, fragments.size());
		assertEquals("longlong", fragments.get(0));
		assertTrue(fragments.get(0).length() > 5);
	}

	@Test
	void multipleSentencesWithinFragmentLength() {
		String content = "Hello.World.How.Are.You.";
		List<String> fragments = TextUtil.split(content, 10);

		assertTrue(fragments.size() > 1);
		assertEquals(fragments, List.of("Hello.World.", "How.Are.You."));
	}

}
