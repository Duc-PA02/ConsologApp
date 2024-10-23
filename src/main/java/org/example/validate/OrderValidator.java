package org.example.validate;

import org.example.model.Product;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class OrderValidator {
    private static final Pattern ISO8601_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1,9}(\\+|-)\\d{2}:\\d{2}$");

    public void validateId(String id, boolean idExists, boolean isUpdate) throws IllegalArgumentException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be empty.");
        }

        if (isUpdate) {
            if (!idExists) {
                throw new IllegalArgumentException("Order ID does not exist for update: " + id);
            }
        } else {
            if (idExists) {
                throw new IllegalArgumentException("Order ID already exists: " + id);
            }
        }
    }

    public void validateCustomerId(String customerId, boolean customerExists) throws IllegalArgumentException {
        if (!customerExists) {
            throw new IllegalArgumentException("Customer ID does not exist: " + customerId);
        }
    }

    public void validateProductQuantities(Map<String, Integer> productQuantities, Set<String> ProductIds) throws IllegalArgumentException {
        if (productQuantities.isEmpty()) {
            throw new IllegalArgumentException("Invalid product quantities.");
        }

        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer quantity = entry.getValue();

            if (!ProductIds.contains(productId)) {
                throw new IllegalArgumentException("Invalid product ID: " + productId);
            }

            if (quantity <= 0) {
                throw new IllegalArgumentException("Product quantity must be greater than 0.");
            }
        }
    }

    public void validateProductStock(Map<String, Integer> productQuantities, Map<String, Product> productStocks) throws IllegalArgumentException {
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer orderedQuantity = entry.getValue();
            Integer availableStock = productStocks.get(productId).getStockAvailable();
            if (availableStock == null || availableStock < 0) {
                throw new IllegalArgumentException("Invalid stock for product ID: " + productId);
            }

            if (orderedQuantity > availableStock) {
                throw new IllegalArgumentException("Ordered quantity exceeds available stock for product ID: " + productId);
            }
        }
    }

    public void validateOrderDate(String orderDate) throws IllegalArgumentException {
        if (!ISO8601_PATTERN.matcher(orderDate).matches()) {
            throw new IllegalArgumentException("Invalid Order Date format: " + orderDate);
        }
    }
}
