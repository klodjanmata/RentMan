package com.rentman.rentman.dto;

import com.rentman.rentman.entity.Reservation;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// DTO for creating new reservations
@Data
public class ReservationCreateDto {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String pickupLocation;
    private String returnLocation;
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;

    private String specialRequests;
    private Boolean insuranceIncluded = false;
    private Boolean additionalDriver = false;
    private Boolean gpsIncluded = false;
    private Boolean childSeatIncluded = false;
}

// DTO for reservation responses
@Data
class ReservationResponseDto {
    private Long id;
    private String reservationNumber;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerEmail;

    // Vehicle info
    private Long vehicleId;
    private String vehicleMake;
    private String vehicleModel;
    private String vehicleLicensePlate;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;

    private Reservation.ReservationStatus status;

    // Pricing
    private BigDecimal dailyRate;
    private Integer totalDays;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal insuranceAmount;
    private BigDecimal additionalFees;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal depositAmount;
    private BigDecimal amountPaid;
    private BigDecimal remainingAmount;

    // Locations and times
    private String pickupLocation;
    private String returnLocation;
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;

    // Additional services
    private Boolean insuranceIncluded;
    private Boolean additionalDriver;
    private Boolean gpsIncluded;
    private Boolean childSeatIncluded;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    private String specialRequests;
    private String notes;

    // Helper methods
    public boolean isActive() {
        return status == Reservation.ReservationStatus.CONFIRMED ||
                status == Reservation.ReservationStatus.IN_PROGRESS;
    }

    public boolean canBeCancelled() {
        return status == Reservation.ReservationStatus.PENDING ||
                status == Reservation.ReservationStatus.CONFIRMED;
    }
}

// DTO for updating reservations
@Data
class ReservationUpdateDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private String pickupLocation;
    private String returnLocation;
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;
    private String specialRequests;
    private String notes;
    private Boolean insuranceIncluded;
    private Boolean additionalDriver;
    private Boolean gpsIncluded;
    private Boolean childSeatIncluded;
}

// DTO for vehicle pickup
@Data
class VehiclePickupDto {
    @NotNull(message = "Pickup mileage is required")
    private Integer pickupMileage;

    @NotNull(message = "Fuel level is required")
    private String fuelLevelPickup;

    private String vehicleConditionPickup;
    private LocalDateTime actualPickupTime;
    private Long handledByEmployeeId;
}

// DTO for vehicle return
@Data
class VehicleReturnDto {
    @NotNull(message = "Return mileage is required")
    private Integer returnMileage;

    @NotNull(message = "Fuel level is required")
    private String fuelLevelReturn;

    private String vehicleConditionReturn;
    private LocalDateTime actualReturnTime;
    private BigDecimal additionalFees;
    private String notes;
    private Long handledByEmployeeId;
}

// DTO for availability check
@Data
class AvailabilityCheckDto {
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}

// DTO for reservation summary/statistics
@Data
class ReservationSummaryDto {
    private long totalReservations;
    private long pendingReservations;
    private long confirmedReservations;
    private long inProgressReservations;
    private long completedReservations;
    private long cancelledReservations;
    private long overdueReservations;

    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal averageReservationValue;

    private int totalActiveReservations;
    private int upcomingReservations;
    private int todayPickups;
    private int todayReturns;
}