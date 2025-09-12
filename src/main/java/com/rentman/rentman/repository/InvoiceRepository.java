package com.rentman.rentman.repository;

import com.rentman.rentman.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Find by invoice number
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    // Find by company
    List<Invoice> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    // Find by company and status
    List<Invoice> findByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, Invoice.InvoiceStatus status);

    // Find by company and type
    List<Invoice> findByCompanyIdAndTypeOrderByCreatedAtDesc(Long companyId, Invoice.InvoiceType type);

    // Find by status
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);

    // Find by type
    List<Invoice> findByType(Invoice.InvoiceType type);

    // Find overdue invoices
    @Query("SELECT i FROM Invoice i WHERE i.status IN ('PENDING', 'SENT') AND i.dueDate < :currentDate")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    // Find invoices by date range
    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate")
    List<Invoice> findInvoicesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find invoices by company and date range
    @Query("SELECT i FROM Invoice i WHERE i.company.id = :companyId AND i.invoiceDate BETWEEN :startDate AND :endDate")
    List<Invoice> findInvoicesByCompanyAndDateRange(@Param("companyId") Long companyId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);

    // Find invoices by amount range
    @Query("SELECT i FROM Invoice i WHERE i.totalAmount BETWEEN :minAmount AND :maxAmount")
    List<Invoice> findByAmountRange(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount);

    // Find invoices by company and amount range
    @Query("SELECT i FROM Invoice i WHERE i.company.id = :companyId AND i.totalAmount BETWEEN :minAmount AND :maxAmount")
    List<Invoice> findByCompanyAndAmountRange(@Param("companyId") Long companyId, 
                                            @Param("minAmount") BigDecimal minAmount, 
                                            @Param("maxAmount") BigDecimal maxAmount);

    // Find paid invoices
    List<Invoice> findByStatusAndPaidDateIsNotNull(Invoice.InvoiceStatus status);

    // Find unpaid invoices
    @Query("SELECT i FROM Invoice i WHERE i.status IN ('PENDING', 'SENT', 'OVERDUE')")
    List<Invoice> findUnpaidInvoices();

    // Find invoices by company and unpaid status
    @Query("SELECT i FROM Invoice i WHERE i.company.id = :companyId AND i.status IN ('PENDING', 'SENT', 'OVERDUE')")
    List<Invoice> findUnpaidInvoicesByCompany(@Param("companyId") Long companyId);

    // Calculate total revenue by company
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.company.id = :companyId AND i.status = 'PAID'")
    BigDecimal calculateTotalRevenueByCompany(@Param("companyId") Long companyId);

    // Calculate total revenue by company and date range
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.company.id = :companyId AND i.status = 'PAID' AND i.paidDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueByCompanyAndDateRange(@Param("companyId") Long companyId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);

    // Calculate total outstanding amount by company
    @Query("SELECT SUM(i.balanceDue) FROM Invoice i WHERE i.company.id = :companyId AND i.status IN ('PENDING', 'SENT', 'OVERDUE')")
    BigDecimal calculateOutstandingAmountByCompany(@Param("companyId") Long companyId);

    // Find invoices due soon (within next 7 days)
    @Query("SELECT i FROM Invoice i WHERE i.status IN ('PENDING', 'SENT') AND i.dueDate BETWEEN :currentDate AND :futureDate")
    List<Invoice> findInvoicesDueSoon(@Param("currentDate") LocalDate currentDate, @Param("futureDate") LocalDate futureDate);

    // Find invoices by payment method
    List<Invoice> findByPaymentMethod(String paymentMethod);

    // Find invoices by company and payment method
    List<Invoice> findByCompanyIdAndPaymentMethod(Long companyId, String paymentMethod);

    // Get invoice statistics by company
    @Query("SELECT " +
           "COUNT(i) as totalInvoices, " +
           "COUNT(CASE WHEN i.status = 'PAID' THEN 1 END) as paidInvoices, " +
           "COUNT(CASE WHEN i.status = 'PENDING' THEN 1 END) as pendingInvoices, " +
           "COUNT(CASE WHEN i.status = 'OVERDUE' THEN 1 END) as overdueInvoices, " +
           "SUM(i.totalAmount) as totalAmount, " +
           "SUM(CASE WHEN i.status = 'PAID' THEN i.totalAmount ELSE 0 END) as paidAmount, " +
           "SUM(CASE WHEN i.status IN ('PENDING', 'SENT', 'OVERDUE') THEN i.balanceDue ELSE 0 END) as outstandingAmount " +
           "FROM Invoice i WHERE i.company.id = :companyId")
    Object[] getInvoiceStatisticsByCompany(@Param("companyId") Long companyId);

    // Get platform revenue statistics
    @Query("SELECT " +
           "COUNT(i) as totalInvoices, " +
           "SUM(i.totalAmount) as totalRevenue, " +
           "SUM(i.platformCommission) as totalCommission, " +
           "SUM(i.processingFee) as totalProcessingFees " +
           "FROM Invoice i WHERE i.status = 'PAID'")
    Object[] getPlatformRevenueStatistics();

    // Find invoices by reservation
    List<Invoice> findByReservationId(Long reservationId);

    // Find subscription invoices by company
    @Query("SELECT i FROM Invoice i WHERE i.company.id = :companyId AND i.type = 'SUBSCRIPTION' ORDER BY i.invoiceDate DESC")
    List<Invoice> findSubscriptionInvoicesByCompany(@Param("companyId") Long companyId);

    // Find commission invoices by company
    @Query("SELECT i FROM Invoice i WHERE i.company.id = :companyId AND i.type = 'COMMISSION' ORDER BY i.invoiceDate DESC")
    List<Invoice> findCommissionInvoicesByCompany(@Param("companyId") Long companyId);

    // Find invoices created in date range
    @Query("SELECT i FROM Invoice i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    List<Invoice> findInvoicesCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    // Find invoices by company and created date range
    @Query("SELECT i FROM Invoice i WHERE i.company.id = :companyId AND i.createdAt BETWEEN :startDate AND :endDate")
    List<Invoice> findInvoicesByCompanyAndCreatedDateRange(@Param("companyId") Long companyId, 
                                                         @Param("startDate") LocalDateTime startDate, 
                                                         @Param("endDate") LocalDateTime endDate);

    // Count invoices by status
    long countByStatus(Invoice.InvoiceStatus status);

    // Count invoices by company and status
    long countByCompanyIdAndStatus(Long companyId, Invoice.InvoiceStatus status);

    // Count invoices by type
    long countByType(Invoice.InvoiceType type);

    // Count invoices by company and type
    long countByCompanyIdAndType(Long companyId, Invoice.InvoiceType type);
}
