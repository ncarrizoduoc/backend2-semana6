package com.minimarket.minimarket.security.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotNull(message = "Debe ingresar un nombre de usuario")
    private String username;
    @NotNull(message = "Debe ingresar una contrasena")
    private String password;
}
