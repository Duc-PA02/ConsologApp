package org.mock.view;

import org.mock.service.CustomerService;
import org.mock.service.OrderService;
import org.mock.service.ProductService;
import org.mock.service.SearchService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SystemManager {
    private final ProductService productService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final SearchService searchService;
    private final ExecutorService executor;

    public SystemManager(String folderPath) {
        this.productService = new ProductService(folderPath);
        this.customerService = new CustomerService(folderPath);
        this.orderService = new OrderService(customerService, productService, folderPath);
        this.searchService = new SearchService(productService, orderService, folderPath);
        this.executor = Executors.newFixedThreadPool(3);
    }
    private void loadDataAndThen(Runnable action) throws InterruptedException, ExecutionException {
        Future<?> loadProduct = executor.submit(productService::loadProducts);
        Future<?> loadCustomer = executor.submit(customerService::loadCustomers);
        Future<?> loadOrder = executor.submit(orderService::loadOrders);

        loadProduct.get();
        loadCustomer.get();
        loadOrder.get();

        action.run();
    }
    public void processFunction(String functionCode) {
        try {
            switch (functionCode) {
                case "1":
                    Future<?> loadProduct = executor.submit(productService::loadAndValidProducts);
                    Future<?> loadCustomer = executor.submit(customerService::loadAndValidCustomers);
                    loadProduct.get();
                    loadCustomer.get();
                    orderService.loadAndValidOrders();
                    productService.writeProductsToFile();
                    customerService.writeCustomersToFile();
                    orderService.writeOrdersToFile();
                    break;
                case "2.1":
                    loadDataAndThen(productService::addNewProducts);
                    break;
                case "2.2":
                    loadDataAndThen(productService::updateProducts);
                    break;
                case "2.3":
                    loadDataAndThen(productService::deleteProducts);
                    break;
                case "3.1":
                    loadDataAndThen(customerService::addNewCustomers);
                    break;
                case "3.2":
                    loadDataAndThen(customerService::updateCustomers);
                    break;
                case "3.3":
                    loadDataAndThen(customerService::deleteCustomers);
                    break;
                case "4.1":
                    loadDataAndThen(orderService::addNewOrders);
                    break;
                case "4.2":
                    loadDataAndThen(orderService::updateOrders);
                    break;
                case "4.3":
                    orderService.loadOrders();
                    orderService.deleteOrders();
                    break;
                case "5.1":
                    productService.loadProducts();
                    orderService.loadOrders();
                    searchService.searchToProduct();
                    break;
                case "5.2":
                    productService.loadProducts();
                    orderService.loadOrders();
                    searchService.searchOrdersByProductId();
                    break;
                default:
                    System.out.println("Invalid function code. Please try again.");
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("An error occurred while processing tasks: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}
