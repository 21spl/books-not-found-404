package io.github.spl21.bookstore.controller;


import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.Purchase;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.service.BookService;
import io.github.spl21.bookstore.service.PurchaseService;
import io.github.spl21.bookstore.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final BookService bookService;
    
    private final UserService userService;

    private final PurchaseService purchaseService;

    AdminController(BookService bookService, UserService userService, PurchaseService purchaseService) {
        this.bookService = bookService;
        this.userService = userService;
        this.purchaseService = purchaseService;
    }
    
    // show admin dashboard
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
    
    // show add book form
    @GetMapping("/add_book")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "admin/add_book";
    }
    
    // save books from AddBookForm
    @PostMapping("/add_book")
    public String addBook(@ModelAttribute("book") Book book, RedirectAttributes redirectAttributes) {
        try {
            Book existing = bookService.addBookIfNotExists(book);
            if (existing != null) {
                redirectAttributes.addFlashAttribute("error", "Book already exists. You can edit it instead.");
                return "redirect:/admin/edit_book/" + existing.getId();
            }
            redirectAttributes.addFlashAttribute("success", "Book added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Something went wrong while adding the book.");
        }
        return "redirect:/admin/add_book";
    }
    
    // show edit book form
    @GetMapping("/edit_book/{id}")
    public String showEditBookForm(@PathVariable("id") Long bookId, Model model, RedirectAttributes redirectAttributes) { // Updated to Long
        Book book = bookService.getBook(bookId);
        if (book == null) {
            redirectAttributes.addFlashAttribute("error", "Book not found.");
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("book", book);
        return "admin/edit_book";
    }
    
    @PostMapping("/update_book")
    public String updateBook(@ModelAttribute("book") Book book, RedirectAttributes redirectAttributes) {
        try {
            bookService.updateBook(book);
            redirectAttributes.addFlashAttribute("success", "Book updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update book.");
        }
        return "redirect:/admin/view_books";
    }
    
    @GetMapping("/view_books")
    public String viewAllBooks(Model model) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        return "admin/view_books";
    }
    
    @GetMapping("/delete_book/{id}")
    public String showDeletePage(@PathVariable("id") Long id, Model model) { // Updated to Long
        Book book = bookService.getBook(id);
        model.addAttribute("book", book);
        return "admin/delete_book";
    }
    
    @PostMapping("/delete_book/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) { // Updated to Long
        bookService.deleteBook(id);
        redirectAttributes.addFlashAttribute("message", "Book deleted successfully!");
        return "redirect:/admin/view_books";
    }
    
    @GetMapping("/view_purchases")
    public String viewAllPurchases(Model model) {
        List<Purchase> purchases = purchaseService.getAllPurchasesWithDetails();
        model.addAttribute("purchases", purchases);
        return "admin/view_purchases";
    }
    
    @GetMapping("/view_users")
    public String viewAllUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/view_users";
    }

    @GetMapping("/purchases/{id}")
    public String viewPurchaseDetails(@PathVariable Long id, Model model) {
        Purchase purchase = purchaseService.findByIdWithItems(id);

        if (purchase == null) {
            return "redirect:/admin/view_purchases";
        }

        model.addAttribute("purchase", purchase);
        return "admin/purchase_details";
    }
}
