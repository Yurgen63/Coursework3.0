package pro.sky.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationTaskService.class);

    // Паттерн для распознавания даты+времени и текста в сообщении.
    private static final Pattern MESSAGE_PATTERN =
            Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");

    // Формат даты+времени во входящем сообщении.
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final NotificationTaskRepository repository;

    public NotificationTaskService(NotificationTaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Обрабатывает сообщение пользователя.
     * Если сообщение соответствует формату "дата время текст" — сохраняет задачу.
     *
     * @param chatId ID чата, куда отправлять напоминание
     * @param text   текст сообщения от пользователя
     * @return true, если задача успешно создана; false — если сообщение не распознано
     */
    public boolean createTaskFromMessage(Long chatId, String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        Matcher matcher = MESSAGE_PATTERN.matcher(text);
        if (!matcher.matches()) {
            logger.warn("Message doesn't match the pattern: {}", text);
            return false;
        }

        String dateTimeString = matcher.group(1);
        String messageText = matcher.group(3);

        LocalDateTime sendAt;
        try {
            sendAt = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.warn("Invalid date format in message: {}", dateTimeString);
            return false;
        }

        NotificationTask task = new NotificationTask(chatId, messageText, sendAt);
        repository.save(task);

        logger.info("Saved notification task: chatId={}, message={}, sendAt={}",
                chatId, messageText, sendAt);
        return true;
    }
}