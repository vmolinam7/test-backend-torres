package com.compania.customerorder.controller;

import com.compania.customerorder.dto.CustomerDTO;
import com.compania.customerorder.security.JwtAuthenticationFilter;
import com.compania.customerorder.security.JwtService;
import com.compania.customerorder.security.SecurityConfig;
import com.compania.customerorder.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(SecurityConfig.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customerDTO = CustomerDTO.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@test.com")
                .telefono("3001234567")
                .direccion("Calle 123")
                .activo(true)
                .totalPedidos(0)
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/customers - Listar todos los clientes")
    void getAllCustomers_Returns200() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(Arrays.asList(customerDTO));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan"))
                .andExpect(jsonPath("$[0].email").value("juan@test.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/customers?activo=true - Listar clientes activos")
    void getActiveCustomers_Returns200() throws Exception {
        when(customerService.getActiveCustomers()).thenReturn(Arrays.asList(customerDTO));

        mockMvc.perform(get("/api/customers").param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/customers/{id} - Obtener cliente por ID")
    void getCustomerById_Returns200() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(customerDTO);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/customers - Crear cliente")
    void createCustomer_Returns201() throws Exception {
        when(customerService.createCustomer(any(CustomerDTO.class))).thenReturn(customerDTO);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/customers - Datos inválidos retorna 400")
    void createCustomer_InvalidData_Returns400() throws Exception {
        CustomerDTO invalidDTO = CustomerDTO.builder()
                .nombre("")
                .apellido("")
                .email("invalid")
                .build();

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/customers/{id} - Actualizar cliente")
    void updateCustomer_Returns200() throws Exception {
        when(customerService.updateCustomer(eq(1L), any(CustomerDTO.class))).thenReturn(customerDTO);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/customers/{id} - Eliminar cliente")
    void deleteCustomer_Returns204() throws Exception {
        doNothing().when(customerService).deleteCustomer(1L);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/customers sin autenticación retorna 403")
    void getAllCustomers_NoAuth_Returns403() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isForbidden());
    }
}
