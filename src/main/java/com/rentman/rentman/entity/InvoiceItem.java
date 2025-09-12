package com.rentman.rentman.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "invoice_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @NotNull(message = "Invoice is required")
    private Invoice invoice;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "tax_rate", precision = 5, scale = 4)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_rate", precision = 5, scale = 4)
    private BigDecimal discountRate = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type")
    private ItemType itemType;

    // Reference to related entities
    @Column(name = "reference_id")
    private Long referenceId; // Could be reservation ID, vehicle ID, etc.

    @Column(name = "reference_type")
    private String referenceType; // "RESERVATION", "VEHICLE", "SUBSCRIPTION", etc.

    @PrePersist
    @PreUpdate
    protected void calculateAmounts() {
        if (quantity != null && unitPrice != null) {
            // Calculate total price
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

            // Apply discount if any
            if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = totalPrice.multiply(discountRate);
                totalPrice = totalPrice.subtract(discountAmount);
            }

            // Calculate tax if any
            if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
                taxAmount = totalPrice.multiply(taxRate);
            }
        }
    }

    // Enums
    public enum ItemType {
        RENTAL_FEE("Rental Fee"),
        INSURANCE("Insurance"),
        GPS("GPS Service"),
        CHILD_SEAT("Child Seat"),
        ADDITIONAL_DRIVER("Additional Driver"),
        PLATFORM_COMMISSION("Platform Commission"),
        SUBSCRIPTION_FEE("Subscription Fee"),
        PROCESSING_FEE("Processing Fee"),
        PENALTY("Penalty"),
        REFUND("Refund"),
        OTHER("Other");

        private final String displayName;

        ItemType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
