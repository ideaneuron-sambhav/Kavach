package com.login.Login.dto.credentials;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredentialsRequest {

    @NotNull(message = "Client ID cannot be null")
    private Long clientId;

    @NotNull(message = "UserName cannot be null")
    private String userName;
/*
    @NotNull(message = "Password cannot be blank")
    private String password;*/

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    @Column(nullable = false)
    @NotNull(message = "Mobile Number cannot be blank")
    private String mobileNumber;

    @Size(min =3, message = "Platform Name must be at least 3 character")
    @NotNull(message = "Platform name cannot be empty")
    private String platformName;

    private Map<String, Object> details;
/*

    @NotNull(message = "2FA field cannot be null")
    private Boolean twoFA;

    @Pattern(
            regexp = "^(SMS|Email)$",
            message = "2 Factor Authentication must be one of: SMS, Email"
    )
    private List<String> twoFATypes;
*/

    private Boolean active;
}
