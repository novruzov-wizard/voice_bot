package com.voice.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.File;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;

@Component
public class VoiceBot extends TelegramLongPollingBot {

    private static final String PYTHON_CMD = System.getenv().getOrDefault("PYTHON_CMD", "python3");
    private static final String TRANSCRIBE_SCRIPT =
            System.getenv().getOrDefault("TRANSCRIBE_SCRIPT", "/app/transcribe.py"); // keep beside your jar or give full path

    @Override
    public String getBotUsername() {
        return "voice_t_txt_bot";
    }

    @Override
    public String getBotToken() {
        String token = "8089135590:AAG-OTH0Dj8_qJxES2CQCnMqfsvgrUA782Q";
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("TELEGRAM_BOT_TOKEN env var is missing");
        }
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;

        // Voice note (usually OGG/OPUS)
        if (update.getMessage().hasVoice()) {
            handleMedia(update, update.getMessage().getVoice().getFileId());
            return;
        }

        // Audio file (often M4A/MP3/etc)
        if (update.getMessage().hasAudio()) {
            handleMedia(update, update.getMessage().getAudio().getFileId());
        }
    }

    private void handleMedia(Update update, String fileId) {
        String chatId = update.getMessage().getChatId().toString();

        try {
            // 1) Get Telegram file path
            File tgFile = execute(new GetFile(fileId));

            // 2) Download
            String fileUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + tgFile.getFilePath();

            String ext = getExtensionFromTelegramPath(tgFile.getFilePath()); // ogg, m4a, mp3...
            Path workDir = Files.createDirectories(Paths.get("work"));
            String base = UUID.randomUUID().toString();

            Path inputPath = workDir.resolve(base + "." + ext);
            try (InputStream in = new URL(fileUrl).openStream()) {
                Files.copy(in, inputPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 3) Convert to wav (recommended)
            Path wavPath = workDir.resolve(base + ".wav");
            convertToWav(inputPath, wavPath);

            // 4) Run python transcription
            String text = runPythonTranscribe(wavPath);

            if (text.isBlank()) text = "(no text recognized)";

            execute(new SendMessage(chatId, "📝 " + text));

            // 5) cleanup
            safeDelete(inputPath);
            safeDelete(wavPath);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                execute(new SendMessage(chatId, "❌ Error: " + e.getMessage()));
            } catch (Exception ignored) {
            }
        }
    }

    private static String getExtensionFromTelegramPath(String telegramPath) {
        int dot = telegramPath.lastIndexOf('.');
        if (dot == -1) return "dat";
        return telegramPath.substring(dot + 1).toLowerCase();
    }

    private static void convertToWav(Path input, Path wavOut) throws IOException, InterruptedException {
        // Requires ffmpeg installed
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", input.toAbsolutePath().toString(),
                "-ac", "1",
                "-ar", "16000",
                wavOut.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        String out = readAll(p.getInputStream());
        int code = p.waitFor();
        if (code != 0) {
            throw new IOException("ffmpeg failed (" + code + "): " + out);
        }
    }

    private static String runPythonTranscribe(Path wavPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                PYTHON_CMD,
                Paths.get(TRANSCRIBE_SCRIPT).toAbsolutePath().toString(),
                wavPath.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        String out = readAll(p.getInputStream());
        int code = p.waitFor();
        if (code != 0) {
            throw new IOException("transcribe.py failed (" + code + "): " + out);
        }
        return out.trim();
    }

    private static String readAll(InputStream in) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }

    private static void safeDelete(Path p) {
        try {
            Files.deleteIfExists(p);
        } catch (Exception ignored) {
        }
    }
}
