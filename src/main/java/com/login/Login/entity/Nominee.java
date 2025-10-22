package com.login.Login.entity;

import com.login.Login.crypto.AesAttributeConverter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "nominees")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Nominee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AesAttributeConverter.class)
    @Size(min = 3, message = "Name must be at least 3 characters")
    @Column(nullable = false)
    private String name;

    @Pattern(regexp = "^(?:[1-9][0-9]?|100)$", message = "Age must be between 1 and 100")
    @NotNull(message = "Age cannot be blank")
    private String age;

    @Column(nullable = false)
    private String relation;

    @Convert(converter = AesAttributeConverter.class)
    @Column(nullable = false)
    private String mobileNumber;

}
