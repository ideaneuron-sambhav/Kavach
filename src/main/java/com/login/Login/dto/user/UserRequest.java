package com.login.Login.dto.user;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    @NotNull(message = "Name cannot be blank")
    @Size(min =3, max = 50, message = "Name must be between 2 and 50 characters")
    @Column(nullable = false)
    private String firstName;

    private String lastName;

    @NotNull(message = "Email cannot be blank")
    @Email(message = "Email is not in appropriate format")
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one number, and one special character"
    )
    @Column(nullable = false)
    private String password;

    @NotNull(message = "Role Cannot be empty")
    @Column(nullable = false)
    private String role;
}
