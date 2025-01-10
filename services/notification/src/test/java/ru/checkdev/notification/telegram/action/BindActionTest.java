package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.ProfileTG;
import ru.checkdev.notification.service.ProfileTGService;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BindActionTest {

    @Mock
    private ProfileTGService profileTGService;

    @Mock
    private TgAuthCallWebClint authCallWebClint;

    @Mock
    private TgConfig tgConfig;

    private BindAction bindAction;

    @Mock
    private Message message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bindAction = new BindAction(profileTGService, authCallWebClint);
    }

    @Test
    void whenHandleThenReturnCorrectMessage() {
        when(message.getChatId()).thenReturn(123456789L);

        SendMessage result = (SendMessage) bindAction.handle(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertEquals("Введите email и пароль для привязки телеграма к сервису(ivanov@mail.com password)", result.getText());
    }

    @Test
    void whenCallbackThenReturnErrorMessageWhenAccountExists() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("ivanov@mail.com password");
        when(profileTGService.findByChatId("123456789")).thenReturn(Optional.of(new ProfileTG()));

        SendMessage result = (SendMessage) bindAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertEquals("Такой аккаунт уже существует.", result.getText());
    }

    @Test
    void whenCallbackThenReturnErrorMessageWhenEmailIsInvalid() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("invalid_email password");
        when(profileTGService.findByChatId("123456789")).thenReturn(Optional.empty());
        when(tgConfig.isEmail("invalid_email")).thenReturn(false);

        SendMessage result = (SendMessage) bindAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Email: invalid_email не корректный."));
    }

    @Test
    void whenCallbackThenReturnServiceUnavailableMessageWhenWebClientFails() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("ivanov@mail.com password");
        when(profileTGService.findByChatId("123456789")).thenReturn(Optional.empty());
        when(tgConfig.isEmail("ivanov@mail.com")).thenReturn(true);
        when(authCallWebClint.doPost(anyString(), any())).thenThrow(new RuntimeException("Service error"));

        SendMessage result = (SendMessage) bindAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Сервис не доступен попробуйте позже"));
    }

    @Test
    void callback_ShouldReturnSuccessMessage_WhenSuccessObjectExists() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("ivanov@mail.com password");
        when(profileTGService.findByChatId("123456789")).thenReturn(Optional.empty());
        when(tgConfig.isEmail("ivanov@mail.com")).thenReturn(true);

        var mockResponse = Map.of("success", "Account linked successfully");
        when(authCallWebClint.doPost(anyString(), any())).thenReturn(Mono.just(mockResponse));
        when(tgConfig.getObjectToMap(mockResponse)).thenReturn(mockResponse);

        SendMessage result = (SendMessage) bindAction.callback(message);
        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertEquals("Account linked successfully", result.getText());
    }
}