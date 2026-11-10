package ro.universitate.erasmus.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.universitate.erasmus.entity.Faculty;
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    List<Faculty> findByUniversityIdOrderByName(Long universityId);
}
