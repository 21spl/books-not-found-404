package io.github.spl21.bookstore.repository;

import io.github.spl21.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;


public interface BookRepository extends JpaRepository<Book, Long> {

    // 1. Direct query matching field combinations
    Optional<Book> findByTitleAndAuthor(String title, String author);

    // 2. Case-insensitive fuzzy search using 'ContainingIgnoreCase' (replaces standard manual lower/LIKE wildcarding)
    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByGenreContainingIgnoreCase(String genre);

    // 3. Custom selection requiring a specific JPQL query
    @Query("SELECT DISTINCT b.genre FROM Book b WHERE b.genre IS NOT NULL")
    List<String> findDistinctGenres();
}
