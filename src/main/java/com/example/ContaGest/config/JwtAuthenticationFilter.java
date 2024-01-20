package com.example.ContaGest.config;

import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.Token;
import com.example.ContaGest.repository.TokenRepository;
import com.example.ContaGest.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private TokenRepository tokenRepository;
    private final HandlerExceptionResolver exceptionResolver;
    @Autowired
    public JwtAuthenticationFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ){

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            jwt = authHeader.substring(7);
            username = jwtService.getUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                var isTokenValid = tokenRepository.findByToken(jwt)
                        .map(t -> !t.isExpired() && !t.isRevoke() && t.getTokenFormat().equals(Token.LOGIN))
                        .orElse(false);
                if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        }catch (Exception e){
            if (e instanceof ExpiredJwtException ) {
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    final String jwtToken = authHeader.substring(7);
                    var token = tokenRepository.findByToken(jwtToken).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
                    if (!token.isExpired() && !token.isRevoke()){
                        token.setRevoke(true);
                        token.setExpired(true);
                        tokenRepository.save(token);
                    }
                }
            }
            exceptionResolver.resolveException(request,response,null,e);
        }
    }
}
