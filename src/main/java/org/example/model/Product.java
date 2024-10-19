package org.example.model;

public class Product {
    private String id;
    private String name;
    private Double price;
    private Integer stockAvailable;

    public Product(String id, String name, Double price, Integer stockAvailable) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockAvailable = stockAvailable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStockAvailable() {
        return stockAvailable;
    }

    public void setStockAvailable(Integer stockAvailable) {
        this.stockAvailable = stockAvailable;
    }
}
