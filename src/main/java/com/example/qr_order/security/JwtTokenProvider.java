package com.example.qr_order.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    private final Key signing_key;
    private final long jwt_expiration;
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecretBase64,
            @Value("${app.jwt.expiration}") long jwt_expiration
    ) {
        // app.jwt.secret phải là BASE64 hợp lệ
        this.signing_key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretBase64));
        this.jwt_expiration = jwt_expiration; // tính theo milliseconds
    }

    public String generate_token(UserDetails user_details){

        String user_name = user_details.getUsername();
        List<String> roles = user_details.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expiry_date = new Date(now.getTime() + jwt_expiration);

        Claims claims = Jwts.claims().setSubject(user_name);
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry_date)
                .signWith(signing_key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signing_key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}

