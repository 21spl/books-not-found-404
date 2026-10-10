package io.github.spl21.bookstore.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.repository.UserRepository;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
