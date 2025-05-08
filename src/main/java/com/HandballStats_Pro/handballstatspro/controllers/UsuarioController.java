package com.HandballStats_Pro.handballstatspro.controllers;

import com.HandballStats_Pro.handballstatspro.dto.UsuarioDTO;
import com.HandballStats_Pro.handballstatspro.dto.UsuarioEmailDTO;
import com.HandballStats_Pro.handballstatspro.dto.UsuarioUpdateDTO;
import com.HandballStats_Pro.handballstatspro.entities.Usuario;
import com.HandballStats_Pro.handballstatspro.exceptions.DuplicateResourceException;
import com.HandballStats_Pro.handballstatspro.exceptions.ResourceNotFoundException;
import com.HandballStats_Pro.handballstatspro.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuario(usuarioDTO));
        }  catch (DuplicateResourceException e) {
            throw new DuplicateResourceException("El correo electrónico ya está registrado");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Admin') or #id == authentication.principal.id")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(usuarioService.obtenerPorId(id));
        } catch (ResourceNotFoundException e) {
            throw e;
        }
    }

    @GetMapping("/email")
    public ResponseEntity<UsuarioEmailDTO> findByEmail(@RequestParam String email) {
        try {
            Usuario u = usuarioService.obtenerUsuarioPorEmail(email);
            UsuarioEmailDTO dto = new UsuarioEmailDTO(u.getIdUsuario(), u.getNombre(), u.getEmail(), u.getRol());
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            throw e;
        }
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('Admin') or #id == authentication.principal.id")
    public ResponseEntity<Usuario> actualizarUsuario(
        @PathVariable Long id,
        @Valid @RequestBody UsuarioUpdateDTO usuarioUpdateDTO
    ) {
        try {
            return ResponseEntity.ok(usuarioService.actualizarUsuario(id, usuarioUpdateDTO));
        } catch (DuplicateResourceException e) {
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin') or #id == authentication.principal.id")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw e;
        }
    }
}