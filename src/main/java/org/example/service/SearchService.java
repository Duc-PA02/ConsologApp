package org.example.service;

import org.example.enums.ProductEnum;
import org.example.model.Order;
import org.example.model.Product;
import org.example.common.FileProcessor;
import org.example.util.MessageKeys;

import java.util.*;
import java.util.stream.Collectors;

public class SearchService {
    private final ProductService productService;
    private final OrderService orderService;
    private final FileProcessor<Product> fileProcessorProduct;
    private final FileProcessor<Order> fileProcessorOrder;
    private final FileProcessor<String> idFileProcessor;

    public SearchService(ProductService productService, OrderService orderService, String folderPath) {
        this.productService = productService;
        this.orderService = orderService;
        this.fileProcessorProduct = new FileProcessor<>(folderPath);
        this.fileProcessorOrder = new FileProcessor<>(folderPath);
        this.idFileProcessor = new FileProcessor<>(folderPath);
    }

    // Tìm kiếm top 3 product có số lượng order lớn nhất
    public void searchToProduct() {
        Map<String, Integer> productOrderCount = new HashMap<>();

        for (Order order : orderService.orders().values()) {
            Set<String> productIdsInOrder = order.getProductQuantities().keySet();
            for (String productId : productIdsInOrder) {
                productOrderCount.put(productId, productOrderCount.getOrDefault(productId, 0) + 1);
            }
        }

        List<String> top3ProductIds = productOrderCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .map(Map.Entry::getKey).toList();

        Map<String, Product> allProducts = productService.getAllProducts();
        List<Product> top3Products = top3ProductIds.stream()
                .map(allProducts::get)
                .collect(Collectors.toList());

        String header = productService.createHeader();
        fileProcessorProduct.writeFile(MessageKeys.FILE_OUTPUT_PRODUCT, top3Products, productService::formatProduct, header);
    }

    public void searchOrdersByProductId() {
        List<String[]> productIdData = idFileProcessor.readFile(MessageKeys.FILE_PATH_SEARCH_PRODUCT_ID);

        List<String> productIds = productIdData.stream()
                .map(values -> values[ProductEnum.ID.ordinal()]).toList();

        findOrdersByProductIds(productIds);
    }

    private void findOrdersByProductIds(List<String> productIds) {
        try {
            List<Order> matchingOrders = orderService.orders().values().stream()
                    .filter(order -> orderContainsProductId(order, productIds))
                    .collect(Collectors.toList());

            if (matchingOrders.isEmpty()) {
                throw new IllegalArgumentException("No orders found for the given product IDs");
            }

            String header = orderService.createHeader();
            fileProcessorOrder.writeFile(MessageKeys.FILE_OUTPUT_ORDER, matchingOrders, orderService::formatOrder, header);
        } catch (Exception e) {
            fileProcessorOrder.writeErrorLog(MessageKeys.FILE_ERROR, "Error while finding orders by product IDs: " + e.getMessage());
        }
    }

    private boolean orderContainsProductId(Order order, List<String> productIds) {
        return order.getProductQuantities().keySet().stream()
                .anyMatch(productIds::contains);
    }
}
