package com.aithietke.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage {

    private int errorCode;
    private Date time;
    private String message;
    private String description;

}
