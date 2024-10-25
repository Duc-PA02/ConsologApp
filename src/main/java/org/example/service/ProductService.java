package org.example.service;

import org.example.enums.ProductEnum;
import org.example.model.Product;
import org.example.common.FileProcessor;
import org.example.util.MessageKeys;
import org.example.validate.ProductValidator;

import java.util.*;

public class ProductService {
    private final FileProcessor<Product> fileProcessor;
    private final ProductValidator productValidator;
    private final Map<String, Product> productMap;

    public ProductService(String folderPath) {
        this.fileProcessor = new FileProcessor<>(folderPath);
        this.productValidator = new ProductValidator();
        this.productMap = new HashMap<>();
    }

    public Collection<Product> loadAndValidateProducts() {
        productMap.clear();
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_PRODUCT);
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            try {
                Product product = validateAndCreateProduct(values);
                productMap.put(product.getId(), product);
            } catch (IllegalArgumentException e) {
                handleException(e);
            }
        }

        return productMap.values();
    }

    public void loadProducts() {
        productMap.clear();
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_PRODUCT);
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            try {
                Product product = createProduct(values);
                productMap.put(product.getId(), product);
            } catch (IllegalArgumentException e) {
                handleException(e);
            }
        }
    }

    private Product createProduct(String[] values) {
        return createProductFromValues(values, false);
    }

    private Product validateAndCreateProduct(String[] values) {
        return createProductFromValues(values, true);
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
            productValidator.validateId(id, productMap.containsKey(id));
            productValidator.validateName(name);
            productValidator.validatePrice(priceStr);
            productValidator.validateStock(stockStr);
        }

        return new Product(id, name, Double.parseDouble(priceStr), Integer.parseInt(stockStr));
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
            Product product = new Product(values[ProductEnum.ID.ordinal()],
                    values[ProductEnum.NAME.ordinal()],
                    Double.parseDouble(values[ProductEnum.PRICE.ordinal()]),
                    Integer.parseInt(values[ProductEnum.STOCK_AVAILABLE.ordinal()]));
            productMap.put(product.getId(), product);
        }
    }

    private void processProductUpdateData(List<String[]> data) {
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            String id = values[ProductEnum.ID.ordinal()];

            if (productMap.containsKey(id)) {
                Product existingProduct = productMap.get(id);
                existingProduct.setName(values[ProductEnum.NAME.ordinal()]);
                existingProduct.setPrice(Double.parseDouble(values[ProductEnum.PRICE.ordinal()]));
                existingProduct.setStockAvailable(Integer.parseInt(values[ProductEnum.STOCK_AVAILABLE.ordinal()]));
            }
        }
    }

    private Set<String> processDeleteProductData(List<String[]> data) {
        Set<String> productIdsToDelete = new HashSet<>();
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            productIdsToDelete.add(values[ProductEnum.ID.ordinal()]);
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
