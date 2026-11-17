package ro.universitate.erasmus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import ro.universitate.erasmus.dto.ProfileRequest;
import ro.universitate.erasmus.entity.*;
import ro.universitate.erasmus.repository.*;
import ro.universitate.erasmus.service.FormOptionsService;

class FormOptionsServiceTest {
    UniversityRepository universities = mock(UniversityRepository.class);
    FacultyRepository faculties = mock(FacultyRepository.class);
    StudyProgramRepository programs = mock(StudyProgramRepository.class);
    CourseRepository courses = mock(CourseRepository.class);
    FormOptionsService service = new FormOptionsService(universities, faculties, programs, courses);

    @Test void selectedStudyYearOnlyOffersAcademicYearsWithCoursesForThatYear() {
        University university = new University("Universitatea A", "Galați", "România");
        university.id = 1L;
        Faculty faculty = new Faculty(university, "Informatică");
        faculty.id = 2L;
        StudyProgram program = new StudyProgram(faculty, "Informatică");
        program.id = 3L;
        Course yearTwo = course(program, 4L, 2, "2026-2027", Semester.FIRST);
        Course yearThree = course(program, 5L, 3, "2027-2028", Semester.SECOND);

        when(universities.findOriginUniversities()).thenReturn(List.of(university));
        when(universities.findById(1L)).thenReturn(Optional.of(university));
        when(faculties.findById(2L)).thenReturn(Optional.of(faculty));
        when(faculties.findByUniversityIdOrderByName(1L)).thenReturn(List.of(faculty));
        when(programs.findById(3L)).thenReturn(Optional.of(program));
        when(programs.findByFacultyIdOrderByName(2L)).thenReturn(List.of(program));
        when(courses.findCatalogCourses(3L, null, null, null)).thenReturn(List.of(yearTwo, yearThree));

        var options = service.options(new ProfileRequest(1L, 2L, 3L, 2, "2027-2028", Semester.SECOND));

        assertThat(options.studyYear()).isEqualTo(2);
        assertThat(options.academicYear()).isNull();
        assertThat(options.academicYears()).containsExactly("2026-2027");
        assertThat(options.semesters()).isEmpty();
    }

    private Course course(StudyProgram program, Long id, int studyYear, String academicYear, Semester semester) {
        Course course = new Course(program, "C" + id, "Curs", "Descriere", 30,
            studyYear, academicYear, semester);
        course.id = id;
        return course;
    }
}
