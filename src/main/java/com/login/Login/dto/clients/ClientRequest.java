package com.login.Login.dto.clients;

import com.login.Login.dto.nominee.NomineeRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRequest {

    @NotNull(message = "Client name cannot be blank")
    private String name;

    @Email(message = "Invalid email format")
    @NotNull(message = "Email cannot be blank")
    private String email;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    @NotNull(message = "Mobile number cannot be blank")
    private String mobileNumber;

    @NotNull(message = "Address cannot be blank")
    private String address;

    private Boolean active;

    @NotNull(message = "Nominee Details are compulsory")
    private NomineeRequest nominee;
}
