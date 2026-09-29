package io.github.spl21.bookstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.github.spl21.bookstore.service.BookService;
import io.github.spl21.bookstore.service.ExternalBookService;

@Controller
public class HomeController {
    
    @Autowired
    private ExternalBookService externalBookService;
    
    @Autowired
    private BookService bookService;
    
    @GetMapping("/")
    public String showHomePage(Model model) {
        List<ExternalBookService.ExternalBook> bestSellers = externalBookService.fetchBestSellingBooks();
        List<String> genres = bookService.getAllGenres();

        model.addAttribute("bestSellers", bestSellers);
        model.addAttribute("genres", genres);

        return "home"; 
    }
}
