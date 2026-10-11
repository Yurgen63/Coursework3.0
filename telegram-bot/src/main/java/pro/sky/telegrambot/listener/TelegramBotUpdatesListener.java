package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        int lastUpdateId = 0;

        for (Update update : updates) {
            lastUpdateId = update.updateId();

            if (update.message() == null) {
                continue;
            }

            String text = update.message().text();
            Long chatId = update.message().chat().id();

            if (text == null) {
                continue;
            }

            // Команда /start — приветствие
            if (text.equals("/start")) {
                String welcomeMessage = "Привет! \n\n" +
                        "Я бот-напоминалка. Отправь мне задачу в формате:\n" +
                        "ДД.ММ.ГГГГ ЧЧ:ММ Текст задачи\n\n" +
                        "Пример:\n" +
                        "01.01.2026 20:00 Сделать домашнюю работу";
                telegramBot.execute(new SendMessage(chatId, welcomeMessage));
                continue;
            }

            // Попытка создать задачу из сообщения
            boolean created = notificationTaskService.createTaskFromMessage(chatId, text);

            if (created) {
                telegramBot.execute(new SendMessage(chatId, "Задача сохранена!"));
            } else {
                telegramBot.execute(new SendMessage(chatId,
                        "Не удалось распознать задачу. Формат: ДД.ММ.ГГГГ ЧЧ:ММ Текст"));
            }
        }

        return lastUpdateId;
    }
}