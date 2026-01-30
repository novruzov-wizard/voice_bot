package com.voice.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class VoiceBotApplication {

	public static void main(String[] args) throws TelegramApiException {
		SpringApplication.run(VoiceBotApplication.class, args);
		VoiceBot bot = new VoiceBot(); // sənin bot class

		// Telegram API
		TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

		// Botu register et
		botsApi.registerBot(bot);
	}

}
