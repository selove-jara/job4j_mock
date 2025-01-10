package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.service.ProfileTGService;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClint;

import java.util.Calendar;

@AllArgsConstructor
@Slf4j
public class UnbindAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String SUCCESS_OBJECT = "success";
    private final TgConfig tgConfig = new TgConfig("tg/", 7);
    private final ProfileTGService profileTGService;
    private final TgAuthCallWebClint authCallWebClint;
    private static final String URL_AUTH_UNBIND = "/unbind";

    @Override
    public BotApiMethod<Message> handle(Message message) {
        var chatId = message.getChatId().toString();
        var text = "Введите email и пароль для отвязки телеграма от сервису(ivanov@mail.com password)";
        return new SendMessage(chatId, text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        var chatId = message.getChatId().toString();
        var emailAndPass = message.getText().split(" ", 2);
        var pass = emailAndPass[1];
        var email = emailAndPass[0];
        var text = "";
        var sl = System.lineSeparator();

        if (!tgConfig.isEmail(email)) {
            text = "Email: " + email + " не корректный." + sl
                    + "Попробуйте снова." + sl
                    + "/unbind";
            return new SendMessage(chatId, text);
        }

        var tgProfile = profileTGService.findByChatId(chatId);
        if (tgProfile.isEmpty()) {
            text = "Аккаунт Telegram не привязан к вашему профилю." + sl
                    + "Попробуйте снова." + sl
                    + "/unbind";
            return new SendMessage(chatId, text);
        }

        var person = new PersonDTO(null, email, pass, true, null,
                Calendar.getInstance(), chatId);

        Object result;
        try {
            result = authCallWebClint.doPost(URL_AUTH_UNBIND, person).block();
        } catch (Exception e) {
            log.error("WebClient error during unbind: {}", e.getMessage());
            text = "Сервис временно недоступен. Попробуйте позже." + sl
                    + "/start";
            return new SendMessage(chatId, text);
        }

        var mapObject = tgConfig.getObjectToMap(result);
        log.info("Unbind response: {}", mapObject);

        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка при отвязке: " + mapObject.get(ERROR_OBJECT);
            return new SendMessage(chatId, text);
        }

        if (mapObject.containsKey(SUCCESS_OBJECT)) {
            text = mapObject.get(SUCCESS_OBJECT);
            return new SendMessage(chatId, text);
        }

        text = "Неизвестная ошибка. Попробуйте позже.";
        return new SendMessage(chatId, text);

    }
}
