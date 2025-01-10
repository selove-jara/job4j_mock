package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.ProfileTG;
import ru.checkdev.notification.service.ProfileTGService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckActionTest {

    @Mock
    private ProfileTGService profileTGService;

    @Mock
    private Message message;

    private CheckAction checkAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        checkAction = new CheckAction(profileTGService);
    }

    @Test
    void whenHandleThenReturnRegistrationMessageWhenProfileIsNotFound() {
        when(message.getChatId()).thenReturn(123456789L);
        when(profileTGService.findByChatId("123456789")).thenReturn(Optional.empty());

        SendMessage result = (SendMessage) checkAction.handle(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Аккаунт Telegram не зарегистрирован."));
        assertTrue(result.getText().contains("/new"));
    }

    @Test
    void whenHandleThenReturnProfileDetailsWhenProfileIsFound() {
        ProfileTG profile = new ProfileTG("ivanov@mail.com", "123456789", "Иванов Иван");
        when(message.getChatId()).thenReturn(123456789L);
        when(profileTGService.findByChatId("123456789")).thenReturn(Optional.of(profile));

        SendMessage result = (SendMessage) checkAction.handle(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("ФИО: Иванов Иван"));
        assertTrue(result.getText().contains("Email: ivanov@mail.com"));
    }

    @Test
    void whenCallbackThenReturnRegistrationMessageWhenProfileIsNotFound() {
        when(message.getChatId()).thenReturn(123456789L);
        when(profileTGService.findByChatId("123456789")).thenReturn(Optional.empty());

        SendMessage result = (SendMessage) checkAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Аккаунт Telegram не зарегистрирован."));
        assertTrue(result.getText().contains("/new"));
    }

    @Test
    void whenCallbackThenReturnProfileDetailsWhenProfileIsFound() {
        ProfileTG profile = new ProfileTG("ivanov@mail.com", "123456789", "Иванов Иван");
        when(message.getChatId()).thenReturn(123456789L);
        when(profileTGService.findByChatId("123456789")).thenReturn(Optional.of(profile));

        SendMessage result = (SendMessage) checkAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("ФИО: Иванов Иван"));
        assertTrue(result.getText().contains("Email: ivanov@mail.com"));
    }
}