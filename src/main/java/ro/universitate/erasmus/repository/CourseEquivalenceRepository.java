package ro.universitate.erasmus.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.universitate.erasmus.entity.CourseEquivalence;
public interface CourseEquivalenceRepository extends JpaRepository<CourseEquivalence, Long> {
    List<CourseEquivalence> findByAgreementId(Long agreementId);
}
