package ru.snake.bot.voiceify.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

		assertEquals("This is a ...", TextUtil.trimText(longText, maxLength));
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

}
