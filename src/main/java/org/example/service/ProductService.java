package org.example.service;

import org.example.enums.ProductEnum;
import org.example.model.Product;
import org.example.common.FileProcessor;
import org.example.util.MessageKeys;
import org.example.validate.ProductValidator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProductService {
    private final FileProcessor<Product> fileProcessor;
    private final ProductValidator productValidator;
    private final Map<String, Product> productMap;

    public ProductService(String folderPath) {
        this.fileProcessor = new FileProcessor<>(folderPath);
        this.productValidator = new ProductValidator();
        this.productMap = new ConcurrentHashMap<>();
    }

    public synchronized Collection<Product> loadAndValidProducts() {
        return loadProducts(true);
    }

    public synchronized Collection<Product> loadProducts() {
        return loadProducts(false);
    }

    private synchronized Collection<Product> loadProducts(boolean validate) {
        productMap.clear();
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_PRODUCT);
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            try {
                Product product = createProductFromValues(values, validate);
                productMap.put(product.getId(), product);
            } catch (IllegalArgumentException e) {
                handleException(e, i);
            }
        }
        return new ArrayList<>(productMap.values());
    }

    private Product createProductFromValues(String[] values, boolean validate) {
        if (values.length < ProductEnum.values().length) {
            throw new IllegalArgumentException("Invalid data length");
        }

        String id = values[ProductEnum.ID.ordinal()];
        String name = values[ProductEnum.NAME.ordinal()];
        String priceStr = values[ProductEnum.PRICE.ordinal()];
        String stockStr = values[ProductEnum.STOCK_AVAILABLE.ordinal()];

        if (validate) {
            productValidator.validateId(id, productMap.containsKey(id), false);
            productValidator.validateName(name);
            productValidator.validatePrice(priceStr);
            productValidator.validateStock(stockStr);
        }

        return new Product(id, name, Double.parseDouble(priceStr), Integer.parseInt(stockStr));
    }

    public synchronized void addNewProducts() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_NEW_PRODUCT);
        processProductData(data);
        writeProductsToFile();
    }

    public synchronized void updateProducts() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_EDIT_PRODUCT);
        processProductUpdateData(data);
        writeProductsToFile();
    }

    public synchronized void deleteProducts() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_DELETE_PRODUCT);
        Set<String> productIdsToDelete = processDeleteProductData(data);

        for (String productId : productIdsToDelete) {
            productMap.remove(productId);
        }
        writeProductsToFile();
    }

    private void processProductData(List<String[]> data) {
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            if (values.length >= ProductEnum.values().length) {
                String id = values[ProductEnum.ID.ordinal()];
                String name = values[ProductEnum.NAME.ordinal()];
                String priceStr = values[ProductEnum.PRICE.ordinal()];
                String stockStr = values[ProductEnum.STOCK_AVAILABLE.ordinal()];
                try {
                    productValidator.validateId(id, productMap.containsKey(id), false);
                    productValidator.validateName(name);
                    productValidator.validatePrice(priceStr);
                    productValidator.validateStock(stockStr);
                    Product product = new Product(id, name, Double.parseDouble(priceStr), Integer.parseInt(stockStr));
                    productMap.put(id, product);
                } catch (IllegalArgumentException e) {
                    handleException(e, i + 1);
                }
            }
        }
    }

    private void processProductUpdateData(List<String[]> data) {
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            if (values.length >= ProductEnum.values().length) {
                try {
                    String id = values[ProductEnum.ID.ordinal()];
                    String name = values[ProductEnum.NAME.ordinal()];
                    String priceStr = values[ProductEnum.PRICE.ordinal()];
                    String stockStr = values[ProductEnum.STOCK_AVAILABLE.ordinal()];

                    productValidator.validateId(id, productMap.containsKey(id), true);
                    productValidator.validateName(name);
                    productValidator.validatePrice(priceStr);
                    productValidator.validateStock(stockStr);

                    Product existingProduct = productMap.get(id);
                    existingProduct.setName(name);
                    existingProduct.setPrice(Double.parseDouble(priceStr));
                    existingProduct.setStockAvailable(Integer.parseInt(stockStr));
                } catch (IllegalArgumentException e) {
                    handleException(e, i + 1);
                }
            }
        }
    }

    private Set<String> processDeleteProductData(List<String[]> data) {
        Set<String> productIdsToDelete = new HashSet<>();
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            if (values.length > 0) {
                String productId = values[ProductEnum.ID.ordinal()];
                try {
                    productValidator.validateId(productId, productMap.containsKey(productId), true);

                    if (!productId.isEmpty()) {
                        productIdsToDelete.add(productId);
                    }

                } catch (IllegalArgumentException e) {
                    handleException(e, i + 1);
                }
            }
        }
        return productIdsToDelete;
    }

    public void writeProductsToFile() {
        String header = createHeader();
        fileProcessor.writeFile(MessageKeys.FILE_OUTPUT_PRODUCT, new ArrayList<>(productMap.values()), this::formatProduct, header);
    }

    public String createHeader() {
        return String.join(MessageKeys.CHARACTER,
                ProductEnum.ID.getHeader(),
                ProductEnum.NAME.getHeader(),
                ProductEnum.PRICE.getHeader(),
                ProductEnum.STOCK_AVAILABLE.getHeader());
    }

    public String formatProduct(Product product) {
        return String.join(MessageKeys.CHARACTER,
                product.getId(),
                product.getName(),
                String.valueOf(product.getPrice()),
                String.valueOf(product.getStockAvailable()));
    }

    public Map<String, Product> getAllProducts() {
        return productMap;
    }

    private void handleException(IllegalArgumentException e, int lineNumber) {
        String errorMessage = "Error on line " + lineNumber + ": " + e.getMessage();
        fileProcessor.writeErrorLog(MessageKeys.FILE_ERROR, errorMessage);
    }
}
