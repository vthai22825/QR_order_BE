package com.example.qr_order.dtos.response;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PageResponse<T> {
    private int page;
    private int size;
    private long total;
    private List<T> items;
}
