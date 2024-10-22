package org.example.service;

import org.example.enums.ProductEnum;
import org.example.model.Product;
import org.example.common.FileProcessor;
import org.example.util.MessageKeys;
import org.example.common.ProductValidator;

import java.io.IOException;
import java.util.*;

public class ProductService {
    private final FileProcessor<Product> fileProcessor;
    private final ProductValidator productValidator;
    private final Map<String, Product> productMap;

    public ProductService() {
        this.fileProcessor = new FileProcessor<>();
        this.productValidator = new ProductValidator();
        this.productMap = new HashMap<>();
    }

    public Collection<Product> loadProducts() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_PRODUCT);
        processProductData(data);
        return productMap.values();
    }

    public void addNewProducts() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_NEW_PRODUCT);
        processProductData(data);
        writeProductsToFile();
    }

    public void updateProducts() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_EDIT_PRODUCT);
        processProductUpdateData(data);
        writeProductsToFile();
    }

    public void deleteProducts() {
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
                    handleException(e);
                }
            }
        }
    }

    private void processProductUpdateData(List<String[]> data) {
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);

            if (values.length >= ProductEnum.values().length) {
                String id = values[ProductEnum.ID.ordinal()];
                String name = values[ProductEnum.NAME.ordinal()];
                String priceStr = values[ProductEnum.PRICE.ordinal()];
                String stockStr = values[ProductEnum.STOCK_AVAILABLE.ordinal()];

                try {
                    productValidator.validateId(id, productMap.containsKey(id), true);
                    productValidator.validateName(name);
                    productValidator.validatePrice(priceStr);
                    productValidator.validateStock(stockStr);

                    if (productMap.containsKey(id)) {
                        Product existingProduct = productMap.get(id);
                        existingProduct.setName(name);
                        existingProduct.setPrice(Double.parseDouble(priceStr));
                        existingProduct.setStockAvailable(Integer.parseInt(stockStr));
                    }
                } catch (IllegalArgumentException e) {
                    handleException(e);
                }
            }
        }
    }

    private Set<String> processDeleteProductData(List<String[]> data) {
        Set<String> productIdsToDelete = new HashSet<>();

        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);

            if (values.length > 0) {
                String id = values[ProductEnum.ID.ordinal()].trim();
                try {
                    productValidator.validateId(id, productMap.containsKey(id), true);
                    if (!id.isEmpty()) {
                        productIdsToDelete.add(id);
                    }
                } catch (IllegalArgumentException e){
                    handleException(e);
                }
            }
        }

        return productIdsToDelete;
    }

    public void writeProductsToFile() {
        String header = createHeader();
        fileProcessor.writeFile(MessageKeys.FILE_OUTPUT_PRODUCT, new ArrayList<>(productMap.values()), this::formatProduct, header);
    }

    protected String createHeader() {
        return String.join(MessageKeys.CHARACTER,
                ProductEnum.ID.getHeader(),
                ProductEnum.NAME.getHeader(),
                ProductEnum.PRICE.getHeader(),
                ProductEnum.STOCK_AVAILABLE.getHeader());
    }

    protected String formatProduct(Product product) {
        return String.join(MessageKeys.CHARACTER,
                product.getId(),
                product.getName(),
                String.valueOf(product.getPrice()),
                String.valueOf(product.getStockAvailable()));
    }

    public Map<String, Product> getAllProducts() {
        return productMap;
    }

    private void handleException(IllegalArgumentException e) {
        fileProcessor.writeErrorLog(MessageKeys.FILE_ERROR, "An error occurred: " + e.getMessage());
    }
}
