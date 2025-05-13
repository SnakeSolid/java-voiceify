package ru.snake.bot.voiceify.worker;

import java.io.File;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.snake.bot.voiceify.database.Language;
import ru.snake.bot.voiceify.settings.Settings;
import ru.snake.bot.voiceify.text.Escaper;
import ru.snake.bot.voiceify.util.TextUtil;
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

	private final int maxFragmentChars;

	private CallbackSuccess callbackSuccess;

	private CallbackError callbackError;

	public Worker(
		final LlmService llmService,
		final WebService webService,
		final YtService ytService,
		final TtsService ttsService,
		final int maxFragmentChars
	) {
		this.llmService = llmService;
		this.webService = webService;
		this.ytService = ytService;
		this.ttsService = ttsService;
		this.maxFragmentChars = maxFragmentChars;
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
				executeSafe(job, () -> processText(job.getText(), language, r -> sendResult(job, r)));
				break;

			case SUBTITLES:
				executeSafe(job, () -> processSubtitles(job.getText(), language, r -> sendResult(job, r)));
				break;

			case ARTICLE:
				executeSafe(job, () -> processArticle(job.getUri(), language, r -> sendResult(job, r)));
				break;

			case VIDEO:
				executeSafe(job, () -> processVideo(job.getUri(), language, r -> sendResult(job, r)));
				break;

			default:
			}
		}
	}

	@FunctionalInterface
	private static interface Executable {
		public void execute() throws Exception;
	}

	private void executeSafe(Job job, Executable executable) {
		try {
			executable.execute();
		} catch (Exception e) {
			try {
				callbackError.call(job.getChatId(), job.getMessageId(), job.getUri(), e.getMessage());
			} catch (Exception ee) {
				LOG.warn("Failed to execute error callback", ee);
			}
		}
	}

	private void sendResult(Job job, JobResult result) {
		try {
			if (result.isSuccess()) {
				callbackSuccess.call(job.getChatId(), job.getMessageId(), result.getText(), result.getSpeechPath());
			} else {
				callbackError.call(job.getChatId(), job.getMessageId(), job.getUri(), result.getMessage());
			}
		} catch (Exception e) {
			LOG.warn("Failed to send result", e);
		}
	}

	private void processVideo(String uri, Language language, Consumer<JobResult> callback) throws Exception {
		LOG.info("Processing video: {}", uri);

		SubtitlesResult resultSubtitles = ytService.videoSubtitles(uri);
		String atricle = llmService.subsToArticle(resultSubtitles.getSubtitles());
		String content = llmService.translateText(atricle, language);
		String text = asLink(uri, resultSubtitles.getTitle());

		contentToSpeech(content, text, callback);
	}

	private void processArticle(String uri, Language language, Consumer<JobResult> callback) throws Exception {
		LOG.info("Processing acticle: {}", uri);

		ArticleResult resultArticle = webService.articleText(uri);
		String content = llmService.translateText(resultArticle.getText(), language);
		String text = asLink(uri, resultArticle.getTitle());

		contentToSpeech(content, text, callback);
	}

	private void processSubtitles(String text, Language language, Consumer<JobResult> callback) throws Exception {
		LOG.info("Processing subtitles: {}", TextUtil.trimText(text, 256));

		String atricle = llmService.subsToArticle(text);
		String content = llmService.translateText(atricle, language);
		String caption = Escaper.escapeMarkdown(llmService.writeCaption(content));

		contentToSpeech(content, caption, callback);
	}

	private void processText(String text, Language language, Consumer<JobResult> callback) throws Exception {
		LOG.info("Processing text `{}`", TextUtil.trimText(text, 256));

		String content = llmService.translateText(text, language);
		String caption = Escaper.escapeMarkdown(llmService.writeCaption(content));

		contentToSpeech(content, caption, callback);
	}

	private void contentToSpeech(String content, String caption, Consumer<JobResult> callback) throws Exception {
		List<String> fragments = TextUtil.split(content, maxFragmentChars);
		int nFragments = fragments.size();

		for (int index = 0; index < nFragments; index += 1) {
			String fragment = fragments.get(index);
			TextToSpeechResult resultTts = ttsService.textToSpeech(fragment);
			String title = makeTitle(caption, nFragments, index);
			JobResult result = new JobResult(
				resultTts.isSuccess(),
				title,
				resultTts.getSpeechPath(),
				resultTts.getMessage()
			);

			callback.accept(result);
		}
	}

	private String makeTitle(String caption, int nFragments, int fragmenIndex) {
		if (nFragments == 1) {
			return caption;
		}

		String text = String.format("%s (часть %d/%d)", caption, fragmenIndex + 1, nFragments);

		return Escaper.escapeMarkdown(text);
	}

	private String asLink(String uri, String caption) {
		String escaped = Escaper.escapeMarkdown(caption);

		return String.format("[%s](%s)", escaped, uri);
	}

	@Override
	public String toString() {
		return "Worker [llmService=" + llmService + ", webService=" + webService + ", ytService=" + ytService
				+ ", ttsService=" + ttsService + ", queue=" + queue + ", processing=" + processing
				+ ", maxFragmentChars=" + maxFragmentChars + ", callbackSuccess=" + callbackSuccess + ", callbackError="
				+ callbackError + "]";
	}

	public static Worker create(final File cacheDirectory, final Settings settings) {
		LlmService llmService = LlmService.create(settings);
		TtsService ttsService = TtsService.create(settings, cacheDirectory);
		WebService webService = WebService.create();
		YtService ytService = YtService.create(settings, cacheDirectory);
		Worker worker = new Worker(llmService, webService, ytService, ttsService, settings.getMaxFragmentChars());

		return worker;
	}

}
