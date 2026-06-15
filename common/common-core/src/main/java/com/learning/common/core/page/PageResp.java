package com.learning.common.core.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResp<T> {
    private List<T> records;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPages;

    public static <T> PageResp<T> of(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be positive");
        }
        int totalPages = (int) Math.ceil((double) total / pageSize);
        return new PageResp<>(records, total, pageNum, pageSize, totalPages);
    }
}
