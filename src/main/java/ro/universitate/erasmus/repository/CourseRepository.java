package ro.universitate.erasmus.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.universitate.erasmus.entity.Course;
import ro.universitate.erasmus.entity.Semester;
public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByProgramIdAndAcademicYearAndSemester(Long programId, String academicYear, Semester semester);
    List<Course> findByProgramIdAndStudyYearAndAcademicYearAndSemesterOrderByCode(
        Long programId, int studyYear, String academicYear, Semester semester);

    @Query("select c from Course c where (:programId is null or c.program.id = :programId) " +
           "and (:academicYear is null or c.academicYear = :academicYear) " +
           "and (:semester is null or c.semester = :semester) " +
           "and (:studyYear is null or c.studyYear = :studyYear) " +
           "order by c.program.faculty.university.name, c.program.faculty.name, c.program.name, c.code")
    List<Course> findCatalogCourses(@Param("programId") Long programId,
                                    @Param("academicYear") String academicYear,
                                    @Param("semester") Semester semester,
                                    @Param("studyYear") Integer studyYear);
}
