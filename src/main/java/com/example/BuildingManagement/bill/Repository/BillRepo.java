package com.example.BuildingManagement.bill.Repository;

import com.example.BuildingManagement.bill.Model.Bill;
import com.example.BuildingManagement.common.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepo extends JpaRepository<Bill, Long> {

    Optional<Bill> findByPaymentOrderId(String paymentOrderId);

    List<Bill> findByTenantIdOrderByBillingPeriodEndDesc(Long tenantId);

    List<Bill> findByRoomIdOrderByBillingPeriodEndDesc(Long roomId);

    List<Bill> findByPaymentStatus(PaymentStatus status);

    // Check if a bill already exists for this tenant + period (prevent duplicates)
    Optional<Bill> findByTenantIdAndBillingPeriodStartAndBillingPeriodEnd(
            Long tenantId, LocalDate start, LocalDate end);
}
