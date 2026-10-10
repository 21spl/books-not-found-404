package io.github.spl21.bookstore.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.repository.BookRepository;
import io.github.spl21.bookstore.service.ExternalBookService;
import io.github.spl21.bookstore.service.ExternalBookService.ExternalBook;

@Component
public class BookDataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final ExternalBookService externalBookService;

    public BookDataSeeder(
            BookRepository bookRepository,
            ExternalBookService externalBookService) {
        this.bookRepository = bookRepository;
        this.externalBookService = externalBookService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        var externalBooks = externalBookService.fetchBestSellingBooks();

        int inserted = 0;

        for (ExternalBook externalBook : externalBooks) {

            // Avoid inserting a book that already exists.
            if (bookRepository
                    .findByTitleAndAuthor(
                            externalBook.getTitle(),
                            externalBook.getAuthor())
                    .isPresent()) {
                continue;
            }

            Book book = new Book();

            book.setTitle(externalBook.getTitle());
            book.setAuthor(externalBook.getAuthor());
            book.setGenre(externalBook.getGenre());
            book.setPrice(externalBook.getPrice());

            // These fields are not supplied by ExternalBook.
            book.setCopies(10);
            book.setDescription(
                    externalBook.getTitle() + " by " + externalBook.getAuthor());

            bookRepository.save(book);
            inserted++;
        }

        System.out.println(
                "Book seeding complete. Inserted " + inserted + " new books.");
    }
}