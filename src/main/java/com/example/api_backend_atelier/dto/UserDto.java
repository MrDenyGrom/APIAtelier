package com.example.api_backend_atelier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String name;
    private String lastName;
    private String number;
    private String email;
    private String status;
    private String role;
}
