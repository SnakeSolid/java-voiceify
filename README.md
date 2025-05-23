# Voiceify Bot

A Telegram bot designed to simplify reading articles from websites. Bot convert
articles from URI, rewrite video subtitles or read test message.

![Bot usage example](images/voiceify.png)

## Features

- [x] Read text messages.
- [x] Read articles by URI.
- [x] Read text documents.
- [x] Convert subtitles into structured articles and read them.
- [x] Support Russian and English voices.
- [x] Support summarization/reduction of large texts.

## Build

Clone repository and start following command:

```sh
mvn package assembly:single
```

## Usage

Start application using following command:

```sh
java -jar target/voiceify-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  --bot-token "TOKEN" \
  --allow-user ID1 \
  --allow-user ID2 \
  --allow-user ID3
```

Where `TOKEN` is telegram bot token. `ID*` owners user id, can be set to -1 bot
will send user id in reply message.

## LLM Settings

By default, the local ollama server located at `http://localhost:11434/`
is used. These settings can be changed using the configuration file (option `--config`).

Example of configuration file with default settings:

```
backend = OpenAi # Or `Ollama` for ollama backend
base_uri = "http://127.0.0.1:1234/v1/"
model_name = "yandexgpt-5-lite-8b-instruct"
context_length         : 8000 # One token approximately 3-5 letters.
timeout                : 300 # LLM responce wait timeout.
max_fragment_chars     : 100000 # Required to avoid Telegram limit 50Mb per file. Depends on TTS service and sound quality.
max_shorten_iterations : 3 # Maximal number of repeats for text shortening.

hosts.video = [ # List of video service host name masks.
	"*.youtube.com"
]
```

## License

Source code is primarily distributed under the terms of the MIT license. See LICENSE for details.
