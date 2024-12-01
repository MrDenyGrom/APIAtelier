package com.example.api_backend_atelier.repository;

import com.example.api_backend_atelier.model.Gender;
import com.example.api_backend_atelier.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    List<Product> findByGender(Gender gender);

    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    List<Product> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
