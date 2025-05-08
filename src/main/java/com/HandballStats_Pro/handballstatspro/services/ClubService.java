package com.HandballStats_Pro.handballstatspro.services;

import com.HandballStats_Pro.handballstatspro.config.UserDetailsImpl;
import com.HandballStats_Pro.handballstatspro.dto.*;
import com.HandballStats_Pro.handballstatspro.entities.*;
import com.HandballStats_Pro.handballstatspro.exceptions.*;
import com.HandballStats_Pro.handballstatspro.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioClubRepository usuarioClubRepository;

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String role() {
        return auth().getAuthorities().stream()
            .map(a -> a.getAuthority())
            .findFirst()
            .orElseThrow(PermissionDeniedException::new);
    }

    private Long userId() {
        Object p = auth().getPrincipal();
        if (p instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) p).getId();
        }
        throw new PermissionDeniedException();
    }

    public ClubResponseDTO crearClub(ClubDTO dto) {
        clubRepository.findByNombre(dto.getNombre())
            .ifPresent(c -> { throw new DuplicateResourceException("Club", "nombre"); });

        Club club = new Club();
        club.setNombre(dto.getNombre());
        club.setCiudad(dto.getCiudad());
        club.setFechaCreacionClub(LocalDateTime.now());
        Club saved = clubRepository.save(club);

        if (role().equals("ROLE_GestorClub")) {
            Usuario u = usuarioRepository.findById(userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId()));
            usuarioClubRepository.save(new UsuarioClub(
                new UsuarioClubId(u.getIdUsuario(), saved.getIdClub()), u, saved
            ));
        }

        return mapToResponseDTO(saved);
    }

    public List<ClubResponseDTO> listarClubs() {
        String r = role();
        List<Club> clubs;

        if (r.equals("ROLE_Admin")) {
            clubs = clubRepository.findAll();
        } else if (r.equals("ROLE_GestorClub")) {
            clubs = usuarioClubRepository.findByUsuario_IdUsuario(userId())
                .stream().map(UsuarioClub::getClub).toList();
        } else {
            throw new PermissionDeniedException();
        }

        return clubs.stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
    }

    public ClubResponseDTO obtenerClubPorId(Long id) {
        Club club = clubRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Club", id));

        String r = role();
        if (r.equals("ROLE_GestorClub")
         && !usuarioClubRepository.existsByUsuario_IdUsuarioAndClub_IdClub(userId(), id)) {
            throw new PermissionDeniedException();
        }

        return mapToResponseDTO(club);
    }

    public ClubResponseDTO actualizarClub(Long id, ClubUpdateDTO dto) {
        Club club = clubRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Club", id));

        if (dto.getNombre() != null) club.setNombre(dto.getNombre());
        if (dto.getCiudad() != null) club.setCiudad(dto.getCiudad());

        return mapToResponseDTO(clubRepository.save(club));
    }

    @Transactional
    public void eliminarClub(Long id) {
        if (!role().equals("ROLE_Admin")) throw new PermissionDeniedException();
        Club club = clubRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Club", id));
        usuarioClubRepository.deleteByClub(club);
        clubRepository.delete(club);
    }

    public void asignarUsuarioAClub(UsuarioClubDTO dto) {
        Usuario u = usuarioRepository.findById(dto.getIdUsuario())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.getIdUsuario()));
        Club c = clubRepository.findById(dto.getIdClub())
            .orElseThrow(() -> new ResourceNotFoundException("Club", dto.getIdClub()));

        UsuarioClubId id = new UsuarioClubId(u.getIdUsuario(), c.getIdClub());
        if (usuarioClubRepository.existsById(id)) {
            throw new DuplicateResourceException("UsuarioClub", "ya asignado");
        }
        usuarioClubRepository.save(new UsuarioClub(id, u, c));
    }

    private ClubResponseDTO mapToResponseDTO(Club club) {
        ClubResponseDTO dto = new ClubResponseDTO();
        dto.setIdClub(club.getIdClub());
        dto.setNombre(club.getNombre());
        dto.setCiudad(club.getCiudad());
        dto.setFechaCreacionClub(club.getFechaCreacionClub());

        // Rellenar gestores solo para Admin
        if (role().equals("ROLE_Admin")) {
            List<UsuarioSimpleDTO> gests = usuarioClubRepository
                .findByClub(club).stream()
                .map(uc -> uc.getUsuario())
                .filter(u -> u.getRol().name().equals("GestorClub"))
                .map(u -> {
                    UsuarioSimpleDTO us = new UsuarioSimpleDTO();
                    us.setIdUsuario(u.getIdUsuario());
                    us.setNombre(u.getNombre());
                    return us;
                })
                .collect(Collectors.toList());
            dto.setGestores(gests);
        } else {
            dto.setGestores(List.of());
        }

        return dto;
    }

    public void removeGestor(Long idClub, Long idUsuario) {
        // buscar relación y borrarla
        UsuarioClubId key = new UsuarioClubId(idUsuario, idClub);
        usuarioClubRepository.deleteById(key);
      }
      
}
