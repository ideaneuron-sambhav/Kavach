package com.login.Login.dto.nominee;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NomineeRequest {

    @Size(min = 2, max = 50, message = "Nominee name must be between 2 and 50 characters")
    @NotNull(message = "Nominee name cannot be blank")
    private String name;

    @Pattern(
            regexp = "^(Father|Mother|Spouse|Brother|Sister|Son|Daughter|Friend|Guardian)$",
            message = "Relation must be one of: Father, Mother, Spouse, Brother, Sister, Son, Daughter, Friend, Guardian"
    )
    @NotNull(message = "Nominee relation cannot be blank")
    private String relation;


    @NotNull(message = "Age cannot be blank")
    private String age;


    @NotNull(message = "Mobile number cannot be blank")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid Indian mobile number format"
    )
    private String mobileNumber;

}
