package com.rentman.rentman.controller;

import com.rentman.rentman.dto.LoginRequest;
import com.rentman.rentman.dto.RegisterRequest;
import com.rentman.rentman.entity.User;
import com.rentman.rentman.security.JwtUtil;
import com.rentman.rentman.service.CustomUserDetailsService;
import com.rentman.rentman.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            String jwt = jwtUtil.generateToken(userDetails);

            // Get user details
            CustomUserDetailsService.CustomUserPrincipal principal = 
                (CustomUserDetailsService.CustomUserPrincipal) userDetails;
            User user = principal.getUser();

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("expiresIn", jwtUtil.getExpirationTime());
            response.put("user", createUserResponse(user));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.registerUser(registerRequest);
            
            // Generate JWT token for the new user
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String jwt = jwtUtil.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("expiresIn", jwtUtil.getExpirationTime());
            response.put("user", createUserResponse(user));
            response.put("message", "User registered successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/register-platform-admin")
    public ResponseEntity<?> registerPlatformAdmin(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Security note: In production, you should protect this endpoint or only allow it once
            // You might want to add a secret key check or disable after first admin is created
            
            User user = userService.registerPlatformAdmin(registerRequest);
            
            // Generate JWT token for the new admin
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String jwt = jwtUtil.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("expiresIn", jwtUtil.getExpirationTime());
            response.put("user", createUserResponse(user));
            response.put("message", "Platform administrator registered successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                if (jwtUtil.validateToken(token)) {
                    String newToken = jwtUtil.refreshToken(token);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("token", newToken);
                    response.put("type", "Bearer");
                    response.put("expiresIn", jwtUtil.getExpirationTime());
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token refresh failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Since we're using JWT, logout is handled on the client side by removing the token
        // We could implement a token blacklist here if needed
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
                CustomUserDetailsService.CustomUserPrincipal principal = 
                    (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
                User user = principal.getUser();
                
                Map<String, Object> response = new HashMap<>();
                response.put("user", createUserResponse(user));
                
                return ResponseEntity.ok(response);
            }
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get current user");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        try {
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Current password and new password are required");
                return ResponseEntity.badRequest().body(error);
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
                CustomUserDetailsService.CustomUserPrincipal principal = 
                    (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
                User user = principal.getUser();
                
                // Verify current password
                if (!userService.verifyPassword(user.getId(), currentPassword)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Current password is incorrect");
                    return ResponseEntity.badRequest().body(error);
                }
                
                // Update password
                userService.updatePassword(user.getId(), newPassword);
                
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password updated successfully");
                return ResponseEntity.ok(response);
            }
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to change password: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("email", user.getEmail());
        userResponse.put("phoneNumber", user.getPhoneNumber());
        userResponse.put("role", user.getRole());
        userResponse.put("status", user.getStatus());
        userResponse.put("emailVerified", user.getEmailVerified());
        userResponse.put("phoneVerified", user.getPhoneVerified());
        
        if (user.getCompany() != null) {
            Map<String, Object> companyInfo = new HashMap<>();
            companyInfo.put("id", user.getCompany().getId());
            companyInfo.put("name", user.getCompany().getCompanyName());
            userResponse.put("company", companyInfo);
        }
        
        // Add permissions
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("canManageFleet", user.getCanManageFleet() != null ? user.getCanManageFleet() : false);
        permissions.put("canManageReservations", user.getCanManageReservations() != null ? user.getCanManageReservations() : false);
        permissions.put("canManageEmployees", user.getCanManageEmployees() != null ? user.getCanManageEmployees() : false);
        permissions.put("canViewReports", user.getCanViewReports() != null ? user.getCanViewReports() : false);
        permissions.put("canManageFinances", user.getCanManageFinances() != null ? user.getCanManageFinances() : false);
        userResponse.put("permissions", permissions);
        
        return userResponse;
    }
}
