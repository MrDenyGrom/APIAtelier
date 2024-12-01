package com.example.api_backend_atelier.model;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Map;


@Getter
public enum Role {
    USER("Пользователь", 1, "Стандартный пользователь с ограниченными правами"),
    ADMIN("Администратор", 3, "Полный доступ ко всем функциям системы"),
    MODERATOR("Модератор", 2, "Может управлять пользователями и модерировать контент"),
    GUEST("Гость", 0, "Ограниченный доступ только для просмотра");


    private static final Logger log = LoggerFactory.getLogger(Role.class);

    private final String displayName;
    private final int accessLevel;
    private final String description;

    Role(String displayName, int accessLevel, String description) {
        this.displayName = displayName;
        this.accessLevel = accessLevel;
        this.description = description;
    }


    public boolean hasHigherAccessThan(Role other) {
        boolean hasHigherAccess = this.accessLevel > other.accessLevel;
        log.debug("Сравнение прав доступа: {} > {} = {}", this, other, hasHigherAccess);
        return hasHigherAccess;

    }

    public String getDetails() {
        return String.format("%s: %s", displayName, description);
    }

    public static Role findByName(String name) {
        return Arrays.stream(Role.values())
                .filter(role -> role.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Роль " + name + " не найдена"));
    }

    public static Role findByAccessLevel(int accessLevel) {
        return Arrays.stream(Role.values())
                .filter(role -> role.accessLevel == accessLevel)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Роль с уровнем доступа " + accessLevel + " не найдена"));
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "name", name(),
                "displayName", displayName,
                "accessLevel", accessLevel,
                "description", description
        );
    }

    public ResponseEntity<Role> toApiResponse() {
        return ResponseEntity.ok(this);
    }
}