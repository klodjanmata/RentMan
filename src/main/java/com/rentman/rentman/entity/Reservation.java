package com.rentman.rentman.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_number", unique = true, nullable = false)
    private String reservationNumber;

    // Customer who made the reservation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Customer is required")
    private User customer;

    // Vehicle being reserved
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @NotNull(message = "Vehicle is required")
    private Vehicle vehicle;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    // Pricing information
    @NotNull(message = "Daily rate is required")
    @Positive(message = "Daily rate must be positive")
    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "insurance_amount", precision = 10, scale = 2)
    private BigDecimal insuranceAmount = BigDecimal.ZERO;

    @Column(name = "additional_fees", precision = 10, scale = 2)
    private BigDecimal additionalFees = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(name = "amount_paid", precision = 10, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    // Pickup and return information
    @Column(name = "pickup_location")
    private String pickupLocation;

    @Column(name = "return_location")
    private String returnLocation;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    // Vehicle condition
    @Column(name = "pickup_mileage")
    private Integer pickupMileage;

    @Column(name = "return_mileage")
    private Integer returnMileage;

    @Column(name = "fuel_level_pickup")
    private String fuelLevelPickup;

    @Column(name = "fuel_level_return")
    private String fuelLevelReturn;

    @Column(name = "vehicle_condition_pickup", columnDefinition = "TEXT")
    private String vehicleConditionPickup;

    @Column(name = "vehicle_condition_return", columnDefinition = "TEXT")
    private String vehicleConditionReturn;

    // Additional information
    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "insurance_included")
    private Boolean insuranceIncluded = false;

    @Column(name = "additional_driver")
    private Boolean additionalDriver = false;

    @Column(name = "gps_included")
    private Boolean gpsIncluded = false;

    @Column(name = "child_seat_included")
    private Boolean childSeatIncluded = false;

    // Employee who handled the reservation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by_employee_id")
    private User handledByEmployee;

    // Company relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Generate reservation number if not set
        if (reservationNumber == null) {
            reservationNumber = generateReservationNumber();
        }

        // Calculate total days and amounts
        calculateTotalDays();
        calculateAmounts();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalDays();
        calculateAmounts();
    }

    // Business logic methods
    private String generateReservationNumber() {
        return "RES" + System.currentTimeMillis();
    }

    private void calculateTotalDays() {
        if (startDate != null && endDate != null) {
            totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate);
            if (totalDays <= 0) {
                totalDays = 1; // Minimum 1 day rental
            }
        }
    }

    private void calculateAmounts() {
        if (dailyRate != null && totalDays != null) {
            subtotal = dailyRate.multiply(BigDecimal.valueOf(totalDays));

            // Calculate total amount
            totalAmount = subtotal
                    .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                    .add(insuranceAmount != null ? insuranceAmount : BigDecimal.ZERO)
                    .add(additionalFees != null ? additionalFees : BigDecimal.ZERO)
                    .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        }
    }

    // Helper methods
    public boolean isActive() {
        return status == ReservationStatus.CONFIRMED || status == ReservationStatus.IN_PROGRESS;
    }

    public boolean canBeCancelled() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;
    }

    public boolean isOverdue() {
        return endDate.isBefore(LocalDate.now()) &&
                (status == ReservationStatus.CONFIRMED || status == ReservationStatus.IN_PROGRESS);
    }

    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(amountPaid != null ? amountPaid : BigDecimal.ZERO);
    }

    public boolean isFullyPaid() {
        return getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0;
    }

    // Enums
    public enum ReservationStatus {
        PENDING("Pending"),
        CONFIRMED("Confirmed"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        NO_SHOW("No Show"),
        OVERDUE("Overdue");

        private final String displayName;

        ReservationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}