package com.rentman.rentman.controller;

import com.rentman.rentman.entity.Company;
import com.rentman.rentman.entity.User;
import com.rentman.rentman.entity.Vehicle;
import com.rentman.rentman.entity.Reservation;
import com.rentman.rentman.entity.Maintenance;
import com.rentman.rentman.entity.Defect;
import com.rentman.rentman.entity.Invoice;
import com.rentman.rentman.dto.CompanyRegistrationRequest;
import com.rentman.rentman.dto.CompanyRegistrationResult;
import com.rentman.rentman.service.CompanyService;
import com.rentman.rentman.repository.CompanyRepository;
import com.rentman.rentman.repository.UserRepository;
import com.rentman.rentman.repository.VehicleRepository;
import com.rentman.rentman.repository.ReservationRepository;
import com.rentman.rentman.repository.MaintenanceRepository;
import com.rentman.rentman.repository.DefectRepository;
import com.rentman.rentman.repository.InvoiceRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*")
public class CompanyController {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private DefectRepository defectRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CompanyService companyService;

    // ========== COMPANY MANAGEMENT ==========

    // Get all companies
    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return ResponseEntity.ok(companies);
    }

    // Get company by ID
    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        Optional<Company> company = companyRepository.findById(id);
        return company.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create new company
    @PostMapping
    public ResponseEntity<?> createCompany(@Valid @RequestBody Company company) {
        try {
            // Check if business registration number already exists
            if (companyRepository.existsByBusinessRegistrationNumber(company.getBusinessRegistrationNumber())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Business registration number already exists");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if tax ID already exists
            if (companyRepository.existsByTaxId(company.getTaxId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Tax ID already exists");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if email already exists
            if (companyRepository.existsByEmail(company.getEmail())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email already exists");
                return ResponseEntity.badRequest().body(error);
            }

            Company savedCompany = companyRepository.save(company);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCompany);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create company: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Register company with admin user
    @PostMapping("/register")
    public ResponseEntity<?> registerCompanyWithAdmin(@Valid @RequestBody CompanyRegistrationRequest request) {
        try {
            CompanyRegistrationResult result = companyService.registerCompanyWithAdmin(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("company", result.getCompany());
            response.put("adminUser", result.getAdminUser());
            response.put("message", "Company and admin user registered successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Update company
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable Long id, @Valid @RequestBody Company companyDetails) {
        try {
            Optional<Company> optionalCompany = companyRepository.findById(id);
            if (optionalCompany.isPresent()) {
                Company company = optionalCompany.get();
                
                // Update fields
                company.setCompanyName(companyDetails.getCompanyName());
                company.setEmail(companyDetails.getEmail());
                company.setPhoneNumber(companyDetails.getPhoneNumber());
                company.setWebsite(companyDetails.getWebsite());
                company.setStreetAddress(companyDetails.getStreetAddress());
                company.setCity(companyDetails.getCity());
                company.setState(companyDetails.getState());
                company.setPostalCode(companyDetails.getPostalCode());
                company.setCountry(companyDetails.getCountry());
                company.setDescription(companyDetails.getDescription());
                company.setLogoUrl(companyDetails.getLogoUrl());
                company.setOperatingHoursStart(companyDetails.getOperatingHoursStart());
                company.setOperatingHoursEnd(companyDetails.getOperatingHoursEnd());
                company.setOperatingDays(companyDetails.getOperatingDays());
                company.setContactPersonName(companyDetails.getContactPersonName());
                company.setContactPersonTitle(companyDetails.getContactPersonTitle());
                company.setEmergencyContact(companyDetails.getEmergencyContact());

                Company updatedCompany = companyRepository.save(company);
                return ResponseEntity.ok(updatedCompany);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update company: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Update company status
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateCompanyStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Optional<Company> optionalCompany = companyRepository.findById(id);
            if (optionalCompany.isPresent()) {
                Company company = optionalCompany.get();
                Company.CompanyStatus newStatus = Company.CompanyStatus.valueOf(status.toUpperCase());
                company.setStatus(newStatus);
                
                if (newStatus == Company.CompanyStatus.ACTIVE) {
                    company.setApprovedAt(LocalDateTime.now());
                    // TODO: Set approvedBy to current admin user ID
                }

                Company updatedCompany = companyRepository.save(company);
                return ResponseEntity.ok(updatedCompany);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid status: " + status);
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update company status: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Update subscription plan
    @PatchMapping("/{id}/subscription")
    public ResponseEntity<?> updateSubscriptionPlan(@PathVariable Long id, @RequestParam String plan) {
        try {
            Optional<Company> optionalCompany = companyRepository.findById(id);
            if (optionalCompany.isPresent()) {
                Company company = optionalCompany.get();
                Company.SubscriptionPlan newPlan = Company.SubscriptionPlan.valueOf(plan.toUpperCase());
                company.setSubscriptionPlan(newPlan);
                company.setMonthlyFee(newPlan.getMonthlyFee());
                company.setMaxVehicles(newPlan.getMaxVehicles());
                company.setMaxEmployees(newPlan.getMaxEmployees());

                Company updatedCompany = companyRepository.save(company);
                return ResponseEntity.ok(updatedCompany);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid subscription plan: " + plan);
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update subscription plan: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Delete company
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
        try {
            if (companyRepository.existsById(id)) {
                companyRepository.deleteById(id);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Company deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete company: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== COMPANY SEARCH AND FILTERING ==========

    // Search companies
    @GetMapping("/search")
    public ResponseEntity<Page<Company>> searchCompanies(
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

            return ResponseEntity.ok(companies);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get companies by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Company>> getCompaniesByStatus(@PathVariable String status) {
        try {
            Company.CompanyStatus companyStatus = Company.CompanyStatus.valueOf(status.toUpperCase());
            List<Company> companies = companyRepository.findByStatus(companyStatus);
            return ResponseEntity.ok(companies);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get active companies
    @GetMapping("/active")
    public ResponseEntity<List<Company>> getActiveCompanies() {
        List<Company> companies = companyRepository.findByStatusAndIsVerifiedTrue(Company.CompanyStatus.ACTIVE);
        return ResponseEntity.ok(companies);
    }

    // Get featured companies
    @GetMapping("/featured")
    public ResponseEntity<List<Company>> getFeaturedCompanies() {
        List<Company> companies = companyRepository.findByIsFeaturedTrueAndStatus(Company.CompanyStatus.ACTIVE);
        return ResponseEntity.ok(companies);
    }

    // Get companies by location
    @GetMapping("/location")
    public ResponseEntity<List<Company>> getCompaniesByLocation(
            @RequestParam String city,
            @RequestParam String state) {
        List<Company> companies = companyRepository.findByLocation(city, state);
        return ResponseEntity.ok(companies);
    }

    // ========== COMPANY DASHBOARD DATA ==========

    // Get company dashboard data
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<?> getCompanyDashboard(@PathVariable Long id) {
        try {
            Optional<Company> optionalCompany = companyRepository.findById(id);
            if (optionalCompany.isPresent()) {
                Company company = optionalCompany.get();
                
                Map<String, Object> dashboard = new HashMap<>();
                dashboard.put("company", company);

                // Get statistics
                Object[] vehicleStats = vehicleRepository.getVehicleStatisticsByCompany(id);
                Object[] reservationStats = reservationRepository.getReservationStatisticsByCompany(id);
                Object[] userStats = userRepository.getUserStatisticsByCompany(id);
                Object[] maintenanceStats = maintenanceRepository.getMaintenanceStatisticsByCompany(id);
                Object[] defectStats = defectRepository.getDefectStatisticsByCompany(id);
                Object[] invoiceStats = invoiceRepository.getInvoiceStatisticsByCompany(id);

                dashboard.put("vehicleStatistics", vehicleStats);
                dashboard.put("reservationStatistics", reservationStats);
                dashboard.put("userStatistics", userStats);
                dashboard.put("maintenanceStatistics", maintenanceStats);
                dashboard.put("defectStatistics", defectStats);
                dashboard.put("invoiceStatistics", invoiceStats);

                // Get recent activities
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                dashboard.put("recentReservations", reservationRepository.findRecentReservationsByCompany(id, thirtyDaysAgo));
                dashboard.put("recentVehicles", vehicleRepository.findVehiclesByCompanyAndCreatedDateRange(id, thirtyDaysAgo, LocalDateTime.now()));
                dashboard.put("recentEmployees", userRepository.findUsersByCompanyAndCreatedDateRange(id, thirtyDaysAgo, LocalDateTime.now()));

                // Get upcoming activities
                LocalDate today = LocalDate.now();
                LocalDate nextWeek = today.plusDays(7);
                dashboard.put("upcomingReservations", reservationRepository.findUpcomingReservationsByCompany(id, today, nextWeek));
                dashboard.put("todayPickups", reservationRepository.findReservationsPendingPickupByCompany(id, today));
                dashboard.put("todayReturns", reservationRepository.findReservationsPendingReturnByCompany(id, today));

                // Get alerts
                dashboard.put("overdueReservations", reservationRepository.findOverdueReservationsByCompany(id, today));
                dashboard.put("vehiclesNeedingMaintenance", vehicleRepository.findVehiclesNeedingMaintenanceByCompany(id, today));
                dashboard.put("criticalDefects", defectRepository.findCriticalDefectsByCompany(id));

                return ResponseEntity.ok(dashboard);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get dashboard data: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== COMPANY EMPLOYEES ==========

    // Get company employees
    @GetMapping("/{id}/employees")
    public ResponseEntity<List<User>> getCompanyEmployees(@PathVariable Long id) {
        List<User> employees = userRepository.findByCompanyIdOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(employees);
    }

    // Get company employees by role
    @GetMapping("/{id}/employees/role/{role}")
    public ResponseEntity<List<User>> getCompanyEmployeesByRole(@PathVariable Long id, @PathVariable String role) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            List<User> employees = userRepository.findByCompanyIdAndRoleOrderByCreatedAtDesc(id, userRole);
            return ResponseEntity.ok(employees);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== COMPANY VEHICLES ==========

    // Get company vehicles
    @GetMapping("/{id}/vehicles")
    public ResponseEntity<List<Vehicle>> getCompanyVehicles(@PathVariable Long id) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyIdOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(vehicles);
    }

    // Get company vehicles by status
    @GetMapping("/{id}/vehicles/status/{status}")
    public ResponseEntity<List<Vehicle>> getCompanyVehiclesByStatus(@PathVariable Long id, @PathVariable String status) {
        try {
            Vehicle.VehicleStatus vehicleStatus = Vehicle.VehicleStatus.valueOf(status.toUpperCase());
            List<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(id, vehicleStatus);
            return ResponseEntity.ok(vehicles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== COMPANY RESERVATIONS ==========

    // Get company reservations
    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<Reservation>> getCompanyReservations(@PathVariable Long id) {
        List<Reservation> reservations = reservationRepository.findByCompanyIdOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(reservations);
    }

    // Get company reservations by status
    @GetMapping("/{id}/reservations/status/{status}")
    public ResponseEntity<List<Reservation>> getCompanyReservationsByStatus(@PathVariable Long id, @PathVariable String status) {
        try {
            Reservation.ReservationStatus reservationStatus = Reservation.ReservationStatus.valueOf(status.toUpperCase());
            List<Reservation> reservations = reservationRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(id, reservationStatus);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== COMPANY MAINTENANCE ==========

    // Get company maintenance records
    @GetMapping("/{id}/maintenance")
    public ResponseEntity<List<Maintenance>> getCompanyMaintenance(@PathVariable Long id) {
        List<Maintenance> maintenance = maintenanceRepository.findByCompanyIdOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(maintenance);
    }

    // Get company maintenance by status
    @GetMapping("/{id}/maintenance/status/{status}")
    public ResponseEntity<List<Maintenance>> getCompanyMaintenanceByStatus(@PathVariable Long id, @PathVariable String status) {
        try {
            Maintenance.MaintenanceStatus maintenanceStatus = Maintenance.MaintenanceStatus.valueOf(status.toUpperCase());
            List<Maintenance> maintenance = maintenanceRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(id, maintenanceStatus);
            return ResponseEntity.ok(maintenance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== COMPANY DEFECTS ==========

    // Get company defects
    @GetMapping("/{id}/defects")
    public ResponseEntity<List<Defect>> getCompanyDefects(@PathVariable Long id) {
        List<Defect> defects = defectRepository.findByCompanyIdOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(defects);
    }

    // Get company defects by status
    @GetMapping("/{id}/defects/status/{status}")
    public ResponseEntity<List<Defect>> getCompanyDefectsByStatus(@PathVariable Long id, @PathVariable String status) {
        try {
            Defect.DefectStatus defectStatus = Defect.DefectStatus.valueOf(status.toUpperCase());
            List<Defect> defects = defectRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(id, defectStatus);
            return ResponseEntity.ok(defects);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== COMPANY INVOICES ==========

    // Get company invoices
    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<Invoice>> getCompanyInvoices(@PathVariable Long id) {
        List<Invoice> invoices = invoiceRepository.findByCompanyIdOrderByCreatedAtDesc(id);
        return ResponseEntity.ok(invoices);
    }

    // Get company invoices by status
    @GetMapping("/{id}/invoices/status/{status}")
    public ResponseEntity<List<Invoice>> getCompanyInvoicesByStatus(@PathVariable Long id, @PathVariable String status) {
        try {
            Invoice.InvoiceStatus invoiceStatus = Invoice.InvoiceStatus.valueOf(status.toUpperCase());
            List<Invoice> invoices = invoiceRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(id, invoiceStatus);
            return ResponseEntity.ok(invoices);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== COMPANY STATISTICS ==========

    // Get company statistics
    @GetMapping("/{id}/statistics")
    public ResponseEntity<?> getCompanyStatistics(@PathVariable Long id) {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // Get all statistics
            Object[] vehicleStats = vehicleRepository.getVehicleStatisticsByCompany(id);
            Object[] reservationStats = reservationRepository.getReservationStatisticsByCompany(id);
            Object[] userStats = userRepository.getUserStatisticsByCompany(id);
            Object[] maintenanceStats = maintenanceRepository.getMaintenanceStatisticsByCompany(id);
            Object[] defectStats = defectRepository.getDefectStatisticsByCompany(id);
            Object[] invoiceStats = invoiceRepository.getInvoiceStatisticsByCompany(id);

            statistics.put("vehicles", vehicleStats);
            statistics.put("reservations", reservationStats);
            statistics.put("users", userStats);
            statistics.put("maintenance", maintenanceStats);
            statistics.put("defects", defectStats);
            statistics.put("invoices", invoiceStats);

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get company revenue by date range
    @GetMapping("/{id}/revenue")
    public ResponseEntity<?> getCompanyRevenue(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            BigDecimal revenue = reservationRepository.calculateRevenueByCompanyAndDateRange(id, startDateTime, endDateTime);
            BigDecimal invoiceRevenue = invoiceRepository.calculateRevenueByCompanyAndDateRange(id, startDate, endDate);

            Map<String, Object> revenueData = new HashMap<>();
            revenueData.put("reservationRevenue", revenue != null ? revenue : BigDecimal.ZERO);
            revenueData.put("invoiceRevenue", invoiceRevenue != null ? invoiceRevenue : BigDecimal.ZERO);
            revenueData.put("totalRevenue", (revenue != null ? revenue : BigDecimal.ZERO)
                    .add(invoiceRevenue != null ? invoiceRevenue : BigDecimal.ZERO));
            revenueData.put("startDate", startDate);
            revenueData.put("endDate", endDate);

            return ResponseEntity.ok(revenueData);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get revenue data: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== PLATFORM STATISTICS ==========

    // Get platform statistics
    @GetMapping("/statistics/platform")
    public ResponseEntity<?> getPlatformStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // Get platform-wide statistics
            Object[] companyStats = companyRepository.getCompanyStatistics();
            Object[] platformRevenue = invoiceRepository.getPlatformRevenueStatistics();

            statistics.put("companies", companyStats);
            statistics.put("revenue", platformRevenue);

            // Get recent activities
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            statistics.put("recentCompanies", companyRepository.findCompaniesCreatedBetween(thirtyDaysAgo, LocalDateTime.now()));

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get platform statistics: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
