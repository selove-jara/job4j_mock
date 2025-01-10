package ru.checkdev.auth.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.checkdev.auth.domain.Profile;
import ru.checkdev.auth.service.PersonService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

/**
 * @author parsentev
 * @since 26.09.2016
 */
@RestController
public class AuthController {
    private final PersonService persons;
    private final String ping = "{}";

    @Autowired
    public AuthController(final PersonService persons) {
        this.persons = persons;
    }

    @RequestMapping("/user")
    public Principal user(Principal user) {
        return user;
    }

    @GetMapping("/ping")
    public String ping() {
        return this.ping;
    }

    @GetMapping("/auth/activated/{key}")
    public Object activated(@PathVariable String key) {
        if (this.persons.activated(key)) {
            return new Object() {
                public boolean getSuccess() {
                    return true;
                }
            };
        } else {
            return new Object() {
                public String getError() {
                    return "Notify has already activated";
                }
            };
        }
    }

    @PostMapping("/registration")
    public Object registration(@RequestBody Profile profile) {
        Optional<Profile> result = this.persons.reg(profile);
        return result.<Object>map(prs -> new Object() {
            public Profile getPerson() {
                return prs;
            }
        }).orElseGet(() -> new Object() {
            public String getError() {
                return String.format("Пользователь с почтой %s уже существует.", profile.getEmail());
            }
        });
    }

    @PostMapping("/forgot")
    public Object forgot(@RequestBody Profile profile) {
        Optional<Profile> result = this.persons.forgot(profile);
        if (result.isPresent()) {
            return new Object() {
                public String getOk() {
                    return "ok";
                }
            };
        } else {
            return new Object() {
                public String getError() {
                    return "E-mail не найден.";
                }
            };
        }
    }

    @GetMapping("/revoke")
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request) {

    }

    @PostMapping("/bind")
    public ResponseEntity<Map<String, String>> bindAccount(@RequestBody Profile profile) {
        Optional<Profile> existingProfile = persons.findByEmail(profile.getEmail());

        if (existingProfile.isPresent() && (existingProfile.get().getChatId() != null && !existingProfile.get().getChatId().isEmpty())) {
            return ResponseEntity.ok(Map.of("error", "Аккаунт уже привязан к другому Telegram."));
        }

        existingProfile.ifPresent(p -> {
            p.setChatId(profile.getChatId());
            persons.update(p.getEmail(), null, p);
        });

        return ResponseEntity.ok(Map.of("success", "Аккаунт успешно привязан."));
    }

    @PostMapping("/unbind")
    public ResponseEntity<Map<String, String>> unBindAccount(@RequestBody Profile profile) {
        Optional<Profile> existingProfile = persons.findByEmail(profile.getEmail());

        if (existingProfile.isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "Пользователь с указанным email не найден."));
        }

        Profile person = existingProfile.get();
        if (person.getChatId() == null || person.getChatId().isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "Аккаунт не привязан к Telegram."));
        }

        person.setChatId(null);
        persons.update(person.getEmail(), null, person);

        return ResponseEntity.ok(Map.of("success", "Аккаунт успешно отвязан от Telegram."));
    }
}
