package ro.universitate.erasmus.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.universitate.erasmus.dto.api.ApiDtos;
import ro.universitate.erasmus.service.FormOptionsService;
import ro.universitate.erasmus.service.RecommendationService;
import ro.universitate.erasmus.service.CatalogWriteService;

@RestController
@RequestMapping("/api/v1")
public class RecommendationApiController {
    private final FormOptionsService forms;
    private final RecommendationService recommendations;
    private final CatalogWriteService catalogWrites;

    public RecommendationApiController(FormOptionsService forms, RecommendationService recommendations,
                                       CatalogWriteService catalogWrites) {
        this.forms = forms;
        this.recommendations = recommendations;
        this.catalogWrites = catalogWrites;
    }

    @GetMapping("/health")
    public ApiDtos.HealthResponse health() {
        return new ApiDtos.HealthResponse("UP", "erasmus-demo");
    }

    @GetMapping("/options")
    public ApiDtos.OptionsResponse options(@ModelAttribute ApiDtos.OptionsQuery query) {
        return ApiDtos.OptionsResponse.from(forms.options(query.toProfileRequest()));
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
