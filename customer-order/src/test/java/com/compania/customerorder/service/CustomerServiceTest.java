package com.compania.customerorder.service;

import com.compania.customerorder.dto.CustomerDTO;
import com.compania.customerorder.entity.Customer;
import com.compania.customerorder.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@test.com")
                .telefono("3001234567")
                .direccion("Calle 123")
                .activo(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orders(new ArrayList<>())
                .build();

        customerDTO = CustomerDTO.builder()
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@test.com")
                .telefono("3001234567")
                .direccion("Calle 123")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Listar todos los clientes")
    void getAllCustomers_ReturnsList() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(customer));

        List<CustomerDTO> result = customerService.getAllCustomers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getNombre());
        verify(customerRepository).findAll();
    }

    @Test
    @DisplayName("Listar clientes activos")
    void getActiveCustomers_ReturnsList() {
        when(customerRepository.findByActivoTrue()).thenReturn(Arrays.asList(customer));

        List<CustomerDTO> result = customerService.getActiveCustomers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActivo());
        verify(customerRepository).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener cliente por ID existente")
    void getCustomerById_Exists_ReturnsCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerDTO result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals("Juan", result.getNombre());
        assertEquals("juan@test.com", result.getEmail());
    }

    @Test
    @DisplayName("Obtener cliente por ID inexistente lanza excepción")
    void getCustomerById_NotFound_ThrowsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.getCustomerById(99L));

        assertTrue(exception.getMessage().contains("Cliente no encontrado"));
    }

    @Test
    @DisplayName("Crear cliente exitosamente")
    void createCustomer_Success() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerDTO result = customerService.createCustomer(customerDTO);

        assertNotNull(result);
        assertEquals("Juan", result.getNombre());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Crear cliente con email duplicado lanza excepción")
    void createCustomer_DuplicateEmail_ThrowsException() {
        when(customerRepository.existsByEmail("juan@test.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.createCustomer(customerDTO));

        assertTrue(exception.getMessage().contains("Ya existe un cliente"));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Actualizar cliente exitosamente")
    void updateCustomer_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        customerDTO.setNombre("Juan Carlos");
        CustomerDTO result = customerService.updateCustomer(1L, customerDTO);

        assertNotNull(result);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Eliminar cliente exitosamente")
    void deleteCustomer_Success() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        doNothing().when(customerRepository).deleteById(1L);

        assertDoesNotThrow(() -> customerService.deleteCustomer(1L));
        verify(customerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar cliente inexistente lanza excepción")
    void deleteCustomer_NotFound_ThrowsException() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> customerService.deleteCustomer(99L));
        verify(customerRepository, never()).deleteById(any());
    }
}
