package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.weather.Weather;
import pro.sky.telegrambot.model.weather.WeatherMain;
import pro.sky.telegrambot.model.weather.WeatherWind;
import pro.sky.telegrambot.service.NotificationTaskService;
import pro.sky.telegrambot.service.WeatherService;

import javax.annotation.PostConstruct;
import java.util.List;

import static pro.sky.telegrambot.listener.BotMessages.*;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;
    private final WeatherService weatherService;


    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      NotificationTaskService notificationTaskService, WeatherService weatherService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
        this.weatherService = weatherService;
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

            if (update.message() == null) continue;
            String rawText = update.message().text();
            Long chatId = update.message().chat().id();
            if (rawText == null) continue;

            // Нормализуем: убираем лишние пробелы, но сохраняем регистр для текста задачи
            String text = rawText.trim();

            // Команды без аргументов
            String command = text.toLowerCase();
            if (command.equals("/start")) {
                reply(chatId, WELCOME);
                continue;
            }
            if (command.equals("/help")) {
                reply(chatId, HELP);
                continue;
            }

            // /weather <город>  (город может содержать пробелы)
            if (command.startsWith("/weather")) {
                String city = text.substring("/weather".length()).trim();
                handleWeather(chatId, city);
                continue;
            }

            // Любое другое сообщение — попытка создать задачу
            boolean created = notificationTaskService.createTaskFromMessage(chatId, text);
            reply(chatId, created ? TASK_SAVED : TASK_INVALID);
        }

        return lastUpdateId;
    }

    private void handleWeather(Long chatId, String city) {
        if (city.isEmpty()) {
            reply(chatId, WEATHER_USAGE);
            return;
        }
        try {
            Weather weather = weatherService.getWeather(city);
            reply(chatId, formatWeather(city, weather));
        } catch (Exception e) {
            logger.error("Failed to get weather for {}", city, e);
            reply(chatId, WEATHER_ERROR_PREFIX + city);
        }
    }

    private String formatWeather(String city, Weather weather) {
        if (weather == null || weather.getMain() == null) {
            return "Нет данных о погоде для " + city;
        }
        WeatherMain main = weather.getMain();
        WeatherWind wind = weather.getWind();

        StringBuilder sb = new StringBuilder();
        sb.append("🌤 Погода в ").append(city).append(":\n");
        sb.append("🌡 Температура: ").append(main.getTemp()).append(" °C\n");
        if (main.getHumidity() != null) {
            sb.append("💧 Влажность: ").append(main.getHumidity()).append(" %\n");
        }
        if (wind != null && wind.getSpeed() != null) {
            sb.append("💨 Ветер: ").append(wind.getSpeed()).append(" м/с");
            if (wind.getDeg() != null) {
                sb.append(", ").append(wind.getDeg()).append("°");
            }
        }
        return sb.toString();
    }

    private void reply(Long chatId, String text) {
        telegramBot.execute(new SendMessage(chatId, text));
    }
}