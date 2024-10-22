package org.example.view;

import org.example.service.CustomerService;
import org.example.service.OrderService;
import org.example.service.ProductService;
import org.example.service.SearchService;

import java.io.IOException;
import java.util.Scanner;

public class SystemManager {
    private final ProductService productService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final SearchService searchService;
    private final Scanner scanner;

    public SystemManager() {
        this.productService = new ProductService();
        this.customerService = new CustomerService();
        this.orderService = new OrderService(customerService, productService);
        this.searchService = new SearchService(productService, orderService);
        this.scanner = new Scanner(System.in);
    }

    public void startMenu() {
        while (true) {
            System.out.println("===== SYSTEM MENU =====");
            System.out.println("1. Load product data");
            System.out.println("2. Load customer data");
            System.out.println("3. Load order data");
            System.out.println("4. Add new product");
            System.out.println("5. Update product");
            System.out.println("6. Delete product");
            System.out.println("7. Add new customer");
            System.out.println("8. Update customer");
            System.out.println("9. Delete customer");
            System.out.println("10. Add new order");
            System.out.println("11. Update order");
            System.out.println("12. Delete order");
            System.out.println("13. Top 3 products with the largest number of orders");
            System.out.println("14. Search Orders By ProductID");
            System.out.println("0. Exit");
            System.out.print("Please select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    productService.loadProducts();
                    productService.writeProductsToFile();
                    break;
                case 2:
                    customerService.loadCustomers();
                    customerService.writeCustomersToFile();
                    break;
                case 3:
                    orderService.loadOrders();
                    orderService.writeOrdersToFile();
                    break;
                case 4:
                    productService.addNewProducts();
                    break;
                case 5:
                    productService.updateProducts();
                    break;
                case 6:
                    productService.deleteProducts();
                    break;
                case 7:
                    customerService.addNewCustomers();
                    break;
                case 8:
                    customerService.updateCustomers();
                    break;
                case 9:
                    customerService.deleteCustomers();
                    break;
                case 10:
                    orderService.addNewOrders();
                    break;
                case 11:
                    orderService.updateOrders();
                    break;
                case 12:
                    orderService.deleteOrders();
                    break;
                case 13:
                    searchService.searchToProduct();
                    break;
                case 14:
                    searchService.searchOrdersByProductId();
                    break;
                case 0:
                    System.out.println("Exiting system...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
