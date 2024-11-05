package org.mock.enums;

public enum OrderEnum {
    ID("id"),
    CUSTOMER_ID("customerId"),
    PRODUCT_QUANTITIES("productQuantities"),
    ORDER_DATE("orderDate"),
    TOTAL_AMOUNT("totalAmount");
    private final String header;

    OrderEnum(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}
