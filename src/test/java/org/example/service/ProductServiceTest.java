package org.example.service;

import org.example.common.FileProcessor;
import org.example.model.Product;
import org.example.service.ProductService;
import org.example.validate.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private FileProcessor<Product> fileProcessor;

    @Mock
    private ProductValidator productValidator;

    private ProductService productService;

    private static final String FOLDER_PATH = "test/folder";

    @BeforeEach
    void setUp() {
        productService = new ProductService(FOLDER_PATH) {
            @Override
            public String createHeader() {
                return "ID,Name,Price,Stock";
            }
        };
        try {
            var field = ProductService.class.getDeclaredField("fileProcessor");
            field.setAccessible(true);
            field.set(productService, fileProcessor);

            field = ProductService.class.getDeclaredField("productValidator");
            field.setAccessible(true);
            field.set(productService, productValidator);
        } catch (Exception e) {
            fail("Failed to inject mocks: " + e.getMessage());
        }
    }

    @Test
    void testLoadProductsSuccess() {
        List<String[]> mockData = new ArrayList<>();
        mockData.add(new String[]{"ID", "Name", "Price", "Stock"});
        mockData.add(new String[]{"1", "Product1", "10.0", "100"});
        mockData.add(new String[]{"2", "Product2", "20.0", "200"});

        when(fileProcessor.readFile(anyString())).thenReturn(mockData);

        Collection<Product> products = productService.loadProducts();

        assertEquals(2, products.size());
        assertTrue(products.stream().anyMatch(p -> p.getId().equals("1")
                && p.getName().equals("Product1")
                && p.getPrice() == 10.0
                && p.getStockAvailable() == 100));
    }

    @Test
    void testLoadAndValidProductsSuccess() {
        List<String[]> mockData = new ArrayList<>();
        mockData.add(new String[]{"ID", "Name", "Price", "Stock"});
        mockData.add(new String[]{"1", "Product1", "10.0", "100"});

        when(fileProcessor.readFile(anyString())).thenReturn(mockData);
        doNothing().when(productValidator).validateId(anyString(), anyBoolean(), anyBoolean());
        doNothing().when(productValidator).validateName(anyString());
        doNothing().when(productValidator).validatePrice(anyString());
        doNothing().when(productValidator).validateStock(anyString());

        Collection<Product> products = productService.loadAndValidProducts();

        assertEquals(1, products.size());
        verify(productValidator).validateId(eq("1"), anyBoolean(), eq(false));
        verify(productValidator).validateName("Product1");
        verify(productValidator).validatePrice("10.0");
        verify(productValidator).validateStock("100");
    }

    @Test
    void testAddNewProductsSuccess() {
        List<String[]> mockData = new ArrayList<>();
        mockData.add(new String[]{"ID", "Name", "Price", "Stock"});
        mockData.add(new String[]{"3", "Product3", "30.0", "300"});

        when(fileProcessor.readFile(anyString())).thenReturn(mockData);
        doNothing().when(productValidator).validateId(anyString(), anyBoolean(), anyBoolean());
        doNothing().when(productValidator).validateName(anyString());
        doNothing().when(productValidator).validatePrice(anyString());
        doNothing().when(productValidator).validateStock(anyString());

        productService.addNewProducts();

        verify(fileProcessor).writeFile(anyString(), anyList(), any(), anyString());
        assertEquals(1, productService.getAllProducts().size());
        assertTrue(productService.getAllProducts().containsKey("3"));
    }

    @Test
    void testUpdateProductsSuccess() {
        List<String[]> initialData = new ArrayList<>();
        initialData.add(new String[]{"ID", "Name", "Price", "Stock"});
        initialData.add(new String[]{"1", "Product1", "10.0", "100"});
        when(fileProcessor.readFile(anyString())).thenReturn(initialData);
        productService.loadProducts();

        List<String[]> updateData = new ArrayList<>();
        updateData.add(new String[]{"ID", "Name", "Price", "Stock"});
        updateData.add(new String[]{"1", "Updated Product", "15.0", "150"});
        when(fileProcessor.readFile(anyString())).thenReturn(updateData);

        productService.updateProducts();

        Product updatedProduct = productService.getAllProducts().get("1");
        assertNotNull(updatedProduct);
        assertEquals("Updated Product", updatedProduct.getName());
        assertEquals(15.0, updatedProduct.getPrice());
        assertEquals(150, updatedProduct.getStockAvailable());
    }

    @Test
    void testDeleteProductsSuccess() {
        List<String[]> initialData = new ArrayList<>();
        initialData.add(new String[]{"ID", "Name", "Price", "Stock"});
        initialData.add(new String[]{"1", "Product1", "10.0", "100"});
        initialData.add(new String[]{"2", "Product2", "20.0", "200"});
        when(fileProcessor.readFile(anyString())).thenReturn(initialData);
        productService.loadProducts();

        List<String[]> deleteData = new ArrayList<>();
        deleteData.add(new String[]{"ID"});
        deleteData.add(new String[]{"1"});
        when(fileProcessor.readFile(anyString())).thenReturn(deleteData);

        productService.deleteProducts();

        assertFalse(productService.getAllProducts().containsKey("1"));
        assertTrue(productService.getAllProducts().containsKey("2"));
        assertEquals(1, productService.getAllProducts().size());
    }

    @Test
    void testLoadProductsWithInvalidData() {
        List<String[]> mockData = new ArrayList<>();
        mockData.add(new String[]{"ID", "Name", "Price", "Stock"});
        mockData.add(new String[]{"1"});

        when(fileProcessor.readFile(anyString())).thenReturn(mockData);

        Collection<Product> products = productService.loadProducts();

        assertTrue(products.isEmpty());
        verify(fileProcessor).writeErrorLog(anyString(), contains("Invalid data length"));
    }

    @Test
    void testLoadProductsWithInvalidNumber() {
        List<String[]> mockData = new ArrayList<>();
        mockData.add(new String[]{"ID", "Name", "Price", "Stock"});
        mockData.add(new String[]{"1", "Product1", "invalid", "100"});

        when(fileProcessor.readFile(anyString())).thenReturn(mockData);

        Collection<Product> products = productService.loadProducts();

        assertTrue(products.isEmpty());
        verify(fileProcessor).writeErrorLog(anyString(), contains("Error on line 1"));
    }
}