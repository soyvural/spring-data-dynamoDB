package com.mvs.security.jwt.model;

import java.io.Serializable;

public record Request(String username, String password) implements Serializable {

    private static final long serialVersionUID = -1L;

}
