package com.rentman.rentman.dto;

import com.rentman.rentman.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDto {
    private Long id;
    private String make;
    private String model;
    private Integer year;
    private String licensePlate;
    private Vehicle.VehicleType type;
    private Vehicle.VehicleStatus status;
    private BigDecimal dailyRate;
    private String fuelType;
    private String transmission;
    private Integer seatingCapacity;
    private String color;
    private Integer mileage;
    private String description;
    private String imageUrl;
    private Long companyId;
    private String companyName;
    private String currentLocation;
    private String pickupLocation;
    private Boolean isFeatured;
    private Boolean isAvailableForRental;
    private String engineSize;
    private Integer fuelCapacity;
    private Integer doors;
    private String luggageCapacity;
    private Boolean airConditioning;
    private Boolean gpsNavigation;
    private Boolean bluetooth;
    private Boolean usbCharging;
    private Boolean backupCamera;
    private Boolean parkingSensors;
    private Boolean sunroof;
    private Boolean leatherSeats;
    private String insuranceProvider;
    private String insurancePolicyNumber;
    private LocalDate insuranceExpiryDate;
    private String registrationNumber;
    private LocalDate registrationExpiryDate;
    private LocalDate inspectionDate;
    private LocalDate nextInspectionDate;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private LocalDate lastCleaningDate;
    private Long totalRentals;
    private BigDecimal totalRevenue;
    private Long totalReviews;
    private BigDecimal averageRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Static method to convert from Vehicle entity
    public static VehicleResponseDto fromVehicle(Vehicle vehicle) {
        VehicleResponseDto dto = new VehicleResponseDto();
        dto.setId(vehicle.getId());
        dto.setMake(vehicle.getMake());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setType(vehicle.getType());
        dto.setStatus(vehicle.getStatus());
        dto.setDailyRate(vehicle.getDailyRate());
        dto.setFuelType(vehicle.getFuelType());
        dto.setTransmission(vehicle.getTransmission());
        dto.setSeatingCapacity(vehicle.getSeatingCapacity());
        dto.setColor(vehicle.getColor());
        dto.setMileage(vehicle.getMileage());
        dto.setDescription(vehicle.getDescription());
        dto.setImageUrl(vehicle.getImageUrl());
        dto.setCompanyId(vehicle.getCompany() != null ? vehicle.getCompany().getId() : null);
        dto.setCompanyName(vehicle.getCompany() != null ? vehicle.getCompany().getCompanyName() : null);
        dto.setCurrentLocation(vehicle.getCurrentLocation());
        dto.setPickupLocation(vehicle.getPickupLocation());
        dto.setIsFeatured(vehicle.getIsFeatured());
        dto.setIsAvailableForRental(vehicle.getIsAvailableForRental());
        dto.setEngineSize(vehicle.getEngineSize());
        dto.setFuelCapacity(vehicle.getFuelCapacity());
        dto.setDoors(vehicle.getDoors());
        dto.setLuggageCapacity(vehicle.getLuggageCapacity());
        dto.setAirConditioning(vehicle.getAirConditioning());
        dto.setGpsNavigation(vehicle.getGpsNavigation());
        dto.setBluetooth(vehicle.getBluetooth());
        dto.setUsbCharging(vehicle.getUsbCharging());
        dto.setBackupCamera(vehicle.getBackupCamera());
        dto.setParkingSensors(vehicle.getParkingSensors());
        dto.setSunroof(vehicle.getSunroof());
        dto.setLeatherSeats(vehicle.getLeatherSeats());
        dto.setInsuranceProvider(vehicle.getInsuranceProvider());
        dto.setInsurancePolicyNumber(vehicle.getInsurancePolicyNumber());
        dto.setInsuranceExpiryDate(vehicle.getInsuranceExpiryDate());
        dto.setRegistrationNumber(vehicle.getRegistrationNumber());
        dto.setRegistrationExpiryDate(vehicle.getRegistrationExpiryDate());
        dto.setInspectionDate(vehicle.getInspectionDate());
        dto.setNextInspectionDate(vehicle.getNextInspectionDate());
        dto.setLastMaintenanceDate(vehicle.getLastMaintenanceDate());
        dto.setNextMaintenanceDate(vehicle.getNextMaintenanceDate());
        dto.setLastCleaningDate(vehicle.getLastCleaningDate());
        dto.setTotalRentals(vehicle.getTotalRentals());
        dto.setTotalRevenue(vehicle.getTotalRevenue());
        dto.setTotalReviews(vehicle.getTotalReviews());
        dto.setAverageRating(vehicle.getAverageRating());
        dto.setCreatedAt(vehicle.getCreatedAt());
        dto.setUpdatedAt(vehicle.getUpdatedAt());
        return dto;
    }
}
