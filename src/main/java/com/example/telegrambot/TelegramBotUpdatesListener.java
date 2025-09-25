package com.example.telegrambot;

import com.example.telegrambot.model.NotificationTask;
import com.example.telegrambot.repository.NotificationTaskRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramBotUpdatesListener {

    private final TelegramBot bot;
    private final NotificationTaskRepository repository;

    private static final Pattern REMINDER_PATTERN =
            Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})\\s+(.+)");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public TelegramBotUpdatesListener(TelegramBot bot, NotificationTaskRepository repository) {
        this.bot = bot;
        this.repository = repository;
    }

    @PostConstruct
    public void start() {
        bot.setUpdatesListener(this::process);
    }

    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() == null || update.message().text() == null) continue;
            String text = update.message().text().trim();
            Long chatId = update.message().chat().id();

            if ("/start".equalsIgnoreCase(text)) {
                bot.execute(new SendMessage(chatId,
                        "Привет! Отправь напоминание в формате: 01.01.2022 20:00 Текст напоминания"));
                continue;
            }

            Matcher matcher = REMINDER_PATTERN.matcher(text);
            if (matcher.matches()) {
                try {
                    String datePart = matcher.group(1);
                    String messagePart = matcher.group(2);

                    LocalDateTime scheduled = LocalDateTime.parse(datePart, DATE_TIME_FORMATTER)
                            .truncatedTo(ChronoUnit.MINUTES);

                    NotificationTask task = new NotificationTask(chatId, messagePart, scheduled);
                    repository.save(task);

                    bot.execute(new SendMessage(chatId,
                            "Напоминание сохранено на " + scheduled.format(DATE_TIME_FORMATTER)));
                } catch (Exception ex) {
                    bot.execute(new SendMessage(chatId,
                            "Не удалось распознать дату/время. Используйте формат dd.MM.yyyy HH:mm"));
                }
            } else {
                bot.execute(new SendMessage(chatId,
                        "Сообщение не соответствует ожидаемому формату. Пример: 01.01.2022 20:00 Сделать домашнюю работу"));
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
