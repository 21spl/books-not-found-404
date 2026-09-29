package io.github.spl21.bookstore.controller;


import io.github.spl21.bookstore.entity.*;
import io.github.spl21.bookstore.repository.*;
import io.github.spl21.bookstore.service.*;
import io.github.spl21.bookstore.enums.*;
import io.github.spl21.bookstore.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private PurchaseService purchaseService;
    
    // show admin dashboard
    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
    
    // show add book form
    @GetMapping("/admin/add_book")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "admin/add_book";
    }
    
    // save books from AddBookForm
    @PostMapping("/admin/add_book")
    public String addBook(@ModelAttribute("book") Book book, RedirectAttributes redirectAttributes) {
        try {
            Book existing = bookService.addBookIfNotExists(book);
            if (existing != null) {
                redirectAttributes.addFlashAttribute("error", "Book already exists. You can edit it instead.");
                return "redirect:/admin/edit_book/" + existing.getBookId();
            }
            redirectAttributes.addFlashAttribute("success", "Book added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong while adding the book.");
        }
        return "redirect:/admin/add_book";
    }
    
    // show edit book form
    @GetMapping("/admin/edit_book/{id}")
    public String showEditBookForm(@PathVariable("id") Long bookId, Model model, RedirectAttributes redirectAttributes) { // Updated to Long
        Book book = bookService.getBook(bookId);
        if (book == null) {
            redirectAttributes.addFlashAttribute("error", "Book not found.");
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("book", book);
        return "admin/edit_book";
    }
    
    @PostMapping("/admin/update_book")
    public String updateBook(@ModelAttribute("book") Book book, RedirectAttributes redirectAttributes) {
        try {
            bookService.updateBook(book);
            redirectAttributes.addFlashAttribute("success", "Book updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update book.");
        }
        return "redirect:/admin/view_books";
    }
    
    @GetMapping("/admin/view_books")
    public String viewAllBooks(Model model) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        return "admin/view_books";
    }
    
    @GetMapping("/admin/delete_book/{id}")
    public String showDeletePage(@PathVariable("id") Long id, Model model) { // Updated to Long
        Book book = bookService.getBook(id);
        model.addAttribute("book", book);
        return "admin/delete_book";
    }
    
    @PostMapping("/admin/delete_book/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) { // Updated to Long
        bookService.deleteBook(id);
        redirectAttributes.addFlashAttribute("message", "Book deleted successfully!");
        return "redirect:/admin/view_books";
    }
    
    @GetMapping("/admin/view_purchases")
    public String viewAllPurchases(Model model) {
        List<Purchase> purchases = purchaseService.getAllPurchasesWithDetails();
        model.addAttribute("purchases", purchases);
        return "admin/view_purchases";
    }
    
    @GetMapping("/admin/view_users")
    public String viewAllUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/view_users";
    }
}
