package com.login.Login.entity;

import com.login.Login.crypto.AesAttributeConverter;
import com.login.Login.crypto.EncryptedJsonConverter;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "clients")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Clients {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String alias;

    @Convert(converter = AesAttributeConverter.class)
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;


    @Convert(converter = AesAttributeConverter.class)
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number")
    @Column(unique = true, nullable = false)
    private String mobileNumber;

    @Convert(converter = AesAttributeConverter.class)
    @Column(nullable = false)
    private String address;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id") //, columnDefinition = "integer[]"
    @JdbcTypeCode(SqlTypes.ARRAY)
    private User assignedUser;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;


    /*@Type(JsonBinaryType.class)*/
    @Column(columnDefinition = "text")
    @Convert(converter = EncryptedJsonConverter.class)
    private Map<String, Object> details;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, columnDefinition = "timestamp(6) without time zone")
    private LocalDateTime createdAt;   // Auto set when created

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "timestamp(6) without time zone")
    private LocalDateTime updatedAt;   // Auto updated when modified

}
