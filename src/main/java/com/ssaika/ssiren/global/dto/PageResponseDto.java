package com.ssaika.ssiren.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponseDto<T>(
    PageInfo page,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    SortInfo sort,
    List<?> contents
) {
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<T>(
            PageInfo.of(page),
            SortInfo.of(page),
            page.getContent()
        );
    }
}