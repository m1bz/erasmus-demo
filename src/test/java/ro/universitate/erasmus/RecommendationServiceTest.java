package ro.universitate.erasmus;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.universitate.erasmus.dto.ProfileRequest;
import ro.universitate.erasmus.entity.*;
import ro.universitate.erasmus.repository.*;
import ro.universitate.erasmus.service.*;

class RecommendationServiceTest {
    UniversityRepository universities = mock(UniversityRepository.class);
    FacultyRepository faculties = mock(FacultyRepository.class);
    StudyProgramRepository programs = mock(StudyProgramRepository.class);
    CourseRepository courses = mock(CourseRepository.class);
    ExchangeAgreementRepository agreements = mock(ExchangeAgreementRepository.class);
    CourseEquivalenceRepository equivalences = mock(CourseEquivalenceRepository.class);
    RecommendationService service = new RecommendationService(universities, faculties, programs, courses, agreements, equivalences);
    University homeUniversity = university(1L, "Origine");
    Faculty faculty = faculty(2L, homeUniversity);
    StudyProgram home = program(3L, faculty, "Informatică");
    Course first = course(10L, home, 20, Semester.FIRST);
    Course second = course(11L, home, 10, Semester.FIRST);
    ProfileRequest profile = new ProfileRequest(1L, 2L, 3L, 2, "2026-2027", Semester.FIRST);

