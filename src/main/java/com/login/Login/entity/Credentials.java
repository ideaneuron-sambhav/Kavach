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

    @Column(nullable = false)
    private String platformName;

    @Column(name = "search_email")
    private String searchEmail;

    @Column(name = "masked_email")
    private String maskedEmail;

    @Column(name = "masked_mobile_number")
    private String maskedMobileNumber;

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
    private void preSave() {
        searchSensitiveData();
        validateTwoFATypes();
    }

    private void searchSensitiveData() {
        if (this.email != null && !this.email.isEmpty()) {
            this.searchEmail = this.email.substring(0, Math.min(4, this.email.length()));
            this.maskedEmail = maskEmail(this.email);
        }
        if (this.mobileNumber != null && !this.mobileNumber.isEmpty()) {
            this.maskedMobileNumber = maskMobile(this.mobileNumber);
        }
    }


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
    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() <= 5) return mobile; // too short to mask

        String firstDigit = mobile.substring(0, 1);
        String lastFour = mobile.substring(mobile.length() - 4);
        String maskedMiddle = "x".repeat(mobile.length() - 5);
        return firstDigit + maskedMiddle + lastFour;
    }
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;

        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email; // too short to mask

        String beforeAt = email.substring(0, atIndex);
        String afterAt = email.substring(atIndex);

        char first = beforeAt.charAt(0);
        char last = beforeAt.charAt(beforeAt.length() - 1);

        String maskedMiddle = "x".repeat(beforeAt.length() - 2);

        return first + maskedMiddle + last + afterAt;
    }
}
