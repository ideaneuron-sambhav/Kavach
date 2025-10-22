package com.login.Login.dto.role;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    private String oldRoleName;

    @NotNull(message = "Role Name cannot be blank")
    @Size(min = 3, message = "Name must be at least 3 characters")
    @Column(unique = true, nullable = false)
    private String roleName; // e.g., "ADMIN", "USER"


    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "permission_ids", columnDefinition = "integer[]")
    private List<Long> permissionIds;
    private Boolean active;
}
