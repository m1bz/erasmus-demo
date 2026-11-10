package ro.universitate.erasmus;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ro.universitate.erasmus.entity.*;

@DataJpaTest
@ActiveProfiles("test")
class DatabaseConstraintsTest {
    @Autowired EntityManager em;
    StudyProgram home;
    StudyProgram host;
    Course homeCourse;
    Course hostCourse;

    @BeforeEach void prepare() {
        University a = save(new University("Origine", "Galați", "România"));
        University b = save(new University("Gazdă", "Graz", "Austria"));
        home = save(new StudyProgram(save(new Faculty(a, "Informatică")), "Informatică"));
        host = save(new StudyProgram(save(new Faculty(b, "Informatică")), "Informatică"));
        homeCourse = save(course(home, "A", 5));
        hostCourse = save(course(host, "B", 5));
        em.flush();
    }

    @Test void duplicateCourseCode() {
        assertThatThrownBy(() -> { save(course(home, "A", 5)); em.flush(); }).isInstanceOf(Exception.class);
    }
    @Test void duplicateAgreement() {
        save(agreement(home, host));
        em.flush();
        assertThatThrownBy(() -> { save(agreement(home, host)); em.flush(); }).isInstanceOf(Exception.class);
    }
    @Test void reusedHostCourse() {
        ExchangeAgreement a = save(agreement(home, host));
        Course anotherHome = save(course(home, "C", 5));
        save(new CourseEquivalence(a, homeCourse, hostCourse, "Conținut comun."));
        em.flush();
        assertThatThrownBy(() -> {
            save(new CourseEquivalence(a, anotherHome, hostCourse, "Conținut comun.")); em.flush();
        }).isInstanceOf(Exception.class);
    }
    @Test void nonPositiveEcts() {
        assertThatThrownBy(() -> { save(course(home, "C", 0)); em.flush(); }).isInstanceOf(Exception.class);
    }
    @Test void requiredField() {
        assertThatThrownBy(() -> { save(new University(null, "Oraș", "România")); em.flush(); }).isInstanceOf(Exception.class);
    }

    private ExchangeAgreement agreement(StudyProgram a, StudyProgram b) {
        return new ExchangeAgreement(a, b, "2026-2027", Semester.FIRST, true);
    }
    private Course course(StudyProgram p, String code, int ects) {
        return new Course(p, code, "Materie", "Descriere", ects, 2, "2026-2027", Semester.FIRST);
    }
    private <T> T save(T object) { em.persist(object); return object; }
}
