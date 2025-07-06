package com.HandballStats_Pro.handballstatspro.services;

import com.HandballStats_Pro.handballstatspro.config.UserDetailsImpl;
import com.HandballStats_Pro.handballstatspro.dto.*;
import com.HandballStats_Pro.handballstatspro.entities.*;
import com.HandballStats_Pro.handballstatspro.enums.Rol;
import com.HandballStats_Pro.handballstatspro.exceptions.PermissionDeniedException;
import com.HandballStats_Pro.handballstatspro.exceptions.ResourceNotFoundException;
import com.HandballStats_Pro.handballstatspro.repositories.EquipoRepository;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioEquipoRepository;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioRepository;
import com.HandballStats_Pro.handballstatspro.repositories.ClubRepository;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioClubRepository;
import com.HandballStats_Pro.handballstatspro.repositories.PartidoRepository;
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
public class EquipoService {

    private final EquipoRepository equipoRepo;
    private final UsuarioEquipoRepository usuarioEquipoRepo;
    private final UsuarioRepository usuarioRepo;
    private final ClubRepository clubRepo;
    private final UsuarioClubRepository usuarioClubRepo;
    private final PartidoRepository partidoRepository;

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

    @Transactional
    public EquipoResponseDTO crearEquipo(EquipoDTO dto) {
        String r = role();
        Equipo eq = new Equipo();
        eq.setNombre(dto.getNombre());
        eq.setCategoria(dto.getCategoria());
        eq.setTemporada(dto.getTemporada());
        eq.setFechaCreacionEquipo(LocalDateTime.now());

        if (r.equals("ROLE_Admin")) {
            if (dto.getIdClub() != null) {
                Club c = clubRepo.findById(dto.getIdClub())
                        .orElseThrow(() -> new ResourceNotFoundException("Club", dto.getIdClub()));
                eq.setClub(c);
            }
        } else if (r.equals("ROLE_GestorClub")) {
            Long clubId = dto.getIdClub();
            if (clubId == null) {
                throw new IllegalArgumentException("GestorClub debe indicar idClub");
            }
            boolean manages = usuarioClubRepo
                    .existsByUsuario_IdUsuarioAndClub_IdClub(userId(), clubId);
            if (!manages) {
                throw new PermissionDeniedException();
            }
            eq.setClub(clubRepo.getReferenceById(clubId));
        } else if (r.equals("ROLE_Entrenador")) {
            // Entrenador no asigna club al crear
        } else {
            throw new PermissionDeniedException();
        }

        Equipo saved = equipoRepo.save(eq);

        if (r.equals("ROLE_Entrenador")) {
            Usuario u = usuarioRepo.findById(userId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId()));
            usuarioEquipoRepo.save(new UsuarioEquipo(u, saved));
        }

