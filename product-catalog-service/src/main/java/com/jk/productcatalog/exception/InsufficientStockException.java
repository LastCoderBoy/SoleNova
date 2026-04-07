package com.jk.productcatalog.exception;

import java.io.Serial;

public class InsufficientStockException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InsufficientStockException(String message) {
        super(message);
    }
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
