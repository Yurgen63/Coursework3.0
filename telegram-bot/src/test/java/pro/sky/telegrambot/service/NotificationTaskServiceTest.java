package pro.sky.telegrambot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTaskServiceTest {

    @Mock
    private NotificationTaskRepository repository;

    @InjectMocks
    private NotificationTaskService service;

    @Test
    void shouldParseMessageAndSaveTask() {
        // given
        Long chatId = 123456789L;
        String message = "01.01.2026 20:00 Сделать домашнюю работу";

        // when
        boolean result = service.createTaskFromMessage(chatId, message);

        // then
        assertTrue(result, "Метод должен вернуть true для корректного сообщения");

        ArgumentCaptor<NotificationTask> captor = ArgumentCaptor.forClass(NotificationTask.class);
        verify(repository, times(1)).save(captor.capture());

        NotificationTask saved = captor.getValue();
        assertEquals(chatId, saved.getChatId());
        assertEquals("Сделать домашнюю работу", saved.getMessage());
        assertEquals(LocalDateTime.of(2026, 1, 1, 20, 0), saved.getSendAt());
    }

    @Test
    void shouldReturnFalseForMessageWithoutDate() {
        // given
        Long chatId = 123L;
        String message = "Просто текст без даты";

        // when
        boolean result = service.createTaskFromMessage(chatId, message);

        // then
        assertFalse(result);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnFalseForNullMessage() {
        boolean result = service.createTaskFromMessage(123L, null);

        assertFalse(result);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnFalseForBlankMessage() {
        boolean result = service.createTaskFromMessage(123L, "   ");

        assertFalse(result);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnFalseForInvalidDate() {
        // 32 января не существует
        boolean result = service.createTaskFromMessage(123L, "32.01.2026 20:00 Задача");

        assertFalse(result);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldTrimTextFromExtraSpaces() {
        // between date and text multiple spaces
        boolean result = service.createTaskFromMessage(123L, "01.01.2026 20:00    Много пробелов");

        assertTrue(result);
        ArgumentCaptor<NotificationTask> captor = ArgumentCaptor.forClass(NotificationTask.class);
        verify(repository).save(captor.capture());

        assertEquals("Много пробелов", captor.getValue().getMessage());
    }
}