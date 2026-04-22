package com.example.BuildingManagement.bill.Service;

import com.example.BuildingManagement.bill.Model.Bill;
import com.example.BuildingManagement.bill.Repository.BillRepo;
import com.example.BuildingManagement.common.enums.PaymentStatus;
import com.example.BuildingManagement.device.IotDevice;
import com.example.BuildingManagement.device.IotDeviceRepo;
import com.example.BuildingManagement.power.PowerMetric;
import com.example.BuildingManagement.power.Repository.PowerMetricRepo;
import com.example.BuildingManagement.room.Model.Room;
import com.example.BuildingManagement.room.Repository.RoomRepo;
import com.example.BuildingManagement.user.Model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepo billRepo;
    private final RoomRepo roomRepo;
    private final IotDeviceRepo iotDeviceRepo;
    private final PowerMetricRepo powerMetricRepo;

    /**
     * Generate monthly bills for all rooms with active tenants and IoT devices.
     * Called by the scheduler on the 1st of each month (for the previous month),
     * or manually via the API.
     *
     * @param billingMonth The month to generate bills for (e.g., 2026-04 means April 2026)
     * @return List of generated bills
     */
    @Transactional
    public List<Bill> generateMonthlyBills(LocalDate billingMonth) {
        List<Bill> generatedBills = new ArrayList<>();

        // Determine billing period
        LocalDate periodStart = billingMonth.withDayOfMonth(1);
        LocalDate periodEnd = billingMonth.withDayOfMonth(billingMonth.lengthOfMonth());

        System.out.println("=== Generating bills for period: " + periodStart + " to " + periodEnd + " ===");

        // Get all rooms (we'll filter for those with tenants + devices)
        List<Room> allRooms = roomRepo.findAll();

        for (Room room : allRooms) {
            User tenant = room.getCurrentTenant();
            if (tenant == null) {
                continue; // Skip vacant rooms
            }

            Optional<IotDevice> deviceOpt = iotDeviceRepo.findByRoomId(room.getId());
            if (deviceOpt.isEmpty()) {
                continue; // Skip rooms without IoT devices
            }

            IotDevice device = deviceOpt.get();

            // Check if bill already exists for this period
            Optional<Bill> existing = billRepo.findByTenantIdAndBillingPeriodStartAndBillingPeriodEnd(
                    tenant.getId(), periodStart, periodEnd);
            if (existing.isPresent()) {
                System.out.println("  Bill already exists for Room " + room.getRoomNumber() +
                        ", Tenant: " + tenant.getFullname() + " — skipping");
                continue;
            }

            // Determine effective billing start (handle partial months)
            // If tenant's room assignment (createdAt) is after periodStart, use that
            LocalDate effectiveStart = periodStart;
            if (room.getCreatedAt() != null && room.getCreatedAt().toLocalDate().isAfter(periodStart)) {
                effectiveStart = room.getCreatedAt().toLocalDate();
            }

            // Get energy readings for the billing period
            LocalDateTime readingStart = effectiveStart.atStartOfDay();
            LocalDateTime readingEnd = periodEnd.atTime(LocalTime.MAX);

            // Find the first reading in/after the billing period start
            Optional<PowerMetric> firstReading = powerMetricRepo
                    .findFirstByIotDeviceIdAndRecordedAtAfterOrderByRecordedAtAsc(
                            device.getId(), readingStart.minusSeconds(1));

            // Find the last reading before/at the billing period end
            Optional<PowerMetric> lastReading = powerMetricRepo
                    .findFirstByIotDeviceIdAndRecordedAtBeforeOrderByRecordedAtDesc(
                            device.getId(), readingEnd.plusSeconds(1));

            if (firstReading.isEmpty() || lastReading.isEmpty()) {
                System.out.println("  No power readings found for Room " + room.getRoomNumber() +
                        " in billing period — skipping");
                continue;
            }

            // Calculate units consumed (kWh) = last reading - first reading
            BigDecimal startEnergy = firstReading.get().getUnitsConsumedTotal();
            BigDecimal endEnergy = lastReading.get().getUnitsConsumedTotal();
            BigDecimal unitsConsumed = endEnergy.subtract(startEnergy).abs();

            // Get unit rate from device
            BigDecimal unitRate = device.getUnitRatePerKwh() != null
                    ? device.getUnitRatePerKwh()
                    : BigDecimal.valueOf(8.0); // Default ₹8/kWh if not set

            // Calculate total amount
            BigDecimal totalAmount = unitsConsumed.multiply(unitRate).setScale(2, RoundingMode.HALF_UP);

            // Create and save the bill
            Bill bill = new Bill();
            bill.setTenant(tenant);
            bill.setRoom(room);
            bill.setBillingPeriodStart(effectiveStart);
            bill.setBillingPeriodEnd(periodEnd);
            bill.setUnitsConsumed(unitsConsumed);
            bill.setUnitRate(unitRate);
            bill.setTotalAmount(totalAmount);
            bill.setPaymentStatus(PaymentStatus.UNPAID);

            billRepo.save(bill);
            generatedBills.add(bill);

            System.out.println("  ✓ Bill generated for Room " + room.getRoomNumber() +
                    " | Tenant: " + tenant.getFullname() +
                    " | Units: " + unitsConsumed + " kWh" +
                    " | Rate: ₹" + unitRate + "/kWh" +
                    " | Total: ₹" + totalAmount);
        }

        System.out.println("=== Bill generation complete. Total bills: " + generatedBills.size() + " ===");
        return generatedBills;
    }

    /**
     * Get all bills for a tenant.
     */
    public List<Bill> getBillsForTenant(Long tenantId) {
        return billRepo.findByTenantIdOrderByBillingPeriodEndDesc(tenantId);
    }

    /**
     * Get all bills for a room.
     */
    public List<Bill> getBillsForRoom(Long roomId) {
        return billRepo.findByRoomIdOrderByBillingPeriodEndDesc(roomId);
    }

    /**
     * Get a single bill by ID.
     */
    public Bill getBillById(Long billId) {
        return billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + billId));
    }

    /**
     * Get all unpaid bills.
     */
    public List<Bill> getUnpaidBills() {
        return billRepo.findByPaymentStatus(PaymentStatus.UNPAID);
    }
}
