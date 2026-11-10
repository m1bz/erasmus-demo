package ro.universitate.erasmus.entity;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"home_program_id", "host_program_id", "academic_year", "semester"}))
public class ExchangeAgreement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "home_program_id", nullable = false) public StudyProgram homeProgram;
    @ManyToOne(optional = false) @JoinColumn(name = "host_program_id", nullable = false) public StudyProgram hostProgram;
    @Column(name = "academic_year", nullable = false) public String academicYear;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public Semester semester;
    @Column(nullable = false) public boolean active;
    protected ExchangeAgreement() {}
    public ExchangeAgreement(StudyProgram homeProgram, StudyProgram hostProgram, String academicYear, Semester semester, boolean active) {
        this.homeProgram = homeProgram; this.hostProgram = hostProgram; this.academicYear = academicYear;
        this.semester = semester; this.active = active;
    }
}
