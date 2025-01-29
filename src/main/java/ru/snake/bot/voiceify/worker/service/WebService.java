package ru.snake.bot.voiceify.worker.service;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;
import net.dankito.readability4j.extended.Readability4JExtended;
import ru.snake.bot.voiceify.worker.data.ArticleResult;

public class WebService {

	private static final Logger LOG = LoggerFactory.getLogger(WebService.class);

	private WebService() {
	}

	public ArticleResult articleText(String uri) throws IOException {
		LOG.info("Load article from `{}`", uri);

		Document document = Jsoup.connect(uri).timeout(60 * 1000).get();
		document.select("table, code, pre").forEach(e -> e.remove());

		String html = document.html();
		Readability4J readability4J = new Readability4JExtended(uri, html);
		Article article = readability4J.parse();
		String title = article.getTitle();
		String text = article.getTextContent();

		return ArticleResult.from(title, text);
	}

	@Override
	public String toString() {
		return "WebService []";
	}

	public static WebService create() {
		return new WebService();
	}

}
