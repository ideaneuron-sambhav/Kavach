package com.login.Login.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "permission")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3,message = "Permission Name must be at least 3 characters")
    @Column(nullable = false, unique = true)
    private String permissionType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}

//User table = 'app_user' list of users
//Permission table = 'permission' list of permission
//Join table = 'user_permissions' list of the user_id (id of users) which are mapped to permission_id(id of permission)
