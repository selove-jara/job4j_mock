package ru.checkdev.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.checkdev.notification.domain.ProfileTG;

import java.util.Optional;

public interface ProfileTGRepository extends JpaRepository<ProfileTG, Long> {
    Optional<ProfileTG> findByChatId(String id);
}
