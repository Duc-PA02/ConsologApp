package org.mock.model;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

public class Order {
    private String id;
    private String customerId;
    private Map<String, Integer> productQuantities;
    private OffsetDateTime orderDate;
    private Double totalAmount;

    public Order(String id, String customerId, Map<String, Integer> productQuantities, OffsetDateTime orderDate) {
        this.id = id;
        this.customerId = customerId;
        this.productQuantities = productQuantities != null ? productQuantities : Collections.emptyMap();
        this.orderDate = orderDate;
        this.totalAmount = 0.0;
    }

    public Order(String id, Map<String, Integer> productQuantities) {
        this(id, null, productQuantities, null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Map<String, Integer> getProductQuantities() {
        return productQuantities;
    }

    public void setProductQuantities(Map<String, Integer> productQuantities) {
        this.productQuantities = productQuantities != null ? productQuantities : Collections.emptyMap();
    }

    public OffsetDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(OffsetDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
