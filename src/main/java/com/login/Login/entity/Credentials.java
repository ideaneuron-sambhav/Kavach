package com.login.Login.entity;

import com.login.Login.crypto.AesAttributeConverter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "credentials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "clients_id", referencedColumnName = "id")
    private Clients clients;

    @Convert(converter = AesAttributeConverter.class)
    @Email(message = "Invalid email format")
    @Column(nullable = false)
    private String email;

    @Convert(converter = AesAttributeConverter.class)
    @NotNull(message = "Password cannot be blank")
    private String password;

    @Convert(converter = AesAttributeConverter.class)
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    @Column(nullable = false)
    private String mobileNumber;

    @Convert(converter = AesAttributeConverter.class)
    @Column(nullable = false)
    private String platformName;

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

    @PrePersist
    @PreUpdate
    private void validateTwoFATypes() {
        if (Boolean.TRUE.equals(this.twoFA)) {
            if (twoFATypes == null || twoFATypes.isEmpty()) {
                throw new RuntimeException("twoFATypes must be provided if 2FA is enabled");
            }
        } else {
            if (twoFATypes != null) {
                twoFATypes.clear();
            }
        }
    }
}
