package com.login.Login.dto.permission;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PermissionRequest {
    @Size(min=3, message = "old permission name must be at least 3 characters")
    private String oldPermissionType;

    @NotNull(message = "Permission Name cannot be empty")
    @Size(min = 3, message = "Permission name must be at least 3 characters")
    @Column(nullable = false, unique = true)
    private String permissionType;
}
