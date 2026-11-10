package ro.universitate.erasmus.service;

import java.math.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.universitate.erasmus.dto.ProfileRequest;
import ro.universitate.erasmus.entity.*;
import ro.universitate.erasmus.repository.*;

@Service
public class RecommendationService {
    private final UniversityRepository universities;
    private final FacultyRepository faculties;
    private final StudyProgramRepository programs;
    private final CourseRepository courses;
    private final ExchangeAgreementRepository agreements;
    private final CourseEquivalenceRepository equivalences;

    public RecommendationService(UniversityRepository universities, FacultyRepository faculties,
                                 StudyProgramRepository programs, CourseRepository courses,
                                 ExchangeAgreementRepository agreements, CourseEquivalenceRepository equivalences) {
        this.universities = universities; this.faculties = faculties; this.programs = programs;
        this.courses = courses; this.agreements = agreements; this.equivalences = equivalences;
    }

    public record Recommendation(Long agreementId, String university, String faculty, String program,
                                 String country, String city, int covered, int total,
                                 BigDecimal percentage, String status) {}
    public record Results(ProfileRequest profile, List<Recommendation> recommendations) {}
    public record ComparisonRow(String homeCourse, int homeEcts, String hostCourse, Integer hostEcts,
                                String justification, boolean covered) {}
    public record Comparison(Recommendation recommendation, List<ComparisonRow> rows, int uncovered) {}

    @Transactional(readOnly = true)
    public Results recommend(ProfileRequest profile) {
        StudyProgram home = validate(profile);
        List<Course> curriculum = curriculum(profile);
        int total = curriculum.stream().mapToInt(c -> c.ects).sum();
        List<ExchangeAgreement> candidates = agreements.findByHomeProgramIdAndAcademicYearAndSemesterAndActiveTrue(
            home.id, profile.academicYear(), profile.semester());
        if (candidates.isEmpty()) throw new NoDestinationsException();
        List<Recommendation> results = new ArrayList<>();
        for (ExchangeAgreement agreement : candidates) {
            if (agreement.homeProgram == null || agreement.hostProgram == null ||
                !agreement.active || !Objects.equals(agreement.homeProgram.id, home.id) ||
                !Objects.equals(agreement.academicYear, profile.academicYear()) ||
                agreement.semester != profile.semester())
                throw new DataIntegrityException("Parteneriat inconsistent: " + agreement.id);
            Map<Long, CourseEquivalence> matched = validMatches(agreement, curriculum);
            int covered = curriculum.stream().filter(c -> matched.containsKey(c.id)).mapToInt(c -> c.ects).sum();
            BigDecimal percentage = BigDecimal.valueOf(covered).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
            String status = covered == total ? "COMPLETĂ" : covered == 0 ? "FĂRĂ ECHIVALĂRI" : "PARȚIALĂ";
            StudyProgram host = agreement.hostProgram;
            University university = host.faculty.university;
            results.add(new Recommendation(agreement.id, university.name, host.faculty.name, host.name,
                university.country, university.city, covered, total, percentage, status));
        }
        results.sort(Comparator.comparingInt(Recommendation::covered).reversed()
            .thenComparing(Recommendation::university, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Recommendation::program, String.CASE_INSENSITIVE_ORDER));
        return new Results(profile, List.copyOf(results));
    }

    @Transactional(readOnly = true)
    public Comparison compare(ProfileRequest profile, Long agreementId) {
        Results results = recommend(profile);
        Recommendation selected = results.recommendations().stream()
            .filter(r -> Objects.equals(r.agreementId(), agreementId)).findFirst()
            .orElseThrow(InvalidProfileException::new);
        ExchangeAgreement agreement = agreements.findById(agreementId).orElseThrow(InvalidProfileException::new);
        List<Course> curriculum = curriculum(profile);
        Map<Long, CourseEquivalence> matched = validMatches(agreement, curriculum);
        List<ComparisonRow> rows = curriculum.stream().map(c -> {
            CourseEquivalence e = matched.get(c.id);
            return e == null
                ? new ComparisonRow(c.name, c.ects, "Neacoperită", null, "Nu există echivalare declarată.", false)
                : new ComparisonRow(c.name, c.ects, e.hostCourse.name, e.hostCourse.ects, e.justification, true);
        }).toList();
        return new Comparison(selected, rows, selected.total() - selected.covered());
    }

