package com.example.ContaGest.config;

import com.example.ContaGest.model.Role;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private final HandlerExceptionResolver exceptionResolver;

    public SecurityConfig(AuthenticationProvider authenticationProvider,
                          LogoutHandler logoutHandler,
                          @Qualifier("handlerExceptionResolver")HandlerExceptionResolver exceptionResolver) {
        this.authenticationProvider = authenticationProvider;
        this.logoutHandler = logoutHandler;
        this.exceptionResolver = exceptionResolver;
    }
    @Bean
    public JwtAuthenticationFilter jwtAuthFilter(){
        return new JwtAuthenticationFilter(exceptionResolver);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorizeHttpRequests) ->
                authorizeHttpRequests
                        .requestMatchers(
                                "/api/v1/auth/register-accountant",
                                "api/v1/auth/login",
                                "api/v1/auth/confirm-accountant",
                                "api/v1/auth/confirm-client",
                                "api/v1/password/confirm-forgot-password",
                                "api/v1/password/password-forgot",
                                "/api/v1/img/**",
                                "/api/v1/auth/confirm-change-email"
                                ).permitAll()
                        .requestMatchers(
                                "/api/v1/auth/authenticate-accountant",
                                "/api/v1/auth/authenticate-client",
                                "/api/v1/auth/authenticate-client"
                        ).denyAll()
                        .requestMatchers(
                                "/api/v1/auth/register-client",
                                "/api/v1/accountant/**"
                                )
                        .hasAuthority(Role.ACCOUNTANT.name())
                        .requestMatchers(
                                "/api/v1/client/**"
                        ).hasAuthority(Role.CLIENT.name())
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement((sessionManagement) ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider).addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .logout((logout) ->
                        logout
                                .logoutUrl("/api/v1/auth/logout")
                                .addLogoutHandler(logoutHandler)
                                .logoutSuccessHandler(((request, response, authentication) -> SecurityContextHolder.clearContext()))
                );

        return http.build();
    }
}


