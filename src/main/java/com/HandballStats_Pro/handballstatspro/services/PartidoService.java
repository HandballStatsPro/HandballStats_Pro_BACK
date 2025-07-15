package com.HandballStats_Pro.handballstatspro.services;

import com.HandballStats_Pro.handballstatspro.dto.*;
import com.HandballStats_Pro.handballstatspro.entities.Equipo;
import com.HandballStats_Pro.handballstatspro.entities.Partido;
import com.HandballStats_Pro.handballstatspro.entities.Usuario;
import com.HandballStats_Pro.handballstatspro.exceptions.PermissionDeniedException;
import com.HandballStats_Pro.handballstatspro.exceptions.ResourceNotFoundException;
import com.HandballStats_Pro.handballstatspro.repositories.EquipoRepository;
import com.HandballStats_Pro.handballstatspro.repositories.PartidoRepository;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoRepository equipoRepository;

    public PartidoService(PartidoRepository partidoRepository, UsuarioRepository usuarioRepository, EquipoRepository equipoRepository) {
        this.partidoRepository = partidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.equipoRepository = equipoRepository;
    }

    @Transactional
    public PartidoResponseDTO crearPartido(PartidoDTO partidoDTO) {
        Usuario usuario = obtenerUsuarioActual();

        // Lógica de permisos: si se asocia un equipo, el usuario debe tener acceso a él
        if (partidoDTO.getIdEquipoLocalAsociado() != null && !tienePermisoEquipo(partidoDTO.getIdEquipoLocalAsociado(), usuario.getIdUsuario(), obtenerRolUsuario())) {
            throw new PermissionDeniedException();
        }
        if (partidoDTO.getIdEquipoVisitanteAsociado() != null && !tienePermisoEquipo(partidoDTO.getIdEquipoVisitanteAsociado(), usuario.getIdUsuario(), obtenerRolUsuario())) {
            throw new PermissionDeniedException();
        }

        Partido partido = new Partido();
        partido.setNombreEquipoLocal(partidoDTO.getNombreEquipoLocal());
        partido.setNombreEquipoVisitante(partidoDTO.getNombreEquipoVisitante());
        partido.setIdEquipoLocalAsociado(partidoDTO.getIdEquipoLocalAsociado());
        partido.setIdEquipoVisitanteAsociado(partidoDTO.getIdEquipoVisitanteAsociado());
        partido.setFecha(partidoDTO.getFecha());
        partido.setResultado(partidoDTO.getResultado());
        partido.setCompeticion(partidoDTO.getCompeticion());
        partido.setFechaRegistro(LocalDateTime.now());
        partido.setIdUsuarioRegistro(usuario.getIdUsuario());

        Partido nuevoPartido = partidoRepository.save(partido);
        return mapToResponseDTO(nuevoPartido);
    }

    public List<PartidoResponseDTO> listarPartidos() {
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();

        if ("ROLE_Admin".equals(rol)) {
            return partidoRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        }

        // 1. Obtener los partidos donde el usuario gestiona al menos uno de los equipos
        List<Long> equipoIds;
        if ("ROLE_GestorClub".equals(rol)) {
            equipoIds = partidoRepository.findEquiposByGestorClub(usuario.getIdUsuario()).stream().map(Equipo::getIdEquipo).toList();
        } else if ("ROLE_Entrenador".equals(rol)) {
            equipoIds = partidoRepository.findEquiposByEntrenador(usuario.getIdUsuario()).stream().map(Equipo::getIdEquipo).toList();
        } else {
            equipoIds = List.of();
        }

        List<Partido> partidosAsociados = equipoIds.isEmpty()
            ? List.of()
            : partidoRepository.findPartidosByEquiposAsociados(equipoIds);

        // 2. Obtener los partidos de scouting creados por este usuario
        List<Partido> partidosDeScouting = partidoRepository.findScoutingPartidosByRegistrador(usuario.getIdUsuario());

        // 3. Combinar ambas listas sin duplicados
        // Usamos un Set para asegurar que cada partido aparezca solo una vez
        java.util.Set<Partido> partidosUnicos = new java.util.LinkedHashSet<>(partidosAsociados);
        partidosUnicos.addAll(partidosDeScouting);

        // 4. Mapear el resultado final a DTOs
        return new java.util.ArrayList<>(partidosUnicos).stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    public PartidoResponseDTO obtenerPartidoPorId(Integer id) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(id)));

        if (!puedeAccederPartido(partido)) {
            throw new PermissionDeniedException();
        }
        return mapToResponseDTO(partido);
    }

    @Transactional
    public PartidoResponseDTO actualizarPartido(Integer id, PartidoUpdateDTO dto) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(id)));

        if (!puedeAccederPartido(partido)) {
            throw new PermissionDeniedException();
        }

        // Actualizar campos
        if (dto.getNombreEquipoLocal() != null) partido.setNombreEquipoLocal(dto.getNombreEquipoLocal());
        if (dto.getNombreEquipoVisitante() != null) partido.setNombreEquipoVisitante(dto.getNombreEquipoVisitante());
        if (dto.getFecha() != null) partido.setFecha(dto.getFecha());
        if (dto.getResultado() != null) partido.setResultado(dto.getResultado());
        if (dto.getCompeticion() != null) partido.setCompeticion(dto.getCompeticion());

        // La lógica para cambiar los equipos asociados puede ser compleja, aquí un ejemplo simple
        partido.setIdEquipoLocalAsociado(dto.getIdEquipoLocalAsociado());
        partido.setIdEquipoVisitanteAsociado(dto.getIdEquipoVisitanteAsociado());


        Partido partidoActualizado = partidoRepository.save(partido);
        return mapToResponseDTO(partidoActualizado);
    }

    @Transactional
    public void eliminarPartido(Integer id) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(id)));

        if (!puedeAccederPartido(partido)) {
            throw new PermissionDeniedException();
        }
        partidoRepository.delete(partido);
    }

    // --- MÉTODOS PRIVADOS Y DE AYUDA ---

    public boolean puedeAccederPartido(Partido partido) {
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();

        if ("ROLE_Admin".equals(rol)) return true;

        // Para partidos de scouting (sin equipos asociados), solo el creador puede verlos/editarlos
        if (partido.getIdEquipoLocalAsociado() == null && partido.getIdEquipoVisitanteAsociado() == null) {
            return partido.getIdUsuarioRegistro().equals(usuario.getIdUsuario());
        }

        // Para partidos con equipos asociados, debe tener permiso sobre al menos uno
        boolean accesoLocal = (partido.getIdEquipoLocalAsociado() != null) && tienePermisoEquipo(partido.getIdEquipoLocalAsociado(), usuario.getIdUsuario(), rol);
        boolean accesoVisitante = (partido.getIdEquipoVisitanteAsociado() != null) && tienePermisoEquipo(partido.getIdEquipoVisitanteAsociado(), usuario.getIdUsuario(), rol);

        return accesoLocal || accesoVisitante;
    }

    private boolean tienePermisoEquipo(Long idEquipo, Long idUsuario, String rol) {
        if ("ROLE_Admin".equals(rol)) return true; 

        if ("ROLE_GestorClub".equals(rol)) {
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
        dto.setNombreEquipoLocal(partido.getNombreEquipoLocal());
        dto.setNombreEquipoVisitante(partido.getNombreEquipoVisitante());
        dto.setIdEquipoLocalAsociado(partido.getIdEquipoLocalAsociado());
        dto.setIdEquipoVisitanteAsociado(partido.getIdEquipoVisitanteAsociado());
        dto.setResultado(partido.getResultado());
        dto.setFecha(partido.getFecha());
        dto.setCompeticion(partido.getCompeticion());
        dto.setFechaRegistro(partido.getFechaRegistro());
        dto.setIdUsuarioRegistro(partido.getIdUsuarioRegistro());
        return dto;
    }

    // Este método ya no es necesario aquí, ya que la lógica de equipos disponibles puede vivir en EquipoService
    public List<EquipoResponseDTO> obtenerEquiposDisponibles() {
        // Se podría mover esta lógica a EquipoService y llamarla desde aquí si fuera necesario
        // Por ahora, lo dejamos simple. El frontend puede llamar a /equipo para obtener la lista
        return equipoRepository.findAll().stream().map(this::mapEquipoToResponseDTO).collect(Collectors.toList());
    }

    private EquipoResponseDTO mapEquipoToResponseDTO(Equipo equipo) {
        EquipoResponseDTO dto = new EquipoResponseDTO();
        dto.setIdEquipo(equipo.getIdEquipo());
        dto.setNombre(equipo.getNombre());
        dto.setCategoria(equipo.getCategoria());
        dto.setSexo(equipo.getSexo());
        dto.setTemporada(equipo.getTemporada());
        if (equipo.getClub() != null) {
            dto.setClubNombre(equipo.getClub().getNombre());
            dto.setIdClub(equipo.getClub().getIdClub());
        }
        return dto;
    }
    
    public List<PartidoResponseDTO> obtenerPartidosPorEquipo(Long idEquipo) {
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        // Verificar que el usuario tenga permiso sobre el equipo
        if (!tienePermisoEquipo(idEquipo, usuario.getIdUsuario(), rol)) {
            throw new PermissionDeniedException();
        }
        
        return partidoRepository.findPartidosByEquiposAsociados(List.of(idEquipo)).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<PartidoResponseDTO> obtenerPartidosPorFecha(LocalDate fecha) {
        return partidoRepository.findByFecha(fecha).stream()
                .filter(this::puedeAccederPartido)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
}