package ru.checkdev.notification.domain;

import lombok.*;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "profile_tg")
public class ProfileTG {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;

    @Column(name = "chat_id")
    private String chatId;

    private String fullname;

    @Column(name = "registration")
    private boolean registration;

    public ProfileTG(String email, String chatId, String fullname) {
        this.email = email;
        this.chatId = chatId;
        this.fullname = fullname;
    }
}
