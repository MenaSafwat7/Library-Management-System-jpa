package com.example.library.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Book> books = new ArrayList<>();

    protected Author() {}
    public Author(String name) { this.name = name; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Book> getBooks() { return Collections.unmodifiableList(books); }
    public void addBook(Book book) {
        if (book.getAuthor() != null && book.getAuthor() != this) {
            throw new IllegalArgumentException("A book already belongs to another author");
        }
        if (!books.contains(book)) {
            books.add(book);
            book.setAuthor(this);
        }
    }
    public void removeBook(Book book) {
        if (books.remove(book)) { book.setAuthor(null); }
    }
}
