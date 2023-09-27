package com.aithietke.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TokenRefreshRequest {

    @NotNull
    private String refreshToken;

}
