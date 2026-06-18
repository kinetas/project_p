package com.example.demo.dto.stock;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PageResponse<T> {
    private List<T> data;
    private long totalCount;
    private int page;
    private int size;
    private int totalPages;
}
