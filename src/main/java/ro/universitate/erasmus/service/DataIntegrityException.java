package ro.universitate.erasmus.service;
import java.util.List;
public class DataIntegrityException extends RuntimeException {
    public DataIntegrityException(String message) { super(message); }
    public DataIntegrityException(List<String> violations) { super(String.join("; ", violations)); }
}
