package io.github.spl21.bookstore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.spl21.bookstore.dto.LoginResult;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.repository.UserRepository;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Registration
    public boolean registerUser(User user) {
        // check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return false;
        }
        
        userRepository.save(user);
        return true;
    }
    
    // login and validation
    @Transactional(readOnly = true)
    public LoginResult authenticate(String email, String password) {
        // first try to find the user by email
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            // no account registered with the email
            return new LoginResult(false, "No account found. Please register first", null);
        }
        if (!user.getPassword().equals(password)) {
            return new LoginResult(false, "Incorrect password. Please try again", null);
        }
        
        return new LoginResult(true, "Login successful", user);
    }
    
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public User findUserById(Long id) { // Updated identifier type to Long
        return userRepository.findById(id).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public User findUserByEmailAndPassword(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    public void updateUser(User user) {
        if (user != null) {
            userRepository.save(user); // save() acts as update/merge automatically
        }
    }
    
    public void deleteUser(Long userId) { // Updated identifier type to Long
        userRepository.deleteById(userId);
    }
}
