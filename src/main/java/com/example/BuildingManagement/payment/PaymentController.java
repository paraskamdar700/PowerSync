package com.example.BuildingManagement.payment;

import com.example.BuildingManagement.bill.Model.Bill;
import com.example.BuildingManagement.bill.Repository.BillRepo;
import com.example.BuildingManagement.common.enums.PaymentStatus;
import com.example.BuildingManagement.device.DeviceControlService;
import com.example.BuildingManagement.device.IotDevice;
import com.example.BuildingManagement.device.IotDeviceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final BillRepo billRepo;
    private final DeviceControlService deviceControlService;
    private final IotDeviceRepo iotDeviceRepo;

    /**
     * Webhook endpoint to receive payment updates from Cashfree.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleCashfreeWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // Log the payload for debugging
            System.out.println("Received Cashfree Webhook: " + payload);

            // Extract data from standard Cashfree webhook format
            if (payload.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                if (data != null && data.containsKey("order") && data.containsKey("payment")) {
                    
                    Map<String, Object> orderMap = (Map<String, Object>) data.get("order");
                    Map<String, Object> paymentMap = (Map<String, Object>) data.get("payment");

                    String orderId = (String) orderMap.get("order_id");
                    String paymentStatus = (String) paymentMap.get("payment_status");

                    if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
                        handleSuccessfulPayment(orderId);
                    }
                }
            } else if (payload.containsKey("order_id") && payload.containsKey("payment_status")) {
                // Support a simplified payload for manual testing
                String orderId = (String) payload.get("order_id");
                String paymentStatus = (String) payload.get("payment_status");
                
                if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
                    handleSuccessfulPayment(orderId);
                }
            }

            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error processing webhook");
        }
    }

    private void handleSuccessfulPayment(String orderId) {
        if (orderId == null) return;
        
        Optional<Bill> billOpt = billRepo.findByPaymentOrderId(orderId);
        if (billOpt.isPresent()) {
            Bill bill = billOpt.get();
            if (bill.getPaymentStatus() != PaymentStatus.PAID) {
                // Mark Bill as Paid
                bill.setPaymentStatus(PaymentStatus.PAID);
                billRepo.save(bill);
                System.out.println("✅ Bill " + bill.getId() + " marked as PAID.");

                // If device is linked, turn it back ON
                Optional<IotDevice> deviceOpt = iotDeviceRepo.findByRoomId(bill.getRoom().getId());
                if (deviceOpt.isPresent()) {
                    Long deviceId = deviceOpt.get().getId();
                    System.out.println("⚡ Attempting to restore power for Device ID: " + deviceId);
                    deviceControlService.turnOn(deviceId);
                }
            }
        } else {
            System.err.println("Warning: Received successful payment for unknown order_id: " + orderId);
        }
    }
}
