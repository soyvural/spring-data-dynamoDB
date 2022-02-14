package com.mvs.security.jwt.model;


import java.io.Serializable;

public record Response(String token, String expiresIn) implements Serializable {

    private static final long serialVersionUID = -1L;

}