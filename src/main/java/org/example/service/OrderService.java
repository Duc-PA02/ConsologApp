package org.example.service;

import org.example.common.FileProcessor;
import org.example.validate.OrderValidator;
import org.example.enums.OrderEnum;
import org.example.model.Order;
import org.example.model.Product;
import org.example.util.MessageKeys;

import java.time.OffsetDateTime;
import java.util.*;

public class OrderService {
    private final FileProcessor<Order> fileProcessor;
    private final OrderValidator orderValidator;
    private final CustomerService customerService ;
    private final ProductService productService;
    private Map<String, Order> orderMap;

    public OrderService(CustomerService customerService, ProductService productService, String folderPath) {
        this.fileProcessor = new FileProcessor<>(folderPath);
        this.orderValidator = new OrderValidator();
        this.customerService = customerService;
        this.productService = productService;
        this.orderMap = new HashMap<>();
    }

    public Collection<Order> loadAndValidOrders() {
        return loadOrders(true);
    }

    public Collection<Order> loadOrders() {
        return loadOrders(false);
    }

    private Collection<Order> loadOrders(boolean validate) {
        orderMap.clear();
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_ORDER);
        processOrderData(data, validate);
        return orderMap.values();
    }

    private void processOrderData(List<String[]> data, boolean validate) {
        Set<String> customerIds = new HashSet<>(customerService.getCustomerIds());
        Map<String, Product> productMap = new HashMap<>(productService.getAllProducts());

        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            try {
                Order order = createOrderFromValues(values, validate, customerIds, productMap);
                orderMap.put(order.getId(), order);
            } catch (IllegalArgumentException e) {
                handleException(e, i);
            }
        }
    }

    private Order createOrderFromValues(String[] values, boolean validate, Set<String> customerIds, Map<String, Product> productMap) {
        if (values.length < OrderEnum.values().length - 1) {
            throw new IllegalArgumentException("Invalid data length");
        }

        String id = values[OrderEnum.ID.ordinal()];
        String customerId = values[OrderEnum.CUSTOMER_ID.ordinal()];
        String productQuantitiesStr = values[OrderEnum.PRODUCT_QUANTITIES.ordinal()];
        String orderDateStr = values[OrderEnum.ORDER_DATE.ordinal()];

        if (validate) {
            orderValidator.validateId(id, orderMap.containsKey(id), false);
            orderValidator.validateCustomerId(customerId, customerIds.contains(customerId));
            Map<String, Integer> productQuantities = parseProductQuantities(productQuantitiesStr);
            orderValidator.validateProductQuantities(productQuantities, productMap.keySet());
            orderValidator.validateProductStock(productQuantities, productMap);
            orderValidator.validateOrderDate(orderDateStr);
        }

        Order order = new Order(id, customerId, parseProductQuantities(productQuantitiesStr), OffsetDateTime.parse(orderDateStr));
        order.setTotalAmount(calculateTotalAmount(order, productMap, false));
        return order;
    }

    public void addNewOrders() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_NEW_ORDER);
        processOrderData(data, true);
        writeOrdersToFile();
    }

    public void updateOrders() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_EDIT_ORDER);

        Set<String> customerIds = new HashSet<>(customerService.getCustomerIds());
        Map<String, Product> productMap = new HashMap<>(productService.getAllProducts());

        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);

            if (values.length >= OrderEnum.values().length - 1) {
                String id = values[OrderEnum.ID.ordinal()];
                String newCustomerId = values[OrderEnum.CUSTOMER_ID.ordinal()];
                String newProductQuantitiesStr = values[OrderEnum.PRODUCT_QUANTITIES.ordinal()];
                String newOrderDateStr = values[OrderEnum.ORDER_DATE.ordinal()];

                try {
                    orderValidator.validateId(id, orderMap.containsKey(id), true);
                    Order existingOrder = orderMap.get(id);

                    if (!newCustomerId.isEmpty() && !customerIds.contains(newCustomerId)) {
                        orderValidator.validateCustomerId(newCustomerId, customerIds.contains(newCustomerId));
                        existingOrder.setCustomerId(newCustomerId);
                    }

                    Map<String, Integer> newProductQuantities = parseProductQuantities(newProductQuantitiesStr);

                    orderValidator.validateProductQuantities(newProductQuantities, productMap.keySet());
                    orderValidator.validateProductStock(newProductQuantities, productMap);

                    if (!newOrderDateStr.isEmpty()){
                        orderValidator.validateOrderDate(newOrderDateStr);
                        existingOrder.setOrderDate(OffsetDateTime.parse(newOrderDateStr));
                    }

                    existingOrder.setProductQuantities(newProductQuantities);
                    double newTotalAmount = calculateTotalAmount(existingOrder, productMap, true);
                    existingOrder.setTotalAmount(newTotalAmount);

                } catch (IllegalArgumentException e) {
                    handleException(e, i);
                }
            }
        }
        writeOrdersToFile();
    }

    public void deleteOrders() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_DELETE_ORDER);

        Set<String> orderIdsToDelete = new HashSet<>();

        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);

            if (values.length > 0) {
                String id = values[OrderEnum.ID.ordinal()];
                try {
                    orderValidator.validateId(id, orderMap.containsKey(id), true);
                    orderIdsToDelete.add(id);
                } catch (IllegalArgumentException e) {
                    handleException(e, i);
                }
            }
        }

        for (String orderId : orderIdsToDelete) {
            orderMap.remove(orderId);
        }

        writeOrdersToFile();
    }
    public void writeOrdersToFile() {
        String header = createHeader();
        fileProcessor.writeFile(MessageKeys.FILE_OUTPUT_ORDER, new ArrayList<>(orderMap.values()), this::formatOrder, header);
    }

    public String createHeader() {
        return String.join(MessageKeys.CHARACTER,
                OrderEnum.ID.getHeader(),
                OrderEnum.CUSTOMER_ID.getHeader(),
                OrderEnum.PRODUCT_QUANTITIES.getHeader(),
                OrderEnum.ORDER_DATE.getHeader(),
                OrderEnum.TOTAL_AMOUNT.getHeader());
    }

    public String formatOrder(Order order) {
        StringBuilder productQuantitiesStr = new StringBuilder();
        order.getProductQuantities().forEach((productId, quantity) ->
                productQuantitiesStr.append(productId).append(MessageKeys.CHAR_SPLIT).append(quantity).append(MessageKeys.CHAR_SPLIT_QUANTITY)
        );

        if (productQuantitiesStr.length() > 0) {
            productQuantitiesStr.setLength(productQuantitiesStr.length() - 1);
        }

        return String.join(MessageKeys.CHARACTER,
                order.getId(),
                order.getCustomerId(),
                productQuantitiesStr.toString(),
                order.getOrderDate().toString(),
                order.getTotalAmount().toString());
    }

    protected Map<String, Integer> parseProductQuantities(String productQuantitiesStr) {
        Map<String, Integer> productQuantities = new HashMap<>();
        String[] pairs = productQuantitiesStr.split(MessageKeys.CHAR_SPLIT_QUANTITY);

        for (String pair : pairs) {
            String[] parts = pair.split(MessageKeys.CHAR_SPLIT);
            if (parts.length == 2) {
                String productId = parts[0];
                Integer quantity = Integer.valueOf(parts[1]);
                productQuantities.put(productId, quantity);
            }
        }

        return productQuantities;
    }

    public static Double calculateTotalAmount(Order order, Map<String, Product> productMap, boolean validate) {
        double totalAmount = 0.0;

        for (Map.Entry<String, Integer> entry : order.getProductQuantities().entrySet()) {
            String productId = entry.getKey();
            Integer quantity = entry.getValue();

            Product product = productMap.get(productId);
            if (product != null) {
                Double price = product.getPrice();
                if (price != null) {
                    totalAmount += price * quantity;
                } else {
                    throw new IllegalArgumentException("Price not found for product ID: " + productId);
                }
            } else {
                if (validate) {
                    throw new IllegalArgumentException("Product not found for ID: " + productId);
                }
            }
        }
        return totalAmount;
    }

    private void handleException(IllegalArgumentException e, int lineNumber) {
        String errorMessage = "Error on line " + lineNumber + ": " + e.getMessage();
        fileProcessor.writeErrorLog(MessageKeys.FILE_ERROR, errorMessage);
    }

    private String generateUniqueOrderId(Set<String> existingIds) {
        Random random = new Random();
        String id;
        do {
            int randomNumber = random.nextInt(1_000_000_0);
            id = "ORD" + String.format("%07d", randomNumber);
        } while (existingIds.contains(id));
        return id;
    }

    public Map<String, Order> orders(){
        return orderMap;
    }
}
