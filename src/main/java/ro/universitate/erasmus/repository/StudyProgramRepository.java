package ro.universitate.erasmus.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.universitate.erasmus.entity.StudyProgram;
public interface StudyProgramRepository extends JpaRepository<StudyProgram, Long> {
    List<StudyProgram> findByFacultyIdOrderByName(Long facultyId);
}
