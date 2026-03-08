package com.interview.demo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {
    private Long       id;
    private String     name;
    private String     description;
    private BigDecimal price;
    private int        stock;
    private String     category;
}
