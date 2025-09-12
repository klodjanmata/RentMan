package com.rentman.rentman.repository;

import com.rentman.rentman.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    // Find by invoice
    List<InvoiceItem> findByInvoiceIdOrderById(Long invoiceId);

    // Find by item type
    List<InvoiceItem> findByItemType(InvoiceItem.ItemType itemType);

    // Find by invoice and item type
    List<InvoiceItem> findByInvoiceIdAndItemType(Long invoiceId, InvoiceItem.ItemType itemType);

    // Find by reference ID and type
    List<InvoiceItem> findByReferenceIdAndReferenceType(Long referenceId, String referenceType);

    // Find by price range
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.totalPrice BETWEEN :minPrice AND :maxPrice")
    List<InvoiceItem> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // Find by invoice and price range
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId AND ii.totalPrice BETWEEN :minPrice AND :maxPrice")
    List<InvoiceItem> findByInvoiceAndPriceRange(@Param("invoiceId") Long invoiceId, 
                                               @Param("minPrice") BigDecimal minPrice, 
                                               @Param("maxPrice") BigDecimal maxPrice);

    // Calculate total amount by invoice
    @Query("SELECT SUM(ii.totalPrice) FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId")
    BigDecimal calculateTotalByInvoice(@Param("invoiceId") Long invoiceId);

    // Calculate total amount by item type
    @Query("SELECT SUM(ii.totalPrice) FROM InvoiceItem ii WHERE ii.itemType = :itemType")
    BigDecimal calculateTotalByItemType(@Param("itemType") InvoiceItem.ItemType itemType);

    // Calculate total amount by invoice and item type
    @Query("SELECT SUM(ii.totalPrice) FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId AND ii.itemType = :itemType")
    BigDecimal calculateTotalByInvoiceAndItemType(@Param("invoiceId") Long invoiceId, 
                                                @Param("itemType") InvoiceItem.ItemType itemType);

    // Find items by company (through invoice)
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.invoice.company.id = :companyId")
    List<InvoiceItem> findByCompanyId(@Param("companyId") Long companyId);

    // Find items by company and item type
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.invoice.company.id = :companyId AND ii.itemType = :itemType")
    List<InvoiceItem> findByCompanyIdAndItemType(@Param("companyId") Long companyId, 
                                               @Param("itemType") InvoiceItem.ItemType itemType);

    // Calculate total revenue by company and item type
    @Query("SELECT SUM(ii.totalPrice) FROM InvoiceItem ii WHERE ii.invoice.company.id = :companyId AND ii.itemType = :itemType AND ii.invoice.status = 'PAID'")
    BigDecimal calculateRevenueByCompanyAndItemType(@Param("companyId") Long companyId, 
                                                  @Param("itemType") InvoiceItem.ItemType itemType);

    // Find items by reservation
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.referenceId = :reservationId AND ii.referenceType = 'RESERVATION'")
    List<InvoiceItem> findByReservationId(@Param("reservationId") Long reservationId);

    // Find items by vehicle
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.referenceId = :vehicleId AND ii.referenceType = 'VEHICLE'")
    List<InvoiceItem> findByVehicleId(@Param("vehicleId") Long vehicleId);

    // Count items by type
    long countByItemType(InvoiceItem.ItemType itemType);

    // Count items by invoice
    long countByInvoiceId(Long invoiceId);

    // Count items by company and type
    @Query("SELECT COUNT(ii) FROM InvoiceItem ii WHERE ii.invoice.company.id = :companyId AND ii.itemType = :itemType")
    long countByCompanyIdAndItemType(@Param("companyId") Long companyId, @Param("itemType") InvoiceItem.ItemType itemType);

    // Get item statistics by company
    @Query("SELECT " +
           "ii.itemType, " +
           "COUNT(ii) as itemCount, " +
           "SUM(ii.totalPrice) as totalAmount, " +
           "AVG(ii.totalPrice) as averageAmount " +
           "FROM InvoiceItem ii " +
           "WHERE ii.invoice.company.id = :companyId AND ii.invoice.status = 'PAID' " +
           "GROUP BY ii.itemType")
    List<Object[]> getItemStatisticsByCompany(@Param("companyId") Long companyId);

    // Find top selling items by company
    @Query("SELECT ii.itemType, COUNT(ii) as itemCount, SUM(ii.totalPrice) as totalAmount " +
           "FROM InvoiceItem ii " +
           "WHERE ii.invoice.company.id = :companyId AND ii.invoice.status = 'PAID' " +
           "GROUP BY ii.itemType " +
           "ORDER BY itemCount DESC")
    List<Object[]> findTopSellingItemsByCompany(@Param("companyId") Long companyId);

    // Find items with discounts
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.discountAmount > 0")
    List<InvoiceItem> findItemsWithDiscounts();

    // Find items by company with discounts
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.invoice.company.id = :companyId AND ii.discountAmount > 0")
    List<InvoiceItem> findItemsWithDiscountsByCompany(@Param("companyId") Long companyId);

    // Calculate total discounts by company
    @Query("SELECT SUM(ii.discountAmount) FROM InvoiceItem ii WHERE ii.invoice.company.id = :companyId")
    BigDecimal calculateTotalDiscountsByCompany(@Param("companyId") Long companyId);

    // Calculate total tax by company
    @Query("SELECT SUM(ii.taxAmount) FROM InvoiceItem ii WHERE ii.invoice.company.id = :companyId")
    BigDecimal calculateTotalTaxByCompany(@Param("companyId") Long companyId);
}
