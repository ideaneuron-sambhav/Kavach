package com.login.Login.dto.platform;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformResponse {

    private Long id;
    private String name;
    private Boolean twoFA;
    private List<String> twoFATypes;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
