package com.example.BuildingManagement.bill.Model;

import com.example.BuildingManagement.common.enums.PaymentStatus;
import com.example.BuildingManagement.room.Model.Room;
import com.example.BuildingManagement.user.Model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Getter
@Setter
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User tenant;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;

    @Column(name = "units_consumed")
    private BigDecimal unitsConsumed;

    @Column(name = "unit_rate")
    private BigDecimal unitRate;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('UNPAID','PAID','OVERDUE') DEFAULT 'UNPAID'")
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "payment_order_id")
    private String paymentOrderId;

    @Column(name = "payment_link", length = 1000)
    private String paymentLink;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
