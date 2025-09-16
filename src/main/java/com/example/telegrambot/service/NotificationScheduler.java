package com.example.telegrambot.service;

import com.example.telegrambot.model.NotificationTask;
import com.example.telegrambot.repository.NotificationTaskRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationScheduler {

    private final NotificationTaskRepository repository;
    private final TelegramBot telegramBot;

    public NotificationScheduler(NotificationTaskRepository repository, TelegramBot telegramBot) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void checkAndSend() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = repository.findByScheduledAt(now);
        for (NotificationTask task : tasks) {
            SendMessage msg = new SendMessage(task.getChatId(), task.getMessageText());
            SendResponse resp = telegramBot.execute(msg);
            if (resp.isOk()) {
                repository.delete(task);
            } else {
                System.err.println("Failed to send to chat " + task.getChatId() + " error: " + resp.errorCode());
            }
        }
    }
}