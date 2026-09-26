package com.example.library;

import com.example.library.model.*;
import jakarta.persistence.*;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {
    private EntityManagerFactory factory;
    private Long authorId;
    @BeforeEach void setUp() {
        factory = Persistence.createEntityManagerFactory("libraryPU");
        authorId = Main.inTransaction(factory, SampleData::insert);
    }
    @AfterEach void tearDown() { factory.close(); }

    @Test void queriesAndCascadedSampleData() {
        Main.inTransaction(factory, em -> {
            var queries = new LibraryQueries(em);
            var books = queries.booksByAuthor("J. K. Rowling");
            assertEquals(2, books.size());
            assertEquals("The Hobbit", queries.booksByPublisher("Allen & Unwin").get(0).getTitle());
            assertEquals(books.get(0).getId(), queries.bookById(books.get(0).getId()).getId());
            assertNull(queries.bookById(-1L));
            assertEquals(1, queries.booksByTitle("HOBBIT").size());
            assertEquals(2, queries.searchBooks("Harry", "Rowling").size());
            assertEquals(1, queries.searchBooks(null, "Tolkien").size());
            assertEquals(3, queries.searchBooks(" ", null).size());
            assertTrue(queries.searchBooks("%", null).isEmpty());
            assertTrue(queries.searchBooks("Harry", "Tolkien").isEmpty());
            assertEquals(3, queries.authorBookCounts().size());
            assertTrue(queries.authorBookCounts().stream().anyMatch(row -> row[0].equals("New Library Author") && row[1].equals(0L)));
            assertEquals(2, em.createQuery("select p from Person p", Person.class).getResultList().size());
            return null;
        });
    }
    @Test void lazyFailureAndFetchJoin() {
        Author lazy = Main.inTransaction(factory, em -> em.find(Author.class, authorId));
        assertThrows(LazyInitializationException.class, () -> lazy.getBooks().size());
        Author fetched = Main.inTransaction(factory, em -> new LibraryQueries(em).authorWithBooks(authorId));
        assertEquals(2, fetched.getBooks().size());
    }
    @Test void orphanRemovalDeletesBookButKeepsSharedEntities() {
        Long removedId = Main.inTransaction(factory, em -> {
            Author author = em.find(Author.class, authorId);
            Book book = author.getBooks().get(0);
            assertSame(author, book.getAuthor());
            author.removeBook(book);
            assertNull(book.getAuthor());
            return book.getId();
        });
        Main.inTransaction(factory, em -> {
            assertNull(em.find(Book.class, removedId));
            assertEquals(1, em.find(Author.class, authorId).getBooks().size());
            assertEquals(2L, em.createQuery("select count(c) from Category c", Long.class).getSingleResult());
            assertEquals(2L, em.createQuery("select count(p) from Publisher p", Long.class).getSingleResult());
            assertEquals(0L, ((Number) em.createNativeQuery("select count(*) from book_categories where book_id = ?1").setParameter(1, removedId).getSingleResult()).longValue());
            return null;
        });
    }
}
