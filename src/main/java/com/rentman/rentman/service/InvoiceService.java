package com.rentman.rentman.service;

import com.rentman.rentman.entity.Invoice;
import com.rentman.rentman.entity.InvoiceItem;
import com.rentman.rentman.entity.Company;
import com.rentman.rentman.entity.Reservation;
import com.rentman.rentman.repository.InvoiceRepository;
import com.rentman.rentman.repository.InvoiceItemRepository;
import com.rentman.rentman.repository.CompanyRepository;
import com.rentman.rentman.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceItemRepository invoiceItemRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    // ========== INVOICE CRUD OPERATIONS ==========

    public Invoice createInvoice(Invoice invoice) {
        // Validate company exists
        Company company = companyRepository.findById(invoice.getCompany().getId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + invoice.getCompany().getId()));

        // Set default values
        if (invoice.getStatus() == null) {
            invoice.setStatus(Invoice.InvoiceStatus.PENDING);
        }

        if (invoice.getInvoiceDate() == null) {
            invoice.setInvoiceDate(LocalDate.now());
        }

        if (invoice.getDueDate() == null) {
            invoice.setDueDate(LocalDate.now().plusDays(30)); // Default 30 days
        }

        // Calculate amounts
        calculateInvoiceAmounts(invoice);

        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoice(Long id, Invoice invoiceDetails) {
        Invoice invoice = getInvoiceById(id);

        // Update fields
        invoice.setType(invoiceDetails.getType());
        invoice.setDescription(invoiceDetails.getDescription());
        invoice.setNotes(invoiceDetails.getNotes());
        invoice.setTermsAndConditions(invoiceDetails.getTermsAndConditions());
        invoice.setBillingPeriodStart(invoiceDetails.getBillingPeriodStart());
        invoice.setBillingPeriodEnd(invoiceDetails.getBillingPeriodEnd());

        // Recalculate amounts
        calculateInvoiceAmounts(invoice);

        return invoiceRepository.save(invoice);
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + id));
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public void deleteInvoice(Long id) {
        Invoice invoice = getInvoiceById(id);
        
        // Only allow deletion of pending invoices
        if (invoice.getStatus() != Invoice.InvoiceStatus.PENDING) {
            throw new RuntimeException("Cannot delete invoice that is not in pending status");
        }

        // Delete associated invoice items first
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceIdOrderById(id);
        invoiceItemRepository.deleteAll(items);

        invoiceRepository.deleteById(id);
    }

    // ========== INVOICE STATUS MANAGEMENT ==========

    public Invoice sendInvoice(Long id) {
        Invoice invoice = getInvoiceById(id);
        
        if (invoice.getStatus() != Invoice.InvoiceStatus.PENDING) {
            throw new RuntimeException("Only pending invoices can be sent");
        }

        invoice.setStatus(Invoice.InvoiceStatus.SENT);
        invoice.setSentAt(LocalDateTime.now());

        return invoiceRepository.save(invoice);
    }

    public Invoice markAsPaid(Long id, String paymentMethod, String paymentReference, String notes) {
        Invoice invoice = getInvoiceById(id);
        
        if (!invoice.canBePaid()) {
            throw new RuntimeException("Invoice cannot be paid in current status: " + invoice.getStatus());
        }

        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setPaidDate(LocalDate.now());
        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaymentReference(paymentReference);
        invoice.setPaymentNotes(notes);
        invoice.setAmountPaid(invoice.getTotalAmount());

        return invoiceRepository.save(invoice);
    }

    public Invoice markAsOverdue(Long id) {
        Invoice invoice = getInvoiceById(id);
        
        if (invoice.getStatus() != Invoice.InvoiceStatus.SENT) {
            throw new RuntimeException("Only sent invoices can be marked as overdue");
        }

        invoice.setStatus(Invoice.InvoiceStatus.OVERDUE);

        return invoiceRepository.save(invoice);
    }

    public Invoice cancelInvoice(Long id, String reason) {
        Invoice invoice = getInvoiceById(id);
        
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Cannot cancel paid invoice");
        }

        invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);
        invoice.setNotes(invoice.getNotes() + "\nCancelled: " + reason);

        return invoiceRepository.save(invoice);
    }

    public Invoice refundInvoice(Long id, String reason) {
        Invoice invoice = getInvoiceById(id);
        
        if (invoice.getStatus() != Invoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Only paid invoices can be refunded");
        }

        invoice.setStatus(Invoice.InvoiceStatus.REFUNDED);
        invoice.setNotes(invoice.getNotes() + "\nRefunded: " + reason);

        return invoiceRepository.save(invoice);
    }

    // ========== INVOICE CREATION BY TYPE ==========

    public Invoice createSubscriptionInvoice(Long companyId, LocalDate billingPeriodStart, LocalDate billingPeriodEnd) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        Invoice invoice = new Invoice();
        invoice.setCompany(company);
        invoice.setType(Invoice.InvoiceType.SUBSCRIPTION);
        invoice.setDescription("Monthly subscription fee for " + company.getCompanyName());
        invoice.setBillingPeriodStart(billingPeriodStart);
        invoice.setBillingPeriodEnd(billingPeriodEnd);
        invoice.setSubtotal(company.getMonthlyFee());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));

        // Create subscription invoice item
        InvoiceItem item = new InvoiceItem();
        item.setInvoice(invoice);
        item.setItemName("Subscription Fee - " + company.getSubscriptionPlan().getDisplayName());
        item.setDescription("Monthly subscription for " + billingPeriodStart + " to " + billingPeriodEnd);
        item.setQuantity(1);
        item.setUnitPrice(company.getMonthlyFee());
        item.setItemType(InvoiceItem.ItemType.SUBSCRIPTION_FEE);
        item.setReferenceId(companyId);
        item.setReferenceType("COMPANY");

        invoice = createInvoice(invoice);
        invoiceItemRepository.save(item);

        return invoice;
    }

    public Invoice createCommissionInvoice(Long companyId, Long reservationId, BigDecimal commissionAmount) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + reservationId));

        Invoice invoice = new Invoice();
        invoice.setCompany(company);
        invoice.setType(Invoice.InvoiceType.COMMISSION);
        invoice.setReservation(reservation);
        invoice.setDescription("Platform commission for reservation " + reservation.getReservationNumber());
        invoice.setSubtotal(commissionAmount);
        invoice.setPlatformCommission(commissionAmount);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));

        // Create commission invoice item
        InvoiceItem item = new InvoiceItem();
        item.setInvoice(invoice);
        item.setItemName("Platform Commission");
        item.setDescription("Commission for reservation " + reservation.getReservationNumber());
        item.setQuantity(1);
        item.setUnitPrice(commissionAmount);
        item.setItemType(InvoiceItem.ItemType.PLATFORM_COMMISSION);
        item.setReferenceId(reservationId);
        item.setReferenceType("RESERVATION");

        invoice = createInvoice(invoice);
        invoiceItemRepository.save(item);

        return invoice;
    }

    public Invoice createReservationInvoice(Long companyId, Long reservationId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + reservationId));

        Invoice invoice = new Invoice();
        invoice.setCompany(company);
        invoice.setType(Invoice.InvoiceType.RESERVATION);
        invoice.setReservation(reservation);
        invoice.setDescription("Payment for reservation " + reservation.getReservationNumber());
        invoice.setSubtotal(reservation.getTotalAmount());
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(7)); // Shorter due date for reservations

        // Create reservation invoice items
        InvoiceItem rentalItem = new InvoiceItem();
        rentalItem.setInvoice(invoice);
        rentalItem.setItemName("Vehicle Rental");
        rentalItem.setDescription(reservation.getVehicle().getMake() + " " + reservation.getVehicle().getModel() + 
                                 " for " + reservation.getTotalDays() + " days");
        rentalItem.setQuantity(reservation.getTotalDays());
        rentalItem.setUnitPrice(reservation.getDailyRate());
        rentalItem.setItemType(InvoiceItem.ItemType.RENTAL_FEE);
        rentalItem.setReferenceId(reservationId);
        rentalItem.setReferenceType("RESERVATION");

        invoice = createInvoice(invoice);
        invoiceItemRepository.save(rentalItem);

        // Add additional services if any
        if (reservation.getInsuranceIncluded() != null && reservation.getInsuranceIncluded()) {
            InvoiceItem insuranceItem = new InvoiceItem();
            insuranceItem.setInvoice(invoice);
            insuranceItem.setItemName("Insurance");
            insuranceItem.setDescription("Insurance coverage for " + reservation.getTotalDays() + " days");
            insuranceItem.setQuantity(reservation.getTotalDays());
            insuranceItem.setUnitPrice(BigDecimal.valueOf(15)); // $15 per day
            insuranceItem.setItemType(InvoiceItem.ItemType.INSURANCE);
            insuranceItem.setReferenceId(reservationId);
            insuranceItem.setReferenceType("RESERVATION");
            invoiceItemRepository.save(insuranceItem);
        }

        return invoice;
    }

    // ========== INVOICE BY COMPANY ==========

    public List<Invoice> getCompanyInvoices(Long companyId) {
        return invoiceRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public List<Invoice> getCompanyInvoicesByStatus(Long companyId, Invoice.InvoiceStatus status) {
        return invoiceRepository.findByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, status);
    }

    public List<Invoice> getCompanyInvoicesByType(Long companyId, Invoice.InvoiceType type) {
        return invoiceRepository.findByCompanyIdAndTypeOrderByCreatedAtDesc(companyId, type);
    }

    public List<Invoice> getCompanyUnpaidInvoices(Long companyId) {
        return invoiceRepository.findUnpaidInvoicesByCompany(companyId);
    }

    public List<Invoice> getCompanyOverdueInvoices(Long companyId) {
        return invoiceRepository.findOverdueInvoices(LocalDate.now()).stream()
                .filter(invoice -> invoice.getCompany().getId().equals(companyId))
                .toList();
    }

    // ========== INVOICE ANALYTICS ==========

    public Object[] getInvoiceStatistics(Long companyId) {
        return invoiceRepository.getInvoiceStatisticsByCompany(companyId);
    }

    public BigDecimal calculateTotalRevenue(Long companyId) {
        return invoiceRepository.calculateTotalRevenueByCompany(companyId);
    }

    public BigDecimal calculateRevenueByDateRange(Long companyId, LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.calculateRevenueByCompanyAndDateRange(companyId, startDate, endDate);
    }

    public BigDecimal calculateOutstandingAmount(Long companyId) {
        return invoiceRepository.calculateOutstandingAmountByCompany(companyId);
    }

    // ========== INVOICE FILTERING AND SEARCH ==========

    public List<Invoice> getInvoicesByDateRange(Long companyId, LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findInvoicesByCompanyAndDateRange(companyId, startDate, endDate);
    }

    public List<Invoice> getInvoicesByAmountRange(Long companyId, BigDecimal minAmount, BigDecimal maxAmount) {
        return invoiceRepository.findByCompanyAndAmountRange(companyId, minAmount, maxAmount);
    }

    public List<Invoice> getInvoicesDueSoon(int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        return invoiceRepository.findInvoicesDueSoon(LocalDate.now(), futureDate);
    }

    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now());
    }

    public List<Invoice> getSubscriptionInvoices(Long companyId) {
        return invoiceRepository.findSubscriptionInvoicesByCompany(companyId);
    }

    public List<Invoice> getCommissionInvoices(Long companyId) {
        return invoiceRepository.findCommissionInvoicesByCompany(companyId);
    }

    // ========== INVOICE ITEMS ==========

    public List<InvoiceItem> getInvoiceItems(Long invoiceId) {
        return invoiceItemRepository.findByInvoiceIdOrderById(invoiceId);
    }

    public InvoiceItem addInvoiceItem(Long invoiceId, InvoiceItem item) {
        Invoice invoice = getInvoiceById(invoiceId);
        
        if (invoice.getStatus() != Invoice.InvoiceStatus.PENDING) {
            throw new RuntimeException("Cannot add items to invoice that is not pending");
        }

        item.setInvoice(invoice);
        InvoiceItem savedItem = invoiceItemRepository.save(item);

        // Recalculate invoice amounts
        calculateInvoiceAmounts(invoice);
        invoiceRepository.save(invoice);

        return savedItem;
    }

    public void removeInvoiceItem(Long itemId) {
        InvoiceItem item = invoiceItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Invoice item not found with ID: " + itemId));

        Invoice invoice = item.getInvoice();
        
        if (invoice.getStatus() != Invoice.InvoiceStatus.PENDING) {
            throw new RuntimeException("Cannot remove items from invoice that is not pending");
        }

        invoiceItemRepository.deleteById(itemId);

        // Recalculate invoice amounts
        calculateInvoiceAmounts(invoice);
        invoiceRepository.save(invoice);
    }

    // ========== INVOICE VALIDATION ==========

    public boolean isInvoicePaid(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        return invoice.isPaid();
    }

    public boolean isInvoiceOverdue(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        return invoice.isOverdue();
    }

    public boolean canBePaid(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        return invoice.canBePaid();
    }

    public BigDecimal getRemainingBalance(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        return invoice.getRemainingBalance();
    }

    // ========== HELPER METHODS ==========

    private void calculateInvoiceAmounts(Invoice invoice) {
        // Calculate subtotal from invoice items
        BigDecimal subtotal = invoiceItemRepository.calculateTotalByInvoice(invoice.getId());
        if (subtotal == null) {
            subtotal = BigDecimal.ZERO;
        }
        invoice.setSubtotal(subtotal);

        // Calculate tax (example: 8.5%)
        BigDecimal taxRate = BigDecimal.valueOf(0.085);
        BigDecimal taxAmount = subtotal.multiply(taxRate);
        invoice.setTaxAmount(taxAmount);

        // Calculate total amount
        BigDecimal totalAmount = subtotal.add(taxAmount)
                .subtract(invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO);
        invoice.setTotalAmount(totalAmount);

        // Calculate balance due
        BigDecimal balanceDue = totalAmount.subtract(invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO);
        invoice.setBalanceDue(balanceDue);

        // Calculate net amount (after platform commission and fees)
        BigDecimal netAmount = totalAmount
                .subtract(invoice.getPlatformCommission() != null ? invoice.getPlatformCommission() : BigDecimal.ZERO)
                .subtract(invoice.getProcessingFee() != null ? invoice.getProcessingFee() : BigDecimal.ZERO);
        invoice.setNetAmount(netAmount);
    }

    public long countInvoicesByStatus(Long companyId, Invoice.InvoiceStatus status) {
        return invoiceRepository.countByCompanyIdAndStatus(companyId, status);
    }

    public long countInvoicesByType(Long companyId, Invoice.InvoiceType type) {
        return invoiceRepository.countByCompanyIdAndType(companyId, type);
    }

    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }
}
