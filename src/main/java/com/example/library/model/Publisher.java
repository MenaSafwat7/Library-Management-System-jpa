package com.example.library.model;
import jakarta.persistence.*;

@Entity
@Table(name = "publishers")
public class Publisher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    protected Publisher() {}
    public Publisher(String name) { this.name = name; }
    public Long getId() { return id; }
    public String getName() { return name; }
}
