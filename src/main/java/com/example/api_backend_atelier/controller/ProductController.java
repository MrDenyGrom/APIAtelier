package com.example.api_backend_atelier.controller;

import com.example.api_backend_atelier.dto.ProductCreateDto;
import com.example.api_backend_atelier.exception.ProductAlreadyExistsException;
import com.example.api_backend_atelier.exception.ProductNotFoundException;
import com.example.api_backend_atelier.exception.UnauthorizedAccessException;
import com.example.api_backend_atelier.model.AppUser;
import com.example.api_backend_atelier.model.Gender;
import com.example.api_backend_atelier.model.Product;
import com.example.api_backend_atelier.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController(value="Панелька управления продуктами")
@RequestMapping(value = "/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/getAllProducts")
    @Operation(summary = "Получение всех продуктов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список продуктов успешно получен", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);

        } catch (UnauthorizedAccessException e) {
            log.error("Ошибка авторизации при получении всех продуктов: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AccessDeniedException e) {
            log.error("Ошибка доступа при получении всех продуктов: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Внутренняя ошибка сервера при получении всех продуктов: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getProductById/{id}")
    @Operation(summary = "Получение продукта по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Продукт успешно найден", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Некорректный аргумент", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Продукт не найден", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Optional<Product> productOptional = productService.getProductById(id);

            if (productOptional.isEmpty()) {
                throw new ProductNotFoundException("Продукт не найден с id: " + id);
            }
            return ResponseEntity.ok(productOptional.get());

        } catch (UnauthorizedAccessException e) {
            log.error("Ошибка авторизации при получении продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AccessDeniedException e) {
            log.error("Ошибка доступа при получении продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ProductNotFoundException e) {
            log.error("Продукт не найден: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            log.error("Неверный аргумент при получении продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Внутренняя ошибка сервера при получении продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/createProduct")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Продукт успешно создан", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Неверный запрос", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Не авторизован", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Нет доступа", content = {@Content}),
            @ApiResponse(responseCode = "409", description = "Конфликт, продукт уже существует", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    @Operation(summary = "Создание продукта")
    public ResponseEntity<Product> createProduct(@RequestBody ProductCreateDto product) {
        try {
            Product createdProduct = productService.createProduct(product);
            log.info("Продукт успешно создан: {}", createdProduct);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (UnauthorizedAccessException e) {
            log.error("Ошибка авторизации при создании продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ProductAlreadyExistsException e) {
            log.error("Конфликт при создании продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (AccessDeniedException e) {
            log.error("Ошибка доступа при создании продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.error("Неверный запрос при создании продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Внутренняя ошибка сервера при создании продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/updateProduct/{id}")
    @Operation(summary = "Обновление продукта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Продукт успешно обновлен", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Неверный запрос", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен, content = {@Content}"),
            @ApiResponse(responseCode = "404", description = "Продукт не найден", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product updatedProduct = productService.updateProduct(id, product);

            if (updatedProduct == null) {
                throw new ProductNotFoundException("Продукт не найден с id: " + id);
            }

            return ResponseEntity.ok(updatedProduct);


        } catch (UnauthorizedAccessException e) {
            log.error("Ошибка авторизации при обновлении продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AccessDeniedException e) {
            log.error("Ошибка доступа при обновлении продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ProductNotFoundException e) {
            log.error("Продукт не найден при обновлении: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            log.error("Неверный аргумент при обновлении продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Внутренняя ошибка сервера при обновлении продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteProduct/{id}")
    @Operation(summary = "Удаление продукта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Продукт успешно удален", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Продукт не найден", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (UnauthorizedAccessException e) {
            log.error("Ошибка авторизации при удалении продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AccessDeniedException e) {
            log.error("Ошибка доступа при удалении продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ProductNotFoundException e) {
            log.error("Продукт не найден при удалении: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Внутренняя ошибка сервера при удалении продукта: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/productByGender/{gender}")
    @Operation(summary = "Получение продуктов по полу")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список продуктов успешно получен", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Неверный запрос", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Продукты не найдены", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<List<Product>> getProductsByGender(@PathVariable Gender gender) {
        try {
            List<Product> products = productService.getProductsByGender(gender);

            if (products == null || products.isEmpty()) {
                throw new ProductNotFoundException("Продукты не найдены для gender: " + gender);
            }

            return ResponseEntity.ok(products);

        } catch (UnauthorizedAccessException e) {
            log.error("Ошибка авторизации при получении продуктов по полу: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AccessDeniedException e) {
            log.error("Ошибка доступа при получении продуктов по полу: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ProductNotFoundException e) {
            log.error("Продукты не найдены: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            log.error("Неверный аргумент при получении продуктов по полу: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Внутренняя ошибка сервера при получении продуктов по полу: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/productByPrice")
    @Operation(summary = "Получение продуктов по ценовому диапазону")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список продуктов успешно получен", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Неверный запрос", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Продукты не найдены", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam Double minPrice, @RequestParam Double maxPrice) {
        try {
            List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);

            if (products == null || products.isEmpty()) {
                throw new ProductNotFoundException("Продукты не найдены в данном ценовом диапазоне");
            }

            return ResponseEntity.ok(products);

        } catch (UnauthorizedAccessException e) {
            log.error("Ошибка авторизации при получении продуктов по цене: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AccessDeniedException e) {
            log.error("Ошибка доступа при получении продуктов по цене: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ProductNotFoundException e) {
            log.error("Продукты не найдены: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            log.error("Неверный аргумент при получении продуктов по цене: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Внутренняя ошибка сервера при получении продуктов по цене: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/productBetweenDate")
    public ResponseEntity<List<Product>> getProductsCreatedBetween(
            @RequestParam String startDate, @RequestParam String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        return ResponseEntity.ok(productService.getProductsCreatedBetween(start, end));
    }


    @GetMapping("/productByCategory/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }
}
