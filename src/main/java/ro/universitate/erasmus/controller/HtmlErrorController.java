package ro.universitate.erasmus.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import ro.universitate.erasmus.dto.api.ApiDtos;

@Controller
public class HtmlErrorController implements ErrorController {
    @RequestMapping("/error")
    public Object error(HttpServletRequest request, HttpServletResponse response, Model model) {
        Object value = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = value instanceof Integer code ? code : 500;
        response.setStatus(status);
        Object pathValue = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String path = pathValue instanceof String uri ? uri : request.getRequestURI();
        String apiPrefix = request.getContextPath() + "/api/v1";
        if (path.equals(apiPrefix) || path.startsWith(apiPrefix + "/")) {
            String code = status == 404 ? "RESURSĂ_INEXISTENTĂ"
                : status == 405 ? "METODĂ_NEACCEPTATĂ" : "CERERE_NEREUȘITĂ";
            String message = status == 404 ? "Adresa API cerută nu există."
                : status == 405 ? "Metoda HTTP nu este disponibilă pentru această adresă API."
                : "Cererea API nu a putut fi procesată.";
            return ResponseEntity.status(status).body(new ApiDtos.ErrorResponse(status, code, message, path));
        }
        model.addAttribute("message", status == 404 ? "Pagina cerută nu există."
            : "A apărut o eroare la afișarea paginii.");
        return "error";
    }
}
