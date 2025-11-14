package com.login.Login.dto.clients;

import com.login.Login.dto.user.UserResponse;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {
    private Long id;
    private String name;
    private String alias;
    private String email;
    private String mobileNumber;
    private String address;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> details;
    private UserResponse user;
}

