package com.jk.authservice.dto.request;

import com.jk.authservice.enums.AccountStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.jk.commonlibrary.constants.AppConstants.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserFilterRequest {

    @Builder.Default
    @PositiveOrZero
    private int page = DEFAULT_PAGE_NUMBER;

    @Builder.Default
    @Positive
    @Max(100)
    private int size = DEFAULT_PAGE_SIZE;

    @Builder.Default
    private String sortBy = DEFAULT_SORT_BY;

    @Builder.Default
    private String sortDirection = DEFAULT_SORT_DIRECTION;

    private AccountStatus status;

    @Size(max = 100, message = "Search term must not exceed 100 characters")
    private String search;
}
