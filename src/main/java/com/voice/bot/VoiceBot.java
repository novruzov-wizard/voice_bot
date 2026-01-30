package com.voice.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

@Component
public class VoiceBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        // Telegram botunun username-i, məsələn: "MyVoiceBot"
        return "voice_t_txt_bot";
    }

    @Override
    public String getBotToken() {
        // Telegram BotFather-dan aldığın token
        return "8089135590:AAG-OTH0Dj8_qJxES2CQCnMqfsvgrUA782Q";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasVoice()) {
            handleVoice(update);
        }
    }

    private void handleVoice(Update update) {
        try {
            String chatId = update.getMessage().getChatId().toString();
            String fileId = update.getMessage().getVoice().getFileId();

            GetFile getFile = new GetFile(fileId);
            File file = execute(getFile);

            String url = "https://api.telegram.org/file/bot"
                    + getBotToken() + "/" + file.getFilePath();

            Path audioPath = Paths.get("audio.ogg");
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, audioPath, StandardCopyOption.REPLACE_EXISTING);
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "python3",
                    "/transcribe.py",
                    audioPath.toString()
            );

            Process p = pb.start();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String text = reader.lines().collect(Collectors.joining());

            SendMessage msg = new SendMessage(chatId, "📝 " + text);
            execute(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}