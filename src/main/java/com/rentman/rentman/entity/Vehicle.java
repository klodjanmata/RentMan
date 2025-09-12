package com.rentman.rentman.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Make is required")
    @Column(nullable = false)
    private String make;

    @NotBlank(message = "Model is required")
    @Column(nullable = false)
    private String model;

    @NotNull(message = "Year is required")
    @Column(nullable = false)
    private Integer year;

    @NotBlank(message = "License plate is required")
    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @NotNull(message = "Daily rate is required")
    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "transmission")
    private String transmission;

    @Column(name = "seating_capacity")
    private Integer seatingCapacity;

    private String color;

    @Column(name = "mileage")
    private Integer mileage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    // Company relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Vehicle location and availability
    @Column(name = "current_location")
    private String currentLocation;

    @Column(name = "pickup_location")
    private String pickupLocation;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_available_for_rental")
    private Boolean isAvailableForRental = true;

    // Vehicle specifications
    @Column(name = "engine_size")
    private String engineSize;

    @Column(name = "fuel_capacity")
    private Integer fuelCapacity;

    @Column(name = "doors")
    private Integer doors;

    @Column(name = "luggage_capacity")
    private String luggageCapacity;

    @Column(name = "air_conditioning")
    private Boolean airConditioning = true;

    @Column(name = "gps_navigation")
    private Boolean gpsNavigation = false;

    @Column(name = "bluetooth")
    private Boolean bluetooth = false;

    @Column(name = "usb_charging")
    private Boolean usbCharging = false;

    @Column(name = "backup_camera")
    private Boolean backupCamera = false;

    @Column(name = "parking_sensors")
    private Boolean parkingSensors = false;

    @Column(name = "sunroof")
    private Boolean sunroof = false;

    @Column(name = "leather_seats")
    private Boolean leatherSeats = false;

    // Insurance and documentation
    @Column(name = "insurance_provider")
    private String insuranceProvider;

    @Column(name = "insurance_policy_number")
    private String insurancePolicyNumber;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "registration_expiry_date")
    private LocalDate registrationExpiryDate;

    @Column(name = "inspection_date")
    private LocalDate inspectionDate;

    @Column(name = "next_inspection_date")
    private LocalDate nextInspectionDate;

    // Performance metrics
    @Column(name = "total_rentals")
    private Long totalRentals = 0L;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    private Long totalReviews = 0L;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @Column(name = "last_cleaning_date")
    private LocalDate lastCleaningDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum VehicleType {
        CAR, SUV, TRUCK, VAN, MOTORCYCLE, LUXURY, CONVERTIBLE
    }

    public enum VehicleStatus {
        AVAILABLE, RENTED, MAINTENANCE, OUT_OF_SERVICE
    }
}