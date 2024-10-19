package org.example.enums;

public enum ProductEnum {
    ID("id"),
    NAME("name"),
    PRICE("price"),
    STOCK_AVAILABLE("Stock Available");

    private final String header;

    ProductEnum(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}
