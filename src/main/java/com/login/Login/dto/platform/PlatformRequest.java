package com.login.Login.dto.platform;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformRequest {

    @Size(min =3, message = "Name must be at least 3 character")
    @NotNull(message = "Platform name cannot be empty")
    private String name;


    @NotNull(message = "2FA field cannot be null")
    private Boolean twoFA;

    @Pattern(
            regexp = "^(SMS|Email)$",
            message = "2 Factor Authentication must be one of: SMS, Email"
    )
    private List<String> twoFATypes;

    private Boolean active; // optional for add/update


}
