package com.compania.customerorder.service;

import com.compania.customerorder.dto.CustomerDTO;
import com.compania.customerorder.entity.Customer;
import com.compania.customerorder.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerDTO> getActiveCustomers() {
        return customerRepository.findByActivoTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        return toDTO(customer);
    }

    public CustomerDTO createCustomer(CustomerDTO dto) {
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Ya existe un cliente con el email: " + dto.getEmail());
        }

        Customer customer = Customer.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .direccion(dto.getDireccion())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        Customer saved = customerRepository.save(customer);
        return toDTO(saved);
    }

    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        customer.setNombre(dto.getNombre());
        customer.setApellido(dto.getApellido());
        customer.setEmail(dto.getEmail());
        customer.setTelefono(dto.getTelefono());
        customer.setDireccion(dto.getDireccion());

        if (dto.getActivo() != null) {
            customer.setActivo(dto.getActivo());
        }

        Customer updated = customerRepository.save(customer);
        return toDTO(updated);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }
        customerRepository.deleteById(id);
    }

    private CustomerDTO toDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .nombre(customer.getNombre())
                .apellido(customer.getApellido())
                .email(customer.getEmail())
                .telefono(customer.getTelefono())
                .direccion(customer.getDireccion())
                .activo(customer.getActivo())
                .createdAt(customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : null)
                .updatedAt(customer.getUpdatedAt() != null ? customer.getUpdatedAt().toString() : null)
                .totalPedidos(customer.getOrders() != null ? customer.getOrders().size() : 0)
                .build();
    }
}
