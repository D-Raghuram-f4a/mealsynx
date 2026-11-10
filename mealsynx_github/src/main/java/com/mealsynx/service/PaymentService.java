package com.mealsynx.service;

import com.mealsynx.dao.CustomerDAO;
import com.mealsynx.dao.PaymentDAO;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.exception.InsufficientWalletBalanceException;
import com.mealsynx.interfaces.PaymentProcessor;
import com.mealsynx.model.Customer;
import com.mealsynx.model.Order;

import java.util.UUID;

public class PaymentService implements PaymentProcessor {
    private final CustomerDAO customerDAO;
    private final PaymentDAO paymentDAO;

    public PaymentService(CustomerDAO customerDAO, PaymentDAO paymentDAO) {
        this.customerDAO = customerDAO;
        this.paymentDAO = paymentDAO;
    }

    @Override
    public boolean processPayment(Customer customer, Order order) {
        double amount = order.getTotalAmount();
        String method = order.getPaymentMethod();
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        if ("FOOD_WALLET".equalsIgnoreCase(method) && customer != null) {
            if (customer.getWalletBalance() < amount) {
                throw new InsufficientWalletBalanceException(customer.getWalletBalance(), amount);
            }
            customer.setWalletBalance(customer.getWalletBalance() - amount);
            customerDAO.save(customer);
            paymentDAO.recordTransaction(txnId, customer.getUsername(), order.getOrderId(), amount, "FOOD_WALLET");
            PersistentDataStore.getInstance().saveToDisk();
            return true;
        } else {
            paymentDAO.recordTransaction(txnId, customer != null ? customer.getUsername() : "WALKIN-COUNTER",
                    order.getOrderId(), amount, method);
            PersistentDataStore.getInstance().saveToDisk();
            return true;
        }
    }

    @Override
    public String getPaymentMethodName() {
        return "MealSynx Food Wallet & Pay-on-Delivery Engine";
    }
}
