package com.minimarket.minimarket.security.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.minimarket.security.config.SecurityConfig;
import com.minimarket.minimarket.security.model.LoginRequest;
import com.minimarket.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.minimarket.security.util.JwtUtil;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private SuspiciousActivityService suspiciousActivityService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private Authentication auth;

    // Declaracion de variables
    private LoginRequest request;

    @BeforeEach
    void setUp(){
        request = new LoginRequest("username", "password");
    }

    @AfterEach
    void tearDown(){
        request = null;
    }

    // Prueba que valida que el AuthController retorne un JWT cuando el usuario se
    // autentique con credenciales validas (usuario y contrasena)
    @Test
    public void loginConUsuarioValidoRetornaJwt() throws Exception{
        // Arrange
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("username", "password")))
            .thenReturn(auth);
        when(jwtUtil.generateToken("username")).thenReturn("token");

        // Act y Assert
        mockMvc.perform(post("/auth/login") // Se llama al endpoint [POST /auth/login]
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isOk()) // Debe retornar un codigo 200
            .andExpect(jsonPath("$.token").value("token")); // Se verifica que el body de respuesta incluya el JWT
    }

    // Prueba que valida que el AuthController responda con un status Unauthorized cuando
    // el usuario intente autenticarse con credenciales invalidas
    @Test
    public void loginConUsuarioInvalidoNoPermiteAcceso() throws Exception{
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(BadCredentialsException.class);

        // Act y Assert
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isUnauthorized()); // Se espera un status Unauthorized
    }

    // Prueba que valida que el endpoint [POST /auth/login] retorne un status Bad Request
    // si se envia un LoginRequest con username null
    @Test
    public void loginSinUsernameRetornaBadRequest() throws Exception{
        request.setUsername(null); // Se deja username como null (no permitido)
        
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Espera status Bad Request
    }

    // Prueba que valida que el endpoint [POST /auth/login] retorne un status Bad Request
    // si se envia un LoginRequest con password null
    @Test
    public void loginSinPasswordRetornaBadRequest() throws Exception{
        request.setPassword(null); // Se deja la contrasena como null (no permitido)
        
        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Espera status Bad Request
    }

}
