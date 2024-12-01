package com.example.api_backend_atelier.controller;

import com.example.api_backend_atelier.exception.UnauthorizedAccessException;
import com.example.api_backend_atelier.model.AppUser;
import com.example.api_backend_atelier.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController(value="Панелька Модератора")
@RequestMapping("/api/moderator")
@RequiredArgsConstructor
public class ModeratorController {

    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(ModeratorController.class);


    @GetMapping("/getStatus/{userNumber}")
    @PreAuthorize("hasAuthority('ROLE_MODERATOR') or hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Получение статуса пользователя (только цифры телефона)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус пользователя успешно получен", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Неверный формат номера пользователя", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - недостаточно прав", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<String> getUserStatus(@PathVariable String userNumber) {
        try {

            AppUser user = userService.findByNumber(userNumber);
            log.info("getUserStatus(): Статус пользователя {} успешно получен", userNumber);
            return ResponseEntity.ok(user.getFrontendStatus());

        } catch (UnauthorizedAccessException e) {
            log.warn("getUserStatus(): Ошибка авторизации при попытке получить информацию о статусе пользователе: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AccessDeniedException e) {
            log.warn("getUserStatus(): Доступ запрещен при попытке получить информацию о статусе пользователе: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            log.warn("getUserStatus(): Пользователь с номером {} не найден: {}", userNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("getUserStatus(): Ошибка при получении статуса пользователя {}: ", userNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/canReset/{userNumber}")
    @PreAuthorize("hasAuthority('ROLE_MODERATOR') or hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Проверка возможности сброса пароля пользователя (только цифры телефона)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Возможность сброса пароля успешно проверена", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Неверный формат номера пользователя", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - недостаточно прав", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<?> canReset(@PathVariable String userNumber) {
        try {
            AppUser user = userService.findByNumber(userNumber);
            log.info("canReset(): возможность изменения пароля пользователя {} успешно получен", userNumber);
            return ResponseEntity.ok(user.canResetPassword());

        } catch (UnauthorizedAccessException e) {
            log.warn("canReset(): Ошибка авторизации при проверке возможности сброса пароля: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AccessDeniedException e) {
            log.warn("canReset(): Доступ запрещен при проверке возможности сброса пароля: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NoSuchElementException e) {
            log.warn("canReset(): Пользователь с номером {} не найден: {}", userNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("canReset(): Ошибка при проверке возможности сброса пароля для пользователя {}: ", userNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/blockUser/{userNumber}")
    @PreAuthorize("hasAuthority('ROLE_MODERATOR') or hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Блокировка пользователя (только цифры телефона)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно заблокирован", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Неверный формат номера пользователя", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - недостаточно прав", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<Void> blockUser(@PathVariable String userNumber) {
        try {

            userService.blockUserByNumber(userNumber);
            log.info("blockUser(): Пользователь с номером {} успешно заблокирован", userNumber);
            return ResponseEntity.ok().build();

        } catch (UnauthorizedAccessException e) {
            log.warn("blockUser(): Неавторизованный доступ при попытке блокировки пользователя {}: {}", userNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (AccessDeniedException e) {
            log.warn("blockUser(): Доступ запрещен при попытке блокировки пользователя {}: {}", userNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (NoSuchElementException e) {
            log.warn("blockUser(): Пользователь с номером {} не найден: {}", userNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            log.error("blockUser(): Ошибка при блокировке пользователя с номером {}: ", userNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/unblockUser/{userNumber}")
    @PreAuthorize("hasAuthority('ROLE_MODERATOR') or hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Разблокировка пользователя (только цифры телефона)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно разблокирован", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Неверный формат номера пользователя", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - недостаточно прав", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<Void> unblockUser(@PathVariable String userNumber) {
        try {
            userService.unblockUserByNumber(userNumber);
            log.info("unblockUser(): Пользователь с номером {} успешно разблокирован", userNumber);
            return ResponseEntity.ok().build();

        } catch (UnauthorizedAccessException e) {
            log.warn("unblockUser(): Неавторизованный доступ при попытке разблокировки пользователя {}: {}", userNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (AccessDeniedException e) {
            log.warn("unblockUser(): Доступ запрещен при попытке разблокировки пользователя {}: {}", userNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (NoSuchElementException e) {
            log.warn("unblockUser(): Пользователь с номером {} не найден при попытке разблокировки: {}", userNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            log.error("unblockUser(): Ошибка при разблокировке пользователя с номером {}: ", userNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAuthority('ROLE_MODERATOR') and hasAuthority('ROLE_ADMIN')")
    @GetMapping("/userId/{userId}")
    @Operation(summary = "Получение информации о пользователе по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе по ID успешно получена", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Неверный формат ID пользователя", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - недостаточно прав", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<?> getUserId(@PathVariable UUID userId) {
        try {
            AppUser user = userService.findById(userId);
            log.info("getUserId(): Информация о пользователе с ID {} успешно получена", userId);
            return ResponseEntity.ok(user.toDto());

        } catch (UnauthorizedAccessException e) {
            log.warn("getUserId(): Неавторизованный доступ при попытке получение информации о пользователе по ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (AccessDeniedException e) {
            log.warn("getUserId(): Доступ запрещен при попытке получение информации о пользователе по ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (NoSuchElementException e) {
            log.warn("getUserId(): Пользователь с ID {} не найден: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            log.error("getUserId(): Ошибка при получении информации о пользователе с ID {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAuthority('ROLE_MODERATOR') and hasAuthority('ROLE_ADMIN')")
    @GetMapping("/userNumber/{userNumber}")
    @Operation(summary = "Получение информации о пользователе по номеру")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе по номеру успешно получена", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Неверный формат номера пользователя", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Неавторизованный доступ", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен - недостаточно прав", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<?> getUserNumber(@PathVariable String userNumber) {
        try {
            AppUser user = userService.findByNumber(userNumber);
            log.info("getUserNumber(): Информация о пользователе с номером {} успешно получена", userNumber);
            return ResponseEntity.ok(user.toDto());

        } catch (UnauthorizedAccessException e) {
            log.warn("getUserNumber(): Неавторизованный доступ при попытке получение информации о пользователе по номеру {}: {}", userNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (AccessDeniedException e) {
            log.warn("getUserNumber(): Доступ запрещен при попытке получение информации о пользователе по номеру {}: {}", userNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (NoSuchElementException e) {
            log.warn("getUserNumber(): Пользователь с номером {} не найден: {}", userNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            log.error("getUserNumber(): Ошибка при получении информации о пользователе с номером {}: ", userNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
