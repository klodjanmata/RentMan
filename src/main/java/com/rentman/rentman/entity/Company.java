package com.rentman.rentman.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "company")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Company name is required")
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @NotBlank(message = "Business registration number is required")
    @Column(name = "business_registration_number", unique = true, nullable = false)
    private String businessRegistrationNumber;

    @NotBlank(message = "Tax ID is required")
    @Column(name = "tax_id", unique = true, nullable = false)
    private String taxId;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @NotBlank(message = "Website is required")
    private String website;

    // Address information
    @NotBlank(message = "Street address is required")
    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "State is required")
    @Column(nullable = false)
    private String state;

    @NotBlank(message = "Postal code is required")
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Column(nullable = false)
    private String country;

    // Business information
    @Column(name = "business_type")
    private String businessType; // LLC, Corporation, Partnership, etc.

    @Column(name = "founded_date")
    private LocalDateTime foundedDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    // Subscription and billing
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.BASIC;

    @Column(name = "monthly_fee", precision = 10, scale = 2)
    private BigDecimal monthlyFee = BigDecimal.ZERO;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate = BigDecimal.valueOf(0.05); // 5% default

    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;

    @Column(name = "payment_method")
    private String paymentMethod; // Credit card, Bank transfer, etc.

    // Company status and settings
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyStatus status = CompanyStatus.PENDING_APPROVAL;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "max_vehicles")
    private Integer maxVehicles = 50; // Based on subscription plan

    @Column(name = "max_employees")
    private Integer maxEmployees = 10; // Based on subscription plan

    // Operating hours
    @Column(name = "operating_hours_start")
    private String operatingHoursStart = "08:00";

    @Column(name = "operating_hours_end")
    private String operatingHoursEnd = "18:00";

    @Column(name = "operating_days")
    private String operatingDays = "Monday-Sunday"; // JSON array of days

    // Contact information
    @Column(name = "contact_person_name")
    private String contactPersonName;

    @Column(name = "contact_person_title")
    private String contactPersonTitle;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    // Bank information for payments
    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_routing_number")
    private String bankRoutingNumber;

    // Performance metrics
    @Column(name = "total_bookings")
    private Long totalBookings = 0L;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    private Long totalReviews = 0L;

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy; // User ID of admin who approved

    // Relationships
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vehicle> vehicles;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> employees;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    // @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Invoice> invoices;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (subscriptionStartDate == null) {
            subscriptionStartDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods
    public boolean isActive() {
        return status == CompanyStatus.ACTIVE;
    }

    public boolean canAddVehicle() {
        return vehicles == null || vehicles.size() < maxVehicles;
    }

    public boolean canAddEmployee() {
        return employees == null || employees.size() < maxEmployees;
    }

    public boolean isSubscriptionActive() {
        return subscriptionEndDate == null || subscriptionEndDate.isAfter(LocalDateTime.now());
    }

    public BigDecimal calculateCommission(BigDecimal amount) {
        return amount.multiply(commissionRate);
    }

    // Enums
    public enum SubscriptionPlan {
        BASIC("Basic", 50, 10, BigDecimal.valueOf(99.00)),
        PROFESSIONAL("Professional", 200, 25, BigDecimal.valueOf(299.00)),
        ENTERPRISE("Enterprise", 1000, 100, BigDecimal.valueOf(599.00)),
        CUSTOM("Custom", 0, 0, BigDecimal.ZERO);

        private final String displayName;
        private final int maxVehicles;
        private final int maxEmployees;
        private final BigDecimal monthlyFee;

        SubscriptionPlan(String displayName, int maxVehicles, int maxEmployees, BigDecimal monthlyFee) {
            this.displayName = displayName;
            this.maxVehicles = maxVehicles;
            this.maxEmployees = maxEmployees;
            this.monthlyFee = monthlyFee;
        }

        public String getDisplayName() { return displayName; }
        public int getMaxVehicles() { return maxVehicles; }
        public int getMaxEmployees() { return maxEmployees; }
        public BigDecimal getMonthlyFee() { return monthlyFee; }
    }

    public enum CompanyStatus {
        PENDING_APPROVAL("Pending Approval"),
        ACTIVE("Active"),
        SUSPENDED("Suspended"),
        INACTIVE("Inactive"),
        REJECTED("Rejected");

        private final String displayName;

        CompanyStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
