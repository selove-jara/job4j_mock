package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.domain.ProfileTG;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.service.ProfileTGService;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;


import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnbindActionTest {

    @Mock
    private ProfileTGService profileTGService;

    @Mock
    private TgAuthCallWebClint authCallWebClint;

    @Mock
    private Message message;

    private UnbindAction unbindAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        unbindAction = new UnbindAction(profileTGService, authCallWebClint);
    }

    @Test
    void whenHandleThenReturnTextMessageWhenCalled() {
        when(message.getChatId()).thenReturn(123456789L);

        SendMessage result = (SendMessage) unbindAction.handle(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Введите email и пароль для отвязки"));
    }

    @Test
    void whenCallbackThenReturnErrorMessageWhenEmailIsInvalid() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("invalid-email password");

        SendMessage result = (SendMessage) unbindAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("не корректный"));
    }

    @Test
    void whenCallbackThenReturnErrorMessageWhenProfileNotFound() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("ivanov@mail.com password");
        when(profileTGService.findByChatId("123456789")).thenReturn(java.util.Optional.empty());

        SendMessage result = (SendMessage) unbindAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Аккаунт Telegram не привязан"));
    }

    @Test
    void whenCallbackThenReturnErrorMessageWhenWebClientFails() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("ivanov@mail.com password");
        when(profileTGService.findByChatId("123456789")).thenReturn(java.util.Optional.of(new ProfileTG()));
        when(authCallWebClint.doPost(anyString(), any(PersonDTO.class))).thenThrow(new RuntimeException("WebClient error"));

        SendMessage result = (SendMessage) unbindAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Сервис временно недоступен"));
    }

    @Test
    void whenCallbackThenReturnErrorMessageWhenErrorInResponse() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("ivanov@mail.com password");
        when(profileTGService.findByChatId("123456789")).thenReturn(java.util.Optional.of(new ProfileTG()));
        when(authCallWebClint.doPost(anyString(), any(PersonDTO.class)))
                .thenReturn(Mono.just(Map.of("error", "Some error")));

        SendMessage result = (SendMessage) unbindAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Ошибка при отвязке"));
    }

    @Test
    void whenCallbackThenReturnSuccessMessageWhenSuccessResponse() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("ivanov@mail.com password");
        when(profileTGService.findByChatId("123456789")).thenReturn(java.util.Optional.of(new ProfileTG()));
        when(authCallWebClint.doPost(anyString(), any(PersonDTO.class)))
                .thenReturn(Mono.just(Map.of("success", "Успешная отвязка")));

        SendMessage result = (SendMessage) unbindAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Успешная отвязка"));
    }

    @Test
    void whenCallbackThenReturnUnknownErrorWhenResponseIsInvalid() {
        when(message.getChatId()).thenReturn(123456789L);
        when(message.getText()).thenReturn("ivanov@mail.com password");
        when(profileTGService.findByChatId("123456789")).thenReturn(java.util.Optional.of(new ProfileTG()));
        when(authCallWebClint.doPost(anyString(), any(PersonDTO.class)))
                .thenReturn(Mono.just(Map.of()));

        SendMessage result = (SendMessage) unbindAction.callback(message);

        assertNotNull(result);
        assertEquals("123456789", result.getChatId());
        assertTrue(result.getText().contains("Неизвестная ошибка"));
    }
}
