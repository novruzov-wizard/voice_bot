package com.voice.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class VoiceBotApplication {

	public static void main(String[] args) throws TelegramApiException {
		SpringApplication.run(VoiceBotApplication.class, args);
		VoiceBot bot = new VoiceBot(); // sənin bot class

		// Telegram API
		TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

		// Webhook-u sil
		try {
			bot.execute(new DeleteWebhook());
			System.out.println("Webhook removed successfully!");
		} catch (TelegramApiException e) {
			System.out.println("Failed to remove webhook:");
			e.printStackTrace();
		}

		// Botu register et
		botsApi.registerBot(bot);
	}

}
