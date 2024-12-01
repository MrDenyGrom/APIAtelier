package com.example.api_backend_atelier.service;

import com.example.api_backend_atelier.dto.ProductCreateDto;
import com.example.api_backend_atelier.exception.ProductNotFoundException;
import com.example.api_backend_atelier.model.Gender;
import com.example.api_backend_atelier.model.Product;
import com.example.api_backend_atelier.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(ProductCreateDto productDto) {
        Product product = new Product();
        product.setPrice(productDto.getPrice());
        product.setUrl(productDto.getUrl());
        product.setGender(productDto.getGender());
        product.setDescription(productDto.getDescription());
        product.setCategory(productDto.getCategory());
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Продукт с ID " + id + " не найден"));

        if (updatedProduct.getPrice() != null) {
            product.setPrice(updatedProduct.getPrice());
        }
        if (updatedProduct.getUrl() != null) {
            product.setUrl(updatedProduct.getUrl());
        }
        if (updatedProduct.getDescription() != null) {
            product.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getCategory() != null) {
            product.setCategory(updatedProduct.getCategory());
        }
        if (updatedProduct.getGender() != null) {
            product.setGender(updatedProduct.getGender());

        }

        return productRepository.save(product);
    }

    public List<Product> getProductsByGender(Gender gender) {
        return productRepository.findByGender(gender);
    }

    public List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Product> getProductsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return productRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
}
