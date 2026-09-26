package com.example.library.model;

import jakarta.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    protected Book() {}
    public Book(String title, Publisher publisher) { this.title = title; this.publisher = publisher; }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Author getAuthor() { return author; }
    void setAuthor(Author author) { this.author = author; }
    public Publisher getPublisher() { return publisher; }
    public Set<Category> getCategories() { return Collections.unmodifiableSet(categories); }
    public void addCategory(Category category) {
        if (categories.add(category)) { category.addBook(this); }
    }
    public void removeCategory(Category category) {
        if (categories.remove(category)) { category.removeBook(this); }
    }
    @Override public String toString() { return id + ": " + title; }
}
