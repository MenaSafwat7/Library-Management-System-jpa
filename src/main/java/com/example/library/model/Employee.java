package com.example.library.model;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("employees")
public class Employee extends Person {
    private String jobTitle;
    protected Employee() {}
    public Employee(String name, String jobTitle) {
        super(name);
        this.jobTitle = jobTitle;
    }
    public String getJobTitle() {
        return jobTitle;
    }
}
