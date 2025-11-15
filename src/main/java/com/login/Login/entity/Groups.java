package com.login.Login.entity;

import com.login.Login.crypto.AesAttributeConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "Groups")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Groups {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String alias;

    private String representativeName;

    @Convert(converter = AesAttributeConverter.class)
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    @Column(unique = true, nullable = false)
    private String mobileNumber;

    @Convert(converter = AesAttributeConverter.class)
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "timestamp(6) without time zone")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "timestamp(6) without time zone")
    private LocalDateTime updatedAt;


}
