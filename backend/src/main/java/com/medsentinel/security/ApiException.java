package com.medsentinel.security;

/**
 * Safe, client-facing exception used by the lightweight HTTP API.
 *
 * <p>The message is deliberately supplied by the route boundary rather than
 * copied from an arbitrary underlying exception.</p>
 */
public class ApiException extends RuntimeException {

    private final int status;
    private final String code;

    public ApiException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public static ApiException badRequest(String message) {
        return new ApiException(400, "BAD_REQUEST", message);
    }

    public static ApiException unauthorized() {
        return new ApiException(401, "UNAUTHENTICATED", "Authentication is required");
    }

    public static ApiException forbidden() {
        return new ApiException(403, "FORBIDDEN", "You do not have permission to perform this action");
    }

    public static ApiException notFound() {
        return new ApiException(404, "NOT_FOUND", "The requested resource was not found");
    }

    public static ApiException methodNotAllowed() {
        return new ApiException(405, "METHOD_NOT_ALLOWED", "The HTTP method is not supported for this endpoint");
    }

    public static ApiException payloadTooLarge() {
        return new ApiException(413, "PAYLOAD_TOO_LARGE", "The request body is too large");
    }

    public static ApiException originNotAllowed() {
        return new ApiException(403, "ORIGIN_NOT_ALLOWED", "The request origin is not allowed");
    }
}
