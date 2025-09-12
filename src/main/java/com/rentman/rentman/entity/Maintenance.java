package com.rentman.rentman.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @NotNull(message = "Vehicle is required")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @NotNull(message = "Company is required")
    private Company company;

    @Column(name = "maintenance_number", unique = true, nullable = false)
    private String maintenanceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Dates
    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    // Mileage information
    @Column(name = "current_mileage")
    private Integer currentMileage;

    @Column(name = "next_mileage_interval")
    private Integer nextMileageInterval;

    // Cost information
    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost", precision = 10, scale = 2)
    private BigDecimal actualCost;

    @Column(name = "labor_cost", precision = 10, scale = 2)
    private BigDecimal laborCost;

    @Column(name = "parts_cost", precision = 10, scale = 2)
    private BigDecimal partsCost;

    // Service provider information
    @Column(name = "service_provider")
    private String serviceProvider;

    @Column(name = "service_provider_contact")
    private String serviceProviderContact;

    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    // Employee who performed/oversaw maintenance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_employee_id")
    private User performedByEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervised_by_employee_id")
    private User supervisedByEmployee;

    // Maintenance details
    @Column(name = "parts_used", columnDefinition = "TEXT")
    private String partsUsed; // JSON array of parts

    @Column(name = "work_performed", columnDefinition = "TEXT")
    private String workPerformed;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    // Vehicle condition after maintenance
    @Column(name = "vehicle_condition_after", columnDefinition = "TEXT")
    private String vehicleConditionAfter;

    @Column(name = "quality_rating")
    private Integer qualityRating; // 1-5 scale

    // Recurring maintenance
    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Column(name = "recurrence_interval_months")
    private Integer recurrenceIntervalMonths;

    @Column(name = "recurrence_interval_miles")
    private Integer recurrenceIntervalMiles;

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (maintenanceNumber == null) {
            maintenanceNumber = generateMaintenanceNumber();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods
    private String generateMaintenanceNumber() {
        return "MAINT" + System.currentTimeMillis();
    }

    public boolean isOverdue() {
        return status == MaintenanceStatus.SCHEDULED && 
               scheduledDate != null && 
               scheduledDate.isBefore(LocalDate.now());
    }

    public boolean isCompleted() {
        return status == MaintenanceStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return status == MaintenanceStatus.IN_PROGRESS;
    }

    public boolean isScheduled() {
        return status == MaintenanceStatus.SCHEDULED;
    }

    public BigDecimal getTotalCost() {
        BigDecimal total = BigDecimal.ZERO;
        if (actualCost != null) total = total.add(actualCost);
        if (laborCost != null) total = total.add(laborCost);
        if (partsCost != null) total = total.add(partsCost);
        return total;
    }

    public boolean isUnderWarranty() {
        return warrantyExpiryDate != null && warrantyExpiryDate.isAfter(LocalDate.now());
    }

    // Enums
    public enum MaintenanceType {
        ROUTINE("Routine Maintenance"),
        REPAIR("Repair"),
        INSPECTION("Inspection"),
        OIL_CHANGE("Oil Change"),
        TIRE_ROTATION("Tire Rotation"),
        BRAKE_SERVICE("Brake Service"),
        TRANSMISSION_SERVICE("Transmission Service"),
        ENGINE_SERVICE("Engine Service"),
        ELECTRICAL("Electrical"),
        BODY_WORK("Body Work"),
        EMERGENCY("Emergency Repair"),
        RECALL("Recall Service"),
        PREVENTIVE("Preventive Maintenance");

        private final String displayName;

        MaintenanceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum MaintenanceStatus {
        SCHEDULED("Scheduled"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        ON_HOLD("On Hold"),
        WAITING_PARTS("Waiting for Parts");

        private final String displayName;

        MaintenanceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
