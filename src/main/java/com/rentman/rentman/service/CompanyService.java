package com.rentman.rentman.service;

import com.rentman.rentman.entity.Company;
import com.rentman.rentman.entity.User;
import com.rentman.rentman.entity.Vehicle;
import com.rentman.rentman.entity.Reservation;
import com.rentman.rentman.repository.CompanyRepository;
import com.rentman.rentman.repository.UserRepository;
import com.rentman.rentman.repository.VehicleRepository;
import com.rentman.rentman.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    // ========== COMPANY CRUD OPERATIONS ==========

    public Company createCompany(Company company) {
        // Validate business registration number uniqueness
        if (companyRepository.existsByBusinessRegistrationNumber(company.getBusinessRegistrationNumber())) {
            throw new RuntimeException("Business registration number already exists: " + company.getBusinessRegistrationNumber());
        }

        // Validate tax ID uniqueness
        if (companyRepository.existsByTaxId(company.getTaxId())) {
            throw new RuntimeException("Tax ID already exists: " + company.getTaxId());
        }

        // Validate email uniqueness
        if (companyRepository.existsByEmail(company.getEmail())) {
            throw new RuntimeException("Email already exists: " + company.getEmail());
        }

        // Set default values
        if (company.getSubscriptionPlan() == null) {
            company.setSubscriptionPlan(Company.SubscriptionPlan.BASIC);
        }
        
        if (company.getStatus() == null) {
            company.setStatus(Company.CompanyStatus.PENDING_APPROVAL);
        }

        // Set subscription limits based on plan
        Company.SubscriptionPlan plan = company.getSubscriptionPlan();
        company.setMaxVehicles(plan.getMaxVehicles());
        company.setMaxEmployees(plan.getMaxEmployees());
        company.setMonthlyFee(plan.getMonthlyFee());

        return companyRepository.save(company);
    }

    public Company updateCompany(Long id, Company companyDetails) {
        Company company = getCompanyById(id);

        // Update allowed fields
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

        return companyRepository.save(company);
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + id));
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Page<Company> searchCompanies(String companyName, String city, String state, 
                                       String country, Company.CompanyStatus status, Pageable pageable) {
        return companyRepository.searchCompanies(companyName, city, state, country, status, pageable);
    }

    public void deleteCompany(Long id) {
        Company company = getCompanyById(id);
        
        // Check if company has active reservations
        List<Reservation> activeReservations = reservationRepository.findCurrentActiveReservationsByCompany(id, java.time.LocalDate.now());
        if (!activeReservations.isEmpty()) {
            throw new RuntimeException("Cannot delete company with active reservations");
        }

        // Check if company has vehicles
        long vehicleCount = vehicleRepository.countByCompanyId(id);
        if (vehicleCount > 0) {
            throw new RuntimeException("Cannot delete company with vehicles. Please remove all vehicles first.");
        }

        // Check if company has employees
        long employeeCount = userRepository.countByCompanyId(id);
        if (employeeCount > 0) {
            throw new RuntimeException("Cannot delete company with employees. Please remove all employees first.");
        }

        companyRepository.deleteById(id);
    }

    // ========== COMPANY STATUS MANAGEMENT ==========

    public Company updateCompanyStatus(Long id, Company.CompanyStatus status) {
        Company company = getCompanyById(id);
        company.setStatus(status);
        
        if (status == Company.CompanyStatus.ACTIVE) {
            company.setApprovedAt(LocalDateTime.now());
            // TODO: Set approvedBy to current admin user ID
        }

        return companyRepository.save(company);
    }

    public Company approveCompany(Long id, Long approvedByUserId) {
        Company company = getCompanyById(id);
        company.setStatus(Company.CompanyStatus.ACTIVE);
        company.setApprovedAt(LocalDateTime.now());
        company.setApprovedBy(approvedByUserId);
        company.setIsVerified(true);

        return companyRepository.save(company);
    }

    public Company suspendCompany(Long id, String reason) {
        Company company = getCompanyById(id);
        company.setStatus(Company.CompanyStatus.SUSPENDED);
        // TODO: Add suspension reason to company entity

        return companyRepository.save(company);
    }

    // ========== SUBSCRIPTION MANAGEMENT ==========

    public Company updateSubscriptionPlan(Long id, Company.SubscriptionPlan plan) {
        Company company = getCompanyById(id);
        
        // Check if company can upgrade/downgrade
        if (plan == Company.SubscriptionPlan.BASIC && company.getSubscriptionPlan() == Company.SubscriptionPlan.ENTERPRISE) {
            // Check if company has more vehicles/employees than basic plan allows
            long vehicleCount = vehicleRepository.countByCompanyId(id);
            long employeeCount = userRepository.countByCompanyId(id);
            
            if (vehicleCount > plan.getMaxVehicles()) {
                throw new RuntimeException("Cannot downgrade: Company has " + vehicleCount + " vehicles, but basic plan allows only " + plan.getMaxVehicles());
            }
            
            if (employeeCount > plan.getMaxEmployees()) {
                throw new RuntimeException("Cannot downgrade: Company has " + employeeCount + " employees, but basic plan allows only " + plan.getMaxEmployees());
            }
        }

        company.setSubscriptionPlan(plan);
        company.setMonthlyFee(plan.getMonthlyFee());
        company.setMaxVehicles(plan.getMaxVehicles());
        company.setMaxEmployees(plan.getMaxEmployees());

        return companyRepository.save(company);
    }

    public Company renewSubscription(Long id, int months) {
        Company company = getCompanyById(id);
        
        if (company.getSubscriptionEndDate() == null) {
            company.setSubscriptionEndDate(LocalDateTime.now().plusMonths(months));
        } else {
            company.setSubscriptionEndDate(company.getSubscriptionEndDate().plusMonths(months));
        }

        return companyRepository.save(company);
    }

    // ========== COMPANY ANALYTICS ==========

    public Object[] getCompanyStatistics(Long companyId) {
        return companyRepository.getCompanyStatistics();
    }

    public BigDecimal calculateCompanyRevenue(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.calculateRevenueByCompanyAndDateRange(companyId, startDate, endDate);
    }

    public BigDecimal calculateMonthlyRevenue(Long companyId, int year, int month) {
        return reservationRepository.calculateMonthlyRevenueByCompany(companyId, year, month);
    }

    // ========== COMPANY VALIDATION ==========

    public boolean canAddVehicle(Long companyId) {
        Company company = getCompanyById(companyId);
        long currentVehicleCount = vehicleRepository.countByCompanyId(companyId);
        return currentVehicleCount < company.getMaxVehicles();
    }

    public boolean canAddEmployee(Long companyId) {
        Company company = getCompanyById(companyId);
        long currentEmployeeCount = userRepository.countByCompanyId(companyId);
        return currentEmployeeCount < company.getMaxEmployees();
    }

    public boolean isSubscriptionActive(Long companyId) {
        Company company = getCompanyById(companyId);
        return company.isSubscriptionActive();
    }

    // ========== COMPANY SEARCH AND FILTERING ==========

    public List<Company> getActiveCompanies() {
        return companyRepository.findByStatusAndIsVerifiedTrue(Company.CompanyStatus.ACTIVE);
    }

    public List<Company> getFeaturedCompanies() {
        return companyRepository.findByIsFeaturedTrueAndStatus(Company.CompanyStatus.ACTIVE);
    }

    public List<Company> getCompaniesByLocation(String city, String state) {
        return companyRepository.findByLocation(city, state);
    }

    public List<Company> getCompaniesByCountry(String country) {
        return companyRepository.findByCountry(country);
    }

    public List<Company> getCompaniesWithExpiringSubscriptions(int daysAhead) {
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(daysAhead);
        return companyRepository.findCompaniesWithExpiringSubscriptions(expiryDate);
    }

    // ========== COMPANY PERFORMANCE ==========

    public List<Company> getTopPerformingCompanies(int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return companyRepository.findTopCompaniesByRevenue(Company.CompanyStatus.ACTIVE, pageable).getContent();
    }

    public List<Company> getCompaniesByRating(BigDecimal minRating) {
        return companyRepository.findByMinimumRating(minRating, Company.CompanyStatus.ACTIVE);
    }

    public List<Company> getCompaniesByRevenueRange(BigDecimal minRevenue, BigDecimal maxRevenue) {
        return companyRepository.findByRevenueRange(minRevenue, maxRevenue);
    }

    // ========== COMPANY DASHBOARD DATA ==========

    public java.util.Map<String, Object> getCompanyDashboard(Long companyId) {
        Company company = getCompanyById(companyId);
        
        java.util.Map<String, Object> dashboard = new java.util.HashMap<>();
        dashboard.put("company", company);

        // Get statistics
        Object[] vehicleStats = vehicleRepository.getVehicleStatisticsByCompany(companyId);
        Object[] reservationStats = reservationRepository.getReservationStatisticsByCompany(companyId);
        Object[] userStats = userRepository.getUserStatisticsByCompany(companyId);

        dashboard.put("vehicleStatistics", vehicleStats);
        dashboard.put("reservationStatistics", reservationStats);
        dashboard.put("userStatistics", userStats);

        // Get recent activities
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        dashboard.put("recentReservations", reservationRepository.findRecentReservationsByCompany(companyId, thirtyDaysAgo));
        dashboard.put("recentVehicles", vehicleRepository.findVehiclesByCompanyAndCreatedDateRange(companyId, thirtyDaysAgo, LocalDateTime.now()));
        dashboard.put("recentEmployees", userRepository.findUsersByCompanyAndCreatedDateRange(companyId, thirtyDaysAgo, LocalDateTime.now()));

        // Get upcoming activities
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate nextWeek = today.plusDays(7);
        dashboard.put("upcomingReservations", reservationRepository.findUpcomingReservationsByCompany(companyId, today, nextWeek));
        dashboard.put("todayPickups", reservationRepository.findReservationsPendingPickupByCompany(companyId, today));
        dashboard.put("todayReturns", reservationRepository.findReservationsPendingReturnByCompany(companyId, today));

        return dashboard;
    }

    // ========== COMPANY EMPLOYEES ==========

    public List<User> getCompanyEmployees(Long companyId) {
        return userRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public List<User> getCompanyEmployeesByRole(Long companyId, User.UserRole role) {
        return userRepository.findByCompanyIdAndRoleOrderByCreatedAtDesc(companyId, role);
    }

    public long getCompanyEmployeeCount(Long companyId) {
        return userRepository.countByCompanyId(companyId);
    }

    // ========== COMPANY VEHICLES ==========

    public List<Vehicle> getCompanyVehicles(Long companyId) {
        return vehicleRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public List<Vehicle> getCompanyVehiclesByStatus(Long companyId, Vehicle.VehicleStatus status) {
        return vehicleRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, status);
    }

    public long getCompanyVehicleCount(Long companyId) {
        return vehicleRepository.countByCompanyId(companyId);
    }

    // ========== COMPANY RESERVATIONS ==========

    public List<Reservation> getCompanyReservations(Long companyId) {
        return reservationRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public List<Reservation> getCompanyReservationsByStatus(Long companyId, Reservation.ReservationStatus status) {
        return reservationRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, status);
    }

    public long getCompanyReservationCount(Long companyId) {
        return reservationRepository.countByCompanyId(companyId);
    }

    // ========== HELPER METHODS ==========

    public boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber) {
        return companyRepository.existsByBusinessRegistrationNumber(businessRegistrationNumber);
    }

    public boolean existsByTaxId(String taxId) {
        return companyRepository.existsByTaxId(taxId);
    }

    public boolean existsByEmail(String email) {
        return companyRepository.existsByEmail(email);
    }

    public Optional<Company> findByBusinessRegistrationNumber(String businessRegistrationNumber) {
        return companyRepository.findByBusinessRegistrationNumber(businessRegistrationNumber);
    }

    public Optional<Company> findByTaxId(String taxId) {
        return companyRepository.findByTaxId(taxId);
    }

    public Optional<Company> findByEmail(String email) {
        return companyRepository.findByEmail(email);
    }
}
