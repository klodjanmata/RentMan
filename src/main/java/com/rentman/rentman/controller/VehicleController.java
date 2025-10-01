package com.rentman.rentman.controller;

import com.rentman.rentman.entity.Vehicle;
import com.rentman.rentman.entity.Company;
import com.rentman.rentman.dto.VehicleResponseDto;
import com.rentman.rentman.repository.VehicleRepository;
import com.rentman.rentman.repository.CompanyRepository;
import com.rentman.rentman.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*") // For now, allow all origins
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    // Helper method to get current user's company ID
    private Long getCurrentUserCompanyId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal principal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            return principal.getCompanyId();
        }
        throw new RuntimeException("User not authenticated");
    }

    // Get all vehicles for current user's company
    @GetMapping
    public ResponseEntity<?> getAllVehicles() {
        try {
            logger.info("=== GET ALL VEHICLES DEBUG ===");
            
            // Get current user's company ID
            Long companyId = getCurrentUserCompanyId();
            logger.info("Company ID: {}", companyId);
            
            // Check if user belongs to a company
            if (companyId == null) {
                logger.warn("User does not belong to a company");
                return ResponseEntity.badRequest().body("User must belong to a company to view vehicles");
            }
            
            // Get vehicles for the user's company
            List<Vehicle> vehicles = vehicleRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
            logger.info("Found {} vehicles for company {}", vehicles.size(), companyId);
            
            // Log vehicle details
            for (Vehicle vehicle : vehicles) {
                logger.info("Vehicle: {} {} {} (ID: {})", vehicle.getMake(), vehicle.getModel(), vehicle.getYear(), vehicle.getId());
            }
            
            // Convert to DTOs to avoid circular references
            List<VehicleResponseDto> responseDtos = vehicles.stream()
                .map(VehicleResponseDto::fromVehicle)
                .toList();
            
            logger.info("Returning {} vehicle DTOs", responseDtos.size());
            return ResponseEntity.ok(responseDtos);
        } catch (RuntimeException e) {
            logger.error("RuntimeException in getAllVehicles: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Exception in getAllVehicles: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to load vehicles: " + e.getMessage());
        }
    }

    // Get vehicle by ID
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();
            // Ensure company is loaded (trigger lazy loading)
            if (vehicle.getCompany() != null) {
                vehicle.getCompany().getCompanyName(); // Access company to initialize proxy
            }
            return ResponseEntity.ok(vehicle);
        }
        return ResponseEntity.notFound().build();
    }

    // Debug endpoint to check company status
    @GetMapping("/debug/company")
    public ResponseEntity<?> getCompanyDebugInfo() {
        try {
            Long companyId = getCurrentUserCompanyId();
            logger.info("Debug - Company ID: {}", companyId);
            
            if (companyId != null) {
                Optional<Company> company = companyRepository.findById(companyId);
                if (company.isPresent()) {
                    Company c = company.get();
                    logger.info("Debug - Company: {} (Status: {})", c.getCompanyName(), c.getStatus());
                    return ResponseEntity.ok(Map.of(
                        "companyId", companyId,
                        "companyName", c.getCompanyName(),
                        "status", c.getStatus().toString()
                    ));
                }
            }
            
            return ResponseEntity.ok(Map.of("error", "Company not found"));
        } catch (Exception e) {
            logger.error("Debug error: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of("error", e.getMessage()));
        }
    }

    // Get available vehicles
    @GetMapping("/available")
    public ResponseEntity<List<Vehicle>> getAvailableVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE);
        return ResponseEntity.ok(vehicles);
    }

    // Get vehicles by type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Vehicle>> getVehiclesByType(@PathVariable String type) {
        try {
            Vehicle.VehicleType vehicleType = Vehicle.VehicleType.valueOf(type.toUpperCase());
            List<Vehicle> vehicles = vehicleRepository.findByType(vehicleType);
            return ResponseEntity.ok(vehicles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Create new vehicle
    @PostMapping
    public ResponseEntity<?> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        try {
            // Get current user's company ID
            Long companyId = getCurrentUserCompanyId();
            
            // Check if user belongs to a company
            if (companyId == null) {
                return ResponseEntity.badRequest().body("User must belong to a company to create vehicles");
            }
            
            // Load the company entity
            Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
            
            // Set the company from the current user
            vehicle.setCompany(company);
            
            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            
            // Convert to DTO to avoid circular references
            VehicleResponseDto responseDto = VehicleResponseDto.fromVehicle(savedVehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (RuntimeException e) {
            logger.error("RuntimeException in createVehicle: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Exception in createVehicle: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to create vehicle: " + e.getMessage());
        }
    }

    // Update vehicle
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicleDetails) {
        try {
            // Get current user's company ID
            Long companyId = getCurrentUserCompanyId();
            
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);

            if (optionalVehicle.isPresent()) {
                Vehicle vehicle = optionalVehicle.get();
                
                // Check if user belongs to the same company as the vehicle
                if (companyId == null || 
                    !companyId.equals(vehicle.getCompany().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only update vehicles from your own company");
                }
                
                vehicle.setMake(vehicleDetails.getMake());
                vehicle.setModel(vehicleDetails.getModel());
                vehicle.setYear(vehicleDetails.getYear());
                vehicle.setLicensePlate(vehicleDetails.getLicensePlate());
                vehicle.setType(vehicleDetails.getType());
                vehicle.setStatus(vehicleDetails.getStatus());
                vehicle.setDailyRate(vehicleDetails.getDailyRate());
                vehicle.setFuelType(vehicleDetails.getFuelType());
                vehicle.setTransmission(vehicleDetails.getTransmission());
                vehicle.setSeatingCapacity(vehicleDetails.getSeatingCapacity());
                vehicle.setColor(vehicleDetails.getColor());
                vehicle.setMileage(vehicleDetails.getMileage());
                vehicle.setDescription(vehicleDetails.getDescription());
                vehicle.setImageUrl(vehicleDetails.getImageUrl());

                Vehicle updatedVehicle = vehicleRepository.save(vehicle);
                VehicleResponseDto responseDto = VehicleResponseDto.fromVehicle(updatedVehicle);
                return ResponseEntity.ok(responseDto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update vehicle");
        }
    }

    // Delete vehicle
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        try {
            // Get current user's company ID
            Long companyId = getCurrentUserCompanyId();
            
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            
            if (optionalVehicle.isPresent()) {
                Vehicle vehicle = optionalVehicle.get();
                
                // Check if user belongs to the same company as the vehicle
                if (companyId == null || 
                    !companyId.equals(vehicle.getCompany().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete vehicles from your own company");
                }
                
                vehicleRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete vehicle");
        }
    }

    // Update vehicle status
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateVehicleStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            // Get current user's company ID
            Long companyId = getCurrentUserCompanyId();
            
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);

            if (optionalVehicle.isPresent()) {
                Vehicle vehicle = optionalVehicle.get();
                
                // Check if user belongs to the same company as the vehicle
                if (companyId == null || 
                    !companyId.equals(vehicle.getCompany().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only update vehicles from your own company");
                }
                
                try {
                    Vehicle.VehicleStatus newStatus = Vehicle.VehicleStatus.valueOf(status.toUpperCase());
                    vehicle.setStatus(newStatus);
                    Vehicle updatedVehicle = vehicleRepository.save(vehicle);
                    VehicleResponseDto responseDto = VehicleResponseDto.fromVehicle(updatedVehicle);
                    return ResponseEntity.ok(responseDto);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Invalid status: " + status);
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update vehicle status");
        }
    }
}