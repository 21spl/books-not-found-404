package io.github.spl21.bookstore.service;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    public void addBook(Book book) {
        bookRepository.save(book);
    }
    
    public Book addBookIfNotExists(Book book) {
        Book existing = bookRepository.findByTitleAndAuthor(book.getTitle(), book.getAuthor()).orElse(null);
        if (existing != null) {
            return existing;
        }
        bookRepository.save(book);
        return null; 
    }
    
    public void updateBook(Book book) {
        bookRepository.save(book); 
    }
    
    public void deleteBook(Long bookId) { // Updated to Long
        bookRepository.deleteById(bookId);
    }
    
    @Transactional(readOnly = true)
    public Book getBook(Long bookId) { // Updated to Long
        return bookRepository.findById(bookId).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
       return bookRepository.findAll(); 
    }
    
    @Transactional(readOnly = true)
    public List<Book> searchBooks(String keyword) {
        Set<Book> results = new LinkedHashSet<>();
        
        // Spring Data JPA guaranteed collections are never null, eliminating null checks
        results.addAll(bookRepository.findByTitleContainingIgnoreCase(keyword));
        results.addAll(bookRepository.findByAuthorContainingIgnoreCase(keyword));
        results.addAll(bookRepository.findByGenreContainingIgnoreCase(keyword));
        
        return new ArrayList<>(results);
    }
    
    @Transactional(readOnly = true)
    public boolean isBookInStock(Long bookId) { // Updated to Long
        Book book = bookRepository.findById(bookId).orElse(null);
        return book != null && book.getCopies() > 0;
    }
    
    @Transactional(readOnly = true)
    public List<String> getAllGenres() {
        return bookRepository.findDistinctGenres();
    }
    
    @Transactional(readOnly = true)
    public List<Book> searchBooksByGenre(String genre) {
        return bookRepository.findByGenreContainingIgnoreCase(genre);
    }
}
