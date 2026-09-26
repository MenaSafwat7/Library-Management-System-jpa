package com.example.library.model;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("customers")
public class Customer extends Person {
    private String membershipNumber;
    protected Customer() {}
    public Customer(String name, String membershipNumber) {
        super(name);
        this.membershipNumber = membershipNumber;
    }
    public String getMembershipNumber() {
        return membershipNumber;
    }
}