    private StudyProgram validate(ProfileRequest p) {
        if (p == null || p.universityId() == null || p.facultyId() == null || p.programId() == null ||
            p.studyYear() == null || p.studyYear() <= 0 || p.academicYear() == null || p.academicYear().isBlank() ||
            p.semester() == null) throw new InvalidProfileException();
        University university = universities.findById(p.universityId()).orElseThrow(InvalidProfileException::new);
        Faculty faculty = faculties.findById(p.facultyId()).orElseThrow(InvalidProfileException::new);
        StudyProgram program = programs.findById(p.programId()).orElseThrow(InvalidProfileException::new);
        if (!Objects.equals(faculty.university.id, university.id) ||
            !Objects.equals(program.faculty.id, faculty.id) || program.level != StudyProgram.Level.BACHELOR ||
            universities.findOriginUniversities().stream().noneMatch(u -> Objects.equals(u.id, university.id)) ||
            !courses.existsByStudyYear(p.studyYear()) ||
            !courses.existsByAcademicYearAndSemester(p.academicYear(), p.semester()) ||
            !courses.existsByProgramIdAndAcademicYearAndSemester(p.programId(), p.academicYear(), p.semester()))
            throw new InvalidProfileException();
        return program;
    }

    private List<Course> curriculum(ProfileRequest p) {
        List<Course> list = courses.findByProgramIdAndStudyYearAndAcademicYearAndSemesterOrderByCode(
            p.programId(), p.studyYear(), p.academicYear(), p.semester());
        if (list.isEmpty()) throw new MissingCurriculumException();
        return list;
    }

    private Map<Long, CourseEquivalence> validMatches(ExchangeAgreement agreement, List<Course> curriculum) {
        if (agreement.homeProgram == null || agreement.hostProgram == null ||
            agreement.homeProgram.faculty == null || agreement.hostProgram.faculty == null ||
            agreement.homeProgram.faculty.university == null || agreement.hostProgram.faculty.university == null ||
            Objects.equals(agreement.homeProgram.faculty.university.id, agreement.hostProgram.faculty.university.id))
            throw new DataIntegrityException("Universitate gazdă invalidă");
        Set<Long> curriculumIds = curriculum.stream().map(c -> c.id).collect(Collectors.toSet());
        Set<Long> homeUsed = new HashSet<>(), hostUsed = new HashSet<>();
        Map<Long, CourseEquivalence> matched = new HashMap<>();
        for (CourseEquivalence e : equivalences.findByAgreementId(agreement.id)) {
            if (e.homeCourse == null || e.hostCourse == null ||
                e.homeCourse.program == null || e.hostCourse.program == null ||
                e.homeCourse.id == null || e.hostCourse.id == null ||
                e.justification == null || e.justification.isBlank() ||
                !Objects.equals(e.homeCourse.program.id, agreement.homeProgram.id) ||
                !Objects.equals(e.hostCourse.program.id, agreement.hostProgram.id) ||
                !Objects.equals(e.homeCourse.academicYear, agreement.academicYear) ||
                !Objects.equals(e.hostCourse.academicYear, agreement.academicYear) ||
                e.homeCourse.semester != agreement.semester || e.hostCourse.semester != agreement.semester ||
                e.homeCourse.ects != e.hostCourse.ects ||
                !homeUsed.add(e.homeCourse.id) || !hostUsed.add(e.hostCourse.id))
                throw new DataIntegrityException("Echivalare inconsistentă în parteneriatul " + agreement.id);
            if (curriculumIds.contains(e.homeCourse.id)) matched.put(e.homeCourse.id, e);
        }
        return matched;
    }
}
