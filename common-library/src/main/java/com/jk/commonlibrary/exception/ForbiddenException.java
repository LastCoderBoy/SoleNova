package com.jk.commonlibrary.exception;

import java.io.Serial;

public class ForbiddenException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1L;

    public ForbiddenException(String message) {}

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
