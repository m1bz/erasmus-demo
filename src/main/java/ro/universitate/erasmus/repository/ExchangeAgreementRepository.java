package ro.universitate.erasmus.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.universitate.erasmus.entity.ExchangeAgreement;
import ro.universitate.erasmus.entity.Semester;
public interface ExchangeAgreementRepository extends JpaRepository<ExchangeAgreement, Long> {
    List<ExchangeAgreement> findByHomeProgramIdAndAcademicYearAndSemesterAndActiveTrue(
        Long homeProgramId, String academicYear, Semester semester);
}
