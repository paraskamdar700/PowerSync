package com.example.BuildingManagement.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CashfreeOrderResponse {

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("payment_session_id")
    private String paymentSessionId;
    
    // Depending on API version, Cashfree may return a direct payment link as well
}
