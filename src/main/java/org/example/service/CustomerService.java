package org.example.service;

import org.example.common.CustomerValidator;
import org.example.common.FileProcessor;
import org.example.enums.CustomerEnum;
import org.example.model.Customer;
import org.example.util.MessageKeys;

import java.io.IOException;
import java.util.*;

public class CustomerService {
    private final FileProcessor<Customer> fileProcessor;
    private final CustomerValidator customerValidator;
    private final Set<String> existingCustomerIds;
    private Map<String, Customer> customerMap;

    public CustomerService() {
        this.fileProcessor = new FileProcessor<>();
        this.customerValidator = new CustomerValidator();
        this.existingCustomerIds = new HashSet<>();
        this.customerMap = new HashMap<>();
    }

    public List<Customer> loadCustomers() throws IOException {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_CUSTOMER);
        processCustomerData(data);
        return new ArrayList<>(customerMap.values());
    }

    public void addNewCustomers() throws IOException {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_NEW_CUSTOMER);
        processCustomerData(data);
        writeCustomersToFile();
    }

    public void updateCustomers() throws IOException {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_EDIT_CUSTOMER);
        List<Customer> nonExistingCustomers = new ArrayList<>();
        processCustomerUpdateData(data, nonExistingCustomers);

        if (!nonExistingCustomers.isEmpty()) {
            writeNonExistingCustomersToFile(nonExistingCustomers);
        }

        writeCustomersToFile();
    }

    public void deleteCustomers() throws IOException {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_DELETE_CUSTOMER);
        Set<String> phoneNumbers = processDeleteCustomerData(data);

        phoneNumbers.forEach(customerMap::remove);
        existingCustomerIds.removeAll(phoneNumbers);
        writeCustomersToFile();
    }

    private void processCustomerData(List<String[]> data) {
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            if (values.length >= CustomerEnum.values().length) {
                String id = values[CustomerEnum.ID.ordinal()];
                String name = values[CustomerEnum.NAME.ordinal()];
                String email = values[CustomerEnum.EMAIL.ordinal()];
                String phoneNumber = values[CustomerEnum.PHONE_NUMBER.ordinal()];

                try {
                    customerValidator.validateId(id, existingCustomerIds.contains(id));
                    customerValidator.validateName(name);
                    customerValidator.validateEmail(email);
                    customerValidator.validatePhoneNumber(phoneNumber, customerMap.containsKey(phoneNumber));

                    existingCustomerIds.add(id);

                    Customer customer = new Customer(id, name, email, phoneNumber);
                    customerMap.put(phoneNumber, customer);
                } catch (IllegalArgumentException e) {
                    handleException(e);
                }
            }
        }
    }

    private void processCustomerUpdateData(List<String[]> data, List<Customer> nonExistingCustomers) {
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);

            if (values.length >= CustomerEnum.values().length) {
                String id = values[CustomerEnum.ID.ordinal()];
                String name = values[CustomerEnum.NAME.ordinal()];
                String email = values[CustomerEnum.EMAIL.ordinal()];
                String phoneNumber = values[CustomerEnum.PHONE_NUMBER.ordinal()];

                try {
                    customerValidator.validateName(name);
                    customerValidator.validateEmail(email);
                    customerValidator.validatePhoneNumber(phoneNumber, false);

                    Customer existingCustomer = customerMap.get(phoneNumber);

                    if (existingCustomer != null) {
                        existingCustomer.setName(name);
                        existingCustomer.setEmail(email);
                    } else {
                        Customer nonExistingCustomer = new Customer(id, name, email, phoneNumber);
                        nonExistingCustomers.add(nonExistingCustomer);
                    }
                } catch (IllegalArgumentException e) {
                    handleException(e);
                }
            }
        }
    }

    private Set<String> processDeleteCustomerData(List<String[]> data) {
        Set<String> phoneNumbers = new HashSet<>();

        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);

            if (values.length > 0) {
                String phoneNumber = values[CustomerEnum.PHONE_NUMBER.ordinal()].trim();
                try {
                    customerValidator.validatePhoneNumber(phoneNumber, customerMap.containsKey(phoneNumber));
                    if (!phoneNumber.isEmpty()) {
                        phoneNumbers.add(phoneNumber);
                    }
                } catch (IllegalArgumentException e){
                    handleException(e);
                }

            }
        }

        return phoneNumbers;
    }

    private void handleException(IllegalArgumentException e) {
        fileProcessor.writeErrorLog(MessageKeys.FILE_ERROR, e.getMessage());
        System.out.println("An error occurred: " + e.getMessage());
    }

    public Set<String> getCustomerIds() {
        return existingCustomerIds;
    }

    public void writeCustomersToFile() throws IOException {
        String header = createHeader();
        fileProcessor.writeFile(MessageKeys.FILE_OUTPUT_CUSTOMER, new ArrayList<>(customerMap.values()), this::formatCustomer, header);
    }

    private void writeNonExistingCustomersToFile(List<Customer> nonExistingCustomers) throws IOException {
        String header = createHeader();
        fileProcessor.writeFile(MessageKeys.FILE_NON_EXISTENT_CUSTOMER, nonExistingCustomers, this::formatCustomer, header);
    }

    private String createHeader() {
        return String.join(MessageKeys.CHARACTER,
                CustomerEnum.ID.getHeader(),
                CustomerEnum.NAME.getHeader(),
                CustomerEnum.EMAIL.getHeader(),
                CustomerEnum.PHONE_NUMBER.getHeader());
    }

    private String formatCustomer(Customer customer) {
        return String.join(MessageKeys.CHARACTER,
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhoneNumber());
    }
}
