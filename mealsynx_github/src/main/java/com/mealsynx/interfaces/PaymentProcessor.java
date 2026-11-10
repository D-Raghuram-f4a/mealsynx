package com.mealsynx.interfaces;

import com.mealsynx.model.Customer;
import com.mealsynx.model.Order;

public interface PaymentProcessor {
    boolean processPayment(Customer customer, Order order);
    String getPaymentMethodName();
}
