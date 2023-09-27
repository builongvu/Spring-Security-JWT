package com.aithietke.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class SignupRequest {

    @NotNull
    private String username;
    @NotNull
    private String email;
    @NotNull
    private String password;
    private Set<String> roles;

}
