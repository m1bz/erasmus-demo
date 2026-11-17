package ro.universitate.erasmus.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.universitate.erasmus.entity.ExchangeAgreement;
import ro.universitate.erasmus.entity.Semester;
public interface ExchangeAgreementRepository extends JpaRepository<ExchangeAgreement, Long> {
    List<ExchangeAgreement> findByHomeProgramIdAndAcademicYearAndSemesterAndActiveTrue(
        Long homeProgramId, String academicYear, Semester semester);
    boolean existsByHomeProgramIdAndHostProgramIdAndAcademicYearAndSemester(
        Long homeProgramId, Long hostProgramId, String academicYear, Semester semester);

    @Query("select a from ExchangeAgreement a where (:homeProgramId is null or a.homeProgram.id = :homeProgramId) " +
           "and (:academicYear is null or a.academicYear = :academicYear) " +
           "and (:semester is null or a.semester = :semester) order by a.id")
    List<ExchangeAgreement> findCatalogAgreements(@Param("homeProgramId") Long homeProgramId,
                                                  @Param("academicYear") String academicYear,
                                                  @Param("semester") Semester semester);
}
