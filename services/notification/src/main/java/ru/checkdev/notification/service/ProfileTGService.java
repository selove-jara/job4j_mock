package ru.checkdev.notification.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.domain.ProfileTG;
import ru.checkdev.notification.repository.ProfileTGRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ProfileTGService {
    private final ProfileTGRepository profileTGRepository;

    public Optional<ProfileTG> findByChatId(String chatId) {
        return profileTGRepository.findByChatId(chatId);
    }

    public ProfileTG save(ProfileTG profileTG) {
        return profileTGRepository.save(profileTG);
    }
}
