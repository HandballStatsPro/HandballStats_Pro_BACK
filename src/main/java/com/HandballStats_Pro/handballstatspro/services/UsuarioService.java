package com.HandballStats_Pro.handballstatspro.services;

import com.HandballStats_Pro.handballstatspro.dto.UsuarioDTO;
import com.HandballStats_Pro.handballstatspro.dto.UsuarioUpdateDTO;
import com.HandballStats_Pro.handballstatspro.entities.Usuario;
import com.HandballStats_Pro.handballstatspro.enums.Rol;
import com.HandballStats_Pro.handballstatspro.exceptions.DuplicateResourceException;
import com.HandballStats_Pro.handballstatspro.exceptions.PermissionDeniedException;
import com.HandballStats_Pro.handballstatspro.exceptions.ResourceNotFoundException;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioClubRepository;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioClubRepository usuarioClubRepository;

    public Usuario crearUsuario(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new DuplicateResourceException("email_existente", "Email ya registrado");
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setContraseña(passwordEncoder.encode(usuarioDTO.getContraseña()));
        usuario.setRol(Rol.Entrenador);
        usuario.setFechaRegistro(LocalDateTime.now());
        
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
    }

    public Usuario actualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario actual = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        if (dto.getNombre() != null) {
            actual.setNombre(dto.getNombre());
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(actual.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateResourceException("El correo electrónico ya está registrado");
            }
            actual.setEmail(dto.getEmail());
        }

        if (dto.getContraseña() != null && !dto.getContraseña().isEmpty()) {
            actual.setContraseña(passwordEncoder.encode(dto.getContraseña()));
        }

        if (dto.getRol() != null && dto.getRol() != actual.getRol()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Admin"));
            
            if (!isAdmin) {
                throw new PermissionDeniedException();
            }
            actual.setRol(dto.getRol());
        }

        return usuarioRepository.save(actual);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        // Eliminar todas las relaciones usuario-club primero
        usuarioClubRepository.deleteByUsuario(usuario);
        
        usuarioRepository.delete(usuario);
    }
}