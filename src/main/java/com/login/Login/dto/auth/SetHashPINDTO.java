package com.login.Login.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetHashPINDTO {
    @NotNull(message = "PIN is required")
    private String pin;

}
