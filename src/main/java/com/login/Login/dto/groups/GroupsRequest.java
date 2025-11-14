package com.login.Login.dto.groups;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GroupsRequest {

    @NotNull(message = "Group Name cannot be empty")
    @Size(min = 3, message = "Group Name must be at least 3 characters")
    @Column(nullable = false, unique = true)
    private String name;

    private String alias;

    @NotNull(message = "Representative Name cannot be empty")
    @Size(min = 3, message = "Representative Name must be at least 3 characters")
    @Column(nullable = false, unique = true)
    private String representativeName;


    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    @NotNull(message = "Mobile number cannot be blank")
    private String mobileNumber;

    @Email(message = "Invalid Email Format!!!")
    @NotNull(message = "Email cannot be null")
    private String email;

}
