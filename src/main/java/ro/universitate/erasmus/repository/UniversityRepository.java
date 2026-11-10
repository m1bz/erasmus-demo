package ro.universitate.erasmus.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import ro.universitate.erasmus.entity.University;
public interface UniversityRepository extends JpaRepository<University, Long> {
    @Query("select distinct a.homeProgram.faculty.university from ExchangeAgreement a where a.active = true")
    List<University> findOriginUniversities();
}
