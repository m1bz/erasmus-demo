package ro.universitate.erasmus.service;
public class InvalidProfileException extends RuntimeException {
    public InvalidProfileException() { super("Profil invalid: verifică universitatea, facultatea, programul și perioada."); }
}
