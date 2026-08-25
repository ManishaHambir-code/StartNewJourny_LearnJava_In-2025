package com.scp.java.ocm.common.web;

import com.scp.java.ocm.common.exception.InvalidSortException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableHelper {
    private PageableHelper() {}

    public static Pageable create(int page, int size, String sort, String... allowedFields) {
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        Set<String> allowed = new HashSet<String>(Arrays.asList(allowedFields));
        if (!allowed.contains(field)) {
            throw new InvalidSortException("Unknown sort field: " + field);
        }
        if (parts.length > 1
                && !"asc".equalsIgnoreCase(parts[1].trim())
                && !"desc".equalsIgnoreCase(parts[1].trim())) {
            throw new InvalidSortException("Invalid sort direction: " + parts[1]);
        }
        Sort.Direction direction =
                parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, field));
    }
}
