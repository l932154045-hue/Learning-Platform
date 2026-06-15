package com.learning.course.dto.req;

import com.learning.common.core.page.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class CourseSearchReq extends PageReq {
    private Long categoryId;
    private String keyword;
    private BigDecimal priceMin = BigDecimal.ZERO;
    private BigDecimal priceMax = new BigDecimal("99999");

    public CourseSearchReq() {
        setSort("saleCount_desc");
    }
}
