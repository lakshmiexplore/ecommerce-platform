package com.store.customer.service;

import com.store.customer.dto.CustomerRequest;
import com.store.customer.dto.CustomerResponse;
import com.store.customer.entity.Customer;
import com.store.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Customer with email " + request.email() + " already exists");
        }

        Customer customer = Customer.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .address(request.address())
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Customer registered with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CustomerResponse getCustomerById(Long id) {
        return customerRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCreatedAt()
        );
    }
}