package com.login.Login.dto.permission;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponse {
    private long id;
    @Column(nullable = false, unique = true)
    private String permissionType;
    private Boolean active;
}
