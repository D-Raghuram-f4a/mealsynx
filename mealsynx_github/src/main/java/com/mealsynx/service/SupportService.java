package com.mealsynx.service;

import com.mealsynx.dao.EmployeeDAO;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.Admin;
import com.mealsynx.model.SupportTicket;

import java.util.ArrayList;
import java.util.List;

public class SupportService {
    public static final String SUPPORT_HELPLINE = "+91-44-2745-9900 (24x7 Campus Dining Desk)";
    public static final String SUPPORT_EMAIL = "support@mealsynx.edu";
    public static final String SUPPORT_LOCATION = "Block A — Ground Floor Student Welfare & Smart Dining Helpdesk (Ext: 404)";

    private final EmployeeDAO employeeDAO;
    private final PersistentDataStore store = PersistentDataStore.getInstance();

    public SupportService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public SupportTicket submitComplaint(String studentUsername, String category, String description) {
        String id = "TKT-" + (501 + store.data().supportTickets.size());
        SupportTicket t = new SupportTicket(id, studentUsername, category, description);
        store.data().supportTickets.add(0, t);
        for (Admin a : employeeDAO.findAllAdmins()) {
            a.pushNotification("🔔 [New Support Complaint " + id + " from " + studentUsername + "] " + category + ": " + description);
        }
        store.saveToDisk();
        return t;
    }

    public List<SupportTicket> getAllTickets() {
        return new ArrayList<>(store.data().supportTickets);
    }
}
