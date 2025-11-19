package com.login.Login.dto.credentials;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredentialsResponse {
    private Long id;
    private String clients;
    private String userName;
/*    private String maskedEmail;
    private String maskedMobileNumber;*/
    private String mobileNumber;
    private String platformName;
/*    private Boolean twoFA;
    private List<String> twoFATypes;*/
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}