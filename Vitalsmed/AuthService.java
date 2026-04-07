package com.trithread.vitalsmed.service;

import com.trithread.vitalsmed.model.User;
import com.trithread.vitalsmed.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    public String register(User user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return "Username already exists";
        }
        userRepo.save(user);
        return "Registered successfully";
    }

    public String login(String username, String password) {
        Optional<User> user = userRepo.findByUsername(username);

        if (user.isEmpty()) return "User not found";
        if (!user.get().getPassword().equals(password)) return "Wrong password";

        return "Login successful";
    }
}