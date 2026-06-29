package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.entity.Producto;
import com.minimarket.minimarket.service.ProductoService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.minimarket.minimarket.security.util.InputSanitizer.*;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Producto> listarProductos() {
        return productoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        return (producto != null) ? ResponseEntity.ok(producto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Producto guardarProducto(@Valid @RequestBody Producto producto) {
        sanitizarProducto(producto);
        return productoService.save(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        sanitizarProducto(producto);
        Producto productoExistente = productoService.findById(id);
        if (productoExistente != null) {
            producto.setId(id);
            return ResponseEntity.ok(productoService.save(producto));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private void sanitizarProducto(Producto producto){
        producto.setNombre(sanitizeInput(producto.getNombre()));
    }
}
