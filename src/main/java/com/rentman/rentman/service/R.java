package com.rentman.rentman.service;

import com.rentman.rentman.dto.ReservationCreateDto;
import com.rentman.rentman.entity.Reservation;
import com.rentman.rentman.entity.User;
import com.rentman.rentman.entity.Vehicle;
import com.rentman.rentman.repository.ReservationRepository;
import com.rentman.rentman.repository.UserRepository;
import com.rentman.rentman.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    // Create new reservation
    public Reservation createReservation(ReservationCreateDto createDto) {
        // Validate customer exists
        User customer = userRepository.findById(createDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + createDto.getCustomerId()));

        // Validate customer is actually a customer
        if (customer.getRole() != User.UserRole.CUSTOMER) {
            throw new RuntimeException("User is not a customer");
        }

        // Validate vehicle exists
        Vehicle vehicle = vehicleRepository.findById(createDto.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + createDto.getVehicleId()));

        // Validate vehicle is available
        if (vehicle.getStatus() != Vehicle.VehicleStatus.AVAILABLE) {
            throw new RuntimeException("Vehicle is not available for rental");
        }

        // Validate dates
        validateReservationDates(createDto.getStartDate(), createDto.getEndDate());

        // Check vehicle availability for the requested dates
        if (!isVehicleAvailable(createDto.getVehicleId(), createDto.getStartDate(), createDto.getEndDate())) {
            throw new RuntimeException("Vehicle is not available for the selected dates");
        }

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setVehicle(vehicle);
        reservation.setStartDate(createDto.getStartDate());
        reservation.setEndDate(createDto.getEndDate());
        reservation.setPickupLocation(createDto.getPickupLocation());
        reservation.setReturnLocation(createDto.getReturnLocation());
        reservation.setPickupTime(createDto.getPickupTime());
        reservation.setReturnTime(createDto.getReturnTime());
        reservation.setSpecialRequests(createDto.getSpecialRequests());

        // Set additional services
        reservation.setInsuranceIncluded(createDto.getInsuranceIncluded());
        reservation.setAdditionalDriver(createDto.getAdditionalDriver());
        reservation.setGpsIncluded(createDto.getGpsIncluded());
        reservation.setChildSeatIncluded(createDto.getChildSeatIncluded());

        // Set pricing (using vehicle's current daily rate)
        reservation.setDailyRate(vehicle.getDailyRate());

        // Calculate additional costs
        calculateAdditionalCosts(reservation);

        // Save reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        // Update vehicle status to reserved (optional, depending on your business logic)
        // You might want to keep it available until confirmed

        return savedReservation;
    }

    // Check vehicle availability
    public boolean isVehicleAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        long conflictingReservations = reservationRepository.countConflictingReservations(
                vehicleId, startDate, endDate);
        return conflictingReservations == 0;
    }

    // Confirm reservation
    public Reservation confirmReservation(Long reservationId, Long employeeId) {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new RuntimeException("Only pending reservations can be confirmed");
        }

        // Set employee who confirmed
        if (employeeId != null) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            if (!employee.isAdminOrEmployee()) {
                throw new RuntimeException("User is not an employee or admin");
            }
            reservation.setHandledByEmployee(employee);
        }

        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(LocalDateTime.now());

        return reservationRepository.save(reservation);
    }

    // Start reservation (vehicle pickup)
    public Reservation startReservation(Long reservationId, Integer pickupMileage,
                                        String fuelLevel, String vehicleCondition, Long employeeId) {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Only confirmed reservations can be started");
        }

        reservation.setStatus(Reservation.ReservationStatus.IN_PROGRESS);
        reservation.setActualStartDate(LocalDate.now());
        reservation.setPickupMileage(pickupMileage);
        reservation.setFuelLevelPickup(fuelLevel);
        reservation.setVehicleConditionPickup(vehicleCondition);

        if (employeeId != null) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            reservation.setHandledByEmployee(employee);
        }

        // Update vehicle status
        Vehicle vehicle = reservation.getVehicle();
        vehicle.setStatus(Vehicle.VehicleStatus.RENTED);
        vehicleRepository.save(vehicle);

        return reservationRepository.save(reservation);
    }

    // Complete reservation (vehicle return)
    // TODO this method is not finished yet
    public Reservation completeReservation(Long reservationId, Integer returnMileage,
                                           String fuelLevel, String vehicleCondition,
                                           BigDecimal additionalFees, String notes, Long employeeId) {
        Reservation reservation = getReservationById(reservationId);

        if (reservation.getStatus() != Reservation.ReservationStatus.IN_PROGRESS) {
            throw new RuntimeException("Only in-progress reservations can be completed");
        }

        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservation.setActualEndDate(LocalDate.now());
        reservation.setCompletedAt(LocalDateTime.now());
        reservation.setReturnMileage(returnMileage);
        reservation.setFuelLevelReturn(fuelLevel);
        reservation.setVehicleConditionReturn(vehicleCondition);
        reservation.setAdditionalFees(additionalFees);
        reservation.setNotes(notes);
        return reservationRepository.save(reservation);
    }
}