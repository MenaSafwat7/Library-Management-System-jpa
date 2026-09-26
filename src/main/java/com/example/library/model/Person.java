package com.example.library.model;
import jakarta.persistence.*;

@Entity
@Table(name = "person")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Person {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    protected Person() {}
    protected Person(String name) {
        this.name = name;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
}
