package com.mvs.security.jwt.controller;


import com.mvs.security.jwt.model.Request;
import com.mvs.security.jwt.model.Response;
import com.mvs.security.jwt.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class AuthenticationController {

    private final TokenUtil tokenUtil;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;


    @Autowired
    public AuthenticationController(final AuthenticationManager authenticationManager, final TokenUtil tokenUtil,
                                    final UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.tokenUtil = tokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Object> createAuthenticationToken(@RequestBody Request authenticationRequest) throws Exception {
        authenticate(authenticationRequest.username(), authenticationRequest.password());
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.username());
        final Response response = tokenUtil.generateToken(userDetails);
        return ResponseEntity.ok().body(response);
    }

    private void authenticate(String username, String password) throws AuthenticationException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new DisabledException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", e);
        }
    }
}