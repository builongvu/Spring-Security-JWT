package com.aithietke.dto;

import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class SigninRequest {

    @NotNull
    private String username;
    @NotNull
    private String password;

}
