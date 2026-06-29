package com.minimarket.minimarket.controller;

import com.minimarket.minimarket.entity.Inventario;
import com.minimarket.minimarket.service.InventarioService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.minimarket.minimarket.security.util.InputSanitizer.*;


@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @GetMapping
    public List<Inventario> listarMovimientosDeInventario() {
        return inventarioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventario> obtenerMovimientoPorId(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        return (inventario != null) ? ResponseEntity.ok(inventario) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Inventario registrarMovimiento(@Valid @RequestBody Inventario inventario) {
        sanitizarInventario(inventario);
        return inventarioService.save(inventario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventario> actualizarMovimiento(@PathVariable Long id, @Valid @RequestBody Inventario inventario) {
        sanitizarInventario(inventario);
        Inventario existente = inventarioService.findById(id);
        if (existente != null) {
            inventario.setId(id);
            return ResponseEntity.ok(inventarioService.save(inventario));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private void sanitizarInventario(Inventario inventario){
        inventario.setTipoMovimiento(sanitizeInput(inventario.getTipoMovimiento()));
    }

}
