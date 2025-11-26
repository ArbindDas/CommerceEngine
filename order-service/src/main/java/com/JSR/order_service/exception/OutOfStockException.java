package com.JSR.order_service.exception;

import java.util.List;

public class OutOfStockException extends RuntimeException{

    private final List<String>outOfStockSkus;

    public OutOfStockException( List<String> outOfStockSkus) {
        super("Some products are not in stock: "+ outOfStockSkus);
        this.outOfStockSkus = outOfStockSkus;
    }

    public List<String> getOutOfStockSkus() {
        return outOfStockSkus;
    }
}

