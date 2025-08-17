package com.organizer.grocery.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthResponse {
    private String token;
    private String fullName;
    private String email;

    public AuthResponse(String token, String fullName, String email) {
        this.token = token;
        this.fullName = fullName;
        this.email = email;
    }

}