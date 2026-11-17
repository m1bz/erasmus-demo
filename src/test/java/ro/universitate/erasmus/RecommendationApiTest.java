package ro.universitate.erasmus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
@Transactional
class RecommendationApiTest {
    private static final String YEAR = "2026-2027";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test void catalogIdsSupportCreateRecommendCorrectAndDeactivateFlow() throws Exception {
        JsonNode universities = json(mvc.perform(get("/api/v1/universities"))
            .andExpect(status().isOk()).andReturn());
        long homeUniversityId = universityId(universities, "Universitatea Carpaților");
        long hostUniversityId = universityId(universities, "Universitatea Centrală");
        JsonNode homeProgram = program(homeUniversityId, "Informatică");
        JsonNode hostProgram = program(hostUniversityId, "Informatică");
        long homeProgramId = homeProgram.path("id").asLong();
        long homeFacultyId = homeProgram.path("facultyId").asLong();
        long hostProgramId = hostProgram.path("id").asLong();
        JsonNode homeCourses = courses(homeProgramId, "FIRST", 2);
        JsonNode hostCourses = courses(hostProgramId, "FIRST", 2);
        CoursePair initial = matchingPair(homeCourses, hostCourses, -1, -1);

        MvcResult created = mvc.perform(post("/api/v1/agreements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"homeProgramId":%d,"hostProgramId":%d,"academicYear":"%s",
                     "semester":"FIRST","active":true}
                    """.formatted(homeProgramId, hostProgramId, YEAR)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.active").value(true))
            .andReturn();
        long agreementId = json(created).path("id").asLong();
        String agreementLocation = created.getResponse().getHeader("Location");
        assertThat(agreementLocation).isEqualTo("/api/v1/agreements/" + agreementId);
        mvc.perform(get(agreementLocation)).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(agreementId));
        mvc.perform(post("/api/v1/agreements").contentType(MediaType.APPLICATION_JSON)
                .content(agreementBody(homeProgramId, hostProgramId, true)))
            .andExpect(status().isConflict());

        MvcResult createdEquivalence = mvc.perform(post("/api/v1/equivalences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(equivalenceBody(agreementId, initial)))
            .andExpect(status().isCreated()).andReturn();
        JsonNode equivalence = json(createdEquivalence);
        long equivalenceId = equivalence.path("id").asLong();
        String equivalenceLocation = createdEquivalence.getResponse().getHeader("Location");
        mvc.perform(get(equivalenceLocation)).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(equivalenceId));
        mvc.perform(get("/api/v1/equivalences").param("agreementId", Long.toString(agreementId)))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(equivalenceId));
        mvc.perform(post("/api/v1/equivalences").contentType(MediaType.APPLICATION_JSON)
                .content(equivalenceBody(agreementId, initial)))
            .andExpect(status().isConflict());

        JsonNode recommendations = recommendations(homeUniversityId, homeFacultyId, homeProgramId);
        JsonNode recommendation = byId(recommendations.path("recommendations"), "agreementId", agreementId);
        assertThat(recommendation.path("coveredEcts").asInt()).isEqualTo(initial.home().path("ects").asInt());
        JsonNode comparison = comparison(agreementId, homeUniversityId, homeFacultyId, homeProgramId);
        JsonNode initialRow = rowByCourse(comparison.path("rows"), initial.home().path("name").asText());
        assertThat(initialRow.path("hostCourse").asText()).isEqualTo(initial.host().path("name").asText());

        CoursePair corrected = matchingPair(homeCourses, hostCourses,
            initial.home().path("id").asLong(), initial.host().path("id").asLong());
        mvc.perform(put("/api/v1/equivalences/{id}", equivalenceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody(agreementId, corrected)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.homeCourseId").value(corrected.home().path("id").asLong()))
            .andExpect(jsonPath("$.hostCourseId").value(corrected.host().path("id").asLong()));
        JsonNode correctedRecommendations = recommendations(homeUniversityId, homeFacultyId, homeProgramId);
        JsonNode correctedRecommendation = byId(correctedRecommendations.path("recommendations"), "agreementId", agreementId);
        assertThat(correctedRecommendation.path("coveredEcts").asInt())
            .isEqualTo(corrected.home().path("ects").asInt());
        JsonNode correctedComparison = comparison(agreementId, homeUniversityId, homeFacultyId, homeProgramId);
        JsonNode correctedRow = rowByCourse(correctedComparison.path("rows"), corrected.home().path("name").asText());
        assertThat(correctedRow.path("hostCourse").asText()).isEqualTo(corrected.host().path("name").asText());

        JsonNode mismatchedHost = courseWithDifferentEcts(hostCourses, corrected.home().path("ects").asInt());
        mvc.perform(put("/api/v1/equivalences/{id}", equivalenceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody(agreementId, new CoursePair(corrected.home(), mismatchedHost))))
            .andExpect(status().isConflict());
        mvc.perform(get(equivalenceLocation)).andExpect(status().isOk())
            .andExpect(jsonPath("$.homeCourseId").value(corrected.home().path("id").asLong()))
            .andExpect(jsonPath("$.hostCourseId").value(corrected.host().path("id").asLong()));

        mvc.perform(patch("/api/v1/agreements/{id}/status", agreementId)
                .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        JsonNode afterDeactivation = recommendations(homeUniversityId, homeFacultyId, homeProgramId);
        assertThat(byIdOrNull(afterDeactivation.path("recommendations"), "agreementId", agreementId)).isNull();
        mvc.perform(patch("/api/v1/agreements/{id}/status", agreementId)
                .contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));
        JsonNode afterReactivation = recommendations(homeUniversityId, homeFacultyId, homeProgramId);
        assertThat(byId(afterReactivation.path("recommendations"), "agreementId", agreementId)).isNotNull();
    }

    @Test void agreementAndEquivalenceRulesRejectInvalidReferencesAndMappings() throws Exception {
        JsonNode universities = json(mvc.perform(get("/api/v1/universities"))
            .andExpect(status().isOk()).andReturn());
        long homeUniversityId = universityId(universities, "Universitatea Dunărea Nouă");
        long hostUniversityId = universityId(universities, "Universitatea Carpaților");
        JsonNode homeProgram = program(homeUniversityId, "Informatică");
        JsonNode sameUniversityProgram = program(homeUniversityId, "Economie");
        JsonNode hostProgram = program(hostUniversityId, "Informatică");
        long homeProgramId = homeProgram.path("id").asLong();
        long hostProgramId = hostProgram.path("id").asLong();

        mvc.perform(post("/api/v1/agreements").contentType(MediaType.APPLICATION_JSON)
                .content(agreementBody(999999, hostProgramId, true)))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("RESURSĂ_INEXISTENTĂ"));
        mvc.perform(post("/api/v1/agreements").contentType(MediaType.APPLICATION_JSON)
                .content(agreementBody(homeProgramId, sameUniversityProgram.path("id").asLong(), true)))
            .andExpect(status().isConflict());

        MvcResult agreement = mvc.perform(post("/api/v1/agreements").contentType(MediaType.APPLICATION_JSON)
                .content(agreementBody(homeProgramId, hostProgramId, true)))
            .andExpect(status().isCreated()).andReturn();
        long agreementId = json(agreement).path("id").asLong();

        JsonNode homeFirst = courses(homeProgramId, "FIRST", 2);
        JsonNode hostFirst = courses(hostProgramId, "FIRST", 2);
        CoursePair valid = matchingPair(homeFirst, hostFirst, -1, -1);
        long differentProgramId = sameUniversityProgram.path("id").asLong();
        JsonNode wrongProgramCourses = courses(differentProgramId, "FIRST", 2);
        CoursePair wrongProgram = new CoursePair(wrongProgramCourses.get(0), hostFirst.get(0));
        mvc.perform(post("/api/v1/equivalences").contentType(MediaType.APPLICATION_JSON)
                .content(equivalenceBody(agreementId, wrongProgram)))
            .andExpect(status().isConflict());

        JsonNode homeSecond = courses(homeProgramId, "SECOND", 2);
        CoursePair wrongPeriod = new CoursePair(homeSecond.get(0), hostFirst.get(0));
        mvc.perform(post("/api/v1/equivalences").contentType(MediaType.APPLICATION_JSON)
                .content(equivalenceBody(agreementId, wrongPeriod)))
            .andExpect(status().isConflict());

        JsonNode mismatchedHost = courseWithDifferentEcts(hostFirst, valid.home().path("ects").asInt());
        CoursePair wrongCredits = new CoursePair(valid.home(), mismatchedHost);
        mvc.perform(post("/api/v1/equivalences").contentType(MediaType.APPLICATION_JSON)
                .content(equivalenceBody(agreementId, wrongCredits)))
            .andExpect(status().isConflict());
        mvc.perform(get("/api/v1/equivalences").param("agreementId", Long.toString(agreementId)))
            .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    private JsonNode recommendations(long universityId, long facultyId, long programId) throws Exception {
        return json(mvc.perform(get("/api/v1/recommendations")
                .param("universityId", Long.toString(universityId))
                .param("facultyId", Long.toString(facultyId))
                .param("programId", Long.toString(programId))
                .param("studyYear", "2").param("academicYear", YEAR).param("semester", "FIRST"))
            .andExpect(status().isOk()).andReturn());
    }

    private JsonNode comparison(long agreementId, long universityId, long facultyId, long programId) throws Exception {
        return json(mvc.perform(get("/api/v1/recommendations/{id}/comparison", agreementId)
                .param("universityId", Long.toString(universityId))
                .param("facultyId", Long.toString(facultyId))
                .param("programId", Long.toString(programId))
                .param("studyYear", "2").param("academicYear", YEAR).param("semester", "FIRST"))
            .andExpect(status().isOk()).andReturn());
    }

    private JsonNode courses(long programId, String semester, int studyYear) throws Exception {
        return json(mvc.perform(get("/api/v1/courses")
                .param("programId", Long.toString(programId)).param("academicYear", YEAR)
                .param("semester", semester).param("studyYear", Integer.toString(studyYear)))
            .andExpect(status().isOk()).andReturn());
    }

    private JsonNode program(long universityId, String name) throws Exception {
        JsonNode programs = json(mvc.perform(get("/api/v1/programs")
                .param("universityId", Long.toString(universityId)))
            .andExpect(status().isOk()).andReturn());
        for (JsonNode item : programs) {
            if (name.equals(item.path("name").asText())) return item;
        }
        throw new AssertionError("Programul nu a fost găsit: " + name);
    }

    private long universityId(JsonNode universities, String name) {
        for (JsonNode item : universities) {
            if (name.equals(item.path("name").asText())) return item.path("id").asLong();
        }
        throw new AssertionError("Universitatea nu a fost găsită: " + name);
    }

    private CoursePair matchingPair(JsonNode homeCourses, JsonNode hostCourses,
                                    long excludedHomeId, long excludedHostId) {
        for (JsonNode home : homeCourses) {
            if (home.path("id").asLong() == excludedHomeId) continue;
            for (JsonNode host : hostCourses) {
                if (host.path("id").asLong() == excludedHostId) continue;
                if (home.path("ects").asInt() == host.path("ects").asInt()) return new CoursePair(home, host);
            }
        }
        throw new AssertionError("Nu există o pereche de cursuri cu ECTS egale.");
    }

    private JsonNode courseWithDifferentEcts(JsonNode courses, int ects) {
        for (JsonNode course : courses) {
            if (course.path("ects").asInt() != ects) return course;
        }
        throw new AssertionError("Nu există un curs gazdă cu alt număr de ECTS.");
    }

    private JsonNode byId(JsonNode items, String field, long id) {
        JsonNode found = byIdOrNull(items, field, id);
        if (found == null) throw new AssertionError("Resursa cu ID-ul " + id + " nu a fost găsită.");
        return found;
    }

    private JsonNode byIdOrNull(JsonNode items, String field, long id) {
        for (JsonNode item : items) {
            if (item.path(field).asLong(-1) == id) return item;
        }
        return null;
    }

    private JsonNode rowByCourse(JsonNode rows, String courseName) {
        for (JsonNode row : rows) {
            if (courseName.equals(row.path("homeCourse").asText())) return row;
        }
        throw new AssertionError("Materie fără comparație: " + courseName);
    }

    private String agreementBody(long homeProgramId, long hostProgramId, boolean active) {
        return """
            {"homeProgramId":%d,"hostProgramId":%d,"academicYear":"%s",
             "semester":"FIRST","active":%s}
            """.formatted(homeProgramId, hostProgramId, YEAR, active);
    }

    private String equivalenceBody(long agreementId, CoursePair pair) {
        return """
            {"agreementId":%d,"homeCourseId":%d,"hostCourseId":%d,
             "justification":"Conținut și rezultate de învățare comparabile."}
            """.formatted(agreementId, pair.home().path("id").asLong(), pair.host().path("id").asLong());
    }

    private String updateBody(long agreementId, CoursePair pair) {
        return """
            {"agreementId":%d,"homeCourseId":%d,"hostCourseId":%d,
             "justification":"Mapare corectată după verificarea planurilor de învățământ."}
            """.formatted(agreementId, pair.home().path("id").asLong(), pair.host().path("id").asLong());
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private record CoursePair(JsonNode home, JsonNode host) {}
}
