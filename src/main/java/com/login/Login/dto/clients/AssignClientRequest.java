package com.login.Login.dto.clients;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignClientRequest {
    @NotNull(message = "Client ID cannot be blank")
    private Long clientId;

    private Long userId; // the user you want to assign
}
