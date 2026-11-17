package ro.universitate.erasmus.service;

public class ApiConflictException extends RuntimeException {
    public ApiConflictException(String message) {
        super(message);
    }
}
