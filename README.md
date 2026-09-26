# Library Management System — JPA practice

A simple Java console project implementing both tasks in `JPA_Day2_2_Main_Tasks.pdf`, using Jakarta Persistence, Hibernate and an H2 in-memory database. No Spring or database installation is needed.

## Run

Requires JDK 17+ and Maven 3.9+ (or IntelliJ's bundled Maven). Open `pom.xml` as a Maven project in your IDE.

```sh
mvn clean test
mvn exec:java
```

Alternatively run `com.example.library.Main` from the IDE after Maven imports dependencies. Each run creates fresh sample data; tables are dropped when the application closes. SQL appears in the console, alongside labeled results. The intentional lazy-loading exception is caught, so a successful run continues to the orphan-removal demonstration.

## Model and mapping choices

| Relationship | Owning side | Inverse side | Storage |
| --- | --- | --- | --- |
| Author to books | `Book.author` | `Author.books`, `mappedBy = "author"` | `books.author_id` |
| Book to publisher | `Book.publisher` | None (unidirectional) | `books.publisher_id` |
| Books to categories | `Book.categories` | `Category.books`, `mappedBy = "categories"` | `book_categories(book_id, category_id)` |

Collections and both to-one relationships explicitly use LAZY fetching. A many-to-many relationship needs a join table because either side can have multiple matches. A many-to-one needs only a foreign key on the book row.

`Author.addBook()` and `removeBook()` synchronize both sides. `addBook()` rejects moving a book from another author: with orphan removal, removing it from its original parent schedules deletion. Category helpers also synchronize both sides.

`Author.books` uses `CascadeType.ALL` and `orphanRemoval = true`. Persisting an author inserts their new books without calling `persist(book)`. Removing a book from the author's collection deletes that book at flush/commit, including its category join rows. Publisher and category are shared entities, so they are persisted explicitly and have no remove cascade. Deleting a book must not delete its publisher or categories.

`Employee` and `Customer` extend the abstract `Person` entity. `SINGLE_TABLE` stores both in `people`, distinguished by `person_type`. This is simple for a small hierarchy and supports polymorphic queries without joins. The tradeoff is nullable subtype columns (`jobTitle` and `membershipNumber`).

## Queries and demonstrations

`LibraryQueries` contains named-parameter JPQL queries for author and publisher, a positional `?1` lookup by book ID, an author/books fetch join, author book counts, and Criteria queries with optional title and author-name filters. Criteria matches case-insensitive substrings, treats blank filters as absent, and escapes literal `%` and `_` characters.

The aggregate query uses LEFT JOIN so authors with no books have count zero. Grouping by both author ID and name keeps authors with identical names separate. The fetch query also uses LEFT JOIN to support an author with no books.

`Main` loads an author in a fresh persistence context, closes it, then accesses the uninitialized books collection. Hibernate throws `LazyInitializationException` because no open session can load those books. A second query uses `JOIN FETCH` to initialize the collection before closing, so accessing it afterward succeeds. This initializes only the requested books collection, not every nested lazy association such as each book's publisher or categories.

All application query strings use standard JPQL. For example, `select b from Book b where b.author.name = :name` works across JPA providers. Hibernate HQL additionally supports features such as common table expressions (`with ...`); such syntax is provider-specific and should not be assumed portable JPQL.

The console inspects H2's `INFORMATION_SCHEMA` to list the generated tables and foreign-key columns. Hibernate's printed DDL shows their referenced tables. Sample data includes two published authors, three books, two publishers, two shared categories, an author with no books, an employee and a customer.

## Verification

Integration tests use real Hibernate and H2 to check cascade inserts, all query/filter combinations, polymorphic loading, detached lazy access, fetch joins, and orphan deletion while preserving shared publishers/categories. The optional `orm.xml` extension is omitted to keep mapping annotations easy to follow.
