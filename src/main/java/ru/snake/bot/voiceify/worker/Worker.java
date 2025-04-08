package ru.snake.bot.voiceify.worker;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.ollama4j.exceptions.OllamaBaseException;
import ru.snake.bot.voiceify.database.Language;
import ru.snake.bot.voiceify.settings.Settings;
import ru.snake.bot.voiceify.text.Escaper;
import ru.snake.bot.voiceify.util.TextUtil;
import ru.snake.bot.voiceify.worker.backend.LlmBackendException;
import ru.snake.bot.voiceify.worker.data.ArticleResult;
import ru.snake.bot.voiceify.worker.data.SubtitlesResult;
import ru.snake.bot.voiceify.worker.data.TextToSpeechResult;
import ru.snake.bot.voiceify.worker.service.LlmService;
import ru.snake.bot.voiceify.worker.service.TtsService;
import ru.snake.bot.voiceify.worker.service.WebService;
import ru.snake.bot.voiceify.worker.service.YtService;

public class Worker {

	private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

	private static final int QUEUE_SIZE = 100;

	private final LlmService llmService;

	private final WebService webService;

	private final YtService ytService;

	private final TtsService ttsService;

	private final BlockingQueue<Job> queue;

	private final AtomicBoolean processing;

	private CallbackSuccess callbackSuccess;

	private CallbackError callbackError;

	public Worker(
		final LlmService llmService,
		final WebService webService,
		final YtService ytService,
		final TtsService ttsService
	) {
		this.llmService = llmService;
		this.webService = webService;
		this.ytService = ytService;
		this.ttsService = ttsService;
		this.queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
		this.processing = new AtomicBoolean();
		this.callbackSuccess = null;
		this.callbackError = null;
	}

	public int getQueueLength() {
		return queue.size();
	}

	public boolean isProcessing() {
		return processing.get();
	}

	public void setCallbackSuccess(CallbackSuccess callbackSuccess) {
		this.callbackSuccess = callbackSuccess;
	}

	public void setCallbackError(CallbackError callbackError) {
		this.callbackError = callbackError;
	}

	public void start() {
		Thread thread = new Thread(this::messageLoop, "Worker thread");
		thread.setDaemon(true);
		thread.start();
	}

	public void sendText(long chatId, int messageId, String text, Language language) throws InterruptedException {
		LOG.info("Queued text: {}", TextUtil.trimText(text, 256));

		Job job = Job.text(chatId, messageId, text, language);
		queue.put(job);
	}

	public void queueSubtitles(long chatId, int messageId, String text, Language language) throws InterruptedException {
		LOG.info("Queued subtitles: {}", TextUtil.trimText(text, 256));

		Job job = Job.subtitles(chatId, messageId, text, language);
		queue.put(job);
	}

	public void queueArticle(long chatId, int messageId, String uri, Language language) throws InterruptedException {
		LOG.info("Queued article: {}", uri);

		Job job = Job.article(chatId, messageId, uri, language);
		queue.put(job);
	}

	public void queueVideo(long chatId, int messageId, String uri, Language language) throws InterruptedException {
		LOG.info("Queued video: {}", uri);

		Job job = Job.video(chatId, messageId, uri, language);
		queue.put(job);
	}

	private void messageLoop() {
		while (true) {
			Job job;

			try {
				processing.set(false);
				job = queue.take();
				processing.set(true);
			} catch (InterruptedException e) {
				break;
			}

			Language language = job.getLanguage();

			switch (job.getType()) {
			case TEXT:
				sendResult(job, () -> processText(job.getText(), language));
				break;

			case SUBTITLES:
				sendResult(job, () -> processSubtitles(job.getText(), language));
				break;

			case ARTICLE:
				sendResult(job, () -> processArticle(job.getUri(), language));
				break;

			case VIDEO:
				sendResult(job, () -> processVideo(job.getUri(), language));
				break;

			default:
			}
		}
	}

	private void sendResult(Job job, Processor<JobResult> processor) {
		try {
			JobResult result = processor.process();

			if (result.isSuccess()) {
				callbackSuccess.call(job.getChatId(), job.getMessageId(), result.getText(), result.getSpeechPath());
			} else {
				callbackError.call(job.getChatId(), job.getMessageId(), job.getUri(), result.getMessage());
			}
		} catch (Exception e) {
			try {
				callbackError.call(job.getChatId(), job.getMessageId(), job.getUri(), e.getMessage());
			} catch (Exception ee) {
				LOG.warn("Failed to execute error callback", ee);
			}
		}
	}

	private JobResult processVideo(String uri, Language language)
			throws Exception, IOException, OllamaBaseException, InterruptedException {
		LOG.info("Processing video: {}", uri);

		SubtitlesResult resultSubtitles = ytService.videoSubtitles(uri);
		String atricle = llmService.subsToArticle(resultSubtitles.getSubtitles());
		String content = llmService.translateText(atricle, language);
		TextToSpeechResult resultTts = ttsService.textToSpeech(content);
		String text = asLink(uri, resultSubtitles.getTitle());

		return new JobResult(resultTts.isSuccess(), text, resultTts.getSpeechPath(), resultTts.getMessage());
	}

	private JobResult processArticle(String uri, Language language)
			throws IOException, LlmBackendException, InterruptedException {
		LOG.info("Processing acticle: {}", uri);

		ArticleResult resultArticle = webService.articleText(uri);
		String content = llmService.translateText(resultArticle.getText(), language);
		TextToSpeechResult resultTts = ttsService.textToSpeech(content);
		String text = asLink(uri, resultArticle.getTitle());

		return new JobResult(resultTts.isSuccess(), text, resultTts.getSpeechPath(), resultTts.getMessage());
	}

	private JobResult processSubtitles(String text, Language language)
			throws Exception, IOException, OllamaBaseException, InterruptedException {
		LOG.info("Processing subtitles: {}", TextUtil.trimText(text, 256));

		String atricle = llmService.subsToArticle(text);
		String content = llmService.translateText(atricle, language);
		String caption = Escaper.escapeMarkdown(llmService.writeCaption(content));
		TextToSpeechResult resultTts = ttsService.textToSpeech(content);

		return new JobResult(resultTts.isSuccess(), caption, resultTts.getSpeechPath(), resultTts.getMessage());
	}

	private JobResult processText(String text, Language language)
			throws IOException, LlmBackendException, InterruptedException {
		LOG.info("Processing text `{}`", TextUtil.trimText(text, 256));

		String content = llmService.translateText(text, language);
		String caption = Escaper.escapeMarkdown(llmService.writeCaption(content));
		TextToSpeechResult resultTts = ttsService.textToSpeech(content);

		return new JobResult(resultTts.isSuccess(), caption, resultTts.getSpeechPath(), resultTts.getMessage());
	}

	private String asLink(String uri, String caption) {
		String escaped = Escaper.escapeMarkdown(caption);

		return String.format("[%s](%s)", escaped, uri);
	}

	@Override
	public String toString() {
		return "Worker [llmService=" + llmService + ", webService=" + webService + ", ytService=" + ytService
				+ ", ttsService=" + ttsService + ", callbackSuccess=" + callbackSuccess + ", callbackError="
				+ callbackError + ", queue=" + queue + "]";
	}

	public static Worker create(final File cacheDirectory, final Settings settings) {
		LlmService llmService = LlmService.create(settings);
		TtsService ttsService = TtsService.create(settings, cacheDirectory);
		WebService webService = WebService.create();
		YtService ytService = YtService.create(settings, cacheDirectory);
		Worker worker = new Worker(llmService, webService, ytService, ttsService);

		return worker;
	}

}
