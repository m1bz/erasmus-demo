package ro.universitate.erasmus.service;
public class NoDestinationsException extends RuntimeException {
    public NoDestinationsException() { super("Nu există destinații cu parteneriat activ pentru acest profil."); }
}
