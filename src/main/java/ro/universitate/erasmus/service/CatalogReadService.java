package ro.universitate.erasmus.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.universitate.erasmus.entity.Course;
import ro.universitate.erasmus.entity.CourseEquivalence;
import ro.universitate.erasmus.entity.ExchangeAgreement;
import ro.universitate.erasmus.entity.Semester;
import ro.universitate.erasmus.entity.StudyProgram;
import ro.universitate.erasmus.entity.University;
import ro.universitate.erasmus.repository.CourseEquivalenceRepository;
import ro.universitate.erasmus.repository.CourseRepository;
import ro.universitate.erasmus.repository.ExchangeAgreementRepository;
import ro.universitate.erasmus.repository.StudyProgramRepository;
import ro.universitate.erasmus.repository.UniversityRepository;

@Service
@Transactional(readOnly = true)
public class CatalogReadService {
    private final UniversityRepository universities;
    private final StudyProgramRepository programs;
    private final CourseRepository courses;
    private final ExchangeAgreementRepository agreements;
    private final CourseEquivalenceRepository equivalences;

    public CatalogReadService(UniversityRepository universities, StudyProgramRepository programs,
                              CourseRepository courses, ExchangeAgreementRepository agreements,
                              CourseEquivalenceRepository equivalences) {
        this.universities = universities;
        this.programs = programs;
        this.courses = courses;
        this.agreements = agreements;
        this.equivalences = equivalences;
    }

    public List<University> universities() {
        return universities.findAllByOrderByNameAsc();
    }

    public List<StudyProgram> programs(Long universityId, Long facultyId) {
        return programs.findCatalogPrograms(universityId, facultyId);
    }

    public List<Course> courses(Long programId, String academicYear, Semester semester, Integer studyYear) {
        return courses.findCatalogCourses(programId, academicYear, semester, studyYear);
    }

    public List<ExchangeAgreement> agreements(Long homeProgramId, String academicYear, Semester semester) {
        return agreements.findCatalogAgreements(homeProgramId, academicYear, semester);
    }

    public ExchangeAgreement agreement(Long id) {
        return agreements.findById(id).orElseThrow(() -> new ApiNotFoundException("Parteneriatul nu există."));
    }

    public List<CourseEquivalence> equivalences(Long agreementId) {
        return equivalences.findCatalogEquivalences(agreementId);
    }

    public CourseEquivalence equivalence(Long id) {
        return equivalences.findById(id).orElseThrow(() -> new ApiNotFoundException("Echivalarea nu există."));
    }
}
