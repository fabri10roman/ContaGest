package com.example.ContaGest.service;

import com.example.ContaGest.model.Role;
import com.example.ContaGest.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.core.GrantedAuthority;


import javax.crypto.SecretKey;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String SECRET_KEY = "416c040522a174e75f52d44e1128e2436808f648586b02407260f9dfa46f45b2";
    private final TokenRepository tokenRepository;
    public String getUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public String getRole(String token){
        return getClaim(token,claims -> (String) claims.get("Role"));
    }

    public <T> T getClaim (String token, Function<Claims, T> claimsResolver){
        final Claims claims = getALlClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims getALlClaims(String token){
        return Jwts
                .parser()
                .verifyWith(getSiginKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateTokenRegistration(UserDetails userDetails){
        int timeMillis = 900000; //15min
        Map<String, Object> claims = new HashMap<>();
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        claims.put("Role", role);
        return Jwts
                .builder()
                .claims().empty().add(claims).and()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + timeMillis))
                .signWith(getSiginKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(UserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        claims.put("Role", role);
        return generateToken(claims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        int timeMillis = 86400000; //24hours
        return Jwts
                .builder()
                .claims().empty().add(extraClaims).and()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + timeMillis))
                .signWith(getSiginKey(), Jwts.SIG.HS256)
                .compact();
    }

    private SecretKey getSiginKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = getUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenValid(String token, Principal connectedUser){
        boolean isTokenValid = tokenRepository.findByToken(token)
                .map(t -> !t.isExpired() && !t.isRevoke())
                .orElse(false);
        if (isTokenValid){
            String role = getRole(token);
            if(role.equals(Role.ACCOUNTANT.name())){
                String username = connectedUser.getName();
                return getUsername(token).equals(username);
            }else if (role.equals(Role.CLIENT.name())){
                String username = connectedUser.getName();
                return getUsername(token).equals(username);
            }
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }

    private Date getExpiration(String token) {
        return getClaim(token,Claims::getExpiration);
    }

}