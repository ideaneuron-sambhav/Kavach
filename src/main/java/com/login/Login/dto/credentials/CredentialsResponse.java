package com.login.Login.dto.credentials;

import com.login.Login.entity.Clients;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredentialsResponse {
    private Long id;
    private Clients clients;
    private String email;
    private String mobileNumber;
    private String platformName;
    private Boolean twoFA;
    private List<String> twoFATypes;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}