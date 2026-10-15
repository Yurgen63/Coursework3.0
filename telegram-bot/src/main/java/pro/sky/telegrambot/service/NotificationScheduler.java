package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationTaskRepository repository;
    private final TelegramBot telegramBot;

    public NotificationScheduler(NotificationTaskRepository repository, TelegramBot telegramBot) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    //Раз в минуту (в 0 секунд каждой минуты) выбирает задачи,
    //время отправки которых совпадает с текущей минутой, и отправляет их.
    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotifications() {
        // Обрезаем текущее время до минут (00 секунд),
        // т.к. шедулер может запуститься с лагом в несколько миллисекунд
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = repository.findAllBySendAt(now);

        if (tasks.isEmpty()) {
            logger.debug("No notifications to send at {}", now);
            return;
        }

        logger.info("Found {} notification(s) to send at {}", tasks.size(), now);

        List<NotificationTask> successfullySent = new ArrayList<>();

        for (NotificationTask task : tasks) {
            try {
                SendResponse response = telegramBot.execute(
                        new SendMessage(task.getChatId(), task.getMessage()));

                if (response.isOk()) {
                    logger.info("Notification sent: chatId={}, message={}, sendAt={}",
                            task.getChatId(), task.getMessage(), task.getSendAt());
                    successfullySent.add(task);
                } else {
                    logger.warn("Failed to send: chatId={}, errorCode={}, description={}",
                            task.getChatId(), response.errorCode(), response.description());
                }
            } catch (Exception e) {
                logger.error("Unexpected error while sending task id={}", task.getId(), e);
            }
        }

        if (!successfullySent.isEmpty()) {
            repository.deleteAll(successfullySent);
            logger.info("Deleted {} sent task(s)", successfullySent.size());
        }
    }
}