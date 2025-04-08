package ru.snake.bot.voiceify.util;

public class Quotes {

	private final String left;

	private final String right;

	public Quotes(String left, String right) {
		this.left = left;
		this.right = right;
	}

	public String getLeft() {
		return left;
	}

	public String getRight() {
		return right;
	}

	@Override
	public String toString() {
		return "Quotes [left=" + left + ", right=" + right + "]";
	}

	public static Quotes from(final String quotes) {
		return new Quotes(quotes, quotes);
	}

	public static Quotes from(final String left, final String right) {
		return new Quotes(left, right);
	}

}
