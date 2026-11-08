package com.ecoembes.entity;

public class Employee {

    private String name;
    private String email;
    private String password;

    public Employee(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}
