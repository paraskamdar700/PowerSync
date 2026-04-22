package com.example.BuildingManagement.bill.Scheduler;

import com.example.BuildingManagement.bill.Service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Scheduler that auto-generates bills on the 1st of every month at midnight.
 * Generates bills for the PREVIOUS month.
 */
@Component
@RequiredArgsConstructor
public class BillScheduler {

    private final BillService billService;

    /**
     * Runs at 00:00:00 on the 1st day of every month.
     * Generates bills for the previous month.
     */
    @Scheduled(cron = "0 0 0 1 * *")
    public void generateMonthlyBills() {
        LocalDate previousMonth = LocalDate.now().minusMonths(1);
        System.out.println("[BillScheduler] Auto-generating bills for: " +
                previousMonth.getMonth() + " " + previousMonth.getYear());

        billService.generateMonthlyBills(previousMonth);
    }
}
