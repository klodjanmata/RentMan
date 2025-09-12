package com.rentman.rentman.service;

import com.rentman.rentman.dto.UserRegistrationDto;
import com.rentman.rentman.dto.RegisterRequest;
import com.rentman.rentman.entity.User;
import com.rentman.rentman.entity.Company;
import com.rentman.rentman.repository.UserRepository;
import com.rentman.rentman.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register new user
    public User registerUser(UserRegistrationDto registrationDto) {
        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists: " + registrationDto.getEmail());
        }

        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(registrationDto.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists: " + registrationDto.getPhoneNumber());
        }

        // Create new user
        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setDateOfBirth(registrationDto.getDateOfBirth());
        user.setDriverLicenseNumber(registrationDto.getDriverLicenseNumber());
        user.setLicenseExpiryDate(registrationDto.getLicenseExpiryDate());
        user.setStreetAddress(registrationDto.getStreetAddress());
        user.setCity(registrationDto.getCity());
        user.setState(registrationDto.getState());
        user.setPostalCode(registrationDto.getPostalCode());
        user.setCountry(registrationDto.getCountry());
        user.setRole(registrationDto.getRole());

        // Employee specific fields
        if (registrationDto.getRole() == User.UserRole.EMPLOYEE ||
                registrationDto.getRole() == User.UserRole.ADMIN) {
            user.setEmployeeId(registrationDto.getEmployeeId());
            user.setDepartment(registrationDto.getDepartment());
            user.setHireDate(registrationDto.getHireDate());
        }

        return userRepository.save(user);
    }

    // Find user by email (for login)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Register user from RegisterRequest
    public User registerUser(RegisterRequest registerRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists: " + registerRequest.getEmail());
        }

        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists: " + registerRequest.getPhoneNumber());
        }

        // Create new user
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setDateOfBirth(registerRequest.getDateOfBirth());
        user.setDriverLicenseNumber(registerRequest.getDriverLicenseNumber());
        user.setLicenseExpiryDate(registerRequest.getLicenseExpiryDate());
        user.setStreetAddress(registerRequest.getStreetAddress());
        user.setCity(registerRequest.getCity());
        user.setState(registerRequest.getState());
        user.setPostalCode(registerRequest.getPostalCode());
        user.setCountry(registerRequest.getCountry());
        user.setRole(registerRequest.getRole());

        // Set company if provided
        if (registerRequest.getCompanyId() != null) {
            Company company = companyRepository.findById(registerRequest.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + registerRequest.getCompanyId()));
            user.setCompany(company);
        }

        // Employee specific fields
        if (registerRequest.getRole() == User.UserRole.EMPLOYEE ||
                registerRequest.getRole() == User.UserRole.COMPANY_ADMIN) {
            user.setEmployeeId(registerRequest.getEmployeeId());
            user.setDepartment(registerRequest.getDepartment());
            user.setHireDate(registerRequest.getHireDate());
        }

        return userRepository.save(user);
    }

    // Login user (simplified - no password hashing yet)
    public Optional<User> loginUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            // Update last login
            User loginUser = user.get();
            loginUser.setLastLogin(LocalDateTime.now());
            userRepository.save(loginUser);
            return user;
        }
        return Optional.empty();
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Get users by role
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }

    // Get all customers
    public List<User> getAllCustomers() {
        return userRepository.findByRoleOrderByCreatedAtDesc(User.UserRole.CUSTOMER);
    }

    // Get all employees
    public List<User> getAllEmployees() {
        return userRepository.findByRole(User.UserRole.EMPLOYEE);
    }

    // Get all admins
    public List<User> getAllAdmins() {
        return userRepository.findByRole(User.UserRole.ADMIN);
    }

    // Update user
    public User updateUser(Long id, User updatedUser) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            user.setDateOfBirth(updatedUser.getDateOfBirth());
            user.setDriverLicenseNumber(updatedUser.getDriverLicenseNumber());
            user.setLicenseExpiryDate(updatedUser.getLicenseExpiryDate());
            user.setStreetAddress(updatedUser.getStreetAddress());
            user.setCity(updatedUser.getCity());
            user.setState(updatedUser.getState());
            user.setPostalCode(updatedUser.getPostalCode());
            user.setCountry(updatedUser.getCountry());
            user.setPreferredLanguage(updatedUser.getPreferredLanguage());
            user.setEmailNotifications(updatedUser.getEmailNotifications());
            user.setSmsNotifications(updatedUser.getSmsNotifications());

            return userRepository.save(user);
        }
        throw new RuntimeException("User not found with id: " + id);
    }

    // Update user status
    public User updateUserStatus(Long id, User.UserStatus status) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setStatus(status);
            return userRepository.save(existingUser);
        }
        throw new RuntimeException("User not found with id: " + id);
    }

    // Delete user
    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    // Search users by name
    public List<User> searchUsersByName(String name) {
        return userRepository.findByFullNameContaining(name);
    }

    // Get user statistics
    public List<Object[]> getUserStatistics() {
        return userRepository.getUserCountByRole();
    }

    // Count users by role
    public long countUsersByRole(User.UserRole role) {
        return userRepository.countByRole(role);
    }

    // Get recent customers (last 30 days)
    public List<User> getRecentCustomers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return userRepository.findRecentCustomers(User.UserRole.CUSTOMER, thirtyDaysAgo);
    }

    // Verify email
    public User verifyEmail(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setEmailVerified(true);
            return userRepository.save(existingUser);
        }
        throw new RuntimeException("User not found with id: " + userId);
    }

    // Verify phone
    public User verifyPhone(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setPhoneVerified(true);
            return userRepository.save(existingUser);
        }
        throw new RuntimeException("User not found with id: " + userId);
    }

    // Verify password
    public boolean verifyPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return passwordEncoder.matches(password, user.getPassword());
    }

    // Update password
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}