package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.domain.ProfileTG;
import ru.checkdev.notification.service.ProfileTGService;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegActionTest {

    @Mock
    private ProfileTGService profileTGService;

    @Mock
    private TgAuthCallWebClint authCallWebClint;

    @Mock
    private TgConfig tgConfig;

    private RegAction regAction;

    @Mock
    private Message message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        regAction = new RegAction(authCallWebClint, "http://example.com", profileTGService);
    }

    @Test
    void whenHandleThenReturnCorrectMessage() {
        when(message.getChatId()).thenReturn(123456789L);

        SendMessage result = (SendMessage) regAction.handle(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertEquals("Введите email и полное имя для регистрации:(ivanov@mail.com Иванов Иван)", result.getText());
    }

    @Test
    void whenCallbackThenReturnErrorMessageWhenEmailIsInvalid() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("invalid_email Иванов Иван");
        when(tgConfig.isEmail("invalid_email")).thenReturn(false);

        SendMessage result = (SendMessage) regAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Email: invalid_email не корректный."));
    }

    @Test
    void whenCallbackThenReturnServiceUnavailableMessageWhenWebClientFails() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("ivanov@mail.com Иванов Иван");
        when(tgConfig.isEmail("ivanov@mail.com")).thenReturn(true);
        when(profileTGService.save(any(ProfileTG.class))).thenReturn(null);
        when(authCallWebClint.doPost(anyString(), any())).thenThrow(new RuntimeException("Service error"));

        SendMessage result = (SendMessage) regAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Сервис не доступен попробуйте позже"));
    }

    @Test
    void whenCallbackThenReturnSuccessMessageWhenRegistrationIsSuccessful() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("ivanov@mail.com Иванов Иван");
        when(tgConfig.isEmail("ivanov@mail.com")).thenReturn(true);
        when(profileTGService.save(any(ProfileTG.class))).thenReturn(null);

        var mockResponse = Map.of("success", "Account registered successfully");
        when(authCallWebClint.doPost(anyString(), any())).thenReturn(Mono.just(mockResponse));
        when(tgConfig.getObjectToMap(mockResponse)).thenReturn(mockResponse);

        SendMessage result = (SendMessage) regAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Вы зарегистрированы"));
        assertTrue(result.getText().contains("Логин: ivanov@mail.com"));
    }
}
