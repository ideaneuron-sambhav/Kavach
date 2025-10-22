package com.login.Login.dto.nominee;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NomineeResponse {
    private String name;
    private String relation;
    private String mobileNumber;
    private String age;
}

