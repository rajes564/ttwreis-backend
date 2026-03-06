package com.ttwreis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;

@Configuration
public class PayPalConfig {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Bean
    public PayPalEnvironment payPalEnvironment() {
    	
    	    System.out.println("=== PayPal Debug ===");
    	    System.out.println("Mode: " + mode);
    	    System.out.println("Client ID: " + clientId);
    	    System.out.println("Secret (first 6 chars): " + (clientSecret != null && clientSecret.length() > 6 ? clientSecret.substring(0, 6) : clientSecret));
    	    System.out.println("===================");
    	    
    	    
        if ("live".equals(mode)) {
            return new PayPalEnvironment.Live(clientId, clientSecret);
        }
        return new PayPalEnvironment.Sandbox(clientId, clientSecret);
    }

    @Bean
    public PayPalHttpClient payPalHttpClient(PayPalEnvironment env) {
        return new PayPalHttpClient(env);
    }
}