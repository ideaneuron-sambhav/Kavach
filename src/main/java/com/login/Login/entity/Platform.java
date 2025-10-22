package com.login.Login.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Table(name = "platforms", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "timestamp(6) without time zone")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "timestamp(6) without time zone")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean twoFA;

    @Column(name = "twofa_types", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> twoFATypes;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true; // active/inactive toggle

    // Validation for 2FA type
    @PrePersist
    @PreUpdate
    private void validateTwoFATypes() {
        if (Boolean.TRUE.equals(this.twoFA)) {
            if (twoFATypes == null || twoFATypes.isEmpty()) {
                throw new RuntimeException("twoFATypes must be provided if 2FA is enabled");
            }
        } else {
            // Remove any stored 2FA types if twoFA is false
            if (twoFATypes != null) {
                twoFATypes.clear();
            }
        }
    }
}
