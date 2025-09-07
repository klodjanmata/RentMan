package com.rentman.rentman.dto;

import com.rentman.rentman.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

// DTO for user registration
@Data
public class UserRegistrationDto {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private LocalDate dateOfBirth;
    private String driverLicenseNumber;
    private LocalDate licenseExpiryDate;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // For employee/admin registration
    private User.UserRole role = User.UserRole.CUSTOMER;
    private String employeeId;
    private String department;
    private LocalDate hireDate;
}

// DTO for login
@Data
class UserLoginDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}

// DTO for user response (without password)
@Data
class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String driverLicenseNumber;
    private LocalDate licenseExpiryDate;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private User.UserRole role;
    private User.UserStatus status;
    private String employeeId;
    private String department;
    private LocalDate hireDate;
    private String profilePictureUrl;
    private Boolean emailVerified;
    private Boolean phoneVerified;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}

// DTO for updating user profile
@Data
class UserUpdateDto {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String driverLicenseNumber;
    private LocalDate licenseExpiryDate;
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String preferredLanguage;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
}