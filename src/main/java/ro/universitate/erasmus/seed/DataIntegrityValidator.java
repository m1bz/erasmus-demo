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
        for (Course c : courses.findAll()) {
            if (c.program == null || c.academicYear == null || c.semester == null || c.ects <= 0)
                errors.add("Curs invalid: " + c.id);
            else totals.merge(c.program.id + "/" + c.studyYear + "/" + c.academicYear + "/" + c.semester,
                              c.ects, Integer::sum);
        }
        totals.forEach((key, total) -> {
            if (total != 30) errors.add("Curriculum cu " + total + " ECTS: " + key);
        });
        for (ExchangeAgreement a : agreements.findAll()) {
            if (a.homeProgram == null || a.hostProgram == null || a.academicYear == null || a.semester == null)
                errors.add("Parteneriat incomplet: " + a.id);
            else if (Objects.equals(a.homeProgram.faculty.university.id, a.hostProgram.faculty.university.id))
                errors.add("Universitatea gazdă coincide cu cea de origine: " + a.id);
            Set<Long> homeIds = new HashSet<>();
            Set<Long> hostIds = new HashSet<>();
            for (CourseEquivalence e : equivalences.findByAgreementId(a.id)) {
                if (e.homeCourse == null || e.hostCourse == null || e.justification == null || e.justification.isBlank()) {
                    errors.add("Echivalare incompletă: " + e.id);
                    continue;
                }
                if (!homeIds.add(e.homeCourse.id)) errors.add("Curs de origine folosit de două ori: " + e.homeCourse.id);
                if (!hostIds.add(e.hostCourse.id)) errors.add("Curs gazdă folosit de două ori: " + e.hostCourse.id);
                if (!Objects.equals(e.homeCourse.program.id, a.homeProgram.id)) errors.add("Program de origine greșit: " + e.id);
                if (!Objects.equals(e.hostCourse.program.id, a.hostProgram.id)) errors.add("Program gazdă greșit: " + e.id);
                if (!Objects.equals(e.homeCourse.academicYear, a.academicYear) ||
                    !Objects.equals(e.hostCourse.academicYear, a.academicYear) ||
                    e.homeCourse.semester != a.semester || e.hostCourse.semester != a.semester)
                    errors.add("Perioadă greșită: " + e.id);
                if (e.homeCourse.ects != e.hostCourse.ects) errors.add("ECTS diferite: " + e.id);
            }
        }
        if (!errors.isEmpty()) throw new DataIntegrityException(errors);
    }
}
