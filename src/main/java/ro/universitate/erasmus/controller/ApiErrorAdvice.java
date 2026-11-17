package ro.universitate.erasmus.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ro.universitate.erasmus.dto.api.ApiDtos;
import ro.universitate.erasmus.service.DataIntegrityException;
import ro.universitate.erasmus.service.ApiConflictException;
import ro.universitate.erasmus.service.ApiNotFoundException;
import ro.universitate.erasmus.service.InvalidProfileException;
import ro.universitate.erasmus.service.MissingCurriculumException;
import ro.universitate.erasmus.service.NoDestinationsException;

@RestControllerAdvice(assignableTypes = RecommendationApiController.class)
public class ApiErrorAdvice {
    @ExceptionHandler({InvalidProfileException.class, MethodArgumentTypeMismatchException.class,
                       MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiDtos.ErrorResponse invalid(Exception error, HttpServletRequest request) {
        return new ApiDtos.ErrorResponse(400, "CERERE_INVALIDĂ",
            "Profil invalid: verifică universitatea, facultatea, programul și perioada.", request.getRequestURI());
    }

    @ExceptionHandler({MissingCurriculumException.class, NoDestinationsException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiDtos.ErrorResponse missing(RuntimeException error, HttpServletRequest request) {
        return new ApiDtos.ErrorResponse(404, "DATE_INEXISTENTE", error.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ApiNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiDtos.ErrorResponse referenceMissing(ApiNotFoundException error, HttpServletRequest request) {
        return new ApiDtos.ErrorResponse(404, "RESURSĂ_INEXISTENTĂ", error.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ApiConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiDtos.ErrorResponse conflict(ApiConflictException error, HttpServletRequest request) {
        return new ApiDtos.ErrorResponse(409, "CONFLICT_DE_DATE", error.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiDtos.ErrorResponse inconsistent(DataIntegrityException error, HttpServletRequest request) {
        return new ApiDtos.ErrorResponse(500, "DATE_INCONSISTENTE",
            "Datele demonstrative nu respectă regulile de integritate.", request.getRequestURI());
    }
}
