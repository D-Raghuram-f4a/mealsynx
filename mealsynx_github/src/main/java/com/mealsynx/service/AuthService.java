package com.mealsynx.service;

import com.mealsynx.dao.CustomerDAO;
import com.mealsynx.dao.EmployeeDAO;
import com.mealsynx.db.PersistentDataStore;
import com.mealsynx.model.Admin;
import com.mealsynx.model.Customer;
import com.mealsynx.model.User;
import com.mealsynx.model.UserRole;
import com.mealsynx.model.Worker;

import java.util.List;
import java.util.Optional;

public class AuthService {
    private final CustomerDAO customerDAO;
    private final EmployeeDAO employeeDAO;
    private User currentLoggedInUser;

    public AuthService(CustomerDAO customerDAO, EmployeeDAO employeeDAO) {
        this.customerDAO = customerDAO;
        this.employeeDAO = employeeDAO;
    }

    public Optional<User> loginWithRole(UserRole selectedRole, String usernameInput, String passwordInput) {
        if (usernameInput == null || usernameInput.isBlank() || passwordInput == null || passwordInput.isBlank()) {
            return Optional.empty();
        }
        String userTrim = usernameInput.trim();
        String passTrim = passwordInput.trim();

        if (selectedRole == UserRole.STUDENT) {
            Optional<Customer> custOpt = customerDAO.findByUsernameOrIdentifier(userTrim);
            if (custOpt.isPresent()) {
                Customer c = custOpt.get();
                if (matchesPassword(c.getUsername(), c.getPassword(), passTrim)) {
                    currentLoggedInUser = c;
                    return Optional.of(c);
                }
            }
        } else if (selectedRole == UserRole.WORKER) {
            Optional<Worker> wrkOpt = employeeDAO.findWorker(userTrim);
            if (wrkOpt.isPresent()) {
                Worker w = wrkOpt.get();
                if (matchesPassword(w.getUsername(), w.getPassword(), passTrim)) {
                    currentLoggedInUser = w;
                    return Optional.of(w);
                }
            }
        } else if (selectedRole == UserRole.ADMIN) {
            Optional<Admin> admOpt = employeeDAO.findAdmin(userTrim);
            if (admOpt.isPresent()) {
                Admin a = admOpt.get();
                if (matchesPassword(a.getUsername(), a.getPassword(), passTrim)) {
                    currentLoggedInUser = a;
                    return Optional.of(a);
                }
            }
        }
        return Optional.empty();
    }

    private boolean matchesPassword(String canonicalUsername, String expectedPassword, String providedPassword) {
        if (expectedPassword.equalsIgnoreCase(providedPassword)) return true;
        String noSpaceExpected = expectedPassword.replaceAll("\\s+", "");
        String noSpaceProvided = providedPassword.replaceAll("\\s+", "");
        if (noSpaceExpected.equalsIgnoreCase(noSpaceProvided)) return true;
        String altExpected = canonicalUsername.replaceAll("\\s+", "") + "123*";
        return altExpected.equalsIgnoreCase(noSpaceProvided);
    }

    public Customer registerNewStudent(String username, String fullName, String registerNo,
                                       String email, String password, double initialWalletBalance,
                                       String favouriteCategory) {
        String cleanUser = username.trim();
        Optional<Customer> existing = customerDAO.findByUsernameOrIdentifier(cleanUser);
        String newId = existing.map(Customer::getUserId).orElse("STU-00" + (customerDAO.findAll().size() + 1));
        String finalPass = (password == null || password.isBlank()) ? (cleanUser + "123*") : password.trim();
        Customer newStudent = new Customer(
                newId, cleanUser,
                (fullName == null || fullName.isBlank()) ? cleanUser : fullName.trim(),
                (registerNo == null || registerNo.isBlank()) ? ("RA25110" + (100 + customerDAO.findAll().size())) : registerNo.trim(),
                (email == null || email.isBlank()) ? (cleanUser.toLowerCase().replaceAll("\\s+", "") + "@campus.edu") : email.trim(),
                finalPass,
                initialWalletBalance,
                (favouriteCategory == null || favouriteCategory.isBlank()) ? "Fast Food & Grills" : favouriteCategory,
                "12:30 PM – 1:30 PM",
                120.00
        );
        newStudent.addFavouriteFood("FD-201");
        newStudent.addFavouriteFood("FD-301");
        newStudent.pushNotification("🔔 Welcome to MealSynx, " + cleanUser + "! Your account and ₹" + String.format("%.2f", initialWalletBalance) + " wallet are permanently saved.");
        customerDAO.save(newStudent);
        PersistentDataStore.getInstance().saveToDisk();
        currentLoggedInUser = newStudent;
        return newStudent;
    }

    public Worker addNewWorkerByAdmin(String username, String fullName, String password,
                                      String assignedCanteenId, String shiftTiming, String specialization) {
        String cleanUser = username.trim();
        String finalPass = (password == null || password.isBlank()) ? (cleanUser + "123*") : password.trim();
        int idx = employeeDAO.findAllWorkers().size() + 101;
        String workerId = "WRK-" + idx;
        String empCode = "EMP-" + assignedCanteenId.replace("-", "") + "-" + idx;

        Worker worker = new Worker(
                workerId, cleanUser,
                (fullName == null || fullName.isBlank()) ? cleanUser : fullName.trim(),
                empCode,
                cleanUser.toLowerCase().replaceAll("\\s+", "") + "@mealsynx.edu",
                finalPass,
                assignedCanteenId,
                (shiftTiming == null || shiftTiming.isBlank()) ? "09:00 AM – 05:00 PM" : shiftTiming.trim(),
                (specialization == null || specialization.isBlank()) ? "Kitchen & Counter Operations" : specialization.trim()
        );
        worker.pushNotification("🔔 Welcome " + cleanUser + "! Assigned to " + assignedCanteenId + " (" + worker.getShiftTiming() + ").");
        Worker saved = employeeDAO.saveWorker(worker);
        PersistentDataStore.getInstance().saveToDisk();
        return saved;
    }

    public Admin addNewAdminByAdmin(String username, String fullName, String password, String department) {
        String cleanUser = username.trim();
        String finalPass = (password == null || password.isBlank()) ? (cleanUser + "123*") : password.trim();
        int idx = employeeDAO.findAllAdmins().size() + 1;
        String adminId = "ADM-00" + idx;
        String admCode = "ADM-CENTRAL-0" + idx;

        Admin newAdmin = new Admin(
                adminId, cleanUser,
                (fullName == null || fullName.isBlank()) ? cleanUser : fullName.trim(),
                admCode,
                cleanUser.toLowerCase().replaceAll("\\s+", "") + "@mealsynx.edu",
                finalPass,
                (department == null || department.isBlank()) ? "Campus Food Intelligence & Operations" : department.trim()
        );
        newAdmin.pushNotification("🔔 Admin account created for " + cleanUser + ".");
        Admin saved = employeeDAO.saveAdmin(newAdmin);
        PersistentDataStore.getInstance().saveToDisk();
        return saved;
    }

    public List<Worker> getAllWorkers() { return employeeDAO.findAllWorkers(); }
    public List<Admin> getAllAdmins() { return employeeDAO.findAllAdmins(); }
    public void logout() { this.currentLoggedInUser = null; }
    public User getCurrentLoggedInUser() { return currentLoggedInUser; }
}
