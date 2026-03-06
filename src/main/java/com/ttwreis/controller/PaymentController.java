package com.ttwreis.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;
import com.ttwreis.entity.User;
import com.ttwreis.repository.UserRepository;
import com.ttwreis.service.PaymentService;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    // ✅ Amount is FIXED here in backend — cannot be altered from frontend
    private static final String FIXED_AMOUNT = "200.00";
    private static final String CURRENCY = "USD";
//    private static final String CURRENCY = "INR";

    @Autowired
    private PayPalHttpClient payPalHttpClient;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private UserRepository userRepository;

    // Step 1: Create Order
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createOrder() throws IOException {
    	
    	System.out.println("---------------------------------------------------------------");
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        AmountWithBreakdown amount = new AmountWithBreakdown()
            .currencyCode(CURRENCY)
            .value(FIXED_AMOUNT); // ← hardcoded ₹200

        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
            .amountWithBreakdown(amount);

        orderRequest.purchaseUnits(List.of(purchaseUnit));

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody(orderRequest);

        HttpResponse<Order> response = payPalHttpClient.execute(request);
        String orderId = response.result().id();
        
        System.out.println("------------------------------------------------------------::::: " );
        
        System.out.println("response::::: " +response);
        System.out.println("orderId::::: " +orderId);
        
        System.out.println("------------------------------------------------------------::::: " );
     

        return ResponseEntity.ok(Map.of("orderId", orderId));
    }

    
    @PostMapping("/capture/{orderId}")
    public ResponseEntity<Map<String, Object>> captureOrder(
            @PathVariable String orderId) throws IOException {

        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        request.prefer("return=representation");
        request.requestBody(new OrderRequest());

        HttpResponse<Order> response = payPalHttpClient.execute(request);
        Order order = response.result();

        Map<String, Object> result = new HashMap<>();
        result.put("status", order.status());
        result.put("orderId", order.id());

        // Verify captured amount matches ₹200 — extra safety check
        String capturedAmount = order.purchaseUnits().get(0)
            .payments().captures().get(0)
            .amount().value();

        if (!FIXED_AMOUNT.equals(capturedAmount)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Amount mismatch detected"));
        }

        result.put("amount", capturedAmount);
        result.put("currency", CURRENCY);
        
        
        System.out.println("------------------------------------------------------------::::: " );
        
        System.out.println("response::::: " +response);
        System.out.println("orderId::::: " +orderId);
        System.out.println("result:::::"+result);
        
        System.out.println("------------------------------------------------------------::::: " );
        
        
        /****
         * 
         * Update Payment Details in the payment table
         * 
         * 
         */
        
        User user=currentUser();
        
        paymentService.savePayment(user,order, orderId, capturedAmount);
        
        
        
        return ResponseEntity.ok(result);
    }
    
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
     try {
         System.out.println("Test hit - clientId injected: " + payPalHttpClient.toString());
         return ResponseEntity.ok("PayPal client is alive");
     } catch (Exception e) {
         return ResponseEntity.status(500).body("Error: " + e.getMessage());
     }
    }
    
    
    private User currentUser() {
        String regNo = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByRegistrationNumber(regNo)
                .orElseThrow(() -> new RuntimeException("User not found: " + regNo));
    }
}