package com.login.Login.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class Response<T> {
    private T data;
    @Column(nullable = false)
    private int httpStatusCode;
    @Column(nullable = false)
    private String message;
}
