package org.mock.service;

import org.mock.common.FileProcessor;
import org.mock.model.Customer;
import org.mock.validate.CustomerValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private FileProcessor<Customer> fileProcessor;

    @Mock
    private CustomerValidator customerValidator;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService("./");
        try {
            var field = CustomerService.class.getDeclaredField("fileProcessor");
            field.setAccessible(true);
            field.set(customerService, fileProcessor);

            field = CustomerService.class.getDeclaredField("customerValidator");
            field.setAccessible(true);
            field.set(customerService, customerValidator);
        } catch (Exception e) {
            fail("Failed to inject mocks: " + e.getMessage());
        }
    }

    @Test
    void testLoadCustomersSuccess() {
        List<String[]> mockData = new ArrayList<>();
        mockData.add(new String[]{"ID", "Name", "Email", "PhoneNumber"});
        mockData.add(new String[]{"C001", "John Doe", "john@email.com", "1234567890"});
        mockData.add(new String[]{"C002", "Jane Doe", "jane@email.com", "0987654321"});

        when(fileProcessor.readFile(anyString())).thenReturn(mockData);

        Collection<Customer> customers = customerService.loadCustomers();

        assertEquals(2, customers.size());
        assertTrue(customers.stream().anyMatch(c ->
                c.getId().equals("C001") &&
                        c.getName().equals("John Doe") &&
                        c.getEmail().equals("john@email.com") &&
                        c.getPhoneNumber().equals("1234567890")
        ));
    }

    @Test
    void testLoadAndValidCustomersSuccess() {
        List<String[]> mockData = new ArrayList<>();
        mockData.add(new String[]{"ID", "Name", "Email", "PhoneNumber"});
        mockData.add(new String[]{"C001", "John Doe", "john@email.com", "1234567890"});

        when(fileProcessor.readFile(anyString())).thenReturn(mockData);
        doNothing().when(customerValidator).validateId(anyString(), anyBoolean());
        doNothing().when(customerValidator).validateName(anyString());
        doNothing().when(customerValidator).validateEmail(anyString(), anyBoolean());
        doNothing().when(customerValidator).validatePhoneNumber(anyString(), anyBoolean());

        Collection<Customer> customers = customerService.loadAndValidCustomers();

        assertEquals(1, customers.size());
        verify(customerValidator).validateId(eq("C001"), anyBoolean());
        verify(customerValidator).validateName("John Doe");
        verify(customerValidator).validateEmail(eq("john@email.com"), anyBoolean());
        verify(customerValidator).validatePhoneNumber(eq("1234567890"), anyBoolean());
    }

    @Test
    void testAddNewCustomersSuccess() {
        List<String[]> mockData = new ArrayList<>();
        mockData.add(new String[]{"ID", "Name", "Email", "PhoneNumber"});
        mockData.add(new String[]{"C003", "Bob Smith", "bob@email.com", "5555555555"});

        when(fileProcessor.readFile(anyString())).thenReturn(mockData);
        doNothing().when(customerValidator).validateId(anyString(), anyBoolean());
        doNothing().when(customerValidator).validateName(anyString());
        doNothing().when(customerValidator).validateEmail(anyString(), anyBoolean());
        doNothing().when(customerValidator).validatePhoneNumber(anyString(), anyBoolean());

        customerService.addNewCustomers();

        verify(fileProcessor).writeFile(anyString(), anyList(), any(), anyString());
        assertTrue(customerService.getCustomerIds().contains("C003"));
    }

    @Test
    void testUpdateCustomersSuccess() {
        List<String[]> initialData = new ArrayList<>();
        initialData.add(new String[]{"ID", "Name", "Email", "PhoneNumber"});
        initialData.add(new String[]{"C001", "John Doe", "john@email.com", "1234567890"});
        when(fileProcessor.readFile(anyString())).thenReturn(initialData);
        customerService.loadCustomers();

        List<String[]> updateData = new ArrayList<>();
        updateData.add(new String[]{"ID", "Name", "Email", "PhoneNumber"});
        updateData.add(new String[]{"C001", "John Updated", "john.updated@email.com", "1234567890"});
        when(fileProcessor.readFile(anyString())).thenReturn(updateData);
        doNothing().when(customerValidator).validateName(anyString());
        doNothing().when(customerValidator).validatePhoneNumber(anyString(), anyBoolean());

        customerService.updateCustomers();

        verify(fileProcessor, times(2)).readFile(anyString());
        verify(fileProcessor).writeFile(anyString(), anyList(), any(), anyString());
    }

    @Test
    void testDeleteCustomersSuccess() {
        List<String[]> initialData = new ArrayList<>();
        initialData.add(new String[]{"ID", "Name", "Email", "PhoneNumber"});
        initialData.add(new String[]{"C001", "John Doe", "john@email.com", "1234567890"});
        initialData.add(new String[]{"C002", "Jane Doe", "jane@email.com", "0987654321"});
        when(fileProcessor.readFile(anyString())).thenReturn(initialData);
        customerService.loadCustomers();

        List<String[]> deleteData = new ArrayList<>();
        deleteData.add(new String[]{"PhoneNumber"});
        deleteData.add(new String[]{"1234567890"});
        when(fileProcessor.readFile(anyString())).thenReturn(deleteData);

        customerService.deleteCustomers();

        assertFalse(customerService.getCustomerIds().contains("C001"));
        assertTrue(customerService.getCustomerIds().contains("C002"));
    }

    @Test
    void testLoadCustomersWithInvalidData() {
        List<String[]> mockData = new ArrayList<>();
        mockData.add(new String[]{"ID", "Name", "Email", "PhoneNumber"});
        mockData.add(new String[]{"C001"});

        when(fileProcessor.readFile(anyString())).thenReturn(mockData);

        Collection<Customer> customers = customerService.loadCustomers();

        assertTrue(customers.isEmpty());
        verify(fileProcessor).writeErrorLog(anyString(), contains("Invalid data length"));
    }

    @Test
    void testUpdateNonExistingCustomer() {
        List<String[]> updateData = new ArrayList<>();
        updateData.add(new String[]{"ID", "Name", "Email", "PhoneNumber"});
        updateData.add(new String[]{"C999", "Non Existing", "non@email.com", "9999999999"});

        when(fileProcessor.readFile(anyString())).thenReturn(updateData);
        doNothing().when(customerValidator).validateName(anyString());
        doNothing().when(customerValidator).validatePhoneNumber(anyString(), anyBoolean());

        customerService.updateCustomers();

        verify(fileProcessor).writeErrorLog(anyString(), contains("does not exist in the system"));
    }

    @Test
    void testDeleteNonExistingCustomer() {
        List<String[]> deleteData = new ArrayList<>();
        deleteData.add(new String[]{"PhoneNumber"});
        deleteData.add(new String[]{"9999999999"});

        when(fileProcessor.readFile(anyString())).thenReturn(deleteData);

        customerService.deleteCustomers();

        verify(fileProcessor).writeErrorLog(anyString(), contains("does not exist in the system"));
    }

    @Test
    void testAddCustomerWithDuplicateId() {
        List<String[]> initialData = new ArrayList<>();
        initialData.add(new String[]{"ID", "Name", "Email", "PhoneNumber"});
        initialData.add(new String[]{"C001", "John Doe", "john@email.com", "1234567890"});
        when(fileProcessor.readFile(anyString())).thenReturn(initialData);
        customerService.loadCustomers();

        List<String[]> newData = new ArrayList<>();
        newData.add(new String[]{"ID", "Name", "Email", "PhoneNumber"});
        newData.add(new String[]{"C001", "Another John", "another@email.com", "1111111111"});
        when(fileProcessor.readFile(anyString())).thenReturn(newData);

        doThrow(new IllegalArgumentException("ID already exists"))
                .when(customerValidator).validateId(eq("C001"), eq(true));

        customerService.addNewCustomers();

        verify(fileProcessor).writeErrorLog(anyString(), contains("ID already exists"));
    }
}