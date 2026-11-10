package ro.universitate.erasmus.service;
public class MissingCurriculumException extends RuntimeException {
    public MissingCurriculumException() { super("Nu există curriculum pentru profilul selectat."); }
}
