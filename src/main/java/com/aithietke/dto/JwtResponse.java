package com.aithietke.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class JwtResponse {

    private String token;
    private String refreshToken;
    private Long id;
    private String username;
    private String email;
    private List<String> roles;

}
