package com.dzaicev.exceptions;

public class NullArgumentException extends Throwable {
    public NullArgumentException(String argument_must_be_not_null) {
        super(argument_must_be_not_null);
    }
}
