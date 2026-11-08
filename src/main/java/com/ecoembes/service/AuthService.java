package com.ecoembes.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.ecoembes.entity.Employee;

/**
 * Login/logout and token validation.
 * StateManagement pattern: activeTokens keeps in memory the employees who are logged in,
 * so the other operations only need the token and do not access the repository again.
 */
@Service
public class AuthService {

    // Simulated repository (no persistence in TW1). Key = email.
    private final Map<String, Employee> employees = new HashMap<>();

    // Server state. Key = token (login timestamp), value = logged-in employee.
    private final Map<String, Employee> activeTokens = new ConcurrentHashMap<>();

    public AuthService() {
        addEmployee(new Employee("Ane Etxeberria", "ane@ecoembes.com", "1234"));
        addEmployee(new Employee("Jon Garcia", "jon@ecoembes.com", "1234"));
    }

    private void addEmployee(Employee e) {
        employees.put(e.getEmail(), e);
    }

    /** Returns the new token if the credentials are correct. */
    public Optional<String> login(String email, String password) {
        Employee employee = employees.get(email);
        if (employee == null || password == null || !employee.checkPassword(password)) {
            return Optional.empty();
        }
        String token = generateToken();
        activeTokens.put(token, employee);
        return Optional.of(token);
    }

    public boolean logout(String token) {
        return token != null && activeTokens.remove(token) != null;
    }

    public boolean isValidToken(String token) {
        return token != null && activeTokens.containsKey(token);
    }

    /** Employee associated with the token, or null if the token is not valid. */
    public Employee getEmployeeByToken(String token) {
        return token == null ? null : activeTokens.get(token);
    }

    /** Token = server timestamp at login (as the project description says). */
    private synchronized String generateToken() {
        long ts = System.currentTimeMillis();
        while (activeTokens.containsKey(String.valueOf(ts))) {
            ts++; // two logins in the same millisecond
        }
        return String.valueOf(ts);
    }
}
