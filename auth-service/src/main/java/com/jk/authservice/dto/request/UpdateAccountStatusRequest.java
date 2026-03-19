package com.jk.authservice.dto.request;

import com.jk.authservice.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountStatusRequest {

    @NotNull(message = "Account status is required")
    private AccountStatus accountStatus;

    @Size(max = 255)
    private String reason;  // audit trail — why was this status set
}
