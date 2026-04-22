package com.example.BuildingManagement.bill.Controller;

import com.example.BuildingManagement.bill.Model.Bill;
import com.example.BuildingManagement.bill.Service.BillService;
import com.example.BuildingManagement.user.Security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    /**
     * Tenant fetches their own bills (authenticated).
     */
    @GetMapping("/my-bills")
    public ResponseEntity<List<Bill>> getMyBills(@AuthenticationPrincipal UserPrincipal principal) {
        List<Bill> bills = billService.getBillsForTenant(principal.getUser().getId());
        return ResponseEntity.ok(bills);
    }

    /**
     * Landlord fetches bills for a specific room.
     */
    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<List<Bill>> getBillsByRoom(@PathVariable Long roomId) {
        List<Bill> bills = billService.getBillsForRoom(roomId);
        return ResponseEntity.ok(bills);
    }

    /**
     * Get a single bill by ID.
     */
    @GetMapping("/{billId}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long billId) {
        Bill bill = billService.getBillById(billId);
        return ResponseEntity.ok(bill);
    }

    /**
     * Get all unpaid bills (Landlord only).
     */
    @GetMapping("/unpaid")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<List<Bill>> getUnpaidBills() {
        return ResponseEntity.ok(billService.getUnpaidBills());
    }

    /**
     * Manually trigger bill generation for a specific month.
     * Use for testing or if the scheduler missed.
     *
     * @param month The month to generate bills for (format: yyyy-MM-dd, day is ignored)
     *              Defaults to previous month if not provided.
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<Map<String, Object>> generateBills(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {

        if (month == null) {
            month = LocalDate.now().minusMonths(1);
        }

        List<Bill> generated = billService.generateMonthlyBills(month);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Bill generation completed",
                "billsGenerated", generated.size(),
                "billingMonth", month.getMonth() + " " + month.getYear(),
                "bills", generated
        ));
    }

    /**
     * Update the unit rate of a specific bill.
     * This will recalculate the bill total and optionally generate a new payment link.
     */
    @PutMapping("/{billId}/unit-rate")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<Map<String, Object>> updateBillUnitRate(
            @PathVariable Long billId,
            @RequestBody Map<String, java.math.BigDecimal> payload) {
        
        java.math.BigDecimal unitRate = payload.get("unitRate");
        if (unitRate == null || unitRate.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid unitRate is required"));
        }

        try {
            Bill updatedBill = billService.updateUnitRate(billId, unitRate);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Unit rate updated successfully",
                    "bill", updatedBill
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
