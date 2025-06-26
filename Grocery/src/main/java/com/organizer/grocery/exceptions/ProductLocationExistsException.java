package com.organizer.grocery.exceptions;

public class ProductLocationExistsException extends RuntimeException {
    public ProductLocationExistsException(String message) {
        super(message);
    }
}