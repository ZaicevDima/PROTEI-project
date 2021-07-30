package com.dzaicev.exceptions;

public class NotFoundException extends Throwable {
    public NotFoundException(String argument_must_be_not_null) {
        super(argument_must_be_not_null);
    }
}
