package ro.universitate.erasmus.entity;

import jakarta.persistence.*;

@Entity
public class University {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, unique = true) public String name;
    @Column(nullable = false) public String city;
    @Column(nullable = false) public String country;
    protected University() {}
    public University(String name, String city, String country) {
        this.name = name; this.city = city; this.country = country;
    }
}
