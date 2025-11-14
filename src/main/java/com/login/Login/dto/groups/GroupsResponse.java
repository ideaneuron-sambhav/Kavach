package com.login.Login.dto.groups;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupsResponse {
    private Long id;
    private String name;
    private String alias;
    private String representativeName;
    private String mobileNumber;
    private String email;
    private boolean active;

}
