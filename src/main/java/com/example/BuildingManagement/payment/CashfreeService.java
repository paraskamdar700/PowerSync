package com.example.BuildingManagement.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CashfreeService {

    @Value("${cashfree.app.id}")
    private String appId;

    @Value("${cashfree.secret.key}")
    private String secretKey;

    @Value("${cashfree.env.url:https://sandbox.cashfree.com/pg/orders}")
    private String cashfreeBaseUrl;

    public CashfreeOrderResponse createOrder(String customerName, String customerEmail, String customerPhone, BigDecimal amount, String billDescription) {
        RestTemplate restTemplate = new RestTemplate();

        String orderId = "order_" + UUID.randomUUID().toString().substring(0, 8);

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", appId);
        headers.set("x-client-secret", secretKey);
        headers.set("x-api-version", "2023-08-01");
        headers.set("Content-Type", "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("order_id", orderId);
        body.put("order_amount", amount.doubleValue());
        body.put("order_currency", "INR");
        body.put("order_note", billDescription);

        Map<String, String> customerDetails = new HashMap<>();
        customerDetails.put("customer_id", "cust_" + customerEmail.hashCode());
        customerDetails.put("customer_name", customerName);
        customerDetails.put("customer_email", customerEmail);
        customerDetails.put("customer_phone", customerPhone != null && !customerPhone.isEmpty() ? customerPhone : "9999999999");
        body.put("customer_details", customerDetails);

        Map<String, String> orderMeta = new HashMap<>();
        orderMeta.put("return_url", "http://localhost:3000/payment-success?order_id={order_id}");
        // We'll also configure webhook separately in Cashfree dashboard or pass webhook_url here
        // orderMeta.put("notify_url", "http://YOUR_SERVER_IP/api/v1/payment/webhook");
        body.put("order_meta", orderMeta);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<CashfreeOrderResponse> response = restTemplate.exchange(
                    cashfreeBaseUrl, HttpMethod.POST, request, CashfreeOrderResponse.class);

            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error creating Cashfree order: " + e.getMessage());
            return null; // Return null if payment gateway integration fails, so bill generation can still proceed without a link
        }
    }
}
