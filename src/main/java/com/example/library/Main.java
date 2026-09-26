package com.example.library;

import com.example.library.model.*;
import jakarta.persistence.*;
import org.hibernate.LazyInitializationException;
import java.util.Arrays;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        try (EntityManagerFactory factory = Persistence.createEntityManagerFactory("libraryPU")) {
            Long authorId = inTransaction(factory, SampleData::insert);
            showQueries(factory);
            showLazyLoading(factory, authorId);
            showOrphanRemoval(factory, authorId);
        }
    }

    private static void showQueries(EntityManagerFactory factory) {
        inTransaction(factory, em -> {
            LibraryQueries queries = new LibraryQueries(em);
            System.out.println("By author: " + queries.booksByAuthor("J. K. Rowling"));
            System.out.println("By publisher: " + queries.booksByPublisher("Allen & Unwin"));
            Long bookId = queries.booksByAuthor("J. K. Rowling").get(0).getId();
            System.out.println("By positional id: " + queries.bookById(bookId));
            System.out.println("Criteria title: " + queries.booksByTitle("hobbit"));
            System.out.println("Criteria both filters: " + queries.searchBooks("Harry", "Rowling"));
            System.out.println("Criteria author only: " + queries.searchBooks(null, "Tolkien"));
            System.out.println("Criteria no filters: " + queries.searchBooks(null, null));
            queries.authorBookCounts().forEach(row -> System.out.println(row[0] + ": " + row[1] + " books"));
            System.out.println("People (polymorphic query): " + em.createQuery("select p from Person p", Person.class).getResultList().size());
            showDatabaseTables(em);
            return null;
        });
    }


    private static void showDatabaseTables(EntityManager em) {
        System.out.println("Generated tables:");
        em.createNativeQuery("select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = 'PUBLIC' order by TABLE_NAME")
                .getResultList().forEach(System.out::println);
        System.out.println("Foreign key columns:");
        em.createNativeQuery("select k.TABLE_NAME, k.COLUMN_NAME, k.CONSTRAINT_NAME from INFORMATION_SCHEMA.KEY_COLUMN_USAGE k join INFORMATION_SCHEMA.TABLE_CONSTRAINTS c on k.CONSTRAINT_NAME = c.CONSTRAINT_NAME and k.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA where c.CONSTRAINT_TYPE = 'FOREIGN KEY' order by k.TABLE_NAME, k.COLUMN_NAME")
                .getResultList().forEach(row -> System.out.println(Arrays.toString((Object[]) row)));
    }

    private static void showLazyLoading(EntityManagerFactory factory, Long authorId) {
        Author detached = inTransaction(factory, em -> em.find(Author.class, authorId));
        try {
            System.out.println(detached.getBooks().size());
            throw new IllegalStateException("Expected an uninitialized detached collection");
        } catch (LazyInitializationException expected) {
            System.out.println("Expected LazyInitializationException: books were not loaded before the EntityManager closed.");
        }
        Author fetched = inTransaction(factory, em -> new LibraryQueries(em).authorWithBooks(authorId));
        System.out.println("JOIN FETCH works after closing: " + fetched.getBooks().size() + " books");
    }

    private static void showOrphanRemoval(EntityManagerFactory factory, Long authorId) {
        Long deletedId = inTransaction(factory, em -> {
            Author author = em.find(Author.class, authorId);
            Book book = author.getBooks().get(0);
            Long id = book.getId();
            author.removeBook(book);
            return id;
        });
        inTransaction(factory, em -> {
            System.out.println("Orphan removed from database: " + (em.find(Book.class, deletedId) == null));
            return null;
        });
    }


    public static <T> T inTransaction(EntityManagerFactory factory, Function<EntityManager, T> work) {
        try (EntityManager em = factory.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            try {
                transaction.begin();
                T result = work.apply(em);
                transaction.commit();
                return result;
            } catch (RuntimeException exception) {
                if (transaction.isActive()) { transaction.rollback(); }
                throw exception;
            }
        }
    }
}
