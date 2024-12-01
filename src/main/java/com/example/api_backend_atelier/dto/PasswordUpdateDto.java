package com.example.api_backend_atelier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordUpdateDto {
    private String oldPassword;
    private String newPassword;
}