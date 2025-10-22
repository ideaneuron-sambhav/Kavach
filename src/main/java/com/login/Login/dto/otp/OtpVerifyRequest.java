package com.login.Login.dto.otp;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @NotNull(message = "refId cannot be null")
    private String refId;

    @NotNull(message = "OTP cannot be empty")
    private String otp;
}
