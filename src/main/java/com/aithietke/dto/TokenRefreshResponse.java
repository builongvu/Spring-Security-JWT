package com.aithietke.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRefreshResponse {

    private String token;
    private String refreshToken;

}
