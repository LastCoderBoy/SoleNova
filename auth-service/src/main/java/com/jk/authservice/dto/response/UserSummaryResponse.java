package com.jk.authservice.dto.response;

import com.jk.authservice.enums.AccountStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private List<String> roles;
    private AccountStatus accountStatus;
}
