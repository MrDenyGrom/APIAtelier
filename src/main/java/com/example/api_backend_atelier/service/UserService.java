package com.example.api_backend_atelier.service;

import com.example.api_backend_atelier.dto.UserDto;
import com.example.api_backend_atelier.exception.AuthenticationException;
import com.example.api_backend_atelier.exception.ResourceNotFoundException;
import com.example.api_backend_atelier.exception.UserAlreadyExistsException;
import com.example.api_backend_atelier.model.AppUser;
import com.example.api_backend_atelier.repository.UserRepository;
import com.example.api_backend_atelier.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public AppUser registerUser(AppUser appUser) {
        log.info("Попытка регистрации пользователя с номером: {}", appUser.getNumber());
        if (userRepository.findByNumber(appUser.getNumber()).isPresent()) {
            log.warn("Пользователь с таким номером уже существует: {}", appUser.getNumber());
            throw new UserAlreadyExistsException("Пользователь с таким номером телефона уже существует.");
        }
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        AppUser savedUser = userRepository.save(appUser);
        log.info("Пользователь успешно зарегистрирован: {}", savedUser.getNumber());
        return savedUser;
    }

    public String authenticateUser(String phoneNumber, String password) {
        log.info("Попытка аутентификации пользователя с номером: {}", phoneNumber);
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(phoneNumber, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(phoneNumber);
            log.info("Пользователь успешно аутентифицирован: {}", phoneNumber);
            return token;
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("Ошибка аутентификации пользователя {}: {}", phoneNumber, e.getMessage());
            throw new AuthenticationException("Неверный номер телефона или пароль.");
        }
    }

    public AppUser getUserByPhoneNumber(String phoneNumber) {
        log.info("Поиск пользователя с номером: {}", phoneNumber);
        return userRepository.findByNumber(phoneNumber)
                .orElseThrow(() -> {
                    log.warn("Пользователь с номером {} не найден.", phoneNumber);
                    return new ResourceNotFoundException("Пользователь с таким номером телефона не найден.");
                });
    }

    public AppUser findById(UUID id) {
        log.info("Поиск пользователя по ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден.", id);
                    return new ResourceNotFoundException("Пользователь с таким ID не найден.");
                });
    }

    public AppUser findByNumber(String number) {
        log.info("Поиск пользователя по номеру: {}", number);
        return userRepository.findByNumber(number)
                .orElseThrow(() -> {
                    log.warn("Пользователь с номером: {} не найден.", number);
                    return new NoSuchElementException("Пользователь не найден по номеру: " + number);

                });
    }

    @Transactional
    public void deleteUser(UUID id) {
        log.info("Попытка удаления пользователя с ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Пользователь с ID {} не найден для удаления.", id);
            throw new ResourceNotFoundException("Пользователь с таким ID не найден.");
        }
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} успешно удален.", id);
    }

    public List<AppUser> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        List<AppUser> users = userRepository.findAll();
        log.info("Найдено {} пользователей", users.size());
        return users;
    }

    @Transactional
    public AppUser updateUser(UUID id, UserDto userDto) {
        log.info("Обновление данных пользователя с ID: {}", id);
        AppUser existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + id + " не найден."));

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getLastName() != null) {
            existingUser.setLastName(userDto.getLastName());
        }
        if (userDto.getNumber() != null) {
            existingUser.setNumber(userDto.getNumber());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }
        log.info("Данные пользователя с ID {} успешно обновлены", id);
        return userRepository.save(existingUser);
    }

    @Transactional
    public void updatePassword(String number, String oldPassword, String newPassword) {
        log.info("Попытка изменения пароля для пользователя с номером: {}", number);
        AppUser user = userRepository.findByNumber(number)
                .orElseThrow(() -> {
                    log.warn("Пользователь с номером {} не найден при попытке смены пароля.", number);
                    return new ResourceNotFoundException("Пользователь с таким номером телефона не найден.");
                });

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Введен неверный старый пароль для пользователя {}", number);
            throw new IllegalArgumentException("Старый пароль неверный");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Пароль успешно изменен для пользователя {}", number);
    }

    public void save(AppUser user) {
        log.info("Сохранение пользователя: {}", user.getNumber());
        userRepository.save(user);
        log.info("Пользователь успешно сохранен: {}", user.getNumber());
    }

    @Transactional
    public void blockUserByNumber(String userNumber) {
        log.info("Блокировка пользователя с номером: {}", userNumber);
        AppUser user = userRepository.findByNumber(userNumber)
                .orElseThrow(() -> {
                    log.warn("Пользователь с номером {} не найден для блокировки.", userNumber);
                    return new ResourceNotFoundException("Пользователь с таким номером телефона не найден.");
                });
        user.lock();
        userRepository.save(user);
        log.info("Пользователь с номером {} заблокирован.", userNumber);
    }

    @Transactional
    public void unblockUserByNumber(String userNumber) {
        log.info("Разблокировка пользователя с номером: {}", userNumber);
        AppUser user = userRepository.findByNumber(userNumber)
                .orElseThrow(() -> {
                    log.warn("Пользователь с номером {} не найден для разблокировки.", userNumber);
                    return new ResourceNotFoundException("Пользователь с таким номером телефона не найден.");
                });
        user.unlock();
        userRepository.save(user);
        log.info("Пользователь с номером {} разблокирован.", userNumber);
    }

//    public boolean resetPassword(UUID userId) {
//        AppUser user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
//
//        if (!user.canResetPassword()) {
//            throw new IllegalArgumentException("Пользователь не может сбросить пароль (нет email).");
//        }
//
//        String newPassword = generateRandomPassword();
//
//        user.setPassword(newPassword);
//        userRepository.save(user);
//
//        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
//            sendPasswordResetEmail(user.getEmail(), newPassword);
//            return true;
//        }
//        return false;
//    }
//
//    private void sendPasswordResetEmail(String toEmail, String newPassword) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject("Сброс пароля");
//        message.setText("Ваш новый пароль: " + newPassword);
//        message.setFrom("no-reply@atelier.com");
//
//        mailSender.send(message);
//    }
//
//
//    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
//    private static final int PASSWORD_LENGTH = 12;
//
//    private String generateRandomPassword() {
//        SecureRandom random = new SecureRandom();
//        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
//
//        for (int i = 0; i < PASSWORD_LENGTH; i++) {
//            int index = random.nextInt(CHARACTERS.length());
//            password.append(CHARACTERS.charAt(index));
//        }
//
//        return password.toString();
//    }

}