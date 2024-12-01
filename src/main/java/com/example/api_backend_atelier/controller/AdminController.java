package com.example.api_backend_atelier.controller;

import com.example.api_backend_atelier.dto.UserDto;
import com.example.api_backend_atelier.model.AppUser;
import com.example.api_backend_atelier.service.UserService;
import com.example.api_backend_atelier.model.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController(value="Админ Панелька")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/userId/{userId}")
    @Operation(summary = "Получение полной информации о пользователе по ID (только для администратора)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content)
    })
    public ResponseEntity<UserDto> getUserId(@PathVariable UUID userId) {
        try {
            AppUser user = userService.findById(userId);
            return ResponseEntity.ok(user.toDto());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Ошибка при получении данных пользователя:", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/userNumber/{userNumber}")
    @Operation(summary = "Получение полной информации о пользователе по номеру (только для администратора)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера", content = @Content)
    })
    public ResponseEntity<UserDto> getUserNumber(@PathVariable String userNumber) {
        try {
            AppUser user = userService.findByNumber(userNumber);
            return ResponseEntity.ok(user.toDto());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Ошибка при получении данных пользователя:", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/updateUser/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Обновление информации о пользователе (только для администратора)")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody UserDto userDto) {
        try {
            AppUser updatedUser = userService.updateUser(id, userDto);
            return ResponseEntity.ok(updatedUser.toDto());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь с ID " + id + " не найден.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обновлении данных пользователя.");
        }
    }

    @DeleteMapping("/deleteUser/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Удаление пользователя (только для администратора)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удален", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - недостаточно прав", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        log.info("Удаление пользователя с ID: {}", id);
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            log.warn("Пользователь с ID {} не найден: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Ошибка при удалении пользователя с ID {}: ", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getAllUsers")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Получение списка всех пользователей (только для администратора)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен", content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - недостаточно прав", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    public ResponseEntity<List<AppUser>> getAllUsers() {
        log.info("Получение списка всех пользователей.");
        try {
            List<AppUser> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Ошибка при получении списка пользователей: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/changeRole{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Изменение роли пользователя (только для администратора)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Роль пользователя успешно изменена", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Некорректная роль указана", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<?> changeUserRole(@PathVariable UUID userId, @RequestParam("role") Role role) {
        final String methodName = "changeUserRole";
        try {
            AppUser user = userService.findById(userId);
            user.setRole(role);
            userService.save(user);
            return ResponseEntity.ok("Роль пользователя с ID " + userId + " успешно изменена на " + role + ".");
        } catch (IllegalArgumentException e) {
            log.warn("{}: Некорректная роль указана: {}", methodName, role);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Указана некорректная роль: " + role + ".");
        } catch (NoSuchElementException e) {
            log.warn("{}: Пользователь с ID {} не найден.", methodName, userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь с ID " + userId + " не найден.");
        } catch (Exception e) {
            log.error("{}: Ошибка при изменении роли пользователя с ID {}: ", methodName, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла внутренняя ошибка сервера.");
        }
    }

}
