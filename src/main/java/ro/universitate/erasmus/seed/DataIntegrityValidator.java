package ro.universitate.erasmus.seed;

import java.util.*;
import org.springframework.stereotype.Component;
import ro.universitate.erasmus.entity.*;
import ro.universitate.erasmus.repository.*;
import ro.universitate.erasmus.service.DataIntegrityException;

@Component
public class DataIntegrityValidator {
    private final CourseRepository courses;
    private final ExchangeAgreementRepository agreements;
    private final CourseEquivalenceRepository equivalences;

    public DataIntegrityValidator(CourseRepository courses, ExchangeAgreementRepository agreements,
                                  CourseEquivalenceRepository equivalences) {
        this.courses = courses; this.agreements = agreements; this.equivalences = equivalences;
    }

    public void validate() {
        List<String> errors = new ArrayList<>();
        Map<String, Integer> totals = new HashMap<>();
        Set<String> courseKeys = new HashSet<>();
        for (Course c : courses.findAll()) {
            if (c.program == null || c.program.id == null || blank(c.academicYear) || blank(c.code)
                    || c.semester == null || c.studyYear <= 0 || c.ects <= 0) {
                errors.add("Curs invalid: " + c.id);
            } else {
                String courseKey = c.program.id + "/" + c.academicYear + "/" + c.code;
                if (!courseKeys.add(courseKey)) errors.add("Cod de curs duplicat: " + courseKey);
                totals.merge(c.program.id + "/" + c.studyYear + "/" + c.academicYear + "/" + c.semester,
                             c.ects, Integer::sum);
            }
        }
        totals.forEach((key, total) -> {
            if (total != 30) errors.add("Curriculum cu " + total + " ECTS: " + key);
        });
        Set<String> agreementKeys = new HashSet<>();
        for (ExchangeAgreement a : agreements.findAll()) {
            Long homeProgramId = idOf(a.homeProgram);
            Long hostProgramId = idOf(a.hostProgram);
            Long homeUniversityId = universityIdOf(a.homeProgram);
            Long hostUniversityId = universityIdOf(a.hostProgram);
            if (homeProgramId == null || hostProgramId == null || homeUniversityId == null || hostUniversityId == null
                    || blank(a.academicYear) || a.semester == null) {
                errors.add("Parteneriat incomplet: " + a.id);
            } else {
                String key = homeProgramId + "/" + hostProgramId + "/" + a.academicYear + "/" + a.semester;
                if (!agreementKeys.add(key)) errors.add("Parteneriat duplicat: " + key);
                if (homeUniversityId != null && homeUniversityId.equals(hostUniversityId))
                    errors.add("Universitatea gazdă coincide cu cea de origine: " + a.id);
            }
            if (a.id == null) continue;
            Set<Long> homeIds = new HashSet<>();
            Set<Long> hostIds = new HashSet<>();
            for (CourseEquivalence e : equivalences.findByAgreementId(a.id)) {
                if (e.homeCourse == null || e.hostCourse == null || e.homeCourse.id == null || e.hostCourse.id == null
                        || blank(e.justification)) {
                    errors.add("Echivalare incompletă: " + e.id);
                    continue;
                }
                if (!homeIds.add(e.homeCourse.id)) errors.add("Curs de origine folosit de două ori: " + e.homeCourse.id);
                if (!hostIds.add(e.hostCourse.id)) errors.add("Curs gazdă folosit de două ori: " + e.hostCourse.id);
                Long equivalenceHomeProgramId = idOf(e.homeCourse.program);
                Long equivalenceHostProgramId = idOf(e.hostCourse.program);
                if (homeProgramId == null || !Objects.equals(equivalenceHomeProgramId, homeProgramId))
                    errors.add("Program de origine greșit: " + e.id);
                if (hostProgramId == null || !Objects.equals(equivalenceHostProgramId, hostProgramId))
                    errors.add("Program gazdă greșit: " + e.id);
                if (blank(e.homeCourse.academicYear) || blank(e.hostCourse.academicYear)
                        || !Objects.equals(e.homeCourse.academicYear, a.academicYear) ||
                    !Objects.equals(e.hostCourse.academicYear, a.academicYear) ||
                    e.homeCourse.semester != a.semester || e.hostCourse.semester != a.semester)
                    errors.add("Perioadă greșită: " + e.id);
                if (e.homeCourse.ects != e.hostCourse.ects) errors.add("ECTS diferite: " + e.id);
            }
        }
        if (!errors.isEmpty()) throw new DataIntegrityException(errors);
    }

    private static Long idOf(StudyProgram program) {
        return program == null ? null : program.id;
    }

    private static Long universityIdOf(StudyProgram program) {
        return program == null || program.faculty == null || program.faculty.university == null
            ? null : program.faculty.university.id;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
