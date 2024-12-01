package com.example.api_backend_atelier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Номер телефона обязателен.")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Номер телефона должен быть в формате от 10 до 15 цифр, может начинаться с '+'.")
    private String number;

    @NotBlank(message = "Пароль обязателен.")
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов.")
    private String password;
}
