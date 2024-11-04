//package org.example.service;
//
//import org.example.common.FileProcessor;
//import org.example.model.Order;
//import org.example.model.Product;
//import org.example.util.MessageKeys;
//import org.example.validate.OrderValidator;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.lang.reflect.Field;
//import java.time.OffsetDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderServiceTest {
//
//    @Mock
//    private CustomerService customerService;
//
//    @Mock
//    private ProductService productService;
//
//    @Spy
//    private OrderValidator orderValidator;
//
//    private OrderService orderService;
//    private FileProcessor<Order> fileProcessor;
//
//    private static final String FOLDER_PATH = "test/folder";
//
//    @BeforeEach
//    void setUp() throws Exception {
//        fileProcessor = mock(FileProcessor.class);
//        orderService = new OrderService(customerService, productService, FOLDER_PATH);
//
//        Field field = OrderService.class.getDeclaredField("fileProcessor");
//        field.setAccessible(true);
//        field.set(orderService, fileProcessor);
//
//        field = OrderService.class.getDeclaredField("orderValidator");
//        field.setAccessible(true);
//        field.set(orderService, orderValidator);
//    }
//
//    @Test
//    void loadAndValidOrders_Success() {
//        List<String[]> mockData = Arrays.asList(
//                new String[]{"ID", "CUSTOMER_ID", "PRODUCT_QUANTITIES", "ORDER_DATE", "TOTAL_AMOUNT"},
//                new String[]{"O001", "C001", "P001:2;P002:1", "2024-01-01T00:00:00Z", "100.0"}
//        );
//
//        Set<String> customerIds = new HashSet<>(Collections.singletonList("C001"));
//        Map<String, Product> productMap = new HashMap<>();
//        Product product1 = new Product("P001", "Product 1", 30.0, 10);
//        Product product2 = new Product("P002", "Product 2", 40.0, 5);
//        productMap.put("P001", product1);
//        productMap.put("P002", product2);
//
//        when(fileProcessor.readFile(MessageKeys.FILE_PATH_ORDER)).thenReturn(mockData);
//        when(customerService.getCustomerIds()).thenReturn(customerIds);
//        when(productService.getAllProducts()).thenReturn(productMap);
//
//        doNothing().when(orderValidator).validateId(any(), anyBoolean(), anyBoolean());
//        doNothing().when(orderValidator).validateCustomerId(any(), anyBoolean());
//        doNothing().when(orderValidator).validateProductQuantities(any(), any());
//        doNothing().when(orderValidator).validateProductStock(any(), any());
//        doNothing().when(orderValidator).validateOrderDate(any());
//
//        Collection<Order> result = orderService.loadAndValidOrders();
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        Order order = result.iterator().next();
//        assertEquals("O001", order.getId());
//        assertEquals("C001", order.getCustomerId());
//        assertEquals(2, order.getProductQuantities().get("P001"));
//        assertEquals(1, order.getProductQuantities().get("P002"));
//    }
//
//    @Test
//    void addNewOrders_Success() {
//        List<String[]> mockData = Arrays.asList(
//                new String[]{"ID", "CUSTOMER_ID", "PRODUCT_QUANTITIES", "ORDER_DATE", "TOTAL_AMOUNT"},
//                new String[]{"O002", "C001", "P001:1", "2024-01-02T00:00:00Z", "30.0"}
//        );
//
//        Set<String> customerIds = new HashSet<>(Collections.singletonList("C001"));
//        Map<String, Product> productMap = new HashMap<>();
//        productMap.put("P001", new Product("P001", "Product 1", 30.0, 10));
//
//        when(fileProcessor.readFile(MessageKeys.FILE_PATH_NEW_ORDER)).thenReturn(mockData);
//        when(customerService.getCustomerIds()).thenReturn(customerIds);
//        when(productService.getAllProducts()).thenReturn(productMap);
//
//        doNothing().when(orderValidator).validateId(any(), anyBoolean(), anyBoolean());
//        doNothing().when(orderValidator).validateCustomerId(any(), anyBoolean());
//        doNothing().when(orderValidator).validateProductQuantities(any(), any());
//        doNothing().when(orderValidator).validateProductStock(any(), any());
//        doNothing().when(orderValidator).validateOrderDate(any());
//
//        orderService.addNewOrders();
//
//        verify(fileProcessor).writeFile(eq(MessageKeys.FILE_OUTPUT_ORDER), any(), any(), any());
//    }
//
//    @Test
//    void updateOrders_Success() throws Exception {
//        Map<String, Order> orderMap = new HashMap<>();
//        Order existingOrder = new Order("O001", "C001",
//                Collections.singletonMap("P001", 1),
//                OffsetDateTime.parse("2024-01-01T00:00:00Z"));
//        orderMap.put("O001", existingOrder);
//
//        Field orderMapField = OrderService.class.getDeclaredField("orderMap");
//        orderMapField.setAccessible(true);
//        orderMapField.set(orderService, orderMap);
//
//        List<String[]> mockUpdateData = Arrays.asList(
//                new String[]{"ID", "CUSTOMER_ID", "PRODUCT_QUANTITIES", "ORDER_DATE", "TOTAL_AMOUNT"},
//                new String[]{"O001", "C001", "P001:2", "2024-01-02T00:00:00Z", "60.0"}
//        );
//
//        Set<String> customerIds = new HashSet<>(Collections.singletonList("C001"));
//        Map<String, Product> productMap = new HashMap<>();
//        productMap.put("P001", new Product("P001", "Product 1", 30.0, 10));
//
//        when(fileProcessor.readFile(MessageKeys.FILE_PATH_EDIT_ORDER)).thenReturn(mockUpdateData);
//        when(customerService.getCustomerIds()).thenReturn(customerIds);
//        when(productService.getAllProducts()).thenReturn(productMap);
//
//        doNothing().when(orderValidator).validateId(any(), anyBoolean(), anyBoolean());
//        doNothing().when(orderValidator).validateCustomerId(any(), anyBoolean());
//        doNothing().when(orderValidator).validateProductQuantities(any(), any());
//        doNothing().when(orderValidator).validateProductStock(any(), any());
//        doNothing().when(orderValidator).validateOrderDate(any());
//
//        orderService.updateOrders();
//
//        Order updatedOrder = orderMap.get("O001");
//        assertEquals(2, updatedOrder.getProductQuantities().get("P001"));
//        assertEquals(OffsetDateTime.parse("2024-01-02T00:00:00Z"), updatedOrder.getOrderDate());
//        verify(fileProcessor).writeFile(eq(MessageKeys.FILE_OUTPUT_ORDER), any(), any(), any());
//    }
//
//    @Test
//    void deleteOrders_Success() throws Exception {
//        Map<String, Order> orderMap = new HashMap<>();
//        Order orderToDelete = new Order("O001", "C001",
//                Collections.singletonMap("P001", 1),
//                OffsetDateTime.parse("2024-01-01T00:00:00Z"));
//        orderMap.put("O001", orderToDelete);
//
//        Field orderMapField = OrderService.class.getDeclaredField("orderMap");
//        orderMapField.setAccessible(true);
//        orderMapField.set(orderService, orderMap);
//
//        List<String[]> mockDeleteData = Arrays.asList(
//                new String[]{"ID"},
//                new String[]{"O001"}
//        );
//
//        when(fileProcessor.readFile(MessageKeys.FILE_PATH_DELETE_ORDER)).thenReturn(mockDeleteData);
//        doNothing().when(orderValidator).validateId(any(), anyBoolean(), anyBoolean());
//
//        orderService.deleteOrders();
//
//        assertTrue(orderMap.isEmpty());
//        verify(fileProcessor).writeFile(eq(MessageKeys.FILE_OUTPUT_ORDER), any(), any(), any());
//    }
//
//    @Test
//    void calculateTotalAmount_Success() {
//        Map<String, Product> productMap = new HashMap<>();
//        Product product1 = new Product("P001", "Product 1", 30.0, 10);
//        Product product2 = new Product("P002", "Product 2", 40.0, 5);
//        productMap.put("P001", product1);
//        productMap.put("P002", product2);
//
//        Map<String, Integer> productQuantities = new HashMap<>();
//        productQuantities.put("P001", 2);
//        productQuantities.put("P002", 1);
//
//        Order order = new Order("O001", "C001", productQuantities,
//                OffsetDateTime.parse("2024-01-01T00:00:00Z"));
//
//        Double totalAmount = OrderService.calculateTotalAmount(order, productMap, true);
//
//        assertEquals(100.0, totalAmount);
//    }
//
//    @Test
//    void calculateTotalAmount_ThrowsException_WhenProductNotFound() {
//        Map<String, Product> productMap = new HashMap<>();
//        Map<String, Integer> productQuantities = new HashMap<>();
//        productQuantities.put("P001", 2); // Product not in productMap
//
//        Order order = new Order("O001", "C001", productQuantities,
//                OffsetDateTime.parse("2024-01-01T00:00:00Z"));
//
//        assertThrows(IllegalArgumentException.class, () ->
//                OrderService.calculateTotalAmount(order, productMap, true));
//    }
//}