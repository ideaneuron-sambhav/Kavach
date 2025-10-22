package com.login.Login.dto.otp;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
public class OtpResponse {

    private final String refId;
    private final String otp;


}
