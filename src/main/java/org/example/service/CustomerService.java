package org.example.service;

import org.example.validate.CustomerValidator;
import org.example.common.FileProcessor;
import org.example.enums.CustomerEnum;
import org.example.model.Customer;
import org.example.util.MessageKeys;

import java.util.*;

public class CustomerService {
    private final FileProcessor<Customer> fileProcessor;
    private final CustomerValidator customerValidator;
    private final Set<String> existingCustomerIds;
    private final Set<String> existingEmails;
    private Map<String, Customer> customerMap;

    public CustomerService(String folderPath) {
        this.fileProcessor = new FileProcessor<>(folderPath);
        this.customerValidator = new CustomerValidator();
        this.existingCustomerIds = new HashSet<>();
        this.customerMap = new HashMap<>();
        this.existingEmails = new HashSet<>();
    }

    public Collection<Customer> loadAndValidCustomers() {
        return loadCustomers(true);
    }

    public Collection<Customer> loadCustomers() {
        return loadCustomers(false);
    }

    public void addNewCustomers() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_NEW_CUSTOMER);
        List<Customer> newCustomers = new ArrayList<>();
        processCustomerUpdateData(data, newCustomers, true);
        writeCustomersToFile();
    }

    public void updateCustomers() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_EDIT_CUSTOMER);
        List<Customer> nonExistingCustomers = new ArrayList<>();
        processCustomerUpdateData(data, nonExistingCustomers, false);

        if (!nonExistingCustomers.isEmpty()) {
            writeNonExistingCustomersToFile(nonExistingCustomers);
        }

        writeCustomersToFile();
    }

    public void deleteCustomers() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_DELETE_CUSTOMER);
        Set<String> phoneNumbers = processDeleteCustomerData(data);

        Set<String> customerIdsToRemove = new HashSet<>();
        Set<String> emailsToRemove = new HashSet<>();

        phoneNumbers.forEach(phoneNumber -> {
            Customer customer = customerMap.get(phoneNumber);
            if (customer != null) {
                customerIdsToRemove.add(customer.getId());
                emailsToRemove.add(customer.getEmail());
                customerMap.remove(phoneNumber);
            }
        });

        existingCustomerIds.removeAll(customerIdsToRemove);
        existingEmails.removeAll(emailsToRemove);

        writeCustomersToFile();
    }

    private Collection<Customer> loadCustomers(boolean validate) {
        customerMap.clear();
        existingCustomerIds.clear();
        existingEmails.clear();
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_CUSTOMER);

        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            try {
                Customer customer = createCustomerFromValues(values, validate);
                customerMap.put(customer.getPhoneNumber(), customer);
                existingCustomerIds.add(customer.getId());
                existingEmails.add(customer.getEmail());
            } catch (IllegalArgumentException e) {
                handleException(e);
            }
        }
        return customerMap.values();
    }

    private Customer createCustomerFromValues(String[] values, boolean validate) {
        if (values.length < CustomerEnum.values().length) {
            throw new IllegalArgumentException("Invalid data length");
        }

        String id = values[CustomerEnum.ID.ordinal()];
        String name = values[CustomerEnum.NAME.ordinal()];
        String email = values[CustomerEnum.EMAIL.ordinal()];
        String phoneNumber = values[CustomerEnum.PHONE_NUMBER.ordinal()];

        if (validate) {
            customerValidator.validateId(id, existingCustomerIds.contains(id));
            customerValidator.validateName(name);
            customerValidator.validateEmail(email, existingEmails.contains(email));
            customerValidator.validatePhoneNumber(phoneNumber, customerMap.containsKey(phoneNumber));
        }

        return new Customer(id, name, email, phoneNumber);
    }

    private void processCustomerUpdateData(List<String[]> data, List<Customer> resultCustomers, boolean isAddOperation) {
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);

            if (values.length >= CustomerEnum.values().length) {
                String id = values[CustomerEnum.ID.ordinal()];
                String name = values[CustomerEnum.NAME.ordinal()];
                String email = values[CustomerEnum.EMAIL.ordinal()];
                String phoneNumber = values[CustomerEnum.PHONE_NUMBER.ordinal()];

                Customer existingCustomer = customerMap.get(phoneNumber);

                if (existingCustomer != null) {
                    existingCustomer.setName(name);
                    existingCustomer.setEmail(email);
                } else if (isAddOperation) {
                    Customer newCustomer = new Customer(id, name, email, phoneNumber);
                    customerMap.put(phoneNumber, newCustomer);
                    existingCustomerIds.add(id);
                    existingEmails.add(email);
                } else {
                    Customer nonExistingCustomer = new Customer(id, name, email, phoneNumber);
                    resultCustomers.add(nonExistingCustomer);
                }
            }
        }
    }

    private Set<String> processDeleteCustomerData(List<String[]> data) {
        Set<String> phoneNumbers = new HashSet<>();

        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);

            if (values.length > 0) {
                String phoneNumber = values[CustomerEnum.PHONE_NUMBER.ordinal()];
                if (!phoneNumber.isEmpty()) {
                    phoneNumbers.add(phoneNumber);
                }
            }
        }
        return phoneNumbers;
    }

    private void handleException(IllegalArgumentException e) {
        fileProcessor.writeErrorLog(MessageKeys.FILE_ERROR, "An error occurred: " + e.getMessage());
    }

    public Set<String> getCustomerIds() {
        return existingCustomerIds;
    }

    public void writeCustomersToFile() {
        String header = createHeader();
        fileProcessor.writeFile(MessageKeys.FILE_OUTPUT_CUSTOMER, new ArrayList<>(customerMap.values()), this::formatCustomer, header);
    }

    private void writeNonExistingCustomersToFile(List<Customer> nonExistingCustomers) {
        String header = createHeader();
        fileProcessor.writeFile(MessageKeys.FILE_ERROR, nonExistingCustomers, this::formatCustomer, header);
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

    private void resetData(){
        existingCustomerIds.clear();
        existingEmails.clear();
        customerMap.clear();
    }
}
