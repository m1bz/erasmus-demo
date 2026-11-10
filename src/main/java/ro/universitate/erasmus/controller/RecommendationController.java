package ro.universitate.erasmus.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ro.universitate.erasmus.dto.ProfileRequest;
import ro.universitate.erasmus.entity.Semester;
import ro.universitate.erasmus.service.*;

@Controller
public class RecommendationController {
    private final FormOptionsService forms;
    private final RecommendationService recommendations;
    public RecommendationController(FormOptionsService forms, RecommendationService recommendations) {
        this.forms = forms; this.recommendations = recommendations;
    }
    @GetMapping("/")
    public String form(@RequestParam(required = false) Long universityId,
                       @RequestParam(required = false) Long facultyId,
                       @RequestParam(required = false) Long programId,
                       @RequestParam(required = false) Integer studyYear,
                       @RequestParam(required = false) String academicYear,
                       @RequestParam(required = false) Semester semester, Model model) {
        model.addAttribute("form", forms.options(new ProfileRequest(
            universityId, facultyId, programId, studyYear, academicYear, semester)));
        return "form";
    }
    @GetMapping("/recomandari")
    public String results(@Valid @ModelAttribute ProfileRequest profile, BindingResult validation, Model model) {
        if (validation.hasErrors()) throw new InvalidProfileException();
        var result = recommendations.recommend(profile);
        model.addAttribute("profile", result.profile());
        model.addAttribute("recommendations", result.recommendations());
        return "results";
    }
    @GetMapping("/comparatie")
    public String comparison(@Valid @ModelAttribute ProfileRequest profile, BindingResult validation,
                             @RequestParam(required = false) Long agreementId, Model model) {
        if (validation.hasErrors() || agreementId == null) throw new InvalidProfileException();
        model.addAttribute("profile", profile);
        model.addAttribute("comparison", recommendations.compare(profile, agreementId));
        return "comparison";
    }
}
