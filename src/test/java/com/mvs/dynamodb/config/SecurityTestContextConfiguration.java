package com.mvs.dynamodb.config;

import java.util.Arrays;
import java.util.Map;

import com.mvs.security.jwt.util.TokenUtil;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@TestConfiguration
public class SecurityTestContextConfiguration {
    public static final String TEST_USER_TOKEN = "test_user_token";
    public static final String TEST_ADMIN_TOKEN = "test_admin_token";

    // JWT_SECURITY_CONFIG map{key=token, value=username}
    private static final Map<String, String> JWT_SECURITY_CONFIG = Map.of(
            TEST_USER_TOKEN, "test_user",
            TEST_ADMIN_TOKEN, "test_admin");

    @Bean
    public TokenUtil tokenUtil() {
        return new TokenUtil() {
            @Override
            public String getUsernameFromToken(String token) {
                return JWT_SECURITY_CONFIG.get(token);
            }

            @Override
            public boolean validateToken(String token, UserDetails userDetails) {
                return true;
            }
        };
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                if ("test_user".equals(username)) {
                    return new User(username, "", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
                } else if ("test_admin".equals(username)) {
                    return new User(username, "", Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
                }
                throw new UsernameNotFoundException("User not found with username: " + username);
            }
        };
    }
}
