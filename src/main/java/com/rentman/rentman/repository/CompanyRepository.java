package com.rentman.rentman.repository;

import com.rentman.rentman.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    // Find by business registration number
    Optional<Company> findByBusinessRegistrationNumber(String businessRegistrationNumber);

    // Find by tax ID
    Optional<Company> findByTaxId(String taxId);

    // Find by email
    Optional<Company> findByEmail(String email);

    // Check if business registration number exists
    boolean existsByBusinessRegistrationNumber(String businessRegistrationNumber);

    // Check if tax ID exists
    boolean existsByTaxId(String taxId);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find by company name (case insensitive)
    List<Company> findByCompanyNameContainingIgnoreCase(String companyName);

    // Find by status
    List<Company> findByStatus(Company.CompanyStatus status);

    // Find active companies
    List<Company> findByStatusAndIsVerifiedTrue(Company.CompanyStatus status);

    // Find featured companies
    List<Company> findByIsFeaturedTrueAndStatus(Company.CompanyStatus status);

    // Find by subscription plan
    List<Company> findBySubscriptionPlan(Company.SubscriptionPlan subscriptionPlan);

    // Find companies by location
    @Query("SELECT c FROM Company c WHERE c.city = :city AND c.state = :state")
    List<Company> findByLocation(@Param("city") String city, @Param("state") String state);

    // Find companies by country
    List<Company> findByCountry(String country);

    // Find companies with expiring subscriptions
    @Query("SELECT c FROM Company c WHERE c.subscriptionEndDate IS NOT NULL AND c.subscriptionEndDate <= :expiryDate")
    List<Company> findCompaniesWithExpiringSubscriptions(@Param("expiryDate") LocalDateTime expiryDate);

    // Find companies by revenue range
    @Query("SELECT c FROM Company c WHERE c.totalRevenue BETWEEN :minRevenue AND :maxRevenue")
    List<Company> findByRevenueRange(@Param("minRevenue") java.math.BigDecimal minRevenue, 
                                   @Param("maxRevenue") java.math.BigDecimal maxRevenue);

    // Find top performing companies by revenue
    @Query("SELECT c FROM Company c WHERE c.status = :status ORDER BY c.totalRevenue DESC")
    Page<Company> findTopCompaniesByRevenue(@Param("status") Company.CompanyStatus status, Pageable pageable);

    // Find companies by rating
    @Query("SELECT c FROM Company c WHERE c.averageRating >= :minRating AND c.status = :status")
    List<Company> findByMinimumRating(@Param("minRating") java.math.BigDecimal minRating, 
                                    @Param("status") Company.CompanyStatus status);

    // Search companies by multiple criteria
    @Query("SELECT c FROM Company c WHERE " +
           "(:companyName IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))) AND " +
           "(:city IS NULL OR LOWER(c.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR LOWER(c.state) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
           "(:country IS NULL OR LOWER(c.country) LIKE LOWER(CONCAT('%', :country, '%'))) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<Company> searchCompanies(@Param("companyName") String companyName,
                                @Param("city") String city,
                                @Param("state") String state,
                                @Param("country") String country,
                                @Param("status") Company.CompanyStatus status,
                                Pageable pageable);

    // Count companies by status
    long countByStatus(Company.CompanyStatus status);

    // Count companies by subscription plan
    long countBySubscriptionPlan(Company.SubscriptionPlan subscriptionPlan);

    // Get company statistics
    @Query("SELECT " +
           "COUNT(c) as totalCompanies, " +
           "COUNT(CASE WHEN c.status = 'ACTIVE' THEN 1 END) as activeCompanies, " +
           "COUNT(CASE WHEN c.status = 'PENDING_APPROVAL' THEN 1 END) as pendingCompanies, " +
           "COUNT(CASE WHEN c.isVerified = true THEN 1 END) as verifiedCompanies, " +
           "SUM(c.totalRevenue) as totalRevenue, " +
           "AVG(c.averageRating) as averageRating " +
           "FROM Company c")
    Object[] getCompanyStatistics();

    // Find companies created in date range
    @Query("SELECT c FROM Company c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Company> findCompaniesCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    // Find companies with most bookings
    @Query("SELECT c FROM Company c WHERE c.status = :status ORDER BY c.totalBookings DESC")
    Page<Company> findCompaniesByBookings(@Param("status") Company.CompanyStatus status, Pageable pageable);

    // Find companies with most vehicles
    @Query("SELECT c FROM Company c WHERE c.status = :status AND SIZE(c.vehicles) > 0 ORDER BY SIZE(c.vehicles) DESC")
    Page<Company> findCompaniesByVehicleCount(@Param("status") Company.CompanyStatus status, Pageable pageable);

    // Find companies near a location (within radius - simplified)
    @Query("SELECT c FROM Company c WHERE c.city = :city AND c.state = :state AND c.status = 'ACTIVE'")
    List<Company> findCompaniesNearLocation(@Param("city") String city, @Param("state") String state);

    // Find recent companies (top N)
    List<Company> findTop10ByOrderByCreatedAtDesc();
}
