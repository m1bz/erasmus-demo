package ro.universitate.erasmus.service;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.universitate.erasmus.dto.api.ApiDtos;
import ro.universitate.erasmus.entity.Course;
import ro.universitate.erasmus.entity.CourseEquivalence;
import ro.universitate.erasmus.entity.ExchangeAgreement;
import ro.universitate.erasmus.entity.StudyProgram;
import ro.universitate.erasmus.repository.CourseEquivalenceRepository;
import ro.universitate.erasmus.repository.CourseRepository;
import ro.universitate.erasmus.repository.ExchangeAgreementRepository;
import ro.universitate.erasmus.repository.StudyProgramRepository;

@Service
public class CatalogWriteService {
    private final StudyProgramRepository programs;
    private final CourseRepository courses;
    private final ExchangeAgreementRepository agreements;
    private final CourseEquivalenceRepository equivalences;

    public CatalogWriteService(StudyProgramRepository programs, CourseRepository courses,
                               ExchangeAgreementRepository agreements, CourseEquivalenceRepository equivalences) {
        this.programs = programs;
        this.courses = courses;
        this.agreements = agreements;
        this.equivalences = equivalences;
    }

    @Transactional
    public ExchangeAgreement createAgreement(ApiDtos.CreateAgreementRequest request) {
        StudyProgram home = programs.findById(request.homeProgramId())
            .orElseThrow(() -> new ApiNotFoundException("Programul de origine nu există."));
        StudyProgram host = programs.findById(request.hostProgramId())
            .orElseThrow(() -> new ApiNotFoundException("Programul gazdă nu există."));

        if (Objects.equals(home.faculty.university.id, host.faculty.university.id)) {
            throw new ApiConflictException("Universitatea gazdă trebuie să fie diferită de universitatea de origine.");
        }
        if (!courses.existsByProgramIdAndAcademicYearAndSemester(home.id, request.academicYear(), request.semester()) ||
            !courses.existsByProgramIdAndAcademicYearAndSemester(host.id, request.academicYear(), request.semester())) {
            throw new ApiConflictException("Ambele programe trebuie să aibă cursuri în perioada selectată.");
        }
        if (agreements.existsByHomeProgramIdAndHostProgramIdAndAcademicYearAndSemester(
                home.id, host.id, request.academicYear(), request.semester())) {
            throw new ApiConflictException("Parteneriatul există deja pentru această perioadă.");
        }

        ExchangeAgreement agreement = new ExchangeAgreement(home, host, request.academicYear(),
            request.semester(), request.active());
        return agreements.saveAndFlush(agreement);
    }

    @Transactional
    public CourseEquivalence createEquivalence(ApiDtos.CreateEquivalenceRequest request) {
        ExchangeAgreement agreement = agreements.findById(request.agreementId())
            .orElseThrow(() -> new ApiNotFoundException("Parteneriatul nu există."));
        Course home = courses.findById(request.homeCourseId())
            .orElseThrow(() -> new ApiNotFoundException("Cursul de origine nu există."));
        Course host = courses.findById(request.hostCourseId())
            .orElseThrow(() -> new ApiNotFoundException("Cursul gazdă nu există."));

        validateEquivalence(agreement, home, host);
        if (equivalences.existsByAgreementIdAndHomeCourseId(agreement.id, home.id) ||
            equivalences.existsByAgreementIdAndHostCourseId(agreement.id, host.id)) {
            throw new ApiConflictException("Fiecare curs poate apărea o singură dată în echivalările parteneriatului.");
        }

        return equivalences.saveAndFlush(new CourseEquivalence(agreement, home, host, request.justification().trim()));
    }

    @Transactional
    public ExchangeAgreement setAgreementActive(Long agreementId, boolean active) {
        ExchangeAgreement agreement = agreements.findById(agreementId)
            .orElseThrow(() -> new ApiNotFoundException("Parteneriatul nu există."));
        agreement.active = active;
        return agreements.saveAndFlush(agreement);
    }

    @Transactional
    public CourseEquivalence updateEquivalence(Long equivalenceId, Long requestAgreementId,
                                               Long homeCourseId, Long hostCourseId, String justification) {
        CourseEquivalence equivalence = equivalences.findById(equivalenceId)
            .orElseThrow(() -> new ApiNotFoundException("Echivalarea nu există."));
        if (!Objects.equals(equivalence.agreement.id, requestAgreementId)) {
            throw new ApiConflictException("Parteneriatul echivalării nu poate fi schimbat.");
        }
        Course home = courses.findById(homeCourseId)
            .orElseThrow(() -> new ApiNotFoundException("Cursul de origine nu există."));
        Course host = courses.findById(hostCourseId)
            .orElseThrow(() -> new ApiNotFoundException("Cursul gazdă nu există."));
        ExchangeAgreement agreement = equivalence.agreement;
        validateEquivalence(agreement, home, host);
        if (equivalences.existsByAgreementIdAndHomeCourseIdAndIdNot(agreement.id, home.id, equivalence.id) ||
            equivalences.existsByAgreementIdAndHostCourseIdAndIdNot(agreement.id, host.id, equivalence.id)) {
            throw new ApiConflictException("Fiecare curs poate apărea o singură dată în echivalările parteneriatului.");
        }
        equivalence.homeCourse = home;
        equivalence.hostCourse = host;
        equivalence.justification = justification.trim();
        return equivalences.saveAndFlush(equivalence);
    }

    private void validateEquivalence(ExchangeAgreement agreement, Course home, Course host) {
        if (!Objects.equals(home.program.id, agreement.homeProgram.id) ||
            !Objects.equals(host.program.id, agreement.hostProgram.id)) {
            throw new ApiConflictException("Cursurile trebuie să aparțină programelor din parteneriat.");
        }
        if (!Objects.equals(home.academicYear, agreement.academicYear) ||
            !Objects.equals(host.academicYear, agreement.academicYear) ||
            home.semester != agreement.semester || host.semester != agreement.semester) {
            throw new ApiConflictException("Cursurile trebuie să aparțină perioadei din parteneriat.");
        }
        if (home.ects != host.ects) {
            throw new ApiConflictException("Cursurile echivalate trebuie să aibă același număr de ECTS.");
        }
    }
}
