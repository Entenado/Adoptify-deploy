package com.adoptify.security.jwt;

import com.adoptify.dto.RequestDto;
import com.adoptify.model.User;
import com.adoptify.security.RouteValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;


@Component
public class JwtService {

    private Key secret;

    @Autowired
    private RouteValidator routeValidator;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @PostConstruct
    protected void init() {
        byte[] apiKeySecretBytes = new byte[64]; // 512 bits
        new SecureRandom().nextBytes(apiKeySecretBytes);
        secret = Keys.hmacShaKeyFor(apiKeySecretBytes);
    }

    public String createToken(User authUser){
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", authUser.getId());
        claims.put("role", authUser.getRoles());

        Date now = new Date();
        Date exp = new Date(now.getTime() + 3600000); // 1 hora de validez

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(authUser.getUsername())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secret, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validate(String token, RequestDto requestDto){
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Additional validation logic if required
            if (!isProtectora(claims) && routeValidator.isProtectora(requestDto)) {
                return false;
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public String getUserNameFromToken(String token){
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception exception) {
            return "Bad token";
        }
    }

    private boolean isProtectora(Claims claims){
        return claims.get("role").toString().contains("ROLE_PROTECTORA");
    }
}
