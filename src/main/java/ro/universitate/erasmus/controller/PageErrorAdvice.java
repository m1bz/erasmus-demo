package ro.universitate.erasmus.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import ro.universitate.erasmus.service.*;

@ControllerAdvice
public class PageErrorAdvice {
    private final FormOptionsService forms;
    public PageErrorAdvice(FormOptionsService forms) { this.forms = forms; }

    @ExceptionHandler({InvalidProfileException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalid(Exception error, Model model) {
        model.addAttribute("form", forms.options(null));
        model.addAttribute("message", "Profil invalid: verifică universitatea, facultatea, programul și perioada.");
        return "form";
    }
    @ExceptionHandler({MissingCurriculumException.class, NoDestinationsException.class})
    public String noData(RuntimeException error, Model model) {
        model.addAttribute("form", forms.options(null));
        model.addAttribute("message", error.getMessage());
        return "form";
    }
    @ExceptionHandler(DataIntegrityException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String corrupt(DataIntegrityException error, Model model) {
        model.addAttribute("message", "Date inconsistente: " + error.getMessage());
        return "error";
    }
}
