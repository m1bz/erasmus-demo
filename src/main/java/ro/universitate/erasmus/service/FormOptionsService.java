package ro.universitate.erasmus.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.universitate.erasmus.dto.ProfileRequest;
import ro.universitate.erasmus.entity.*;
import ro.universitate.erasmus.repository.*;

@Service
public class FormOptionsService {
    private final UniversityRepository universities;
    private final FacultyRepository faculties;
    private final StudyProgramRepository programs;
    private final CourseRepository courses;
    public FormOptionsService(UniversityRepository universities, FacultyRepository faculties,
                              StudyProgramRepository programs, CourseRepository courses) {
        this.universities = universities; this.faculties = faculties; this.programs = programs; this.courses = courses;
    }
    public record Options(List<University> universities, List<Faculty> faculties, List<StudyProgram> programs,
                          List<Integer> studyYears, List<String> academicYears, List<Semester> semesters,
                          Long universityId, Long facultyId, Long programId, Integer studyYear,
                          String academicYear, Semester semester) {}

    @Transactional(readOnly = true)
    public Options options(ProfileRequest input) {
        List<University> origins = new ArrayList<>(universities.findOriginUniversities());
        origins.sort(Comparator.comparing(u -> u.name, String.CASE_INSENSITIVE_ORDER));
        Long universityId = input == null ? null : input.universityId();
        Long facultyId = input == null ? null : input.facultyId();
        Long programId = input == null ? null : input.programId();
        if (universityId != null && universities.findById(universityId).isEmpty()) throw new InvalidProfileException();
        Faculty selectedFaculty = facultyId == null ? null : faculties.findById(facultyId).orElseThrow(InvalidProfileException::new);
        StudyProgram selectedProgram = programId == null ? null : programs.findById(programId).orElseThrow(InvalidProfileException::new);
        if (universityId == null || origins.stream().noneMatch(u -> u.id.equals(universityId))) {
            facultyId = null; programId = null;
        } else if (selectedFaculty != null && !selectedFaculty.university.id.equals(universityId)) {
            facultyId = null; programId = null;
        }
        List<Faculty> facultyOptions = universityId == null ? List.of() : faculties.findByUniversityIdOrderByName(universityId);
        if (facultyId == null || selectedProgram != null && !selectedProgram.faculty.id.equals(facultyId)) programId = null;
        List<StudyProgram> programOptions = facultyId == null ? List.of() : programs.findByFacultyIdOrderByName(facultyId);
        Long finalProgramId = programId;
        List<Course> existing = finalProgramId == null ? List.of() : courses.findAll().stream()
            .filter(c -> c.program.id.equals(finalProgramId)).toList();
        List<Integer> studyYears = existing.stream().map(c -> c.studyYear).distinct().sorted().toList();
        List<String> academicYears = existing.stream().map(c -> c.academicYear).distinct().sorted().toList();
        Integer studyYear = input == null || !studyYears.contains(input.studyYear()) ? null : input.studyYear();
        String academicYear = input == null || !academicYears.contains(input.academicYear()) ? null : input.academicYear();
        List<Semester> semesters = existing.stream().filter(c -> Objects.equals(c.studyYear, studyYear)
                && Objects.equals(c.academicYear, academicYear))
            .map(c -> c.semester).distinct().sorted().toList();
        Semester semester = input == null || !semesters.contains(input.semester()) ? null : input.semester();
        return new Options(origins, facultyOptions, programOptions, studyYears, academicYears, semesters,
            universityId, facultyId, programId, studyYear, academicYear, semester);
    }
}
