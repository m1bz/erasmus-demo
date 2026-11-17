package ro.universitate.erasmus.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.universitate.erasmus.entity.StudyProgram;
public interface StudyProgramRepository extends JpaRepository<StudyProgram, Long> {
    List<StudyProgram> findByFacultyIdOrderByName(Long facultyId);

    @Query("select p from StudyProgram p where (:universityId is null or p.faculty.university.id = :universityId) " +
           "and (:facultyId is null or p.faculty.id = :facultyId) " +
           "order by p.faculty.university.name, p.faculty.name, p.name")
    List<StudyProgram> findCatalogPrograms(@Param("universityId") Long universityId,
                                           @Param("facultyId") Long facultyId);
}
