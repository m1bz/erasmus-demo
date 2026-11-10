package ro.universitate.erasmus.entity;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"agreement_id", "home_course_id"}),
    @UniqueConstraint(columnNames = {"agreement_id", "host_course_id"})
})
public class CourseEquivalence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "agreement_id", nullable = false) public ExchangeAgreement agreement;
    @ManyToOne(optional = false) @JoinColumn(name = "home_course_id", nullable = false) public Course homeCourse;
    @ManyToOne(optional = false) @JoinColumn(name = "host_course_id", nullable = false) public Course hostCourse;
    @Column(nullable = false, length = 700) public String justification;
    protected CourseEquivalence() {}
    public CourseEquivalence(ExchangeAgreement agreement, Course homeCourse, Course hostCourse, String justification) {
        this.agreement = agreement; this.homeCourse = homeCourse; this.hostCourse = hostCourse; this.justification = justification;
    }
}
