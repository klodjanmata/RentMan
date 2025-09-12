package com.rentman.rentman.controller;

import com.rentman.rentman.entity.Vehicle;
import com.rentman.rentman.entity.Company;
import com.rentman.rentman.repository.VehicleRepository;
import com.rentman.rentman.repository.CompanyRepository;
import com.rentman.rentman.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    // ========== VEHICLE SEARCH ==========

    // Advanced vehicle search for customers
    @GetMapping("/vehicles")
    public ResponseEntity<?> searchVehicles(
            // Basic filters
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) BigDecimal minRate,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) Integer minSeating,
            @RequestParam(required = false) Integer maxSeating,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String location,
            
            // Company filters
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            
            // Features
            @RequestParam(required = false) Boolean airConditioning,
            @RequestParam(required = false) Boolean gpsNavigation,
            @RequestParam(required = false) Boolean bluetooth,
            @RequestParam(required = false) Boolean usbCharging,
            @RequestParam(required = false) Boolean backupCamera,
            @RequestParam(required = false) Boolean parkingSensors,
            @RequestParam(required = false) Boolean sunroof,
            @RequestParam(required = false) Boolean leatherSeats,
            
            // Availability
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            // Pagination and sorting
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dailyRate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            // Convert type string to enum
            Vehicle.VehicleType vehicleType = null;
            if (type != null && !type.isEmpty()) {
                vehicleType = Vehicle.VehicleType.valueOf(type.toUpperCase());
            }

            // Perform search
            List<Vehicle> vehicles = vehicleRepository.searchVehicles(
                companyId, vehicleType, make, model, minRate, maxRate, minYear, maxYear,
                fuelType, transmission, minSeating, maxSeating, location,
                airConditioning, gpsNavigation, bluetooth, backupCamera, sunroof, leatherSeats
            );

            // Filter by availability if dates provided
            if (startDate != null && endDate != null) {
                vehicles = vehicles.stream()
                    .filter(vehicle -> isVehicleAvailable(vehicle.getId(), startDate, endDate))
                    .toList();
            }

            // Apply company location filters
            if (city != null || state != null || country != null) {
                vehicles = vehicles.stream()
                    .filter(vehicle -> {
                        Company company = vehicle.getCompany();
                        if (company == null) return false;
                        
                        boolean cityMatch = city == null || city.equalsIgnoreCase(company.getCity());
                        boolean stateMatch = state == null || state.equalsIgnoreCase(company.getState());
                        boolean countryMatch = country == null || country.equalsIgnoreCase(company.getCountry());
                        
                        return cityMatch && stateMatch && countryMatch;
                    })
                    .toList();
            }

            // Apply company name filter
            if (companyName != null && !companyName.isEmpty()) {
                vehicles = vehicles.stream()
                    .filter(vehicle -> {
                        Company company = vehicle.getCompany();
                        return company != null && company.getCompanyName().toLowerCase()
                            .contains(companyName.toLowerCase());
                    })
                    .toList();
            }

            // Apply color filter
            if (color != null && !color.isEmpty()) {
                vehicles = vehicles.stream()
                    .filter(vehicle -> color.equalsIgnoreCase(vehicle.getColor()))
                    .toList();
            }

            // Sort results
            if (sortBy != null && !sortBy.isEmpty()) {
                vehicles = sortVehicles(vehicles, sortBy, sortDir);
            }

            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, vehicles.size());
            List<Vehicle> paginatedVehicles = vehicles.subList(start, end);

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("vehicles", paginatedVehicles);
            response.put("totalCount", vehicles.size());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) vehicles.size() / size));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid search parameters: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Search failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Quick vehicle search with minimal parameters
    @GetMapping("/vehicles/quick")
    public ResponseEntity<?> quickSearchVehicles(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            Vehicle.VehicleType vehicleType = null;
            if (type != null && !type.isEmpty()) {
                vehicleType = Vehicle.VehicleType.valueOf(type.toUpperCase());
            }

            List<Vehicle> vehicles = vehicleRepository.searchVehicles(
                null, vehicleType, null, null, null, maxRate, null, null,
                null, null, null, null, location,
                null, null, null, null, null, null
            );

            // Limit results
            if (vehicles.size() > limit) {
                vehicles = vehicles.subList(0, limit);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("vehicles", vehicles);
            response.put("count", vehicles.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Quick search failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Search vehicles by company
    @GetMapping("/vehicles/company/{companyId}")
    public ResponseEntity<?> searchVehiclesByCompany(
            @PathVariable Long companyId,
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) BigDecimal minRate,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) Integer minSeating,
            @RequestParam(required = false) Integer maxSeating,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Vehicle.VehicleStatus status = null; // Include all statuses for company search
            Vehicle.VehicleType vehicleType = null;
            if (type != null && !type.isEmpty()) {
                vehicleType = Vehicle.VehicleType.valueOf(type.toUpperCase());
            }

            List<Vehicle> vehicles = vehicleRepository.searchVehiclesByCompany(
                companyId, status, vehicleType, make, model, minRate, maxRate,
                minYear, maxYear, fuelType, transmission, minSeating, maxSeating, location
            );

            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, vehicles.size());
            List<Vehicle> paginatedVehicles = vehicles.subList(start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("vehicles", paginatedVehicles);
            response.put("totalCount", vehicles.size());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) vehicles.size() / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Company vehicle search failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== COMPANY SEARCH ==========

    // Search companies
    @GetMapping("/companies")
    public ResponseEntity<?> searchCompanies(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Company.CompanyStatus companyStatus = null;
            if (status != null && !status.isEmpty()) {
                companyStatus = Company.CompanyStatus.valueOf(status.toUpperCase());
            }

            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Company> companies = companyRepository.searchCompanies(
                companyName, city, state, country, companyStatus, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("companies", companies.getContent());
            response.put("totalCount", companies.getTotalElements());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", companies.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Company search failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get companies near location
    @GetMapping("/companies/near")
    public ResponseEntity<?> getCompaniesNearLocation(
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            List<Company> companies = companyRepository.findCompaniesNearLocation(city, state);
            
            if (companies.size() > limit) {
                companies = companies.subList(0, limit);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("companies", companies);
            response.put("count", companies.size());
            response.put("location", city + ", " + state);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to find companies near location: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== SEARCH SUGGESTIONS ==========

    // Get search suggestions for makes
    @GetMapping("/suggestions/makes")
    public ResponseEntity<?> getMakeSuggestions(@RequestParam(required = false) String query) {
        try {
            List<Vehicle> vehicles = vehicleRepository.findAll();
            List<String> makes = vehicles.stream()
                .map(Vehicle::getMake)
                .distinct()
                .filter(make -> query == null || make.toLowerCase().contains(query.toLowerCase()))
                .sorted()
                .limit(20)
                .toList();

            return ResponseEntity.ok(makes);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get make suggestions: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get search suggestions for models
    @GetMapping("/suggestions/models")
    public ResponseEntity<?> getModelSuggestions(
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String query) {

        try {
            List<Vehicle> vehicles = vehicleRepository.findAll();
            List<String> models = vehicles.stream()
                .filter(vehicle -> make == null || vehicle.getMake().equalsIgnoreCase(make))
                .map(Vehicle::getModel)
                .distinct()
                .filter(model -> query == null || model.toLowerCase().contains(query.toLowerCase()))
                .sorted()
                .limit(20)
                .toList();

            return ResponseEntity.ok(models);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get model suggestions: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get search suggestions for locations
    @GetMapping("/suggestions/locations")
    public ResponseEntity<?> getLocationSuggestions(@RequestParam(required = false) String query) {
        try {
            List<Company> companies = companyRepository.findAll();
            List<String> locations = companies.stream()
                .map(company -> company.getCity() + ", " + company.getState())
                .distinct()
                .filter(location -> query == null || location.toLowerCase().contains(query.toLowerCase()))
                .sorted()
                .limit(20)
                .toList();

            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get location suggestions: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== SEARCH FILTERS ==========

    // Get available search filters
    @GetMapping("/filters")
    public ResponseEntity<?> getSearchFilters() {
        try {
            Map<String, Object> filters = new HashMap<>();

            // Vehicle types
            filters.put("vehicleTypes", Vehicle.VehicleType.values());

            // Fuel types
            List<Vehicle> vehicles = vehicleRepository.findAll();
            List<String> fuelTypes = vehicles.stream()
                .map(Vehicle::getFuelType)
                .filter(fuelType -> fuelType != null && !fuelType.isEmpty())
                .distinct()
                .sorted()
                .toList();
            filters.put("fuelTypes", fuelTypes);

            // Transmissions
            List<String> transmissions = vehicles.stream()
                .map(Vehicle::getTransmission)
                .filter(transmission -> transmission != null && !transmission.isEmpty())
                .distinct()
                .sorted()
                .toList();
            filters.put("transmissions", transmissions);

            // Colors
            List<String> colors = vehicles.stream()
                .map(Vehicle::getColor)
                .filter(color -> color != null && !color.isEmpty())
                .distinct()
                .sorted()
                .toList();
            filters.put("colors", colors);

            // Year range
            int minYear = vehicles.stream().mapToInt(Vehicle::getYear).min().orElse(2000);
            int maxYear = vehicles.stream().mapToInt(Vehicle::getYear).max().orElse(2024);
            filters.put("yearRange", Map.of("min", minYear, "max", maxYear));

            // Price range
            BigDecimal minPrice = vehicles.stream()
                .map(Vehicle::getDailyRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = vehicles.stream()
                .map(Vehicle::getDailyRate)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.valueOf(1000));
            filters.put("priceRange", Map.of("min", minPrice, "max", maxPrice));

            // Seating capacity range
            int minSeating = vehicles.stream()
                .mapToInt(vehicle -> vehicle.getSeatingCapacity() != null ? vehicle.getSeatingCapacity() : 2)
                .min()
                .orElse(2);
            int maxSeating = vehicles.stream()
                .mapToInt(vehicle -> vehicle.getSeatingCapacity() != null ? vehicle.getSeatingCapacity() : 8)
                .max()
                .orElse(8);
            filters.put("seatingRange", Map.of("min", minSeating, "max", maxSeating));

            return ResponseEntity.ok(filters);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get search filters: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== HELPER METHODS ==========

    private boolean isVehicleAvailable(Long vehicleId, LocalDate startDate, LocalDate endDate) {
        long conflictingReservations = reservationRepository.countConflictingReservations(
            vehicleId, startDate, endDate);
        return conflictingReservations == 0;
    }

    private List<Vehicle> sortVehicles(List<Vehicle> vehicles, String sortBy, String sortDir) {
        boolean ascending = !sortDir.equalsIgnoreCase("desc");
        
        return vehicles.stream()
            .sorted((v1, v2) -> {
                int comparison = 0;
                switch (sortBy.toLowerCase()) {
                    case "dailyrate":
                        comparison = v1.getDailyRate().compareTo(v2.getDailyRate());
                        break;
                    case "year":
                        comparison = v1.getYear().compareTo(v2.getYear());
                        break;
                    case "make":
                        comparison = v1.getMake().compareTo(v2.getMake());
                        break;
                    case "model":
                        comparison = v1.getModel().compareTo(v2.getModel());
                        break;
                    case "averagerating":
                        BigDecimal rating1 = v1.getAverageRating() != null ? v1.getAverageRating() : BigDecimal.ZERO;
                        BigDecimal rating2 = v2.getAverageRating() != null ? v2.getAverageRating() : BigDecimal.ZERO;
                        comparison = rating1.compareTo(rating2);
                        break;
                    default:
                        comparison = v1.getDailyRate().compareTo(v2.getDailyRate());
                }
                return ascending ? comparison : -comparison;
            })
            .toList();
    }
}
