package ro.universitate.erasmus.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.universitate.erasmus.entity.Course;
import ro.universitate.erasmus.entity.Semester;
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByProgramIdAndStudyYearAndAcademicYearAndSemesterOrderByCode(
        Long programId, int studyYear, String academicYear, Semester semester);
}
