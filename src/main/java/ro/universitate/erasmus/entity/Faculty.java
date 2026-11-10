package ro.universitate.erasmus.entity;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"university_id", "name"}))
public class Faculty {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) @JoinColumn(nullable = false) public University university;
    @Column(nullable = false) public String name;
    protected Faculty() {}
    public Faculty(University university, String name) { this.university = university; this.name = name; }
}
