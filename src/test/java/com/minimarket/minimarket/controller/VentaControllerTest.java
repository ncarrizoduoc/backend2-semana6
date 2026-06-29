package com.minimarket.minimarket.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.minimarket.entity.DetalleVenta;
import com.minimarket.minimarket.entity.Rol;
import com.minimarket.minimarket.entity.Usuario;
import com.minimarket.minimarket.entity.Venta;
import com.minimarket.minimarket.security.config.SecurityConfig;
import com.minimarket.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.minimarket.security.util.JwtUtil;
import com.minimarket.minimarket.service.impl.VentaServiceImpl;


@WebMvcTest(VentaController.class)
@Import(SecurityConfig.class)
public class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VentaServiceImpl ventaService;

    @MockitoBean
    private SuspiciousActivityService suspiciousActivityService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // Declaracion de objetos
    Rol rol;
    Set<Rol> roles;
    Usuario usuario;
    List<DetalleVenta> detalles;
    private Venta venta;

    // Se crean objetos (de clase Venta y otros) para probar las llamadas a endpoints
    @BeforeEach
    void setUp(){
        // Rol
        rol = new Rol(Long.valueOf(1), "CLIENTE", new HashSet<Usuario>());
        roles = new HashSet<>(Set.of(rol));
        //Usuario
        usuario = new Usuario(Long.valueOf(1), "UsuarioPrueba", "ContrasenaPrueba", roles);
        // Lista de detalles de ventas (vacia)
        detalles = new ArrayList<>();
        //Objeto Venta (para probar endpoint POST)
        venta = new Venta(Long.valueOf(1), usuario, Date.valueOf("2026-12-30"), detalles);
    }

    // Despues de cada prueba, se asigna null a los objetos para liberar espacio
    @AfterEach
    void tearDown(){
        rol = null;
        roles = null;
        usuario = null;
        detalles = null;
        venta = null;
    }

    // Prueba que verifica que un usuario con rol CAJERO pueda acceder al endpoint [POST /api/ventas] y
    // guardar una venta (debe incluir un RequestBody con una venta valida)
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void cajeroPuedeGenerarVentaTest() throws Exception{
        // Arrange
        when(ventaService.save(any(Venta.class))).thenAnswer(invocation ->{
            return invocation.getArgument(0);
        });

        // Act y Assert
        mockMvc.perform(post("/api/ventas") // Se llama al endpoint [POST /api/ventas]
            .contentType(MediaType.APPLICATION_JSON) // Se envia un body formato Json
            .content(new ObjectMapper().writeValueAsString(venta))) // El body contiene un objeto Venta valido
            .andExpect(status().isOk()) // Verificar que retorna un status OK
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))) // Verificar que el ID de la venta sea el esperado
            .andExpect(jsonPath("$.detalles").value(detalles)); // Verificar que los detalles de venta sean los esperados
    }

    // Prueba que verifica que un usuario no autorizado (sin rol CAJERO) no pueda acceder
    // al endpoint [POST /api/ventas] para registrar una venta, pues el endpoint solo admite
    // usuarios con rol CAJERO
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeGuardarVentaTest() throws Exception{
        mockMvc.perform(post("/api/ventas") // Se llama al endpoint [POST /api/ventas]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(venta))) // El body contiene un objeto Venta valido
            .andExpect(status().isForbidden()); // Espera un Status 403 (prohibido)
    }

    // Prueba que verifica que un usuario con rol CAJERO pueda ver las ventas en sistema
    // llamando al endpoint [GET /api/ventas]
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void cajeroPuedeVerVentasTest() throws Exception{
        // Arrange
        List<Venta> ventas = new ArrayList<Venta>(List.of(venta));
        when(ventaService.findAll()).thenReturn(ventas);

        // Assert
        mockMvc.perform(get("/api/ventas")) // Se llama al endpoint [GET /api/ventas]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$", hasSize(1))) // Se verifica que la lista de ventas retornada tenga 1 elemento
            .andExpect(jsonPath("$[0].id").value(Long.valueOf(1))); // Se verifica que el ID del primer elemento sea 1
    }

    // Prueba que verifica que un usuario no autorizado (sin rol CAJERO) no pueda ver el listado de ventas
    // llamando al endpoint [GET /api/ventas]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeVerVentasTest() throws Exception{
        mockMvc.perform(get("/api/ventas")) // Se llama al endpoint [GET /api/ventas]
            .andExpect(status().isForbidden()); // Se espera un codigo 403 (Forbidden)
    }

    // Prueba que verifica que si un usuario con rol CAJERO llama al endpoint [GET /api/ventas]
    // para buscar una venta por ID, retorne la venta (cuando exista)
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void cajeroPuedeBuscarVentaExistentePorIdTest() throws Exception{
        // Arrange
        when(ventaService.findById(Long.valueOf(1))).thenReturn(venta);

        // Act y Assert
        mockMvc.perform(get("/api/ventas/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/ventas/1]
            .andExpect(status().isOk()) // Se espera un codigo 200 (OK)
            .andExpect(jsonPath("$.id").value(Long.valueOf(1))); // Se verifica que el ID de la venta retornada sea 1
    }

    // Metodo que verifica que si un usuario con rol CAJERO llama al endpoint [GET /api/ventas]
    // para buscar una venta por ID, y la venta no existe, recibe una respuesta vacia
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void BuscarVentaPorIdRetornaNullSiNoExisteTest() throws Exception{
        // Arrange
        when(ventaService.findById(Long.valueOf(1))).thenReturn(null);

        // Act y Assert
        mockMvc.perform(get("/api/ventas/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/ventas/1]
            .andExpect(status().isNotFound()) // Se espera un codigo 404 (NotFound)
            .andExpect(content().string("")); // Se verifica que el body de la respuesta este vacio
    }
    
    // Prueba que valida que un usuario no autorizado (sin rol CAJERO) no pueda acceder al 
    // endpoint [GET /api/ventas]
    @Test
    @WithAnonymousUser
    public void usuarioNoAutorizadoNoPuedeBuscarVenta() throws Exception{
        mockMvc.perform(get("/api/ventas/{id}", Long.valueOf(1))) // Se llama al endpoint [GET /api/ventas/1]
            .andExpect(status().isForbidden()); // Se espera un codigo 403 (Forbidden)
    }

    // Prueba que valida que el endpoint [POST /api/ventas] retorne Bad Request si el usuario adjunta
    // una Venta no valida (que no cumple con las restricciones de datos implementadas en la clase
    // Venta) en el body de la solicitud. Espera como respuesta un status Bad Request
    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void guardarVentaNoValidaLanzaErrorTest() throws Exception{
        venta.setFecha(null); // Se asigna fecha null (que no esta permitido)

        mockMvc.perform(post("/api/ventas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(venta)))
            .andExpect(status().isBadRequest()); // Se espera un status Bad Request

    }

}
