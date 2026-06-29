package com.minimarket.minimarket.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.minimarket.entity.Categoria;
import com.minimarket.minimarket.entity.Inventario;
import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.security.config.SecurityConfig;
import com.minimarket.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.minimarket.security.util.JwtUtil;
import com.minimarket.minimarket.service.impl.InventarioServiceImpl;

@WebMvcTest(InventarioController.class)
@Import(SecurityConfig.class)
public class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventarioServiceImpl inventarioService;

    @MockitoBean
    private SuspiciousActivityService suspiciousActivityService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // Declaracion de objetos
    private Categoria categoria;
    private Producto producto;
    private Inventario inventario;

    @BeforeEach
    void setUp(){
        categoria = new Categoria(Long.valueOf(1), "Abarrotes", new ArrayList<Producto>());
        producto = new Producto(Long.valueOf(1), "Arroz", 12990.0, 10, categoria);
        inventario = new Inventario(Long.valueOf(1), producto, 10, "Entrada",
            Date.valueOf("2026-12-30"));
    }

    @AfterEach
    void tearDown(){
        categoria = null;
        producto = null;
        inventario = null;
    }

    // Prueba que valida que un usuario autorizado (con rol CAJERO) pueda acceder al endpoint
    // [PUT /api/inventario] para editar un inventario
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void usuarioAutorizadoPuedeModificarInventarioTest() throws Exception{
        // Arrange
        when(inventarioService.findById(Long.valueOf(99))).thenReturn(new Inventario());
        when(inventarioService.save(any(Inventario.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        mockMvc.perform(put("/api/inventario/{id}", Long.valueOf(99)) // Se llama al endpoint [PUT /api/inventario/99]
            .contentType(MediaType.APPLICATION_JSON) // Se envia un body formato Json
            .content(new ObjectMapper().writeValueAsString(inventario))) // El body contiene un objeto Inventario valido
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(99))) // Valida que el inventario retornado tenga ID 99
            .andExpect(jsonPath("$.tipoMovimiento").value("Entrada")); // Valida que el nombre del inventario sea el esperado
    }

    // Prueba que valida que un usuario autorizado (con rol CAJERO) llama al endpoint [PUT /api/inventario/{id}]
    // para modificar un inventario que no existe (por ID), recibe un status Not Found
    @Test
    @WithMockUser(authorities = {"CAJERO"}) 
    public void retornaNotFoundSiInventarioModificadoNoExisteTest() throws Exception{
        when(inventarioService.findById(Long.valueOf(99))).thenReturn(null);

        mockMvc.perform(put("/api/inventario/{id}", Long.valueOf(99)) // Se llama al endpoint [PUT /api/inventario/99]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(inventario)))
            .andExpect(status().isNotFound()); // Se espera un status Not Found

    }


    // Prueba que valida que un usuario no autorizado (sin rol CAJERO) no pueda acceder al endpoint
    // [PUT /api/inventario/{id}] para editar un inventario
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeModificarInventarioTest() throws Exception{
        mockMvc.perform(put("/api/inventario/{id}", Long.valueOf(99)) // Se llama al endpoint [PUT /api/inventario/99]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(inventario)))
            .andExpect(status().isForbidden()); // Se espera un codigo 403 (Forbidden)
    }

    // Prueba que verifica que solo un usuario autorizado (rol CAJERO) pueda
    // acceder al endpoint [GET /api/inventarios] para ver los inventarios
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void usuarioAutorizadoPuedeVerInventariosTest() throws Exception{
        // Arrange
        List<Inventario> inventarios = new ArrayList<Inventario>(List.of(inventario));
        when(inventarioService.findAll()).thenReturn(inventarios);

        // Assert
        mockMvc.perform(get("/api/inventario")) // Se llama al endpoint [GET /api/inventario]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$", hasSize(1))) // Se verifica que la lista de inventario retornada tenga 1 elemento
            .andExpect(jsonPath("$[0].id").value(Long.valueOf(1))); // Se verifica que el ID del elemento en la lista sea 1
    }

    // Prueba que valida que un usuario no autorizado (sin rol CAJERO) no pueda
    // acceder al endpoint [GET /api/inventario]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeVerInventariosTest() throws Exception{
        mockMvc.perform(get("/api/inventario")) // Llama el endpoint [GET /api/inventarios]
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }

    // Prueba que valida que si un usuario autorizado (rol CAJERO) llama al endpoint
    // [GET /api/inventario] retorne el inventario buscado por su ID, si existe
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void buscarPorIdRetornaInventarioSiExisteTest() throws Exception{
        // Arrange
        when(inventarioService.findById(Long.valueOf(1))).thenReturn(inventario);

        // Assert
        mockMvc.perform(get("/api/inventario/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/inventario/1]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))) // Se valida el ID del inventario encontrado
            .andExpect(jsonPath("$.tipoMovimiento").value("Entrada")); // Se valida el nombre del inventario encontrado
    }

    // Prueba que valida que si un usuario autorizado (rol CAJERO) llama al endpoint
    // [GET /api/inventario] retorne un body vacio, si el inventario buscado no existe
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void buscarPorIdRetornaNullSiNoExisteTest() throws Exception{
        // Arrange
        when(inventarioService.findById(Long.valueOf(1))).thenReturn(null);

        // Assert
        mockMvc.perform(get("/api/inventario/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/inventario/1]
            .andExpect(status().isNotFound()) // Se espera un status Not Found
            .andExpect(content().string("")); // Se espera un body de respuesta vacio
            
    }

    // Prueba que valida que un usuario autorizado (rol CAJERO) pueda acceder al
    // endpoint [POST /api/inventario] para guardar un inventario
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void usuarioAutorizadoPuedeGuardarInventarioTest() throws Exception{
        // Arrange
        when(inventarioService.save(any(Inventario.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act y Assert
        mockMvc.perform(post("/api/inventario") // Se llama al endpoint [POST /api/inventario]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(inventario)))
            .andExpect(status().isOk()) // Espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))) // Valida el ID del inventario retornado
            .andExpect(jsonPath("$.tipoMovimiento").value("Entrada")); // Valida el nombre del inventario retornado

    }

    // Prueba que valida que un usuario no autorizado (sin rol CAJERO) no pueda acceder
    // al endpoint [POST /api/inventario]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeGuardarInventarioTest() throws Exception{
        mockMvc.perform(post("/api/inventario") // Se llama al endpoint [POST /api/inventario]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(inventario)))
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }

    // Prueba que valida que un usuario autorizado (con rol CAJERO) pueda acceder al endpoint
    // [DELETE /api/inventario] para eliminar un inventario por ID, si el inventario existe
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void usuarioAutorizadoPuedeEliminarInventarioSiExisteTest() throws Exception{
        // Arrange
        when(inventarioService.findById(Long.valueOf(1))).thenReturn(inventario);

        // Act y Assert
        mockMvc.perform(delete("/api/inventario/{id}", Long.valueOf(1))) // Se llama al endpoint [DELETE /api/inventario/1]
            .andExpect(status().isNoContent()); // Espera un status No Content
    }

    // Prueba que valida que si un usuario autorizado (rol CAJERO) intenta eliminar un inventario
    // que no existe, a traves del endpoint [DELETE /api/inventario/{id}], recibe un status Not Found
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void eliminarInventarioRetornaNotFoundSiNoExisteTest() throws Exception{
        // Arrange
        when(inventarioService.findById(Long.valueOf(1))).thenReturn(null);

        // Act y Assert
        mockMvc.perform(delete("/api/inventario/{id}", Long.valueOf(1))) // Se llama al endpoint [DELETE /api/inventario/1]
            .andExpect(status().isNotFound()); // Espera un status Not Found
    }

    // Prueba que valida que un usuario no autorizado no pueda acceder al endpoint [DELETE /api/inventario/{id}]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeEliminarInventarioTest() throws Exception{
        mockMvc.perform(delete("/api/inventario/{id}", Long.valueOf(1))) // Llama al endpoint [DELETE /api/inventario/1]
            .andExpect(status().isForbidden()); // Espera un status Forbidden
    }




}
