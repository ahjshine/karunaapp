package com.medsentinel.security;

/** Raised when a compatibility service method is called without a principal. */
public class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException() {
        super("Authenticated principal required");
    }
}
