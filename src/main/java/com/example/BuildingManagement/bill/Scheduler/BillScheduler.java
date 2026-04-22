package com.example.BuildingManagement.bill.Scheduler;

import com.example.BuildingManagement.bill.Service.BillService;
import com.example.BuildingManagement.device.DeviceControlService;
import com.example.BuildingManagement.device.IotDevice;
import com.example.BuildingManagement.device.IotDeviceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Scheduler that auto-generates bills on the 1st of every month at midnight.
 * Generates bills for the PREVIOUS month.
 */
@Component
@RequiredArgsConstructor
public class BillScheduler {

    private final BillService billService;
    private final DeviceControlService deviceControlService;
    private final IotDeviceRepo iotDeviceRepo;

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

    /**
     * Runs daily at 10 AM to check for unpaid bills that are past their due date.
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void enforcePowerCutoff() {
        System.out.println("[BillScheduler] Checking for unpaid bills past due date...");
        
        List<com.example.BuildingManagement.bill.Model.Bill> unpaidBills = billService.getUnpaidBills();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        for (com.example.BuildingManagement.bill.Model.Bill bill : unpaidBills) {
            if (bill.getDueDate() != null && now.isAfter(bill.getDueDate())) {
                System.out.println("⚠️ Bill " + bill.getId() + " is overdue! Cutting off power for room: " + bill.getRoom().getRoomNumber());
                
                // Get IoT Device for this room
                Optional<IotDevice> deviceOpt = iotDeviceRepo.findByRoomId(bill.getRoom().getId());
                if (deviceOpt.isPresent()) {
                    deviceControlService.turnOff(deviceOpt.get().getId());
                }
            }
        }
    }
}
