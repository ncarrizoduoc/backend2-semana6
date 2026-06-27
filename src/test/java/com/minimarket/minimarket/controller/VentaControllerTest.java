package com.minimarket.minimarket.controller;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.h2console.autoconfigure.H2ConsoleProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;

import com.minimarket.minimarket.config.RolEnum;
import com.minimarket.minimarket.entity.Venta;
import com.minimarket.minimarket.repository.VentaRepository;
import com.minimarket.minimarket.security.config.SecurityConfig;
import com.minimarket.minimarket.security.filter.JwtAuthenticationFilter;
import com.minimarket.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.minimarket.security.util.JwtUtil;
import com.minimarket.minimarket.service.VentaService;

@WebMvcTest(VentaController.class)
@Import(SecurityConfig.class)
public class VentaControllerTestMock {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VentaService ventaService;

    @MockitoBean
    private SuspiciousActivityService suspiciousActivityService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(authorities = {"CAJERO"})
    public void empleadoPuedeVerVentaTest() throws Exception{
        mockMvc.perform(get("/api/ventas"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    public void noEmpleadoNoPuedeVerVentaTest() throws Exception{
        mockMvc.perform(get("/api/ventas"))
            .andExpect(status().isForbidden());
    }

}
