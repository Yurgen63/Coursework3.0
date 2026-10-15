package pro.sky.telegrambot.listener;

public final class BotMessages {

    private BotMessages() {
    }

    public static final String WELCOME =
            "👋 Привет! Я бот-напоминалка.\n\n" +
                    "Что я умею:\n" +
                    "📝 Ставить напоминания\n" +
                    "🌤 Показывать погоду\n\n" +
                    "Выберите команду в меню или отправьте задачу в формате:\n" +
                    "ДД.ММ.ГГГГ ЧЧ:ММ Текст задачи\n\n" +
                    "Пример:\n" +
                    "01.01.2026 20:00 Сделать домашнюю работу\n\n" +
                    "Подробнее — /help";
    public static final String HELP =
            "📖 Справка по командам\n\n" +
                    "📝 Напоминание:\n" +
                    "ДД.ММ.ГГГГ ЧЧ:ММ Текст задачи\n" +
                    "Пример: 01.01.2026 20:00 Позвонить маме\n\n" +
                    "⏰ Напоминание придёт в указанное время в этот же чат." +
                    "🌤 Погода:\n" +
                    "/weather <город>\n" +
                    "Пример: /weather Москва\n\n";

    public static final String TASK_SAVED =
            "✅ Задача сохранена! Напомню в указанное время.";

    public static final String TASK_INVALID =
            "❌ Не удалось распознать задачу.\n\n" +
                    "Формат: ДД.ММ.ГГГГ ЧЧ:ММ Текст\n" +
                    "Пример: 01.01.2026 20:00 Позвонить маме\n\n" +
                    "Подробнее — /help";

    public static final String WEATHER_USAGE =
            "ℹ️ Укажите город после команды.\n" +
                    "Пример: /weather Москва";

    public static final String WEATHER_ERROR_PREFIX =
            "⚠️ Не удалось получить погоду для города: ";
}