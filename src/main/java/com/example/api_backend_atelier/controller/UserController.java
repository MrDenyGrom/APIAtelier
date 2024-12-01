package com.example.api_backend_atelier.controller;

import com.example.api_backend_atelier.dto.AuthRequest;
import com.example.api_backend_atelier.dto.PasswordUpdateDto;
import com.example.api_backend_atelier.dto.UserRegistrationRequest;
import com.example.api_backend_atelier.exception.AuthenticationException;
import com.example.api_backend_atelier.exception.UnauthorizedAccessException;
import com.example.api_backend_atelier.exception.UserAlreadyExistsException;
import com.example.api_backend_atelier.model.AppUser;
import com.example.api_backend_atelier.model.Gender;
import com.example.api_backend_atelier.security.JwtAuthenticationFilter;
import com.example.api_backend_atelier.security.JwtTokenProvider;
import com.example.api_backend_atelier.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController(value="Юзер Панелька")
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Неверный запрос - некорректные входные данные", content = {@Content}),
            @ApiResponse(responseCode = "409", description = "Конфликт - пользователь с таким номером телефона уже существует", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<AppUser> registerUser(@Valid @RequestBody UserRegistrationRequest request, Gender gender) {
        log.info("Регистрация нового пользователя: {}", request.getNumber());
        try {
            AppUser user = new AppUser();
            user.setNumber(request.getNumber());
            user.setPassword(request.getPassword());
            user.setGender(gender);

            AppUser createdUser = userService.registerUser(user);
            log.info("Пользователь успешно зарегистрирован: {}", createdUser.getNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (AuthenticationException e) {
            log.warn("Ошибка регистрации пользователя, неверный данные: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (UserAlreadyExistsException e) {
            log.warn("Ошибка регистрации пользователя, пользователь с таким номером телефона существует: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при регистрации пользователя: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Авторизация существующего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная авторизация", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Ошибка авторизации - неверные учетные данные", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<Void> login(@Valid @RequestBody AuthRequest authRequest, HttpServletResponse response) {
        log.info("Попытка входа пользователя с номером: {}", authRequest.getNumber());
        try {
            String token = userService.authenticateUser(authRequest.getNumber(), authRequest.getPassword());
            response.setHeader("Authorization", "Bearer " + token);
            log.info("Пользователь успешно авторизован: {}", authRequest.getNumber());
            return ResponseEntity.ok().build();
        } catch (AuthenticationException e) {
            log.warn("Ошибка авторизации пользователя при аутентификации: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Ошибка при авторизации пользователя: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Получение информации о текущем пользователе")
    @PreAuthorize("isAuthenticated()")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе получена", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Ошибка авторизации - пользователь не авторизован", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<AppUser> getCurrentUser(HttpServletRequest request) {
        try {
            String token = JwtAuthenticationFilter.getJwtFromRequest(request);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                log.warn("getCurrentUser(): Неверный токен.");
                throw new UnauthorizedAccessException("Неверный токен.");
            }

            String phoneNumber = jwtTokenProvider.getPhoneNumberFromToken(token);
            AppUser currentUser = userService.getUserByPhoneNumber(phoneNumber);
            log.info("getCurrentUser(): Информация о текущем пользователе {} получена.", currentUser.getNumber());
            return ResponseEntity.ok(currentUser);
        } catch (UnauthorizedAccessException e) {
            log.warn("Ошибка авторизации при попытке получить информацию о пользователе: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Ошибка при получении текущего пользователя: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход текущего пользователя")
    @PreAuthorize("isAuthenticated()")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно вышел", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Ошибка авторизации - пользователь не авторизован", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<Void> logoutUser(HttpServletRequest request) {
        try {
            log.info("Пользователь выходит.");
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                log.info("Сессия пользователя успешно завершена.");
                return ResponseEntity.ok().build();
            } else {
                log.warn("Пользователь попытался выйти, но сессия отсутствует.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            log.error("Ошибка при выходе пользователя: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/updatePassword")
    @Operation(summary = "Обновление пароля пользователя (для текущего пользователя)")
    @PreAuthorize("isAuthenticated()")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль успешно обновлен", content = {@Content}),
            @ApiResponse(responseCode = "400", description = "Ошибка обновления пароля - неверные данные", content = {@Content}),
            @ApiResponse(responseCode = "409", description = "Старый пароль совпадает с новым", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<String> updatePassword(@Valid @RequestBody PasswordUpdateDto passwordDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        try {
            if (Objects.equals(passwordDto.getOldPassword(), passwordDto.getNewPassword())) {
                log.warn("Ошибка обновления пароля: новый пароль совпадает со старым.");
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            userService.updatePassword(currentUsername, passwordDto.getOldPassword(), passwordDto.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            log.warn("Ошибка авторизации пользователя при обновлении пароля: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Ошибка при изменении пароля пользователя: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/canReset")
    @Operation(summary = "Проверка возможности сброса пароля пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Возможность сброса пароля успешно проверена", content = {@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Boolean.class))}),
            @ApiResponse(responseCode = "401", description = "Ошибка авторизации - пользователь не авторизован", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = {@Content})
    })
    public ResponseEntity<Boolean> canReset(HttpServletRequest request) {
        log.info("Проверка возможности сброса пароля.");
        try {
            String token = JwtAuthenticationFilter.getJwtFromRequest(request);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                log.warn("canReset(): Неверный токен.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String phoneNumber = jwtTokenProvider.getPhoneNumberFromToken(token);
            AppUser currentUser = userService.getUserByPhoneNumber(phoneNumber);
            boolean canReset = currentUser.canResetPassword();

            log.info("canReset(): Возможность сброса пароля для пользователя {}: {}", phoneNumber, canReset);
            return ResponseEntity.ok(canReset);
        } catch (UnauthorizedAccessException e) {
            log.warn("Ошибка авторизации при проверке возможности сброса пароля: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Ошибка при проверке возможности сброса пароля: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}