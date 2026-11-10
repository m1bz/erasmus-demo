package ro.universitate.erasmus;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.universitate.erasmus.entity.*;
import ro.universitate.erasmus.repository.*;
import ro.universitate.erasmus.seed.DataIntegrityValidator;
import ro.universitate.erasmus.service.DataIntegrityException;

class ValidatorTest {
    CourseRepository courses = mock(CourseRepository.class);
    ExchangeAgreementRepository agreements = mock(ExchangeAgreementRepository.class);
    CourseEquivalenceRepository equivalences = mock(CourseEquivalenceRepository.class);
    DataIntegrityValidator validator = new DataIntegrityValidator(courses, agreements, equivalences);
    University origin = new University("Origine", "Galați", "România");
    University destination = new University("Gazdă", "Graz", "Austria");
    StudyProgram home;
    StudyProgram host;
    Course a;
    Course b;
    ExchangeAgreement agreement;

    @BeforeEach void prepare() {
        origin.id = 1L; destination.id = 2L;
        home = new StudyProgram(new Faculty(origin, "Informatică"), "Informatică"); home.id = 1L;
        host = new StudyProgram(new Faculty(destination, "Informatică"), "Informatică"); host.id = 2L;
        a = course(home, 1L, 30); b = course(host, 2L, 30);
        agreement = new ExchangeAgreement(home, host, "2026-2027", Semester.FIRST, true); agreement.id = 1L;
        when(courses.findAll()).thenReturn(List.of(a, b));
        when(agreements.findAll()).thenReturn(List.of(agreement));
        when(equivalences.findByAgreementId(1L)).thenReturn(List.of(pair(a, b)));
    }
    @Test void wrongHomeProgram() {
        Course other = course(host, 3L, 30);
        when(equivalences.findByAgreementId(1L)).thenReturn(List.of(pair(other, b)));
        assertViolation("Program de origine greșit");
    }
    @Test void wrongHostProgram() {
        when(equivalences.findByAgreementId(1L)).thenReturn(List.of(pair(a, a)));
        assertViolation("Program gazdă greșit");
    }
    @Test void wrongPeriod() {
        b.semester = Semester.SECOND;
        assertViolation("Perioadă greșită");
    }
    @Test void differentCredits() {
        b.ects = 29;
        assertViolation("ECTS diferite");
    }
    @Test void sameUniversity() {
        destination.id = origin.id;
        assertViolation("Universitatea gazdă coincide");
    }
    @Test void wrongCurriculumTotal() {
        a.ects = 29;
        assertViolation("Curriculum cu 29 ECTS");
    }
    @Test void duplicateCourseUse() {
        when(equivalences.findByAgreementId(1L)).thenReturn(List.of(pair(a,b), pair(a,b)));
        assertViolation("folosit de două ori");
    }
    private void assertViolation(String fragment) {
        assertThatThrownBy(validator::validate).isInstanceOf(DataIntegrityException.class).hasMessageContaining(fragment);
    }
    private CourseEquivalence pair(Course x, Course y) {
        return new CourseEquivalence(agreement, x, y, "Competențe comune.");
    }
    private Course course(StudyProgram p, Long id, int ects) {
        Course c = new Course(p, "C" + id, "Curs", "Descriere", ects, 2, "2026-2027", Semester.FIRST);
        c.id = id; return c;
    }
}
