package com.example.library;

import com.example.library.model.*;
import jakarta.persistence.EntityManager;

public final class SampleData {
    private SampleData() {}
    public static Long insert(EntityManager em) {
        Publisher bloomsbury = new Publisher("Bloomsbury");
        Publisher allen = new Publisher("Allen & Unwin");
        Category fantasy = new Category("Fantasy");
        Category adventure = new Category("Adventure");
        em.persist(bloomsbury);
        em.persist(allen);
        em.persist(fantasy);
        em.persist(adventure);

        Author rowling = new Author("J. K. Rowling");
        Book stone = new Book("Harry Potter and the Philosopher's Stone", bloomsbury);
        stone.addCategory(fantasy);
        stone.addCategory(adventure);
        rowling.addBook(stone);
        Book chamber = new Book("Harry Potter and the Chamber of Secrets", bloomsbury);
        chamber.addCategory(fantasy);
        rowling.addBook(chamber);
        Author tolkien = new Author("J. R. R. Tolkien");
        Book hobbit = new Book("The Hobbit", allen);
        hobbit.addCategory(fantasy);
        hobbit.addCategory(adventure);
        tolkien.addBook(hobbit);
        em.persist(rowling);
        em.persist(tolkien);
        em.persist(new Author("New Library Author"));
        em.persist(new Employee("Mona Hassan", "Librarian"));
        em.persist(new Customer("Omar Ali", "LIB-001"));
        return rowling.getId();
    }
}
