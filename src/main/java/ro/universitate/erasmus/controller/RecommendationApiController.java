package ro.universitate.erasmus.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.universitate.erasmus.dto.api.ApiDtos;
import ro.universitate.erasmus.service.FormOptionsService;
import ro.universitate.erasmus.service.RecommendationService;
import ro.universitate.erasmus.service.CatalogWriteService;
import ro.universitate.erasmus.service.CatalogReadService;

@RestController
@RequestMapping("/api/v1")
public class RecommendationApiController {
    private final FormOptionsService forms;
    private final RecommendationService recommendations;
    private final CatalogWriteService catalogWrites;
    private final CatalogReadService catalogReads;

    public RecommendationApiController(FormOptionsService forms, RecommendationService recommendations,
                                       CatalogWriteService catalogWrites, CatalogReadService catalogReads) {
        this.forms = forms;
        this.recommendations = recommendations;
        this.catalogWrites = catalogWrites;
        this.catalogReads = catalogReads;
    }

    @GetMapping("/health")
    public ApiDtos.HealthResponse health() {
        return new ApiDtos.HealthResponse("UP", "erasmus-demo");
    }

    @GetMapping("/options")
    public ApiDtos.OptionsResponse options(@ModelAttribute ApiDtos.OptionsQuery query) {
        return ApiDtos.OptionsResponse.from(forms.options(query.toProfileRequest()));
    }

    @GetMapping("/universities")
    public List<ApiDtos.UniversityResponse> universities() {
        return catalogReads.universities().stream().map(ApiDtos.UniversityResponse::from).toList();
    }

    @GetMapping("/programs")
    public List<ApiDtos.ProgramResponse> programs(@ModelAttribute ApiDtos.ProgramsQuery query) {
        return catalogReads.programs(query.universityId(), query.facultyId()).stream()
            .map(ApiDtos.ProgramResponse::from).toList();
    }

    @GetMapping("/courses")
    public List<ApiDtos.CourseResponse> courses(@Valid @ModelAttribute ApiDtos.CoursesQuery query) {
        return catalogReads.courses(query.programId(), query.academicYear(), query.semester(), query.studyYear()).stream()
            .map(ApiDtos.CourseResponse::from).toList();
    }

    @GetMapping("/agreements")
    public List<ApiDtos.AgreementResponse> agreements(@ModelAttribute ApiDtos.AgreementsQuery query) {
        return catalogReads.agreements(query.homeProgramId(), query.academicYear(), query.semester()).stream()
            .map(ApiDtos.AgreementResponse::from).toList();
    }

    @GetMapping("/agreements/{agreementId}")
    public ApiDtos.AgreementResponse agreement(@PathVariable Long agreementId) {
        return ApiDtos.AgreementResponse.from(catalogReads.agreement(agreementId));
    }

    @PatchMapping(path = "/agreements/{agreementId}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiDtos.AgreementResponse setAgreementActive(@PathVariable Long agreementId,
            @Valid @RequestBody ApiDtos.AgreementStatusRequest request) {
        return ApiDtos.AgreementResponse.from(catalogWrites.setAgreementActive(agreementId, request.active()));
    }

    @GetMapping("/equivalences")
    public List<ApiDtos.EquivalenceResponse> equivalences(@Valid @ModelAttribute ApiDtos.EquivalencesQuery query) {
        return catalogReads.equivalences(query.agreementId()).stream().map(ApiDtos.EquivalenceResponse::from).toList();
    }

    @GetMapping("/equivalences/{equivalenceId}")
    public ApiDtos.EquivalenceResponse equivalence(@PathVariable Long equivalenceId) {
        return ApiDtos.EquivalenceResponse.from(catalogReads.equivalence(equivalenceId));
    }

    @PutMapping(path = "/equivalences/{equivalenceId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiDtos.EquivalenceResponse updateEquivalence(@PathVariable Long equivalenceId,
            @Valid @RequestBody ApiDtos.UpdateEquivalenceRequest request) {
        var updated = catalogWrites.updateEquivalence(equivalenceId, request.agreementId(), request.homeCourseId(),
            request.hostCourseId(), request.justification());
        return ApiDtos.EquivalenceResponse.from(updated);
    }

    @GetMapping("/recommendations")
    public ApiDtos.RecommendationsResponse recommendations(@Valid @ModelAttribute ApiDtos.ProfileQuery query) {
        return ApiDtos.RecommendationsResponse.from(recommendations.recommend(query.toProfileRequest()));
    }

    @GetMapping("/recommendations/{agreementId}/comparison")
    public ApiDtos.ComparisonResponse comparison(@PathVariable Long agreementId,
                                                  @Valid @ModelAttribute ApiDtos.ProfileQuery query) {
        return ApiDtos.ComparisonResponse.from(recommendations.compare(query.toProfileRequest(), agreementId));
    }

    @PostMapping(path = "/agreements", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiDtos.AgreementResponse> createAgreement(
            @Valid @RequestBody ApiDtos.CreateAgreementRequest request) {
        var agreement = catalogWrites.createAgreement(request);
        return ResponseEntity.created(URI.create("/api/v1/agreements/" + agreement.id))
            .body(ApiDtos.AgreementResponse.from(agreement));
    }

    @PostMapping(path = "/equivalences", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiDtos.EquivalenceResponse> createEquivalence(
            @Valid @RequestBody ApiDtos.CreateEquivalenceRequest request) {
        var equivalence = catalogWrites.createEquivalence(request);
        return ResponseEntity.created(URI.create("/api/v1/equivalences/" + equivalence.id))
            .body(ApiDtos.EquivalenceResponse.from(equivalence));
    }
}
