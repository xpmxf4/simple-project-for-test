package com.concurrency.shop.dto;

import com.concurrency.shop.domain.product.Product;
import lombok.Getter;

@Getter
public class StockResponse {
    private final Long productId;
    private final String productName;
    private final Integer stockQuantity;

    public StockResponse(Product product) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.stockQuantity = product.getStockQuantity();
    }
}
