package io.github.spl21.bookstore.controller;


import java.util.List;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.service.BookService;

@Controller
public class BookController {

    private final BookService bookService;

    BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam("query") String keyword, Model model) {
        List<Book> books = bookService.searchBooks(keyword);
        model.addAttribute("books", books);
        return "public/book_list";
    }

    @GetMapping("/books")
    public String exploreGenre(@RequestParam("genre") String genre, Model model) {
        List<Book> booksByGenre = bookService.searchBooksByGenre(genre);
        model.addAttribute("genre", genre);
        model.addAttribute("books", booksByGenre);
        return "public/book_list";
    }
}
