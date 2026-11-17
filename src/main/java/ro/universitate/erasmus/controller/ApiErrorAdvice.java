package ro.universitate.erasmus.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
                       MethodArgumentNotValidException.class, BindException.class,
                       MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiDtos.ErrorResponse invalid(Exception error, HttpServletRequest request) {
        String fields = error instanceof BindException binding
            ? binding.getBindingResult().getFieldErrors().stream().map(field -> field.getField()).distinct()
                .collect(java.util.stream.Collectors.joining(", "))
            : "";
        String message = fields.isEmpty() ? "Cerere invalidă: verifică valorile și parametrii trimiși."
            : "Cerere invalidă: verifică aceste câmpuri: " + fields + ".";
        return new ApiDtos.ErrorResponse(400, "CERERE_INVALIDĂ", message, request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiDtos.ErrorResponse unreadable(HttpServletRequest request) {
        return new ApiDtos.ErrorResponse(400, "JSON_INVALID",
            "Corpul JSON lipsește sau conține un câmp cu format invalid.", request.getRequestURI());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ApiDtos.ErrorResponse unsupportedMedia(HttpServletRequest request) {
        return new ApiDtos.ErrorResponse(415, "FORMAT_NEACCEPTAT",
            "Trimite corpul cererii în format JSON cu Content-Type: application/json.", request.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiDtos.ErrorResponse unsupportedMethod(HttpServletRequest request) {
        return new ApiDtos.ErrorResponse(405, "METODĂ_NEACCEPTATĂ",
            "Metoda HTTP nu este disponibilă pentru această adresă API.", request.getRequestURI());
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiDtos.ErrorResponse concurrentConflict(HttpServletRequest request) {
        return new ApiDtos.ErrorResponse(409, "CONFLICT_DE_DATE",
            "Modificarea intră în conflict cu o regulă de unicitate sau integritate.", request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiDtos.ErrorResponse inconsistent(DataIntegrityException error, HttpServletRequest request) {
        return new ApiDtos.ErrorResponse(500, "DATE_INCONSISTENTE",
            "Datele demonstrative nu respectă regulile de integritate.", request.getRequestURI());
    }
}
