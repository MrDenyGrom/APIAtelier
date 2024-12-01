package com.example.api_backend_atelier.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public enum Gender {
    MALE("Мужской", "Male"),
    FEMALE("Женский", "Female"),
    OTHER("Другой/Универсальный", "Other/Universal");

    private static final Logger log = LoggerFactory.getLogger(Gender.class);

    private final String russianName;
    private final String englishName;

    Gender(String russianName, String englishName) {
        this.russianName = russianName;
        this.englishName = englishName;
    }

    public int toInt() {
        return switch (this) {
            case MALE -> 1;
            case FEMALE -> 2;
            case OTHER -> 0;
        };
    }

    public String getLocalizedName(String language) {
        String result = switch (language.toLowerCase()) {
            case "ru" -> russianName;
            default -> englishName;
        };
        log.debug("Локализованное имя для {}: {}", language, result);
        return result;

    }

    public static Gender findByName(String name) {
        return Arrays.stream(Gender.values())
                .filter(gender -> gender.russianName.equalsIgnoreCase(name) || gender.englishName.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Гендер с именем " + name + " не найден"));
    }

    public boolean matchesName(String name) {
        return this.russianName.equalsIgnoreCase(name) || this.englishName.equalsIgnoreCase(name);
    }

    public boolean isFormalApplicable() {
        return this != OTHER;
    }

    public String getFormalAddress(String culture) {
        return switch (culture.toLowerCase()) {
            case "ru" -> switch (this) {
                case MALE -> "Господин";
                case FEMALE -> "Госпожа";
                default -> "Господин/Госпожа";
            };
            case "en" -> switch (this) {
                case MALE -> "Mr.";
                case FEMALE -> "Ms.";
                default -> "Mx.";
            };
            default -> "Уважаемый(ая)";
        };
    }

    public static Gender safeFind(String name) {
        try {
            return findByName(name);
        } catch (IllegalArgumentException e) {
            log.error("Ошибка поиска гендера: {}", name, e);
            return OTHER;
        }
    }

}