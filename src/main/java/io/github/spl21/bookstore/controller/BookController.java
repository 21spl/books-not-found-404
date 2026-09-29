package io.github.spl21.bookstore.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.service.BookService;

@Controller
public class BookController {

    @Autowired 
    private BookService bookService;

    @GetMapping("/search")
    public String searchBooks(@RequestParam("query") String keyword, Model model) {
        List<Book> books = bookService.searchBooks(keyword);
        model.addAttribute("books", books);
        return "book_list";
    }

    @GetMapping("/books")
    public String exploreGenre(@RequestParam("genre") String genre, Model model) {
        List<Book> booksByGenre = bookService.searchBooksByGenre(genre);
        model.addAttribute("genre", genre);
        model.addAttribute("books", booksByGenre);
        return "book_list";
    }
}
