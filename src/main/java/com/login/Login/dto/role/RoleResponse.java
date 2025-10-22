package com.login.Login.dto.role;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Data
@Builder
public class RoleResponse {
    private Long id;

    @Column(unique = true, nullable = false)
    private String roleName; // e.g., "ADMIN", "USER"

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "permission_ids", columnDefinition = "integer[]")
    private List<Long> permissionIds;

    private Boolean active;
}
