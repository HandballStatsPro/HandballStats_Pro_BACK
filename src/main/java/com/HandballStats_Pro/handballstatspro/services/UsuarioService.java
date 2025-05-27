package com.HandballStats_Pro.handballstatspro.services;

import com.HandballStats_Pro.handballstatspro.dto.UsuarioDTO;
import com.HandballStats_Pro.handballstatspro.dto.UsuarioUpdateDTO;
import com.HandballStats_Pro.handballstatspro.entities.Usuario;
import com.HandballStats_Pro.handballstatspro.enums.Rol;
import com.HandballStats_Pro.handballstatspro.exceptions.ApiException;
import com.HandballStats_Pro.handballstatspro.exceptions.DuplicateResourceException;
import com.HandballStats_Pro.handballstatspro.exceptions.PermissionDeniedException;
import com.HandballStats_Pro.handballstatspro.exceptions.ResourceNotFoundException;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioClubRepository;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioEquipoRepository;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioRepository;
import com.HandballStats_Pro.handballstatspro.repositories.PartidoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioClubRepository usuarioClubRepository;
    private final UsuarioEquipoRepository usuarioEquipoRepository;
    private final PartidoRepository partidoRepository;

    @Value("${avatar.max-size}")
    private long maxAvatarSize;

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

        if (usuarioDTO.getAvatarBase64() != null && !usuarioDTO.getAvatarBase64().isEmpty()) {
            byte[] img = Base64.getDecoder().decode(usuarioDTO.getAvatarBase64());
            if (img.length > maxAvatarSize) {
                throw new ApiException(
                    HttpStatus.BAD_REQUEST, 
                    "avatar_too_large",
                    "Avatar excede tamaño máximo de " + (maxAvatarSize/1024) + " KB"
                );
            }
            usuario.setAvatarBase64(img);
        }
        
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

        if (dto.getAvatarBase64() != null) {
            byte[] img = Base64.getDecoder().decode(dto.getAvatarBase64());
            if (img.length > maxAvatarSize) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "avatar_too_large",
                    "Avatar excede tamaño máximo de " + (maxAvatarSize/1024) + " KB");
            }
            actual.setAvatarBase64(img);
        }

        return usuarioRepository.save(actual);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        System.out.println("DEBUG: Iniciando eliminación de usuario con ID: " + id);
        Usuario usuario = null;
        try {
            usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
            System.out.println("DEBUG: Usuario encontrado: " + usuario.getIdUsuario());
        } catch (ResourceNotFoundException e) {
            System.err.println("ERROR: No se encontró el usuario con ID: " + id);
            return; // Sale del método si el usuario no existe
        }

        try {
            System.out.println("DEBUG: Eliminando relaciones usuario-club...");
            usuarioClubRepository.deleteByUsuario(usuario);
            System.out.println("DEBUG: Relaciones usuario-club eliminadas.");
        } catch (Exception e) {
            System.err.println("ERROR al eliminar relaciones usuario-club: " + e.getMessage());
        }

        try {
            System.out.println("DEBUG: Eliminando relaciones usuario-equipo...");
            usuarioEquipoRepository.deleteByUsuario_IdUsuario(id);
            System.out.println("DEBUG: Relaciones usuario-equipo eliminadas.");
        } catch (Exception e) {
            System.err.println("ERROR al eliminar relaciones usuario-equipo: " + e.getMessage());
        }

        try {
            System.out.println("DEBUG: Actualizando id_usuario_registro en partidos...");
            partidoRepository.updateIdUsuarioRegistroToZero(id);
            System.out.println("DEBUG: id_usuario_registro en partidos actualizado.");
        } catch (Exception e) {
            System.err.println("ERROR al actualizar id_usuario_registro en partidos: " + e.getMessage());
        }

        try {
            System.out.println("DEBUG: Eliminando usuario...");
            usuarioRepository.delete(usuario);
            System.out.println("DEBUG: Usuario eliminado.");
        } catch (Exception e) {
            System.err.println("ERROR al eliminar usuario: " + e.getMessage());
        }
        System.out.println("DEBUG: Finalizada la eliminación de usuario con ID: " + id);
    }
}