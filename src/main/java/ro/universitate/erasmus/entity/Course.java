package ro.universitate.erasmus.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;

@Entity
@Check(constraints = "ects > 0")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"program_id", "academic_year", "code"}))
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "program_id", nullable = false) public StudyProgram program;
    @Column(nullable = false) public String code;
    @Column(nullable = false) public String name;
    @Column(nullable = false, length = 500) public String description;
    @Column(nullable = false) public int ects;
    @Column(nullable = false) public int studyYear;
    @Column(name = "academic_year", nullable = false) public String academicYear;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public Semester semester;
    protected Course() {}
    public Course(StudyProgram program, String code, String name, String description, int ects,
                  int studyYear, String academicYear, Semester semester) {
        this.program = program; this.code = code; this.name = name; this.description = description;
        this.ects = ects; this.studyYear = studyYear; this.academicYear = academicYear; this.semester = semester;
    }
}
