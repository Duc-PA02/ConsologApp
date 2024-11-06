package org.mock.service;

import org.mock.validate.CustomerValidator;
import org.mock.common.FileProcessor;
import org.mock.enums.CustomerEnum;
import org.mock.model.Customer;
import org.mock.util.MessageKeys;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CustomerService {
    private final FileProcessor<Customer> fileProcessor;
    private final CustomerValidator customerValidator;
    private final Set<String> existingCustomerIds = ConcurrentHashMap.newKeySet();
    private final Set<String> existingEmails = ConcurrentHashMap.newKeySet();
    private Map<String, Customer> customerMap;

    public CustomerService(String folderPath) {
        this.fileProcessor = new FileProcessor<>(folderPath);
        this.customerValidator = new CustomerValidator();
        this.customerMap = new ConcurrentHashMap<>();
    }

    public synchronized Collection<Customer> loadAndValidCustomers() {
        return loadCustomers(true);
    }

    public synchronized Collection<Customer> loadCustomers() {
        return loadCustomers(false);
    }

    public synchronized void addNewCustomers() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_NEW_CUSTOMER);
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            if (values.length >= CustomerEnum.values().length) {
                String id = values[CustomerEnum.ID.ordinal()];
                String name = values[CustomerEnum.NAME.ordinal()];
                String email = values[CustomerEnum.EMAIL.ordinal()];
                String phoneNumber = values[CustomerEnum.PHONE_NUMBER.ordinal()];
                try {
                    customerValidator.validateId(id, existingCustomerIds.contains(id));
                    customerValidator.validateEmail(email, existingEmails.contains(email));
                    customerValidator.validateName(name);
                    if (customerMap.containsKey(phoneNumber)) {
                        Customer existingCustomer = customerMap.get(phoneNumber);
                        existingCustomer.setId(id);
                        existingCustomer.setName(name);
                        existingCustomer.setEmail(email);
                    } else {
                        customerValidator.validatePhoneNumber(phoneNumber, customerMap.containsKey(phoneNumber));
                        existingCustomerIds.add(id);
                        existingEmails.add(email);
                        Customer customer = new Customer(id, name, email, phoneNumber);
                        customerMap.put(phoneNumber, customer);
                    }
                } catch (IllegalArgumentException e) {
                    handleException(e, i + 1);
                }
            }
        }
        writeCustomersToFile();
    }

    public synchronized void updateCustomers() {
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_EDIT_CUSTOMER);
        List<Customer> nonExistingCustomers = new ArrayList<>();
        processCustomerUpdateData(data, nonExistingCustomers);
        if (!nonExistingCustomers.isEmpty()) {
            handleNonExistingCustomers(nonExistingCustomers);
        }
        writeCustomersToFile();
    }

    public synchronized void deleteCustomers() {
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
            }else {
                String errorMessage = "Customer with phone number " + phoneNumber + " does not exist in the system.";
                fileProcessor.writeErrorLog(MessageKeys.FILE_ERROR, errorMessage);
            }
        });
        existingCustomerIds.removeAll(customerIdsToRemove);
        existingEmails.removeAll(emailsToRemove);
        writeCustomersToFile();
    }

    private Collection<Customer> loadCustomers(boolean validate) {
        resetData();
        List<String[]> data = fileProcessor.readFile(MessageKeys.FILE_PATH_CUSTOMER);
        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            try {
                Customer customer = createCustomerFromValues(values, validate);
                customerMap.put(customer.getPhoneNumber(), customer);
                existingCustomerIds.add(customer.getId());
                existingEmails.add(customer.getEmail());
            } catch (IllegalArgumentException e) {
                handleException(e, i+ 1);
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
                    customerValidator.validatePhoneNumber(phoneNumber, false);
                    Customer existingCustomer = customerMap.get(phoneNumber);
                    if (existingCustomer != null) {
                        if (!existingCustomer.getId().equals(id) && existingCustomerIds.contains(id)) {
                            throw new IllegalArgumentException("ID " + id + " already exists.");
                        }
                        if (!existingCustomer.getEmail().equals(email) && existingEmails.contains(email)) {
                            throw new IllegalArgumentException("Email " + email + " already exists.");
                        }
                        existingCustomer.setId(id);
                        existingCustomer.setName(name);
                        existingCustomer.setEmail(email);
                    } else {
                        Customer nonExistingCustomer = new Customer(id, name, email, phoneNumber);
                        nonExistingCustomers.add(nonExistingCustomer);
                    }
                } catch (IllegalArgumentException e) {
                    handleException(e, i + 1);
                }
            }
        }
    }

    private Set<String> processDeleteCustomerData(List<String[]> data) {
        Set<String> phoneNumbers = new HashSet<>();

        for (int i = 1; i < data.size(); i++) {
            String[] values = data.get(i);
            if (values.length > 0) {
                if (values.length == 1) {
                    String phoneNumber = values[0];
                    if (!phoneNumber.isEmpty()) {
                        phoneNumbers.add(phoneNumber);
                    } else {
                        String errorMessage = "Invalid or missing phone number at line " + (i + 1);
                        fileProcessor.writeErrorLog(MessageKeys.FILE_ERROR, errorMessage);
                    }
                } else {
                    String phoneNumber = values[CustomerEnum.PHONE_NUMBER.ordinal()];
                    if (!phoneNumber.isEmpty()) {
                        phoneNumbers.add(phoneNumber);
                    } else {
                        String errorMessage = "Invalid or missing phone number at line " + (i + 1);
                        fileProcessor.writeErrorLog(MessageKeys.FILE_ERROR, errorMessage);
                    }
                }
            }
        }
        return phoneNumbers;
    }

    private void handleException(IllegalArgumentException e, int lineNumber) {
        String errorMessage = "Error on line " + lineNumber + ": " + e.getMessage();
        fileProcessor.writeErrorLog(MessageKeys.FILE_ERROR, errorMessage);
    }

    private void handleNonExistingCustomers(List<Customer> nonExistingCustomers) {
        for (Customer customer : nonExistingCustomers) {
            String errorMessage = "Customer with phone number " + customer.getPhoneNumber() + " does not exist in the system.";
            fileProcessor.writeErrorLog(MessageKeys.FILE_ERROR, errorMessage);
        }
    }

    public Set<String> getCustomerIds() {
        return existingCustomerIds;
    }

    public void writeCustomersToFile() {
        String header = createHeader();
        fileProcessor.writeFile(MessageKeys.FILE_OUTPUT_CUSTOMER, new ArrayList<>(customerMap.values()), this::formatCustomer, header);
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
