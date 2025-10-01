package com.rentman.rentman.controller;

import com.rentman.rentman.dto.ReservationCreateDto;
import com.rentman.rentman.entity.Reservation;
import com.rentman.rentman.service.ReservationService;
import com.rentman.rentman.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // Helper method to get current user ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal principal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            return principal.getUserId();
        }
        throw new RuntimeException("User not authenticated");
    }

    // Create new reservation
    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody ReservationCreateDto createDto) {
        try {
            // Automatically set customer ID from authenticated user
            Long customerId = getCurrentUserId();
            createDto.setCustomerId(customerId);
            
            Reservation reservation = reservationService.createReservation(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get all reservations (for current user)
    @GetMapping
    public ResponseEntity<List<Reservation>> getAllReservations() {
        try {
            // Get current user's reservations
            Long userId = getCurrentUserId();
            List<Reservation> reservations = reservationService.getCustomerReservations(userId);
            return ResponseEntity.ok(reservations);
        } catch (RuntimeException e) {
            // If user not authenticated or error, return empty list
            return ResponseEntity.ok(List.of());
        }
    }

    // Get reservation by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.getReservationById(id);
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Get reservation by reservation number
    @GetMapping("/number/{reservationNumber}")
    public ResponseEntity<Reservation> getReservationByNumber(@PathVariable String reservationNumber) {
        Optional<Reservation> reservation = reservationService.getReservationByNumber(reservationNumber);
        return reservation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get customer's reservations
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Reservation>> getCustomerReservations(@PathVariable Long customerId) {
        List<Reservation> reservations = reservationService.getCustomerReservations(customerId);
        return ResponseEntity.ok(reservations);
    }

    // Get vehicle's reservations
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<Reservation>> getVehicleReservations(@PathVariable Long vehicleId) {
        List<Reservation> reservations = reservationService.getVehicleReservations(vehicleId);
        return ResponseEntity.ok(reservations);
    }

    // Get reservations by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getReservationsByStatus(@PathVariable String status) {
        try {
            Reservation.ReservationStatus reservationStatus = Reservation.ReservationStatus.valueOf(status.toUpperCase());
            List<Reservation> reservations = reservationService.getReservationsByStatus(reservationStatus);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid status: " + status);
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get current active reservations
    @GetMapping("/active")
    public ResponseEntity<List<Reservation>> getCurrentActiveReservations() {
        List<Reservation> reservations = reservationService.getCurrentActiveReservations();
        return ResponseEntity.ok(reservations);
    }

    // Get upcoming reservations
    @GetMapping("/upcoming")
    public ResponseEntity<List<Reservation>> getUpcomingReservations() {
        List<Reservation> reservations = reservationService.getUpcomingReservations();
        return ResponseEntity.ok(reservations);
    }

    // Get overdue reservations
    @GetMapping("/overdue")
    public ResponseEntity<List<Reservation>> getOverdueReservations() {
        List<Reservation> reservations = reservationService.getOverdueReservations();
        return ResponseEntity.ok(reservations);
    }

    // Get today's pickups
    @GetMapping("/today/pickups")
    public ResponseEntity<List<Reservation>> getTodayPickups() {
        List<Reservation> reservations = reservationService.getTodayPickups();
        return ResponseEntity.ok(reservations);
    }

    // Get today's returns
    @GetMapping("/today/returns")
    public ResponseEntity<List<Reservation>> getTodayReturns() {
        List<Reservation> reservations = reservationService.getTodayReturns();
        return ResponseEntity.ok(reservations);
    }

    // Check vehicle availability
    @GetMapping("/availability")
    public ResponseEntity<?> checkVehicleAvailability(
            @RequestParam Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            boolean available = reservationService.isVehicleAvailable(vehicleId, startDate, endDate);
            Map<String, Object> response = new HashMap<>();
            response.put("vehicleId", vehicleId);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("available", available);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Confirm reservation
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirmReservation(@PathVariable Long id, @RequestParam(required = false) Long employeeId) {
        try {
            Reservation reservation = reservationService.confirmReservation(id, employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reservation confirmed successfully");
            response.put("reservation", reservation);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Start reservation (pickup)
    @PatchMapping("/{id}/start")
    public ResponseEntity<?> startReservation(
            @PathVariable Long id,
            @RequestParam Integer pickupMileage,
            @RequestParam String fuelLevel,
            @RequestParam(required = false) String vehicleCondition,
            @RequestParam(required = false) Long employeeId) {

        try {
            Reservation reservation = reservationService.startReservation(
                    id, pickupMileage, fuelLevel, vehicleCondition, employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reservation started successfully");
            response.put("reservation", reservation);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Complete reservation (return)
    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> completeReservation(
            @PathVariable Long id,
            @RequestParam Integer returnMileage,
            @RequestParam String fuelLevel,
            @RequestParam(required = false) String vehicleCondition,
            @RequestParam(required = false) BigDecimal additionalFees,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Long employeeId) {

        try {
            Reservation reservation = reservationService.completeReservation(
                    id, returnMileage, fuelLevel, vehicleCondition, additionalFees, notes, employeeId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reservation completed successfully");
            response.put("reservation", reservation);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Cancel reservation
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(
            @PathVariable Long id,
            @RequestParam(required = false) String cancellationReason) {

        try {
            Reservation reservation = reservationService.cancelReservation(id, cancellationReason);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reservation cancelled successfully");
            response.put("reservation", reservation);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Update reservation
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @Valid @RequestBody Reservation reservationDetails) {
        try {
            Reservation updatedReservation = reservationService.updateReservation(id, reservationDetails);
            return ResponseEntity.ok(updatedReservation);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Delete reservation
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long id) {
        try {
            reservationService.deleteReservation(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Reservation deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get reservation statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReservationStatistics() {
        Map<String, Object> statistics = reservationService.getReservationStatistics();
        return ResponseEntity.ok(statistics);
    }

    // Get revenue for date range
    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) String startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) String endDate) {

        try {
            // Parse dates and calculate revenue
            // This is simplified - you might want to use proper date parsing
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Revenue calculation endpoint - implement date parsing");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}