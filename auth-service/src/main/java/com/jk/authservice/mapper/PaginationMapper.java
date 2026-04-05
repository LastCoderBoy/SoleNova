package com.jk.authservice.mapper;

import com.jk.commonlibrary.dto.PaginatedResponse;
import org.springframework.data.domain.Page;

public final class PaginationMapper {

    private PaginationMapper() {
        // utility class
    }

    public static <T> PaginatedResponse<T> fromPage(Page<T> page) {
        if (page == null) {
            return PaginatedResponse.empty();
        }

        return PaginatedResponse.of(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),   // zero-based page index
                page.getSize()
        );
    }
}