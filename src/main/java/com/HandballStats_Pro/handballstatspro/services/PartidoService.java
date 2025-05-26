package com.HandballStats_Pro.handballstatspro.services;

import com.HandballStats_Pro.handballstatspro.dto.PartidoDTO;
import com.HandballStats_Pro.handballstatspro.dto.PartidoResponseDTO;
import com.HandballStats_Pro.handballstatspro.dto.PartidoUpdateDTO;
import com.HandballStats_Pro.handballstatspro.entities.Partido;
import com.HandballStats_Pro.handballstatspro.entities.Usuario;
import com.HandballStats_Pro.handballstatspro.exceptions.ResourceNotFoundException;
import com.HandballStats_Pro.handballstatspro.exceptions.PermissionDeniedException;
import com.HandballStats_Pro.handballstatspro.repositories.PartidoRepository;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioRepository;
import com.HandballStats_Pro.handballstatspro.entities.Equipo;
import com.HandballStats_Pro.handballstatspro.repositories.EquipoRepository;
import com.HandballStats_Pro.handballstatspro.services.EquipoService;
import com.HandballStats_Pro.handballstatspro.dto.EquipoResponseDTO;

import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoService equipoService;
    private final EquipoRepository equipoRepository;

    public PartidoService(PartidoRepository partidoRepository, UsuarioRepository usuarioRepository, EquipoService equipoService, EquipoRepository equipoRepository) {
        this.partidoRepository = partidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.equipoService = equipoService;
        this.equipoRepository = equipoRepository;
    }

    public PartidoResponseDTO crearPartido(PartidoDTO partidoDTO) {
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        // Validar permisos según el rol
        if (!puedeCrearPartido(partidoDTO.getIdEquipoPropio(), usuario.getIdUsuario(), rol)) {
            throw new PermissionDeniedException();
        }

        Partido partido = new Partido();
        partido.setResultado(partidoDTO.getResultado());
        partido.setIdEquipoPropio(partidoDTO.getIdEquipoPropio());
        partido.setNombreRival(partidoDTO.getNombreRival());
        partido.setEsLocal(partidoDTO.getEsLocal());
        partido.setFecha(partidoDTO.getFecha());
        partido.setFechaRegistro(LocalDateTime.now());
        partido.setIdUsuarioRegistro(usuario.getIdUsuario());

        Partido nuevoPartido = partidoRepository.save(partido);
        return mapToResponseDTO(nuevoPartido);
    }

    public List<PartidoResponseDTO> listarPartidos() {
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        List<Partido> partidos;
        
        if ("ROLE_Admin".equals(rol)) {
            // Admin puede ver todos los partidos
            partidos = partidoRepository.findAll();
        } else if ("ROLE_GestorClub".equals(rol)) {
            // Gestor puede ver partidos de equipos de sus clubes
            partidos = partidoRepository.findPartidosByGestorClub(usuario.getIdUsuario());
        } else if ("ROLE_Entrenador".equals(rol)) {
            // Entrenador puede ver partidos de sus equipos
            partidos = partidoRepository.findPartidosByEntrenador(usuario.getIdUsuario());
        } else {
            partidos = List.of(); // Otros roles no tienen acceso
        }
        
        return partidos.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public PartidoResponseDTO obtenerPartidoPorId(Integer id) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(id)));
        
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        // Validar permisos según el rol
        if (!puedeAccederPartido(partido.getIdEquipoPropio(), usuario.getIdUsuario(), rol)) {
            throw new PermissionDeniedException();
        }
        
        return mapToResponseDTO(partido);
    }

    public PartidoResponseDTO actualizarPartido(Integer id, PartidoUpdateDTO partidoUpdateDTO) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(id)));
        
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        // Validar permisos según el rol
        if (!puedeEditarPartido(partido.getIdEquipoPropio(), usuario.getIdUsuario(), rol)) {
            throw new PermissionDeniedException();
        }
        
        // Si se cambia el equipo propio, validar que tenga permisos sobre el nuevo equipo
        if (partidoUpdateDTO.getIdEquipoPropio() != null && 
            !partidoUpdateDTO.getIdEquipoPropio().equals(partido.getIdEquipoPropio())) {
            if (!puedeCrearPartido(partidoUpdateDTO.getIdEquipoPropio(), usuario.getIdUsuario(), rol)) {
                throw new PermissionDeniedException();
            }
        }

        if (partidoUpdateDTO.getResultado() != null) {
            partido.setResultado(partidoUpdateDTO.getResultado());
        }
        if (partidoUpdateDTO.getIdEquipoPropio() != null) {
            partido.setIdEquipoPropio(partidoUpdateDTO.getIdEquipoPropio());
        }
        if (partidoUpdateDTO.getNombreRival() != null) {
            partido.setNombreRival(partidoUpdateDTO.getNombreRival());
        }
        if (partidoUpdateDTO.getEsLocal() != null) {
            partido.setEsLocal(partidoUpdateDTO.getEsLocal());
        }
        if (partidoUpdateDTO.getFecha() != null) {
            partido.setFecha(partidoUpdateDTO.getFecha());
        }

        Partido partidoActualizado = partidoRepository.save(partido);
        return mapToResponseDTO(partidoActualizado);
    }

    public void eliminarPartido(Integer id) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(id)));
        
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        // Validar permisos según el rol
        if (!puedeEliminarPartido(partido.getIdEquipoPropio(), usuario.getIdUsuario(), rol)) {
            throw new PermissionDeniedException();
        }
        
        partidoRepository.delete(partido);
    }

    public List<PartidoResponseDTO> obtenerPartidosPorEquipo(Long idEquipo) {
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        // Validar que el usuario tenga acceso al equipo
        if (!puedeAccederPartido(idEquipo, usuario.getIdUsuario(), rol)) {
            throw new PermissionDeniedException();
        }
        
        List<Partido> partidos = partidoRepository.findByIdEquipoPropio(idEquipo);
        return partidos.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PartidoResponseDTO> obtenerPartidosPorFecha(LocalDate fecha) {
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        List<Partido> partidos = partidoRepository.findByFecha(fecha);
        
        // Filtrar según permisos del usuario
        if ("ROLE_Admin".equals(rol)) {
            // Admin puede ver todos
        } else if ("ROLE_GestorClub".equals(rol)) {
            // Filtrar solo equipos de sus clubes
            partidos = partidos.stream()
                    .filter(p -> partidoRepository.existsEquipoInGestorClubes(p.getIdEquipoPropio(), usuario.getIdUsuario()))
                    .collect(Collectors.toList());
        } else if ("ROLE_Entrenador".equals(rol)) {
            // Filtrar solo sus equipos
            partidos = partidos.stream()
                    .filter(p -> partidoRepository.existsEquipoInEntrenador(p.getIdEquipoPropio(), usuario.getIdUsuario()))
                    .collect(Collectors.toList());
        } else {
            partidos = List.of();
        }
        
        return partidos.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<EquipoResponseDTO> obtenerEquiposDisponibles() {
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        List<Equipo> equipos;
        
        if ("ROLE_Admin".equals(rol)) {
            // Admin puede ver todos los equipos
            equipos = equipoRepository.findAll();
        } else if ("ROLE_GestorClub".equals(rol)) {
            // Gestor puede ver equipos de sus clubes
            equipos = partidoRepository.findEquiposByGestorClub(usuario.getIdUsuario());
        } else if ("ROLE_Entrenador".equals(rol)) {
            // Entrenador puede ver sus equipos
            equipos = partidoRepository.findEquiposByEntrenador(usuario.getIdUsuario());
        } else {
            equipos = List.of();
        }
        
        return equipos.stream()
                .map(this::mapEquipoToResponseDTO)
                .collect(Collectors.toList());
    }

    // Métodos privados para validación de permisos
    private boolean puedeCrearPartido(Long idEquipo, Long idUsuario, String rol) {
        if ("ROLE_Admin".equals(rol)) {
            return true;
        } else if ("ROLE_GestorClub".equals(rol)) {
            return partidoRepository.existsEquipoInGestorClubes(idEquipo, idUsuario);
        } else if ("ROLE_Entrenador".equals(rol)) {
            return partidoRepository.existsEquipoInEntrenador(idEquipo, idUsuario);
        }
        return false;
    }

    private boolean puedeAccederPartido(Long idEquipo, Long idUsuario, String rol) {
        return puedeCrearPartido(idEquipo, idUsuario, rol);
    }

    private boolean puedeEditarPartido(Long idEquipo, Long idUsuario, String rol) {
        return puedeCrearPartido(idEquipo, idUsuario, rol);
    }

    private boolean puedeEliminarPartido(Long idEquipo, Long idUsuario, String rol) {
        if ("ROLE_Admin".equals(rol)) {
            return true;
        } else if ("ROLE_GestorClub".equals(rol)) {
            return partidoRepository.existsEquipoInGestorClubes(idEquipo, idUsuario);
        } else if ("ROLE_Entrenador".equals(rol)) {
            return partidoRepository.existsEquipoInEntrenador(idEquipo, idUsuario);
        }
        return false;
    }

    private Usuario obtenerUsuarioActual() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", username));
    }

    private String obtenerRolUsuario() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
    }

    private PartidoResponseDTO mapToResponseDTO(Partido partido) {
        PartidoResponseDTO dto = new PartidoResponseDTO();
        dto.setIdPartido(partido.getIdPartido());
        dto.setResultado(partido.getResultado());
        dto.setIdEquipoPropio(partido.getIdEquipoPropio());
        dto.setNombreRival(partido.getNombreRival());
        dto.setEsLocal(partido.getEsLocal());
        dto.setFecha(partido.getFecha());
        dto.setFechaRegistro(partido.getFechaRegistro());
        dto.setIdUsuarioRegistro(partido.getIdUsuarioRegistro());
        return dto;
    }
    
    private EquipoResponseDTO mapEquipoToResponseDTO(Equipo equipo) {
        EquipoResponseDTO dto = new EquipoResponseDTO();
        dto.setIdEquipo(equipo.getIdEquipo());
        dto.setNombre(equipo.getNombre());
        dto.setCategoria(equipo.getCategoria());
        dto.setCompeticion(equipo.getCompeticion());
        dto.setFechaCreacionEquipo(equipo.getFechaCreacionEquipo());
        
        if (equipo.getClub() != null) {
            dto.setIdClub(equipo.getClub().getIdClub());
            dto.setClubNombre(equipo.getClub().getNombre());
        }
        
        return dto;
    }
}