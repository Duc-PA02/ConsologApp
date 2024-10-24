package org.example.validate;

public class ProductValidator {
    public void validateId(String id, boolean idExists, boolean isUpdate) throws IllegalArgumentException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be empty.");
        }

        if (isUpdate) {
            if (!idExists) {
                throw new IllegalArgumentException("Product ID does not exist for update: " + id);
            }
        } else {
            if (idExists) {
                throw new IllegalArgumentException("Product ID already exists: " + id);
            }
        }
    }


    public void validateName(String name) throws IllegalArgumentException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product Name cannot be empty.");
        }
    }

    public void validatePrice(String priceStr) throws IllegalArgumentException {
        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                throw new IllegalArgumentException("Price must be greater than or equal to 0.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Price must be a valid number.");
        }
    }

    public void validateStock(String stockStr) throws IllegalArgumentException {
        try {
            int stockAvailable = Integer.parseInt(stockStr);
            if (stockAvailable < 0) {
                throw new IllegalArgumentException("StockAvailable must be greater than or equal to 0.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("StockAvailable must be a valid integer.");
        }
    }
}
