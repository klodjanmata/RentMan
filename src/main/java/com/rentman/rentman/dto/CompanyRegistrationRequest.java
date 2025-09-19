package com.rentman.rentman.dto;

import com.rentman.rentman.entity.Company;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyRegistrationRequest {
    
    // Company Information
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    @NotBlank(message = "Business registration number is required")
    private String businessRegistrationNumber;
    
    @NotBlank(message = "Tax ID is required")
    private String taxId;
    
    @NotBlank(message = "Company email is required")
    @Email(message = "Please provide a valid company email address")
    private String companyEmail;
    
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    
    @NotBlank(message = "Website is required")
    private String website;
    
    // Address Information
    @NotBlank(message = "Street address is required")
    private String streetAddress;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
    
    @NotBlank(message = "Postal code is required")
    private String postalCode;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    // Business Information
    private String businessType;
    private String description;
    private String contactPersonName;
    private String contactPersonTitle;
    
    // Subscription Plan
    @NotNull(message = "Subscription plan is required")
    private Company.SubscriptionPlan subscriptionPlan;
    
    // Admin User Information
    @NotBlank(message = "Admin first name is required")
    private String adminFirstName;
    
    @NotBlank(message = "Admin last name is required")
    private String adminLastName;
    
    @NotBlank(message = "Admin email is required")
    @Email(message = "Please provide a valid admin email address")
    private String adminEmail;
    
    @NotBlank(message = "Admin password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String adminPassword;
    
    @NotBlank(message = "Admin phone number is required")
    private String adminPhoneNumber;
    
    // Admin Address (can be same as company or different)
    private String adminStreetAddress;
    private String adminCity;
    private String adminState;
    private String adminPostalCode;
    private String adminCountry;
    
    // Admin Personal Information
    private String adminDateOfBirth;
    private String adminDriverLicenseNumber;
    private String adminLicenseExpiryDate;
}
