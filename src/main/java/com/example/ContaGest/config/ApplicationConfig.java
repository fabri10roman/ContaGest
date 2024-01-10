package com.example.ContaGest.config;

import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.AccountantModel;
import com.example.ContaGest.model.ClientModel;
import com.example.ContaGest.model.UserInfoModel;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    //private final UserInfoRepository userInfoRepository;
    private final ClientRepository clientRepository;
    private final AccountantRepository accountantRepository;
/*
    @Bean
    public UserDetailsService userDetailsService(){
        return username -> userInfoRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
*/
    @Bean
    public UserDetailsService userDetailsService(){
        return username -> {

            Optional<AccountantModel> accountantModel = accountantRepository.findByUsername(username);
            if(accountantModel.isPresent()){
                return new User(accountantModel.get().getUsername(),accountantModel.get().getPassword(),new ArrayList<>());
            }
            Optional<ClientModel> clientModel = clientRepository.findByUsername(username);
            if (clientModel.isPresent()){
                return new User(clientModel.get().getUsername(),clientModel.get().getPassword(),new ArrayList<>());
            }
            throw new ResourceNotFoundException("User not found with userCI: " + username);
        };
    }
    @Bean
    public AuthenticationProvider authenticationProvider (){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
