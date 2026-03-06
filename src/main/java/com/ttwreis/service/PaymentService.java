package com.ttwreis.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paypal.orders.Capture;
import com.paypal.orders.Order;
import com.ttwreis.entity.Payment;
import com.ttwreis.entity.Payment.PaymentStatus;
import com.ttwreis.entity.User;
import com.ttwreis.repository.PaymentRepository;

@Service
public class PaymentService {

	
	private PaymentRepository paymentRepository;

	public PaymentService(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}
	
    @Transactional
    public void savePayment(User user,Order order, String transactionId, String amount) {
    	
    	Long applicationId = user.getApplication().getId();

        Payment payment = paymentRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));
        
        Capture capture = order.purchaseUnits()
                .get(0)
                .payments()
                .captures()
                .get(0);

        payment.setTransactionId(transactionId);
        payment.setAmount(Double.parseDouble(amount));
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        
        payment.setGateway("PAYPAL");
        
        // PayPal capture ID (bank reference)
        payment.setBankRef(capture.id());
        
        // store raw gateway response
        payment.setGatewayResponse(order.toString());

        paymentRepository.save(payment);
    }
	
}
