package ro.universitate.erasmus.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.universitate.erasmus.entity.University;
public interface UniversityRepository extends JpaRepository<University, Long> {}
