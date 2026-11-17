package ro.universitate.erasmus;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ro.universitate.erasmus.dto.ProfileRequest;
import ro.universitate.erasmus.entity.Semester;
import ro.universitate.erasmus.repository.CourseRepository;
import ro.universitate.erasmus.service.RecommendationService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class RecommendationApiTest {
    @Autowired MockMvc mvc;
    @Autowired RecommendationService recommendations;
    @Autowired CourseRepository courses;

    @Test void healthAndOptionsAreAvailableAsJson() throws Exception {
        mvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("erasmus-demo"));

        mvc.perform(get("/api/v1/options"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.universities[0].name").exists())
            .andExpect(jsonPath("$.semesters").isArray());
    }

    @Test void recommendationsReturnDtoFieldsAndComparisonRows() throws Exception {
        var profile = profile();
        var results = recommendations.recommend(profile);
        Long agreementId = results.recommendations().get(1).agreementId();

        mvc.perform(get("/api/v1/recommendations")
                .param("universityId", "1").param("facultyId", "1").param("programId", "1")
                .param("studyYear", "2").param("academicYear", "2026-2027").param("semester", "FIRST"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recommendations[0].university").value("Universitatea Alpină"))
            .andExpect(jsonPath("$.recommendations[0].coveredEcts").value(30))
            .andExpect(jsonPath("$.recommendations[1].percentage").value(73.3));

        mvc.perform(get("/api/v1/recommendations/{agreementId}/comparison", agreementId)
                .param("universityId", "1").param("facultyId", "1").param("programId", "1")
                .param("studyYear", "2").param("academicYear", "2026-2027").param("semester", "FIRST"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uncoveredEcts").value(8))
            .andExpect(jsonPath("$.rows[0].homeCourse").exists());
    }

    @Test void invalidProfileAndPeriodReturnConsistentJsonErrors() throws Exception {
        mvc.perform(get("/api/v1/recommendations")
                .param("universityId", "999").param("facultyId", "1").param("programId", "1")
                .param("studyYear", "2").param("academicYear", "2026-2027").param("semester", "FIRST"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("CERERE_INVALIDĂ"))
            .andExpect(jsonPath("$.path").value("/api/v1/recommendations"));

        mvc.perform(get("/api/v1/recommendations")
                .param("universityId", "1").param("facultyId", "1").param("programId", "1")
                .param("studyYear", "2").param("academicYear", "2026-2027").param("semester", "THIRD"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Profil invalid")));
    }

    @Test void postCreatesAgreementAndThenItsEquivalence() throws Exception {
        // The POST body is JSON and the response is a location-addressable DTO.
        var created = mvc.perform(post("/api/v1/agreements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"homeProgramId":1,"hostProgramId":6,"academicYear":"2026-2027",
                     "semester":"FIRST","active":false}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.homeProgramId").value(1))
            .andExpect(jsonPath("$.hostProgramId").value(6))
            .andReturn();
        long agreementId = Long.parseLong(created.getResponse().getContentAsString()
            .replaceAll(".*\\\"id\\\":(\\d+).*", "$1"));

        var home = courses.findByProgramIdAndStudyYearAndAcademicYearAndSemesterOrderByCode(
            1L, 2, "2026-2027", Semester.FIRST).getFirst();
        var host = courses.findByProgramIdAndStudyYearAndAcademicYearAndSemesterOrderByCode(
            6L, 2, "2026-2027", Semester.FIRST).getFirst();
        String equivalenceJson = """
            {"agreementId":%d,"homeCourseId":%d,"hostCourseId":%d,
             "justification":"Conținut și rezultate de învățare comparabile."}
            """.formatted(agreementId, home.id, host.id);

        mvc.perform(post("/api/v1/equivalences")
                .contentType(MediaType.APPLICATION_JSON).content(equivalenceJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.agreementId").value(agreementId))
            .andExpect(jsonPath("$.homeEcts").value(home.ects))
            .andExpect(jsonPath("$.hostEcts").value(host.ects));

        mvc.perform(post("/api/v1/equivalences")
                .contentType(MediaType.APPLICATION_JSON).content(equivalenceJson))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("CONFLICT_DE_DATE"));
    }

    @Test void postRejectsInvalidAndDuplicateAgreements() throws Exception {
        mvc.perform(post("/api/v1/agreements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"homeProgramId\":1,\"academicYear\":\"2026-2027\",\"semester\":\"FIRST\",\"active\":true}"))
            .andExpect(status().isBadRequest());

        mvc.perform(post("/api/v1/agreements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"homeProgramId":1,"hostProgramId":5,"academicYear":"2026-2027",
                     "semester":"FIRST","active":true}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    private ProfileRequest profile() {
        return new ProfileRequest(1L, 1L, 1L, 2, "2026-2027", Semester.FIRST);
    }
}
