package io.github.spl21.bookstore.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.spl21.bookstore.entity.Book;


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

    // 4. fetches a specific book row and locks it explicitly
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithPessimisticWrite(@Param("id") Long id);
}
