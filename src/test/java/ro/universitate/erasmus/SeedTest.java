package ro.universitate.erasmus;

import static org.assertj.core.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ro.universitate.erasmus.entity.*;
import ro.universitate.erasmus.repository.*;
import ro.universitate.erasmus.seed.DataIntegrityValidator;

@SpringBootTest
@ActiveProfiles("demo")
class SeedTest {
    @Autowired UniversityRepository universities;
    @Autowired StudyProgramRepository programs;
    @Autowired FacultyRepository faculties;
    @Autowired CourseRepository courses;
    @Autowired ExchangeAgreementRepository agreements;
    @Autowired DataIntegrityValidator validator;

    @Test void fixedStructureAndCurricula() {
        assertThat(universities.count()).isEqualTo(6);
        assertThat(faculties.count()).isEqualTo(12);
        assertThat(programs.count()).isEqualTo(12);
        for (StudyProgram p : programs.findAll()) for (Semester term : Semester.values()) {
            List<Course> list = courses.findByProgramIdAndStudyYearAndAcademicYearAndSemesterOrderByCode(
                p.id, 2, "2026-2027", term);
            assertThat(list).hasSize(6);
            assertThat(list.stream().mapToInt(c -> c.ects).sum()).isEqualTo(30);
        }
        validator.validate();
    }

    @Test void demoHasEightOriginProfiles() {
        long romanian = universities.findAll().stream().filter(u -> u.country.equals("România")).count();
        assertThat(romanian).isEqualTo(2);
        assertThat(agreements.count()).isGreaterThanOrEqualTo(20);
    }
}
