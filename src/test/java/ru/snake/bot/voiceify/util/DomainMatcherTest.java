package ru.snake.bot.voiceify.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DomainMatcherTest {

	@Test
	public void testMatchWithMultipleMasks() {
		assertTrue(DomainMatcher.match("www.youtube.com", "*.youtube.com", "*.google.com"));
		assertTrue(DomainMatcher.match("m.youtube.com", "*.youtube.com", "*.google.com"));
		assertFalse(DomainMatcher.match("www.youtubekids.com", "*.youtube.com", "*.google.com"));
	}

	@Test
	public void testMatchWithNoMasks() {
		assertFalse(DomainMatcher.match("www.youtube.com"));
	}

	@Test
	public void testMatchWithExactMatch() {
		assertTrue(DomainMatcher.match("example.com", "example.com", "*.example.com"));
		assertFalse(DomainMatcher.match("example.com", "another.com", "*.example.com"));
	}

	@Test
	public void testMatchWithDifferentLengthMasks() {
		assertFalse(DomainMatcher.match("www.youtube.com", "*.youtube.com.au", "*.google.com"));
		assertTrue(DomainMatcher.match("www.youtube.com", "*.youtube.com", "*.youtube.com.au"));
	}

	@Test
	public void testMatchingDomains() {
		assertTrue(DomainMatcher.match("www.youtube.com", "*.youtube.com"));
		assertTrue(DomainMatcher.match("m.youtube.com", "*.youtube.com"));
		assertTrue(DomainMatcher.match("subdomain.youtube.com", "*.youtube.com"));
	}

	@Test
	public void testNonMatchingDomains() {
		assertFalse(DomainMatcher.match("www.youtubekids.com", "www.youtube.com"));
		assertFalse(DomainMatcher.match("www.youtube.com", "*.youtubekids.com"));
		assertFalse(DomainMatcher.match("m.youtubekids.com", "*.youtube.com"));
		assertFalse(DomainMatcher.match("youtube.com", "*.youtubekids.com"));
	}

	@Test
	public void testWildcardOnly() {
		assertTrue(DomainMatcher.match("any.domain.com", "*.domain.com"));
		assertTrue(DomainMatcher.match("another.domain.com", "*.domain.com"));
		assertFalse(DomainMatcher.match("domain.com", "*.domain.com"));
	}

	@Test
	public void testDifferentLengthDomains() {
		assertFalse(DomainMatcher.match("www.youtube.com", "*.youtube.com.au"));
		assertFalse(DomainMatcher.match("m.youtube.com", "*.youtube.com.au"));
	}

	@Test
	public void testExactMatch() {
		assertTrue(DomainMatcher.match("example.com", "example.com"));
		assertFalse(DomainMatcher.match("example.com", "another.com"));
	}

}
