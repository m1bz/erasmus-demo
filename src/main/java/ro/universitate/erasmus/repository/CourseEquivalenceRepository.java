package ro.universitate.erasmus.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.universitate.erasmus.entity.CourseEquivalence;
public interface CourseEquivalenceRepository extends JpaRepository<CourseEquivalence, Long> {
    List<CourseEquivalence> findByAgreementId(Long agreementId);
    boolean existsByAgreementIdAndHomeCourseId(Long agreementId, Long homeCourseId);
    boolean existsByAgreementIdAndHostCourseId(Long agreementId, Long hostCourseId);
    boolean existsByAgreementIdAndHomeCourseIdAndIdNot(Long agreementId, Long homeCourseId, Long id);
    boolean existsByAgreementIdAndHostCourseIdAndIdNot(Long agreementId, Long hostCourseId, Long id);

    @Query("select e from CourseEquivalence e where (:agreementId is null or e.agreement.id = :agreementId) " +
           "order by e.agreement.id, e.id")
    List<CourseEquivalence> findCatalogEquivalences(@Param("agreementId") Long agreementId);
}
