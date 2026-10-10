package io.github.spl21.bookstore.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.github.spl21.bookstore.service.BookService;
import io.github.spl21.bookstore.service.ExternalBookService;

@Controller
public class HomeController {
    
    private final ExternalBookService externalBookService;
    
    private final BookService bookService;

    HomeController(ExternalBookService externalBookService, BookService bookService) {
        this.externalBookService = externalBookService;
        this.bookService = bookService;
    }
    
    @GetMapping("/")
    public String showHomePage(Model model) {
        List<ExternalBookService.ExternalBook> bestSellers = externalBookService.fetchBestSellingBooks();
        List<String> genres = bookService.getAllGenres();

        model.addAttribute("bestSellers", bestSellers);
        model.addAttribute("genres", genres);

        return "public/home"; 
    }
}
