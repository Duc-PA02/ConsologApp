package org.mock.enums;

public enum CustomerEnum {
    ID("id"),
    NAME("name"),
    EMAIL("email"),
    PHONE_NUMBER("phoneNumber");

    private final String header;

    CustomerEnum(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}
