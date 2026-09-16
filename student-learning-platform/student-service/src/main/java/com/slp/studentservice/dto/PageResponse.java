package com.slp.studentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PageResponse", description = "Generic paginated response wrapper")
public class PageResponse<T> {

    private List<T> content;

    @Schema(example = "0")
    private int page;

    @Schema(example = "10")
    private int size;

    @Schema(example = "42")
    private long totalElements;

    @Schema(example = "5")
    private int totalPages;

    @Schema(example = "false")
    private boolean last;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
