package com.example.api_backend_atelier.dto;

import com.example.api_backend_atelier.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateDto {
    private Double price;
    private String url;
    private Gender gender;
    private String description;
    private String category;
}