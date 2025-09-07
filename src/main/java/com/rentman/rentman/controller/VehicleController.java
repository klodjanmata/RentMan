package com.rentman.rentman.controller;

import com.rentman.rentman.entity.Vehicle;
import com.rentman.rentman.repository.VehicleRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*") // For now, allow all origins
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

    // Get all vehicles
    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return ResponseEntity.ok(vehicles);
    }

    // Get vehicle by ID
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(id);
        return vehicle.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
    public ResponseEntity<Vehicle> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        try {
            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Update vehicle
    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicleDetails) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);

        if (optionalVehicle.isPresent()) {
            Vehicle vehicle = optionalVehicle.get();
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
            return ResponseEntity.ok(updatedVehicle);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete vehicle
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update vehicle status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Vehicle> updateVehicleStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);

        if (optionalVehicle.isPresent()) {
            try {
                Vehicle vehicle = optionalVehicle.get();
                Vehicle.VehicleStatus newStatus = Vehicle.VehicleStatus.valueOf(status.toUpperCase());
                vehicle.setStatus(newStatus);
                Vehicle updatedVehicle = vehicleRepository.save(vehicle);
                return ResponseEntity.ok(updatedVehicle);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}