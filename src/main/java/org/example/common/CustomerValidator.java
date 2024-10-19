package org.example.common;

import java.util.regex.Pattern;

public class CustomerValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9}$");

    public void validateId(String id, boolean idExists) throws IllegalArgumentException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be empty.");
        }

        if (idExists) {
            throw new IllegalArgumentException("Customer ID already exists: " + id);
        }
    }

    public void validateName(String name) throws IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer Name cannot be empty.");
        }
    }

    public void validateEmail(String email) throws IllegalArgumentException {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
    }

    public void validatePhoneNumber(String phoneNumber, boolean phoneExists) throws IllegalArgumentException {
        if (!PHONE_PATTERN.matcher(phoneNumber).matches() || phoneExists) {
            throw new IllegalArgumentException("Invalid or duplicated phone number: " + phoneNumber);
        }
    }
}
