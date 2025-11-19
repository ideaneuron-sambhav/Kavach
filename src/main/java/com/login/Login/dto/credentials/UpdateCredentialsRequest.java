package com.login.Login.dto.credentials;

import com.login.Login.dto.otp.OtpVerifyRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateCredentialsRequest {
    // Getters & Setters
    private CredentialsRequest credentialsRequest;
    private OtpVerifyRequest otpVerifyRequest;

}
