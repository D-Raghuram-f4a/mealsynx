package com.mealsynx.dao;

import com.mealsynx.db.PersistentDataStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    private final PersistentDataStore store = PersistentDataStore.getInstance();

    public void recordTransaction(String txnId, String username, String orderId, double amount, String method) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM hh:mm a"));
        store.data().paymentTransactions.add(0, String.format("[%s] %s | User: %-10s | Ref: %-10s | ₹%7.2f | %s",
                ts, txnId, username, orderId, amount, method));
        store.saveToDisk();
    }

    public List<String> getAllTransactions() {
        return new ArrayList<>(store.data().paymentTransactions);
    }
}
