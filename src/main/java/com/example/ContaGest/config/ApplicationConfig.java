package com.example.ContaGest.config;

import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserInfoRepository userInfoRepository;
    @Bean
    public UserDetailsService userDetailsService(){
        return username -> userInfoRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