    @BeforeEach void prepare() {
        when(universities.findById(1L)).thenReturn(Optional.of(homeUniversity));
        when(universities.findOriginUniversities()).thenReturn(List.of(homeUniversity));
        when(faculties.findById(2L)).thenReturn(Optional.of(faculty));
        when(programs.findById(3L)).thenReturn(Optional.of(home));
        when(courses.findByProgramIdAndStudyYearAndAcademicYearAndSemesterOrderByCode(
            3L, 2, "2026-2027", Semester.FIRST)).thenReturn(List.of(first, second));
        when(courses.existsByStudyYear(2)).thenReturn(true);
        when(courses.existsByAcademicYearAndSemester("2026-2027", Semester.FIRST)).thenReturn(true);
        when(courses.existsByProgramIdAndAcademicYearAndSemester(3L, "2026-2027", Semester.FIRST)).thenReturn(true);
    }
    @Test void completePartialAndZeroAreSorted() {
        ExchangeAgreement complete = agreement(1L, "Alpină", Semester.FIRST, true);
        ExchangeAgreement partial = agreement(2L, "Baltică", Semester.FIRST, true);
        ExchangeAgreement zero = agreement(3L, "Centrală", Semester.FIRST, true);
        when(agreements.findByHomeProgramIdAndAcademicYearAndSemesterAndActiveTrue(3L, "2026-2027", Semester.FIRST))
            .thenReturn(List.of(zero, partial, complete));
        map(complete, pair(complete, first, hostCourse(complete, 20)),
            pair(complete, second, course(2000L, complete.hostProgram, 10, Semester.FIRST)));
        map(partial, pair(partial, first, hostCourse(partial, 20)));
        map(zero);
        var found = service.recommend(profile).recommendations();
        assertThat(found).extracting(RecommendationService.Recommendation::agreementId).containsExactly(1L, 2L, 3L);
        assertThat(found).extracting(RecommendationService.Recommendation::covered).containsExactly(30, 20, 0);
        assertThat(found).extracting(RecommendationService.Recommendation::status)
            .containsExactly("COMPLETĂ", "PARȚIALĂ", "FĂRĂ ECHIVALĂRI");
        assertThat(found.get(1).percentage()).isEqualByComparingTo(new BigDecimal("66.7"));
    }
    @Test void inactiveAndOtherSemesterAreExcluded() {
        when(agreements.findByHomeProgramIdAndAcademicYearAndSemesterAndActiveTrue(3L, "2026-2027", Semester.FIRST))
            .thenReturn(List.of(agreement(1L, "Alpină", Semester.FIRST, true)));
        map(agreement(1L, "Alpină", Semester.FIRST, true));
        assertThat(service.recommend(profile).recommendations()).hasSize(1);
    }
    @Test void sameHomeCourseCannotBeCountedTwice() {
        ExchangeAgreement a = agreement(1L, "Alpină", Semester.FIRST, true);
        when(agreements.findByHomeProgramIdAndAcademicYearAndSemesterAndActiveTrue(3L, "2026-2027", Semester.FIRST))
            .thenReturn(List.of(a));
        map(a, pair(a, first, hostCourse(a, 20)), pair(a, first, hostCourse(a, 20)));
        assertThatThrownBy(() -> service.recommend(profile)).isInstanceOf(DataIntegrityException.class);
    }
    @Test void wrongProgramAndSemesterAreCorruption() {
        ExchangeAgreement a = agreement(1L, "Alpină", Semester.FIRST, true);
        when(agreements.findByHomeProgramIdAndAcademicYearAndSemesterAndActiveTrue(3L, "2026-2027", Semester.FIRST))
            .thenReturn(List.of(a));
        Course wrong = course(99L, a.hostProgram, 20, Semester.FIRST);
        map(a, pair(a, wrong, hostCourse(a, 20)));
        assertThatThrownBy(() -> service.recommend(profile)).isInstanceOf(DataIntegrityException.class);
        Course shifted = course(98L, home, 20, Semester.SECOND);
        map(a, pair(a, shifted, hostCourse(a, 20)));
        assertThatThrownBy(() -> service.recommend(profile)).isInstanceOf(DataIntegrityException.class);
    }
    @Test void missingCourseReferenceIsCorruption() {
        ExchangeAgreement a = agreement(1L, "Alpină", Semester.FIRST, true);
        when(agreements.findByHomeProgramIdAndAcademicYearAndSemesterAndActiveTrue(3L, "2026-2027", Semester.FIRST))
            .thenReturn(List.of(a));
        map(a, pair(a, first, null));
        assertThatThrownBy(() -> service.recommend(profile)).isInstanceOf(DataIntegrityException.class);
    }
    @Test void noCurriculumAndNoDestinationsAreDistinct() {
        when(courses.findByProgramIdAndStudyYearAndAcademicYearAndSemesterOrderByCode(
            3L, 2, "2026-2027", Semester.FIRST)).thenReturn(List.of());
        assertThatThrownBy(() -> service.recommend(profile)).isInstanceOf(MissingCurriculumException.class);
        when(courses.findByProgramIdAndStudyYearAndAcademicYearAndSemesterOrderByCode(
            3L, 2, "2026-2027", Semester.FIRST)).thenReturn(List.of(first, second));
        assertThatThrownBy(() -> service.recommend(profile)).isInstanceOf(NoDestinationsException.class);
    }
    @Test void manipulatedRelationshipsAreRejected() {
        ProfileRequest wrong = new ProfileRequest(999L, 2L, 3L, 2, "2026-2027", Semester.FIRST);
        assertThatThrownBy(() -> service.recommend(wrong)).isInstanceOf(InvalidProfileException.class);
        Faculty other = faculty(4L, university(5L, "Alta"));
        when(faculties.findById(4L)).thenReturn(Optional.of(other));
        assertThatThrownBy(() -> service.recommend(new ProfileRequest(1L, 4L, 3L, 2, "2026-2027", Semester.FIRST)))
            .isInstanceOf(InvalidProfileException.class);
    }
    @Test void roundedHundredPercentIsStillPartial() {
        Course large = course(30L, home, 1999, Semester.FIRST);
        Course small = course(31L, home, 1, Semester.FIRST);
        when(courses.findByProgramIdAndStudyYearAndAcademicYearAndSemesterOrderByCode(
            3L, 2, "2026-2027", Semester.FIRST)).thenReturn(List.of(large, small));
        ExchangeAgreement a = agreement(1L, "Alpină", Semester.FIRST, true);
        when(agreements.findByHomeProgramIdAndAcademicYearAndSemesterAndActiveTrue(3L, "2026-2027", Semester.FIRST))
            .thenReturn(List.of(a));
        map(a, pair(a, large, hostCourse(a, 1999)));
        var result = service.recommend(profile).recommendations().getFirst();
        assertThat(result.percentage()).isEqualByComparingTo(new BigDecimal("100.0"));
        assertThat(result.status()).isEqualTo("PARȚIALĂ");
    }
    @Test void equivalenceForDifferentStudyYearIsIgnored() {
        ExchangeAgreement a = agreement(1L, "Alpină", Semester.FIRST, true);
        when(agreements.findByHomeProgramIdAndAcademicYearAndSemesterAndActiveTrue(3L, "2026-2027", Semester.FIRST))
            .thenReturn(List.of(a));
        Course older = course(40L, home, 10, Semester.FIRST);
        older.studyYear = 3;
        map(a, pair(a, older, hostCourse(a, 10)));
        assertThat(service.recommend(profile).recommendations().getFirst().covered()).isZero();
    }
    private void map(ExchangeAgreement a, CourseEquivalence... pairs) {
        when(equivalences.findByAgreementId(a.id)).thenReturn(List.of(pairs));
    }
    private University university(Long id, String name) {
        University u = new University(name, "Oraș", "Țară"); u.id = id; return u;
    }
    private Faculty faculty(Long id, University u) {
        Faculty f = new Faculty(u, "Informatică"); f.id = id; return f;
    }
    private StudyProgram program(Long id, Faculty f, String name) {
        StudyProgram p = new StudyProgram(f, name); p.id = id; return p;
    }
    private Course course(Long id, StudyProgram p, int ects, Semester term) {
        Course c = new Course(p, "C"+id, "Curs", "Descriere", ects, 2, "2026-2027", term);
        c.id = id; return c;
    }
    private ExchangeAgreement agreement(Long id, String name, Semester term, boolean active) {
        StudyProgram host = program(id+100, faculty(id+200, university(id+300, name)), "Informatică");
        ExchangeAgreement a = new ExchangeAgreement(home, host, "2026-2027", term, active);
        a.id = id; return a;
    }
    private Course hostCourse(ExchangeAgreement a, int ects) { return course(a.id+1000, a.hostProgram, ects, Semester.FIRST); }
    private CourseEquivalence pair(ExchangeAgreement a, Course h, Course d) {
        return new CourseEquivalence(a, h, d, "Aceleași competențe concrete.");
    }
}
