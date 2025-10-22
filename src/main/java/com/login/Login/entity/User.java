package com.login.Login.entity;

import com.login.Login.crypto.AesAttributeConverter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Convert(converter = AesAttributeConverter.class)
    @Size(min = 3, message = "Name must be at least 3 characters")
    @Column(nullable = false)
    private String firstName;

    @Convert(converter = AesAttributeConverter.class)
    private String lastName;

    @Convert(converter = AesAttributeConverter.class)
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;


    @Column(nullable = false)
    private String password;


    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;  // User active/inactive status

    @ManyToOne
    @JoinColumn(name = "role_id",nullable = false)
    private Role role;

    public List<Long> getPermissions() {
        if (role != null) {
            return role.getPermissionIds();
        }
        return List.of();
    }

    public User(String firstName, String lastName, String email, String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
