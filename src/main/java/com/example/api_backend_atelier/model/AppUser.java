package com.example.api_backend_atelier.model;

import com.example.api_backend_atelier.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Entity
@Table(name = "app_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AppUser {

    private static final Logger log = LoggerFactory.getLogger(AppUser.class);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(min = 1, max = 50, message = "Имя должно содержать от 1 до 50 символов.")
    @Column(length = 50)
    private String name;

    @Size(min = 1, max = 50, message = "Фамилия должна содержать от 1 до 50 символов.")
    @Column(length = 50)
    private String lastName;

    @NotBlank(message = "Номер телефона обязателен.")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Номер телефона должен быть в формате от 10 до 15 цифр, может начинаться с '+'.")
    @Column(nullable = false, unique = true, length = 15)
    private String number;

    @NotBlank(message = "Пароль обязателен.")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов.")
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Pattern(regexp = "^https://vk\\.com/[a-zA-Z0-9_.]{3,}$", message = "VK ID должен быть в формате: https://vk.com/имя_пользователя.")
    @Column(unique = true, length = 100)
    private String vkId;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Email(message = "Email должен быть в корректном формате.")
    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean locked = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @JsonIgnore
    private String refreshToken;

    public void activate() {
        this.enabled = true;
        this.locked = false;
        log.info("Пользователь {} активирован.", this.number);
    }

    public void deactivate() {
        this.enabled = false;
        log.info("Пользователь {} деактивирован.", this.number);
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean isActive() {
        return enabled && !locked;
    }

    public String getWelcomeMessage() {
        if (name == null || name.isEmpty()) {
            return "Здравствуйте, уважаемый пользователь!";
        }
        return String.format("Здравствуйте, %s %s!", name, lastName == null ? "" : lastName);
    }

    public void updateFullName(String newName, String newLastName) {
        log.info("Обновление имени и фамилии для пользователя {}: {} -> {}, {} -> {}",
                this.number, this.name, newName, this.lastName, newLastName);

        this.name = newName;
        this.lastName = newLastName;
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(role);
    }

    public String getMaskedPhoneNumber() {
        if (number != null && number.length() >= 10) {
            return number.replaceAll("(\\+?\\d{1,3})\\d{6}(\\d{4})", "$1******$2");
        }
        return "Номер недоступен";
    }

    public boolean canReceiveNotifications() {
        return email != null || number != null;
    }

    public void logStatus() {
        String status = String.format("Пользователь %s: Активен - %s, Заблокирован - %s",
                this.number, this.enabled, this.locked);
        log.info(status);
    }

    public String getShortDescription() {
        return String.format("Пользователь [%s]: %s %s, Роль: %s, Статус: %s",
                number, name, lastName == null ? "" : lastName,
                role.name(), isActive() ? "Активен" : "Неактивен");
    }

    public String getFrontendStatus() {
        if (locked) {
            return "Заблокирован";
        } else if (enabled) {
            return "Активен";
        } else {
            return "Неактивен";
        }
    }

    public boolean canResetPassword() {
        return email != null && !email.isEmpty();
    }

    public String toJson() {
        return String.format(
                "{\"id\":\"%s\", \"name\":\"%s\", \"lastName\":\"%s\", \"number\":\"%s\", \"email\":\"%s\", \"status\":\"%s\", \"role\":\"%s\"}",
                id, name, lastName, number, email, getFrontendStatus(), role.name()
        );
    }

    public String getPersonalizedGreeting() {
        String greeting = switch (gender) {
            case MALE -> "Здравствуйте, господин " + name;
            case FEMALE -> "Здравствуйте, госпожа " + name;
            default -> "Здравствуйте, уважаемый пользователь " + name;
        };
        log.debug("Сформировано персонализированное приветствие: {}", greeting);
        return greeting;
    }

    public UserDto toDto() {
        return new UserDto(
                id,
                name,
                lastName,
                number,
                email,
                getFrontendStatus(),
                role.name()
        );
    }

    public String getAuthority() {
        return "ROLE_" + role.name();
    }
}
