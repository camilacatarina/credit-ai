package com.camila.creditai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private String username;
    private String type;

    public LoginResponse(String token, String username) {
        this.token = token;
        this.username = username;
        this.type = "Bearer";
    }
}