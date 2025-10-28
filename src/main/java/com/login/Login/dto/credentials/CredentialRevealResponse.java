package com.login.Login.dto.credentials;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredentialRevealResponse {
    private Long credentialId;
    private String password;
}
