package com.rentman.rentman.controller;

import com.rentman.rentman.dto.UserRegistrationDto;
import com.rentman.rentman.entity.User;
import com.rentman.rentman.service.UserService;
import com.rentman.rentman.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // Register new user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User user = userService.registerUser(registrationDto);

            // Create response without password
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("status", user.getStatus());
            response.put("message", "User registered successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Login user (simplified)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        Optional<User> user = userService.loginUser(email, password);

        if (user.isPresent()) {
            User loginUser = user.get();

            Map<String, Object> response = new HashMap<>();
            response.put("id", loginUser.getId());
            response.put("firstName", loginUser.getFirstName());
            response.put("lastName", loginUser.getLastName());
            response.put("email", loginUser.getEmail());
            response.put("role", loginUser.getRole());
            response.put("status", loginUser.getStatus());
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            List<User> users = userService.getUsersByRole(userRole);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get all customers
    @GetMapping("/customers")
    public ResponseEntity<List<User>> getAllCustomers() {
        List<User> customers = userService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    // Get all employees
    @GetMapping("/employees")
    public ResponseEntity<List<User>> getAllEmployees() {
        List<User> employees = userService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    // Get all admins
    @GetMapping("/admins")
    public ResponseEntity<List<User>> getAllAdmins() {
        List<User> admins = userService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsService.CustomUserPrincipal principal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            User currentUser = principal.getUser();
            
            // Check if user is updating their own profile OR is an admin/company admin
            if (!currentUser.getId().equals(id) && 
                currentUser.getRole() != User.UserRole.ADMIN && 
                currentUser.getRole() != User.UserRole.COMPANY_ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "You can only update your own profile");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            User updatedUser = userService.updateUserProfile(id, updates);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Update user status
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
            User updatedUser = userService.updateUserStatus(id, userStatus);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid status: " + status);
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Search users by name
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String name) {
        List<User> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }

    // Get user statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalCustomers", userService.countUsersByRole(User.UserRole.CUSTOMER));
        stats.put("totalEmployees", userService.countUsersByRole(User.UserRole.EMPLOYEE));
        stats.put("totalAdmins", userService.countUsersByRole(User.UserRole.ADMIN));
        stats.put("recentCustomers", userService.getRecentCustomers());

        return ResponseEntity.ok(stats);
    }

    // Verify email
    @PatchMapping("/{id}/verify-email")
    public ResponseEntity<?> verifyEmail(@PathVariable Long id) {
        try {
            User user = userService.verifyEmail(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email verified successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Verify phone
    @PatchMapping("/{id}/verify-phone")
    public ResponseEntity<?> verifyPhone(@PathVariable Long id) {
        try {
            User user = userService.verifyPhone(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Phone verified successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}