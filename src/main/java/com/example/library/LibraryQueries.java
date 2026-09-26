package com.example.library;

import com.example.library.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LibraryQueries {
    private final EntityManager em;
    public LibraryQueries(EntityManager em) { this.em = em; }

    public List<Book> booksByAuthor(String name) {
        return em.createQuery("select b from Book b where b.author.name = :name order by b.id", Book.class)
                .setParameter("name", name).getResultList();
    }
    public List<Book> booksByPublisher(String name) {
        return em.createQuery("select b from Book b where b.publisher.name = :name order by b.id", Book.class)
                .setParameter("name", name).getResultList();
    }
    public Book bookById(Long id) {
        return em.createQuery("select b from Book b where b.id = ?1", Book.class)
                .setParameter(1, id).getResultStream().findFirst().orElse(null);
    }
    public Author authorWithBooks(Long id) {
        return em.createQuery("select distinct a from Author a left join fetch a.books where a.id = :id", Author.class)
                .setParameter("id", id).getSingleResult();
    }
    public List<Object[]> authorBookCounts() {
        return em.createQuery("select a.name, count(b) from Author a left join a.books b group by a.id, a.name order by a.name", Object[].class)
                .getResultList();
    }
    public List<Book> booksByTitle(String title) { return searchBooks(title, null); }

    // Null or blank filters are omitted; matching is a case-insensitive substring.
    public List<Book> searchBooks(String title, String authorName) {
        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(Book.class);
        var book = query.from(Book.class);
        List<Predicate> predicates = new ArrayList<>();
        if (title != null && !title.isBlank()) {
            predicates.add(cb.like(cb.lower(book.get("title")), pattern(title), '\\'));
        }
        if (authorName != null && !authorName.isBlank()) {
            predicates.add(cb.like(cb.lower(book.get("author").get("name")), pattern(authorName), '\\'));
        }
        query.select(book).where(predicates.toArray(Predicate[]::new)).orderBy(cb.asc(book.get("id")));
        return em.createQuery(query).getResultList();
    }
    private String pattern(String text) {
        return "%" + text.trim().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
