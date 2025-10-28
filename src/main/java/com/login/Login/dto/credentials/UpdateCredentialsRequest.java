package com.login.Login.dto.credentials;

import com.login.Login.dto.otp.OtpVerifyRequest;

public class UpdateCredentialsRequest {
    private CredentialsRequest credentialsRequest;
    private OtpVerifyRequest otpVerifyRequest;

    // Getters & Setters
    public CredentialsRequest getCredentialsRequest() { return credentialsRequest; }
    public void setCredentialsRequest(CredentialsRequest credentialsRequest) { this.credentialsRequest = credentialsRequest; }

    public OtpVerifyRequest getOtpVerifyRequest() { return otpVerifyRequest; }
    public void setOtpVerifyRequest(OtpVerifyRequest otpVerifyRequest) { this.otpVerifyRequest = otpVerifyRequest; }
}
