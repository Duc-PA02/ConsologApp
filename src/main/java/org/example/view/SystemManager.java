package org.example.view;

import org.example.service.CustomerService;
import org.example.service.OrderService;
import org.example.service.ProductService;
import org.example.service.SearchService;

public class SystemManager {
    private final ProductService productService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final SearchService searchService;

    public SystemManager(String folderPath) {
        this.productService = new ProductService(folderPath);
        this.customerService = new CustomerService(folderPath);
        this.orderService = new OrderService(customerService, productService, folderPath);
        this.searchService = new SearchService(productService, orderService, folderPath);
    }
    public void processFunction(String functionCode) {
        switch (functionCode) {
            case "1":
                productService.loadProducts();
                productService.writeProductsToFile();
                customerService.loadCustomers();
                customerService.writeCustomersToFile();
                orderService.loadOrders();
                orderService.writeOrdersToFile();
                break;
            case "2.1":
                productService.addNewProducts();
                break;
            case "2.2":
                productService.updateProducts();
                break;
            case "2.3":
                productService.deleteProducts();
                break;
            case "3.1":
                customerService.addNewCustomers();
                break;
            case "3.2":
                customerService.updateCustomers();
                break;
            case "3.3":
                customerService.deleteCustomers();
                break;
            case "4.1":
                orderService.addNewOrders();
                break;
            case "4.2":
                orderService.updateOrders();
                break;
            case "4.3":
                orderService.deleteOrders();
                break;
            case "5.1":
                searchService.searchToProduct();
                break;
            case "5.2":
                searchService.searchOrdersByProductId();
                break;
            default:
                System.out.println("Invalid function code. Please try again.");
        }
    }
}
