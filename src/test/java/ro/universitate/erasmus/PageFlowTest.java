package ro.universitate.erasmus;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ro.universitate.erasmus.dto.ProfileRequest;
import ro.universitate.erasmus.entity.Semester;
import ro.universitate.erasmus.service.RecommendationService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class PageFlowTest {
    @Autowired MockMvc mvc;
    @Autowired RecommendationService service;

    @Test void formFiltersAndResetsDependentSelections() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(content().string(containsString("Universitatea Dunărea Nouă")))
            .andExpect(content().string(containsString("Date fictive; recomandări demonstrative")));
        var response = mvc.perform(get("/").param("universityId", "2").param("facultyId", "1").param("programId", "1"))
            .andExpect(status().isOk()).andReturn();
        String html = response.getResponse().getContentAsString();
        assertThat(html).contains("Universitatea Carpaților").contains("Economie");
        assertThat(html).doesNotContain("value=\"1\" selected=\"selected\"");
    }

    @Test void resultsAndComparisonShowRomanianNumbers() throws Exception {
        String html = mvc.perform(get("/recomandari")
                .param("universityId", "1").param("facultyId", "1").param("programId", "1")
                .param("studyYear", "2").param("academicYear", "2026-2027").param("semester", "FIRST"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("30/30 ECTS", "22/30 ECTS", "0/30 ECTS", "73,3%", "FĂRĂ ECHIVALĂRI");
        assertThat(html.indexOf("Universitatea Alpină")).isLessThan(html.indexOf("Universitatea Baltică"));
        assertThat(html.indexOf("Universitatea Baltică")).isLessThan(html.indexOf("Universitatea Centrală"));
        assertThat(html).doesNotContain("Universitatea Iberică");
        Long id = service.recommend(new ProfileRequest(1L, 1L, 1L, 2, "2026-2027", Semester.FIRST))
            .recommendations().get(1).agreementId();
        mvc.perform(get("/comparatie")
                .param("universityId", "1").param("facultyId", "1").param("programId", "1")
                .param("studyYear", "2").param("academicYear", "2026-2027").param("semester", "FIRST")
                .param("agreementId", id.toString()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("8 ECTS neacoperite")))
            .andExpect(content().string(containsString("Neacoperită")));
    }

    @Test void invalidIdsAndPeriodShowFormWithBadRequest() throws Exception {
        mvc.perform(get("/recomandari")
                .param("universityId", "999").param("facultyId", "1").param("programId", "1")
                .param("studyYear", "2").param("academicYear", "2026-2027").param("semester", "FIRST"))
            .andExpect(status().isBadRequest()).andExpect(content().string(containsString("Profil invalid")));
        mvc.perform(get("/recomandari")
                .param("universityId", "1").param("facultyId", "1").param("programId", "1")
                .param("studyYear", "2").param("academicYear", "2026-2027").param("semester", "THIRD"))
            .andExpect(status().isBadRequest()).andExpect(content().string(containsString("Profil invalid")));
    }

    @Test void unknownRouteHasNotFoundStatus() throws Exception {
        mvc.perform(get("/ruta-inexistenta")).andExpect(status().isNotFound());
        mvc.perform(get("/error").requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Pagina cerută nu există.")))
            .andExpect(content().string(containsString("Date fictive")));
    }
}
