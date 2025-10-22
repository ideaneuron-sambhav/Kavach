package com.login.Login.dto.auth;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private List<String> permissions;
    private List<Long> permissionIds;

}

