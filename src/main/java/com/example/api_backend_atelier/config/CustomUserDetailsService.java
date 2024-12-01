package com.example.api_backend_atelier.config;

import com.example.api_backend_atelier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String number) throws UsernameNotFoundException {
        return userRepository.findByNumber(number)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Пользователь с номером %s не найден.", number);
                    log.error(errorMessage);
                    return new UsernameNotFoundException(errorMessage);
                });
    }
}