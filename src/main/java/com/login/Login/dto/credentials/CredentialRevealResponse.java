package com.login.Login.dto.credentials;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredentialRevealResponse {
    private Long credentialId;
    private Map<String, Object> details;
}
