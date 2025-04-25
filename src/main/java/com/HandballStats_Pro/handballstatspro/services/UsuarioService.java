package com.HandballStats_Pro.handballstatspro.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.HandballStats_Pro.handballstatspro.dto.UsuarioDTO;
import com.HandballStats_Pro.handballstatspro.dto.UsuarioUpdateDTO;
import com.HandballStats_Pro.handballstatspro.entities.Usuario;
import com.HandballStats_Pro.handballstatspro.enums.Rol;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // CREATE
    public Usuario crearUsuario(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new RuntimeException("Email ya registrado");
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setContraseña(passwordEncoder.encode(usuarioDTO.getContraseña()));
        usuario.setRol(Rol.Entrenador);
        usuario.setFechaRegistro(LocalDateTime.now());
        
        return usuarioRepository.save(usuario);
    }

    // READ
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }

    // UPDATE (con control de rol y cambios parciales)
    public Usuario actualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        // Obtener el usuario actual por su ID
        Usuario actual = usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Nombre
        if (dto.getNombre() != null) {
            actual.setNombre(dto.getNombre());
        }

        // Email
        if (dto.getEmail() != null && !dto.getEmail().equals(actual.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email ya registrado");
            }
            actual.setEmail(dto.getEmail());
        }

        // Contraseña
        if (dto.getContraseña() != null && !dto.getContraseña().isEmpty()) {
            actual.setContraseña(passwordEncoder.encode(dto.getContraseña()));
        }

        // **Rol** (solo Admin puede cambiarlo)
        if (dto.getRol() != null && dto.getRol() != actual.getRol()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Admin"));
            if (!isAdmin) {
                throw new AccessDeniedException("Solo Admin puede cambiar roles");
            }
            actual.setRol(dto.getRol());
        }

        // Guardar y devolver
        return usuarioRepository.save(actual);
    }

    // DELETE
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
