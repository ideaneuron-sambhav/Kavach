package com.login.Login.dto.clients;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClientIdRequest {
    @NotNull(message = "Client Id cannot be blank")
    private Long clientId;
}
