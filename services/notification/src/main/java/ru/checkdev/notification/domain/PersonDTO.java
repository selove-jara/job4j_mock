package ru.checkdev.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.util.Calendar;
import java.util.List;

/**
 * DTO модель класса Person сервиса Auth.
 *
 * @author parsentev
 * @since 25.09.2016
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonDTO {
    private String username;
    private String email;
    private String password;
    private boolean privacy;
    private List<RoleDTO> roles;
    private Calendar created;
    private String chatId;

    public PersonDTO(String username, String email, String chatId) {
        this.username = username;
        this.email = email;
        this.chatId = chatId;
    }
}
