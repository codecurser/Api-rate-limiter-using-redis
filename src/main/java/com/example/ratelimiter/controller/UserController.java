package com.example.ratelimiter.controller;

import com.example.ratelimiter.dto.ApiResponse;
import com.example.ratelimiter.dto.UserDto;
import com.example.ratelimiter.model.User;
import com.example.ratelimiter.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserDto userDto) {
        User user = new User(null, userDto.getName(), userDto.getEmail());
        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedUser, "User created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userRepository.findAll(), "Users fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable String id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success(user, "User fetched successfully")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable String id, @Valid @RequestBody UserDto userDto) {
        return userRepository.findById(id).map(user -> {
            user.setName(userDto.getName());
            user.setEmail(userDto.getEmail());
            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("User not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
