package ro.universitate.erasmus.entity;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"faculty_id", "name"}))
public class StudyProgram {
    public enum Level { BACHELOR }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) @JoinColumn(nullable = false) public Faculty faculty;
    @Column(nullable = false) public String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public Level level;
    protected StudyProgram() {}
    public StudyProgram(Faculty faculty, String name) {
        this.faculty = faculty; this.name = name; this.level = Level.BACHELOR;
    }
}