        return mapToDTO(saved);
    }

    public List<EquipoResponseDTO> listarEquipos() {
        String r = role();
        List<Equipo> list;

        if (r.equals("ROLE_Admin")) {
            list = equipoRepo.findAll();

        } else if (r.equals("ROLE_GestorClub")) {
            List<Long> clubIds = usuarioClubRepo.findByUsuario_IdUsuario(userId())
                    .stream()
                    .map(uc -> uc.getClub().getIdClub())
                    .toList();
            list = equipoRepo.findByClub_IdClubIn(clubIds);

        } else if (r.equals("ROLE_Entrenador")) {
            list = usuarioEquipoRepo.findByUsuario_IdUsuario(userId())
                    .stream()
                    .map(UsuarioEquipo::getEquipo)
                    .toList();

        } else {
            throw new PermissionDeniedException();
        }

        return list.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EquipoResponseDTO obtenerEquipoPorId(Long id) {
        Equipo eq = equipoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo", id));

        String r = role();
        if (r.equals("ROLE_Admin")) {
            // puede ver cualquier equipo
        } else if (r.equals("ROLE_GestorClub")) {
            boolean ok = usuarioClubRepo
                    .existsByUsuario_IdUsuarioAndClub_IdClub(userId(), eq.getClub().getIdClub());
            if (!ok) throw new PermissionDeniedException();

        } else if (r.equals("ROLE_Entrenador")) {
            boolean ok = usuarioEquipoRepo
                    .existsByUsuario_IdUsuarioAndEquipo_IdEquipo(userId(), id);
            if (!ok) throw new PermissionDeniedException();

        } else {
            throw new PermissionDeniedException();
        }

        return mapToDTO(eq);
    }

    @Transactional
    public EquipoResponseDTO actualizarEquipo(Long id, EquipoUpdateDTO dto) {
        Equipo eq = equipoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo", id));

        String r = role();
        if (r.equals("ROLE_GestorClub")) {
            boolean ok = usuarioClubRepo
                    .existsByUsuario_IdUsuarioAndClub_IdClub(userId(), eq.getClub().getIdClub());
            if (!ok) throw new PermissionDeniedException();

        } else if (r.equals("ROLE_Entrenador")) {
            boolean ok = usuarioEquipoRepo
                    .existsByUsuario_IdUsuarioAndEquipo_IdEquipo(userId(), id);
            if (!ok) throw new PermissionDeniedException();

        } else if (!r.equals("ROLE_Admin")) {
            throw new PermissionDeniedException();
        }

        if (dto.getNombre() != null)      eq.setNombre(dto.getNombre());
        if (dto.getCategoria() != null)   eq.setCategoria(dto.getCategoria());
        if (dto.getTemporada() != null) eq.setTemporada(dto.getTemporada());

        if (dto.getIdClub() != null) {
            if (r.equals("ROLE_Admin")) {
                Club c = clubRepo.findById(dto.getIdClub())
                        .orElseThrow(() -> new ResourceNotFoundException("Club", dto.getIdClub()));
                eq.setClub(c);

            } else if (r.equals("ROLE_GestorClub")) {
                boolean manages = usuarioClubRepo
                        .existsByUsuario_IdUsuarioAndClub_IdClub(userId(), dto.getIdClub());
                if (!manages) throw new PermissionDeniedException();
                eq.setClub(clubRepo.getReferenceById(dto.getIdClub()));
            }
            // Entrenador no puede cambiar club
        }

        return mapToDTO(equipoRepo.save(eq));
    }

    @Transactional
    public void eliminarEquipo(Long id) {
        if (!role().equals("ROLE_Admin")) {
            throw new PermissionDeniedException();
        }
        Equipo eq = equipoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo", id));
        usuarioEquipoRepo.deleteByEquipo(eq);
        partidoRepository.deleteByEquipoPropioId(id);
        equipoRepo.delete(eq);
    }

    @Transactional
    public void asignarEntrenadorAEquipo(UsuarioEquipoDTO dto) {
        String r = role();
        Long callerId = userId();

        // Permisos
        if (!(r.equals("ROLE_Admin") || r.equals("ROLE_GestorClub"))) {
            throw new PermissionDeniedException();
        }

        // Cargo equipo
        Equipo e = equipoRepo.findById(dto.getIdEquipo())
            .orElseThrow(() -> {
                return new ResourceNotFoundException("Equipo", dto.getIdEquipo());
            });

        // Si es gestor, compruebo que gestione el club del equipo
        if (r.equals("ROLE_GestorClub")) {
            boolean ok = usuarioClubRepo.existsByUsuario_IdUsuarioAndClub_IdClub(
                callerId, e.getClub().getIdClub());
            if (!ok) {
                throw new PermissionDeniedException();
            }
        }

        // Cargo usuario a asignar
        Usuario u = usuarioRepo.findById(dto.getIdUsuario())
            .orElseThrow(() -> {
                return new ResourceNotFoundException("Usuario", dto.getIdUsuario());
            });

        // Validar que el usuario a asignar tiene rol ENTRENADOR
        boolean usuarioEsEntrenador = u.getRol() == Rol.Entrenador;
        if (!usuarioEsEntrenador) {
            throw new IllegalArgumentException("Usuario no es entrenador");
        }


        // Guarda asignación si no existe
        UsuarioEquipoId id = new UsuarioEquipoId(u.getIdUsuario(), e.getIdEquipo());
        if (usuarioEquipoRepo.existsById(id)) {
        } else {
            usuarioEquipoRepo.save(new UsuarioEquipo(u, e));
        }
    }

    @Transactional
    public void desasignarEntrenadorDeEquipo(Long idEquipo, Long idUsuario) {
        String callerRole = role();
        Long callerId   = userId();

        // Solo Admin o GestorClub
        if (!(callerRole.equals("ROLE_Admin") || callerRole.equals("ROLE_GestorClub"))) {
            throw new PermissionDeniedException();
        }

        // Cargo el equipo
        Equipo equipo = equipoRepo.findById(idEquipo)
            .orElseThrow(() -> {
                return new ResourceNotFoundException("Equipo", idEquipo);
            });

        // Si es GestorClub, verificar que gestiona el club del equipo
        if (callerRole.equals("ROLE_GestorClub")) {
            boolean gestiona = usuarioClubRepo
                .existsByUsuario_IdUsuarioAndClub_IdClub(callerId, equipo.getClub().getIdClub());
            if (!gestiona) {
                throw new PermissionDeniedException();
            }
        }

        // Cargo al usuario a desasignar
        Usuario u = usuarioRepo.findById(idUsuario)
            .orElseThrow(() -> {
                return new ResourceNotFoundException("Usuario", idUsuario);
            });

        // Verificar que es entrenador
        boolean usuarioEsEntrenador = u.getRol() == Rol.Entrenador;
        if (!usuarioEsEntrenador) {
            throw new IllegalArgumentException("Usuario no es entrenador");
        }

        // Verificar que la asignación existe
        UsuarioEquipoId ueId = new UsuarioEquipoId(idUsuario, idEquipo);
        if (!usuarioEquipoRepo.existsById(ueId)) {
            return;
        }

        // Borro la asignación
        usuarioEquipoRepo.deleteById(ueId);
    }

    @Transactional
    public void asignarEquipoAClub(Long idEquipo, Long idClub) {
        String rol = role();
        Long uid = userId();

        Equipo equipo = equipoRepo.findById(idEquipo)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo", idEquipo));

        Club club = clubRepo.findById(idClub)
                .orElseThrow(() -> new ResourceNotFoundException("Club", idClub));

        if (rol.equals("ROLE_Admin")) {
        } else if (rol.equals("ROLE_GestorClub")) {
            boolean gestionaDestino = usuarioClubRepo.existsByUsuario_IdUsuarioAndClub_IdClub(uid, idClub);
            boolean gestionaActual = equipo.getClub() == null || 
                    usuarioClubRepo.existsByUsuario_IdUsuarioAndClub_IdClub(uid, equipo.getClub().getIdClub());

    
            if (!gestionaDestino || !gestionaActual) {
                throw new PermissionDeniedException();
            }
        } else {
            throw new PermissionDeniedException();
        }

        equipo.setClub(club);
        equipoRepo.save(equipo);
    }


    private EquipoResponseDTO mapToDTO(Equipo eq) {
        EquipoResponseDTO dto = new EquipoResponseDTO();
        dto.setIdEquipo(eq.getIdEquipo());
        dto.setIdClub(eq.getClub() != null
                ? eq.getClub().getIdClub()
                : null);
        dto.setClubNombre(eq.getClub() != null
                ? eq.getClub().getNombre()
                : null);
        dto.setNombre(eq.getNombre());
        dto.setCategoria(eq.getCategoria());
        dto.setTemporada(eq.getTemporada());
        dto.setFechaCreacionEquipo(eq.getFechaCreacionEquipo());

        if (role().equals("ROLE_Admin") || role().equals("ROLE_GestorClub")) {
            List<UsuarioSimpleDTO> entrenadores = usuarioEquipoRepo
                    .findByEquipo_IdEquipo(eq.getIdEquipo())
                    .stream()
                    .map(ue -> {
                        UsuarioSimpleDTO u = new UsuarioSimpleDTO();
                        u.setIdUsuario(ue.getUsuario().getIdUsuario());
                        u.setNombre(ue.getUsuario().getNombre());
                        return u;
                    })
                    .collect(Collectors.toList());
            dto.setEntrenadores(entrenadores);
        }

        return dto;
    }
}
