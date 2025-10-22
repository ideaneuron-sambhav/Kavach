package com.login.Login.dto.user;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserResponse {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private boolean active;
    private List<Long> permissionIds;

}
