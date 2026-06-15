package com.learning.common.core.page;

import lombok.Data;

@Data
public class PageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String sort;
}
