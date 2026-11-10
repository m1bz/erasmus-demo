package ro.universitate.erasmus;

import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ro.universitate.erasmus.dto.ProfileRequest;
import ro.universitate.erasmus.entity.*;
import ro.universitate.erasmus.repository.*;
import ro.universitate.erasmus.service.RecommendationService;

@SpringBootTest
@ActiveProfiles("demo")
class DemoOracleTest {
    @Autowired RecommendationService service;
    @Autowired UniversityRepository universities;
    @Autowired FacultyRepository faculties;
    @Autowired StudyProgramRepository programs;
    @Autowired ExchangeAgreementRepository agreements;
    @Autowired CourseEquivalenceRepository equivalences;

    @Test void profileA() {
        var results = service.recommend(profile("Universitatea Dunărea Nouă", "Informatică", Semester.FIRST));
        assertThat(results.recommendations()).extracting(RecommendationService.Recommendation::university)
            .containsExactly("Universitatea Alpină", "Universitatea Baltică", "Universitatea Centrală");
        assertThat(results.recommendations()).extracting(RecommendationService.Recommendation::covered)
            .containsExactly(30, 22, 0);
        assertThat(results.recommendations()).extracting(RecommendationService.Recommendation::total)
            .containsExactly(30, 30, 30);
        assertThat(results.recommendations()).extracting(RecommendationService.Recommendation::status)
            .containsExactly("COMPLETĂ", "PARȚIALĂ", "FĂRĂ ECHIVALĂRI");
        assertThat(results.recommendations()).extracting(RecommendationService.Recommendation::percentage)
            .containsExactly(new BigDecimal("100.0"), new BigDecimal("73.3"), new BigDecimal("0.0"));
        var comparison = service.compare(results.profile(), results.recommendations().get(1).agreementId());
        assertThat(comparison.uncovered()).isEqualTo(8);
        assertThat(comparison.rows().stream().filter(r -> !r.covered()).mapToInt(r -> r.homeEcts()).sum()).isEqualTo(8);
        assertThat(comparison.rows().stream().filter(r -> !r.covered()).map(r -> r.homeCourse()).toList())
            .containsExactly("Proiectare software", "Dezvoltare web");
        var inactive = agreements.findAll().stream().filter(a -> !a.active && a.homeProgram.id.equals(results.profile().programId())
            && a.semester == Semester.FIRST).findFirst().orElseThrow();
        assertThat(inactive.hostProgram.faculty.university.name).isEqualTo("Universitatea Iberică");
        assertThat(equivalences.findByAgreementId(inactive.id)).hasSize(6);
    }

    @Test void profileB() {
        var results = service.recommend(profile("Universitatea Carpaților", "Economie", Semester.SECOND));
        assertThat(results.recommendations()).extracting(RecommendationService.Recommendation::university)
            .containsExactly("Universitatea Alpină", "Universitatea Baltică",
                             "Universitatea Centrală", "Universitatea Iberică");
        assertThat(results.recommendations()).extracting(RecommendationService.Recommendation::covered)
            .containsExactly(30, 26, 26, 17);
        assertThat(results.recommendations()).extracting(RecommendationService.Recommendation::status)
            .containsExactly("COMPLETĂ", "PARȚIALĂ", "PARȚIALĂ", "PARȚIALĂ");
        assertThat(results.recommendations()).extracting(RecommendationService.Recommendation::percentage)
            .containsExactly(new BigDecimal("100.0"), new BigDecimal("86.7"),
                             new BigDecimal("86.7"), new BigDecimal("56.7"));
    }

    @Test void everyOriginProfileHasCompleteAndPartialDestination() {
        for (String name : new String[]{"Universitatea Dunărea Nouă", "Universitatea Carpaților"})
            for (String faculty : new String[]{"Informatică", "Economie"})
                for (Semester semester : Semester.values()) {
                    var found = service.recommend(profile(name, faculty, semester)).recommendations();
                    assertThat(found).anyMatch(r -> r.status().equals("COMPLETĂ"));
                    assertThat(found).anyMatch(r -> r.status().equals("PARȚIALĂ"));
                }
    }
    @Test void otherSemesterAgreementDoesNotEnterProfileA() {
        var second = service.recommend(profile("Universitatea Dunărea Nouă", "Informatică", Semester.SECOND));
        assertThat(second.recommendations()).anyMatch(r -> r.university().equals("Universitatea Iberică"));
        var first = service.recommend(profile("Universitatea Dunărea Nouă", "Informatică", Semester.FIRST));
        assertThat(first.recommendations()).noneMatch(r -> r.university().equals("Universitatea Iberică"));
    }
    private ProfileRequest profile(String universityName, String facultyName, Semester semester) {
        University u = universities.findAll().stream().filter(x -> x.name.equals(universityName)).findFirst().orElseThrow();
        Faculty f = faculties.findByUniversityIdOrderByName(u.id).stream()
            .filter(x -> x.name.equals(facultyName)).findFirst().orElseThrow();
        StudyProgram p = programs.findByFacultyIdOrderByName(f.id).getFirst();
        return new ProfileRequest(u.id, f.id, p.id, 2, "2026-2027", semester);
    }
}
