package com.login.Login.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "role")

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Size(min = 3, message = "Name must be at least 3 characters")
    @Column(unique = true, nullable = false)
    private String name; // e.g., "ADMIN", "USER"

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "permission_ids", columnDefinition = "integer[]")
    private List<Long> permissionIds;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}

