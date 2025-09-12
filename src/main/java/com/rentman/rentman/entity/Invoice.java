package com.rentman.rentman.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @NotNull(message = "Company is required")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    // Invoice dates
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    // Amounts
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", precision = 15, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "balance_due", precision = 15, scale = 2)
    private BigDecimal balanceDue;

    // Payment information
    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "payment_notes", columnDefinition = "TEXT")
    private String paymentNotes;

    // Invoice details
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    // Commission and fees
    @Column(name = "platform_commission", precision = 15, scale = 2)
    private BigDecimal platformCommission = BigDecimal.ZERO;

    @Column(name = "processing_fee", precision = 15, scale = 2)
    private BigDecimal processingFee = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 15, scale = 2)
    private BigDecimal netAmount;

    // Billing period (for subscription invoices)
    @Column(name = "billing_period_start")
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end")
    private LocalDate billingPeriodEnd;

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    // Relationships
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceItem> invoiceItems;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (invoiceNumber == null) {
            invoiceNumber = generateInvoiceNumber();
        }
        calculateAmounts();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateAmounts();
    }

    // Business logic methods
    private String generateInvoiceNumber() {
        return "INV" + System.currentTimeMillis();
    }

    private void calculateAmounts() {
        if (subtotal != null) {
            // Calculate total amount
            totalAmount = subtotal
                    .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                    .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);

            // Calculate balance due
            balanceDue = totalAmount.subtract(amountPaid != null ? amountPaid : BigDecimal.ZERO);

            // Calculate net amount (after platform commission and fees)
            netAmount = totalAmount
                    .subtract(platformCommission != null ? platformCommission : BigDecimal.ZERO)
                    .subtract(processingFee != null ? processingFee : BigDecimal.ZERO);
        }
    }

    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    public boolean isOverdue() {
        return status == InvoiceStatus.OVERDUE || 
               (status == InvoiceStatus.PENDING && dueDate.isBefore(LocalDate.now()));
    }

    public boolean canBePaid() {
        return status == InvoiceStatus.PENDING || status == InvoiceStatus.OVERDUE;
    }

    public BigDecimal getRemainingBalance() {
        return totalAmount.subtract(amountPaid != null ? amountPaid : BigDecimal.ZERO);
    }

    // Enums
    public enum InvoiceType {
        SUBSCRIPTION("Subscription Fee"),
        COMMISSION("Platform Commission"),
        RESERVATION("Reservation Payment"),
        PENALTY("Penalty Fee"),
        REFUND("Refund"),
        ADJUSTMENT("Adjustment");

        private final String displayName;

        InvoiceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum InvoiceStatus {
        PENDING("Pending"),
        SENT("Sent"),
        PAID("Paid"),
        OVERDUE("Overdue"),
        CANCELLED("Cancelled"),
        REFUNDED("Refunded");

        private final String displayName;

        InvoiceStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
