package com.mvs.security.jwt.util;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.mvs.security.jwt.model.Response;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class TokenUtil implements Serializable {

    public static final long VALIDITY_MS = Duration.ofHours(24).toMillis();
    private static final long serialVersionUID = 1L;

    @Value("${jwt.secret}")
    private String secret;

    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    //retrieve expiration date from jwt token
    public Date expiration(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(claimsFromToken(token));
    }

    //for retrieving any information from token we will need the secret key
    private Claims claimsFromToken(String token) {
        Key key = new SecretKeySpec(secret.getBytes(), 0, secret.getBytes().length, "HmacSHA512");
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //check if the token has expired
    private boolean isTokenExpired(String token) {
        return Date.from(Instant.now()).after(expiration(token));
    }

    //generate token for user
    public Response generateToken(UserDetails userDetails) {
        return doGenerateToken(Maps.newHashMap(), userDetails.getUsername());
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string 
    private Response doGenerateToken(Map<String, Object> claims, String subject) {
        Key key = new SecretKeySpec(secret.getBytes(), 0, secret.getBytes().length, "HmacSHA512");
        Date expiresIn = Date.from(Instant.now().plusMillis(VALIDITY_MS));
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(expiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .setIssuer("com.mvs")
                .compact();
        return new Response(token, expiresIn.toString());
    }


    //validate token
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return Strings.nullToEmpty(username).equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }
}