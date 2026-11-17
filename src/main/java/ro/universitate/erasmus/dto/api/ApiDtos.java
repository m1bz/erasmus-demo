package ro.universitate.erasmus.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import ro.universitate.erasmus.dto.ProfileRequest;
import ro.universitate.erasmus.entity.Semester;
import ro.universitate.erasmus.entity.CourseEquivalence;
import ro.universitate.erasmus.entity.ExchangeAgreement;
import ro.universitate.erasmus.service.FormOptionsService;
import ro.universitate.erasmus.service.RecommendationService;

public final class ApiDtos {
    private ApiDtos() {}

    public record ProfileQuery(
        @NotNull Long universityId,
        @NotNull Long facultyId,
        @NotNull Long programId,
        @NotNull @Positive Integer studyYear,
        @NotBlank String academicYear,
        @NotNull Semester semester
    ) {
        public ProfileRequest toProfileRequest() {
            return new ProfileRequest(universityId, facultyId, programId, studyYear, academicYear, semester);
        }
    }

    public record OptionsQuery(Long universityId, Long facultyId, Long programId,
                               Integer studyYear, String academicYear, Semester semester) {
        public ProfileRequest toProfileRequest() {
            return new ProfileRequest(universityId, facultyId, programId, studyYear, academicYear, semester);
        }
    }

    public record HealthResponse(String status, String service) {}
    public record UniversityOption(Long id, String name, String city, String country) {}
    public record FacultyOption(Long id, String name, Long universityId) {}
    public record ProgramOption(Long id, String name, Long facultyId, String level) {}
    public record SemesterOption(String value, String label) {}
    public record OptionsResponse(List<UniversityOption> universities, List<FacultyOption> faculties,
                                  List<ProgramOption> programs, List<Integer> studyYears,
                                  List<String> academicYears, List<SemesterOption> semesters,
                                  Long universityId, Long facultyId, Long programId, Integer studyYear,
                                  String academicYear, Semester semester) {
        public static OptionsResponse from(FormOptionsService.Options options) {
            return new OptionsResponse(
                options.universities().stream().map(u -> new UniversityOption(u.id, u.name, u.city, u.country)).toList(),
                options.faculties().stream().map(f -> new FacultyOption(f.id, f.name, f.university.id)).toList(),
                options.programs().stream().map(p -> new ProgramOption(p.id, p.name, p.faculty.id, p.level.name())).toList(),
                options.studyYears(), options.academicYears(),
                options.semesters().stream().map(s -> new SemesterOption(s.name(), s.label)).toList(),
                options.universityId(), options.facultyId(), options.programId(), options.studyYear(),
                options.academicYear(), options.semester());
        }
    }

    public record RecommendationResponse(Long agreementId, String university, String faculty, String program,
                                         String country, String city, int coveredEcts, int totalEcts,
                                         BigDecimal percentage, String status) {
        public static RecommendationResponse from(RecommendationService.Recommendation value) {
            return new RecommendationResponse(value.agreementId(), value.university(), value.faculty(), value.program(),
                value.country(), value.city(), value.covered(), value.total(), value.percentage(), value.status());
        }
    }

    public record RecommendationsResponse(ProfileRequest profile, List<RecommendationResponse> recommendations) {
        public static RecommendationsResponse from(RecommendationService.Results results) {
            return new RecommendationsResponse(results.profile(),
                results.recommendations().stream().map(RecommendationResponse::from).toList());
        }
    }

    public record ComparisonRowResponse(String homeCourse, int homeEcts, String hostCourse, Integer hostEcts,
                                        String justification, boolean covered) {
        public static ComparisonRowResponse from(RecommendationService.ComparisonRow row) {
            return new ComparisonRowResponse(row.homeCourse(), row.homeEcts(), row.hostCourse(), row.hostEcts(),
                row.justification(), row.covered());
        }
    }

    public record ComparisonResponse(RecommendationResponse recommendation, List<ComparisonRowResponse> rows,
                                     int uncoveredEcts) {
        public static ComparisonResponse from(RecommendationService.Comparison comparison) {
            return new ComparisonResponse(RecommendationResponse.from(comparison.recommendation()),
                comparison.rows().stream().map(ComparisonRowResponse::from).toList(), comparison.uncovered());
        }
    }

    public record ErrorResponse(int status, String error, String message, String path) {}

    public record CreateAgreementRequest(@NotNull Long homeProgramId, @NotNull Long hostProgramId,
                                         @NotBlank String academicYear, @NotNull Semester semester,
                                         @NotNull Boolean active) {}

    public record AgreementResponse(Long id, Long homeProgramId, String homeProgram,
                                    Long hostProgramId, String hostProgram, String academicYear,
                                    Semester semester, boolean active) {
        public static AgreementResponse from(ExchangeAgreement agreement) {
            return new AgreementResponse(agreement.id, agreement.homeProgram.id, agreement.homeProgram.name,
                agreement.hostProgram.id, agreement.hostProgram.name, agreement.academicYear,
                agreement.semester, agreement.active);
        }
    }

    public record CreateEquivalenceRequest(@NotNull Long agreementId, @NotNull Long homeCourseId,
                                           @NotNull Long hostCourseId, @NotBlank @Size(max = 700) String justification) {}

    public record EquivalenceResponse(Long id, Long agreementId, Long homeCourseId, String homeCourse,
                                      int homeEcts, Long hostCourseId, String hostCourse, int hostEcts,
                                      String justification) {
        public static EquivalenceResponse from(CourseEquivalence equivalence) {
            return new EquivalenceResponse(equivalence.id, equivalence.agreement.id,
                equivalence.homeCourse.id, equivalence.homeCourse.name, equivalence.homeCourse.ects,
                equivalence.hostCourse.id, equivalence.hostCourse.name, equivalence.hostCourse.ects,
                equivalence.justification);
        }
    }
}
