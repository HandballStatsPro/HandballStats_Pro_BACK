package com.HandballStats_Pro.handballstatspro.services;

import com.HandballStats_Pro.handballstatspro.dto.AccionDTO;
import com.HandballStats_Pro.handballstatspro.dto.AccionResponseDTO;
import com.HandballStats_Pro.handballstatspro.dto.AccionUpdateDTO;
import com.HandballStats_Pro.handballstatspro.entities.Accion;
import com.HandballStats_Pro.handballstatspro.entities.Partido;
import com.HandballStats_Pro.handballstatspro.entities.Usuario;
import com.HandballStats_Pro.handballstatspro.enums.*;
import com.HandballStats_Pro.handballstatspro.exceptions.PermissionDeniedException;
import com.HandballStats_Pro.handballstatspro.exceptions.ResourceNotFoundException;
import com.HandballStats_Pro.handballstatspro.exceptions.ApiException;
import com.HandballStats_Pro.handballstatspro.repositories.AccionRepository;
import com.HandballStats_Pro.handballstatspro.repositories.PartidoRepository;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccionService {
    
    private final AccionRepository accionRepository;
    private final PartidoRepository partidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PartidoService partidoService;
    
    public AccionService(AccionRepository accionRepository, PartidoRepository partidoRepository, 
                        UsuarioRepository usuarioRepository, PartidoService partidoService) {
        this.accionRepository = accionRepository;
        this.partidoRepository = partidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.partidoService = partidoService;
    }
    
    @Transactional
    public AccionResponseDTO crearAccion(AccionDTO accionDTO) {
        // Verificar que el partido existe
        Partido partido = partidoRepository.findById(accionDTO.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(accionDTO.getIdPartido())));
        
        // Verificar permisos sobre el partido
        if (!partidoService.puedeAccederPartido(partido)) {
            throw new PermissionDeniedException();
        }
        
        // Aplicar todas las reglas de validación
        validarAccion(accionDTO);
        
        // Crear la acción
        Accion accion = new Accion();
        accion.setIdPartido(accionDTO.getIdPartido());
        accion.setIdPosesion(accionDTO.getIdPosesion());
        accion.setEquipoAccion(accionDTO.getEquipoAccion());
        accion.setTipoAtaque(accionDTO.getTipoAtaque());
        accion.setOrigenAccion(accionDTO.getOrigenAccion());
        accion.setEvento(accionDTO.getEvento());
        accion.setDetalleFinalizacion(accionDTO.getDetalleFinalizacion());
        accion.setZonaLanzamiento(accionDTO.getZonaLanzamiento());
        accion.setDetalleEvento(accionDTO.getDetalleEvento());
        accion.setCambioPosesion(accionDTO.getCambioPosesion());
        
        Accion nuevaAccion = accionRepository.save(accion);
        return mapToResponseDTO(nuevaAccion, partido);
    }
    
    public List<AccionResponseDTO> listarAccionesPorPartido(Integer idPartido) {
        // Verificar que el partido existe
        Partido partido = partidoRepository.findById(idPartido)
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(idPartido)));
        
        // Verificar permisos sobre el partido
        if (!partidoService.puedeAccederPartido(partido)) {
            throw new PermissionDeniedException();
        }
        
        return accionRepository.findByIdPartidoOrderByIdAccionAsc(idPartido).stream()
                .map(accion -> mapToResponseDTO(accion, partido))
                .collect(Collectors.toList());
    }
    
    public AccionResponseDTO obtenerAccionPorId(Integer id) {
        Accion accion = accionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accion", "id", String.valueOf(id)));
        
        Partido partido = partidoRepository.findById(accion.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(accion.getIdPartido())));
        
        // Verificar permisos sobre el partido
        if (!partidoService.puedeAccederPartido(partido)) {
            throw new PermissionDeniedException();
        }
        
        return mapToResponseDTO(accion, partido);
    }
    
    @Transactional
    public AccionResponseDTO actualizarAccion(Integer id, AccionUpdateDTO dto) {
        Accion accion = accionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accion", "id", String.valueOf(id)));
        
        Partido partido = partidoRepository.findById(accion.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(accion.getIdPartido())));
        
        // Verificar permisos sobre el partido
        if (!partidoService.puedeAccederPartido(partido)) {
            throw new PermissionDeniedException();
        }
        
        // Crear DTO temporal para validación
        AccionDTO tempDTO = new AccionDTO();
        tempDTO.setIdPartido(accion.getIdPartido());
        tempDTO.setIdPosesion(accion.getIdPosesion());
        tempDTO.setEquipoAccion(dto.getEquipoAccion() != null ? dto.getEquipoAccion() : accion.getEquipoAccion());
        tempDTO.setTipoAtaque(dto.getTipoAtaque() != null ? dto.getTipoAtaque() : accion.getTipoAtaque());
        tempDTO.setOrigenAccion(dto.getOrigenAccion() != null ? dto.getOrigenAccion() : accion.getOrigenAccion());
        tempDTO.setEvento(dto.getEvento() != null ? dto.getEvento() : accion.getEvento());
        tempDTO.setDetalleFinalizacion(dto.getDetalleFinalizacion() != null ? dto.getDetalleFinalizacion() : accion.getDetalleFinalizacion());
        tempDTO.setZonaLanzamiento(dto.getZonaLanzamiento() != null ? dto.getZonaLanzamiento() : accion.getZonaLanzamiento());
        tempDTO.setDetalleEvento(dto.getDetalleEvento() != null ? dto.getDetalleEvento() : accion.getDetalleEvento());
        tempDTO.setCambioPosesion(dto.getCambioPosesion() != null ? dto.getCambioPosesion() : accion.getCambioPosesion());
        
        // Validar los cambios
        validarAccion(tempDTO);
        
        // Actualizar campos
        if (dto.getEquipoAccion() != null) accion.setEquipoAccion(dto.getEquipoAccion());
        if (dto.getTipoAtaque() != null) accion.setTipoAtaque(dto.getTipoAtaque());
        if (dto.getOrigenAccion() != null) accion.setOrigenAccion(dto.getOrigenAccion());
        if (dto.getEvento() != null) accion.setEvento(dto.getEvento());
        if (dto.getDetalleFinalizacion() != null) accion.setDetalleFinalizacion(dto.getDetalleFinalizacion());
        if (dto.getZonaLanzamiento() != null) accion.setZonaLanzamiento(dto.getZonaLanzamiento());
        if (dto.getDetalleEvento() != null) accion.setDetalleEvento(dto.getDetalleEvento());
        if (dto.getCambioPosesion() != null) accion.setCambioPosesion(dto.getCambioPosesion());
        
        Accion accionActualizada = accionRepository.save(accion);
        return mapToResponseDTO(accionActualizada, partido);
    }
    
    @Transactional
    public void eliminarAccion(Integer id) {
        Accion accion = accionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Accion", "id", String.valueOf(id)));
        
        Partido partido = partidoRepository.findById(accion.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(accion.getIdPartido())));
        
        // Verificar permisos sobre el partido
        if (!partidoService.puedeAccederPartido(partido)) {
            throw new PermissionDeniedException();
        }
        
        accionRepository.delete(accion);
    }
    
    // MÉTODOS DE VALIDACIÓN - IMPLEMENTACIÓN DE LAS 5 REGLAS
    
    private void validarAccion(AccionDTO accionDTO) {
        validarRegla1_7Metros(accionDTO);
        validarRegla2_TipoAtaque(accionDTO);
        validarRegla3_EventoPrincipal(accionDTO);
        validarRegla4_CambioPosesion(accionDTO);
        validarRegla5_LogicaSecuencial(accionDTO);
    }
    
    // Regla 1: El Caso Especial de 7 Metros
    private void validarRegla1_7Metros(AccionDTO accionDTO) {
        if (accionDTO.getOrigenAccion() == OrigenAccion._7m) {
            if (accionDTO.getDetalleFinalizacion() != DetalleFinalizacion._7m) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_7M_DETAIL", "Si el origen_accion es '7m', detalle_finalizacion debe ser '7m'");
            }
            if (accionDTO.getTipoAtaque() != TipoAtaque.Posicional) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_7M_TYPE", "Si el origen_accion es '7m', tipo_ataque debe ser 'Posicional'");
            }
        }
        
        if (accionDTO.getDetalleFinalizacion() == DetalleFinalizacion._7m) {
            if (accionDTO.getOrigenAccion() != OrigenAccion._7m) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_7M_ORIGIN", "Si detalle_finalizacion es '7m', origen_accion debe ser '7m'");
            }
        }
    }
    
    // Regla 2: Lógica del Tipo de Ataque
    private void validarRegla2_TipoAtaque(AccionDTO accionDTO) {
        if (accionDTO.getTipoAtaque() == TipoAtaque.Contraataque) {
            if (accionDTO.getDetalleFinalizacion() != DetalleFinalizacion.Contragol && 
                accionDTO.getDetalleFinalizacion() != DetalleFinalizacion._1a_oleada && 
                accionDTO.getDetalleFinalizacion() != DetalleFinalizacion._2a_oleada && 
                accionDTO.getDetalleFinalizacion() != DetalleFinalizacion._3a_oleada) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_COUNTERATTACK_DETAIL", "Si tipo_ataque es 'Contraataque', detalle_finalizacion debe ser 'Contragol', '1ª oleada', '2ª oleada' o '3ª oleada'");
            }
        }
        
        if (accionDTO.getTipoAtaque() == TipoAtaque.Posicional) {
            if (accionDTO.getDetalleFinalizacion() == DetalleFinalizacion.Contragol || 
                accionDTO.getDetalleFinalizacion() == DetalleFinalizacion._1a_oleada || 
                accionDTO.getDetalleFinalizacion() == DetalleFinalizacion._2a_oleada || 
                accionDTO.getDetalleFinalizacion() == DetalleFinalizacion._3a_oleada) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_POSITIONAL_DETAIL", "Si tipo_ataque es 'Posicional', detalle_finalizacion no puede ser 'Contragol', '1ª oleada', '2ª oleada' o '3ª oleada'");
            }
        }
    }
    
    // Regla 3: Lógica del Evento Principal
    private void validarRegla3_EventoPrincipal(AccionDTO accionDTO) {
        switch (accionDTO.getEvento()) {
            case Gol:
                if (accionDTO.getDetalleFinalizacion() == null || accionDTO.getZonaLanzamiento() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "GOAL_REQUIRED_FIELDS", "Para evento 'Gol', detalle_finalizacion y zona_lanzamiento son obligatorios");
                }
                if (accionDTO.getDetalleEvento() != null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "GOAL_INVALID_DETAIL", "Para evento 'Gol', detalle_evento debe ser nulo");
                }
                break;
                
            case Lanzamiento_Parado:
                if (accionDTO.getDetalleFinalizacion() == null || accionDTO.getZonaLanzamiento() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_STOPPED_REQUIRED_FIELDS", "Para evento 'Lanzamiento_Parado', detalle_finalizacion y zona_lanzamiento son obligatorios");
                }
                if (accionDTO.getDetalleEvento() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_STOPPED_REQUIRED_DETAIL", "Para evento 'Lanzamiento_Parado', detalle_evento es obligatorio");
                }
                if (accionDTO.getDetalleEvento() != DetalleEvento.Parada_Portero && 
                    accionDTO.getDetalleEvento() != DetalleEvento.Bloqueo_Defensor) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_STOPPED_INVALID_DETAIL", "Para evento 'Lanzamiento_Parado', detalle_evento debe ser 'Parada_Portero' o 'Bloqueo_Defensor'");
                }
                break;
                
            case Lanzamiento_Fuera:
                if (accionDTO.getDetalleFinalizacion() == null || accionDTO.getZonaLanzamiento() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_MISSED_REQUIRED_FIELDS", "Para evento 'Lanzamiento_Fuera', detalle_finalizacion y zona_lanzamiento son obligatorios");
                }
                if (accionDTO.getDetalleEvento() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_MISSED_REQUIRED_DETAIL", "Para evento 'Lanzamiento_Fuera', detalle_evento es obligatorio");
                }
                if (accionDTO.getDetalleEvento() != DetalleEvento.Palo && 
                    accionDTO.getDetalleEvento() != DetalleEvento.Fuera_Directo) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_MISSED_INVALID_DETAIL", "Para evento 'Lanzamiento_Fuera', detalle_evento debe ser 'Palo' o 'Fuera_Directo'");
                }
                break;
                
            case Perdida:
                if (accionDTO.getDetalleFinalizacion() != null || accionDTO.getZonaLanzamiento() != null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "TURNOVER_INVALID_FIELDS", "Para evento 'Perdida', detalle_finalizacion y zona_lanzamiento deben ser nulos");
                }
                if (accionDTO.getDetalleEvento() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "TURNOVER_REQUIRED_DETAIL", "Para evento 'Perdida', detalle_evento es obligatorio");
                }
                if (accionDTO.getDetalleEvento() != DetalleEvento.Pasos && 
                    accionDTO.getDetalleEvento() != DetalleEvento.Dobles && 
                    accionDTO.getDetalleEvento() != DetalleEvento.FaltaAtaque && 
                    accionDTO.getDetalleEvento() != DetalleEvento.Pasivo && 
                    accionDTO.getDetalleEvento() != DetalleEvento.InvasionArea && 
                    accionDTO.getDetalleEvento() != DetalleEvento.Robo && 
                    accionDTO.getDetalleEvento() != DetalleEvento.Pie && 
                    accionDTO.getDetalleEvento() != DetalleEvento.BalonFuera) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "TURNOVER_INVALID_DETAIL", "Para evento 'Perdida', detalle_evento debe ser uno de los valores válidos para pérdida");
                }
                break;
        }
    }
    
    // Regla 4: Lógica de Cambio de Posesión
    private void validarRegla4_CambioPosesion(AccionDTO accionDTO) {
        boolean deberiaCambiarPosesion = true;
        
        // Casos donde NO cambia la posesión
        if (accionDTO.getEvento() == Evento.Lanzamiento_Parado && 
            (accionDTO.getDetalleEvento() == DetalleEvento.Parada_Portero || 
             accionDTO.getDetalleEvento() == DetalleEvento.Bloqueo_Defensor)) {
            deberiaCambiarPosesion = false;
        }
        
        if (accionDTO.getEvento() == Evento.Lanzamiento_Fuera && 
            accionDTO.getDetalleEvento() == DetalleEvento.Palo) {
            deberiaCambiarPosesion = false;
        }
        
        if (accionDTO.getCambioPosesion() != deberiaCambiarPosesion) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_POSSESSION_CHANGE", "El valor de cambio_posesion no es correcto según las reglas establecidas");
        }
    }
    
    // Regla 5: Lógica Secuencial (Validación entre Acciones)
    private void validarRegla5_LogicaSecuencial(AccionDTO accionDTO) {
        // El origen_accion de '7m' no se rige por esta regla secuencial
        if (accionDTO.getOrigenAccion() == OrigenAccion._7m) {
            return;
        }
        
        // Buscar la última acción en el partido
        Optional<Accion> ultimaAccion = accionRepository.findLastActionInMatch(accionDTO.getIdPartido());
        
        if (ultimaAccion.isPresent()) {
            Accion accionAnterior = ultimaAccion.get();
            
            if (accionDTO.getOrigenAccion() == OrigenAccion.Rebote_directo || 
                accionDTO.getOrigenAccion() == OrigenAccion.Rebote_indirecto) {
                
                if (accionAnterior.getCambioPosesion()) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REBOUND_SEQUENCE", "Para origen_accion 'Rebote_directo' o 'Rebote_indirecto', la acción anterior debe tener cambio_posesion = false");
                }
            }
            
            if (accionDTO.getOrigenAccion() == OrigenAccion.Juego_Continuado) {
                if (!accionAnterior.getCambioPosesion()) {
                    throw new ApiException("Para origen_accion 'Juego_Continuado', la acción anterior debe tener cambio_posesion = true");
                }
            }
        } else {
            // Si no hay acción anterior, solo 'Juego_Continuado' es válido (inicio de posesión)
            if (accionDTO.getOrigenAccion() != OrigenAccion.Juego_Continuado) {
                throw new ApiException("Para la primera acción del partido, origen_accion debe ser 'Juego_Continuado'");
            }
        }
    }
    
    // MÉTODOS AUXILIARES
    
    private AccionResponseDTO mapToResponseDTO(Accion accion, Partido partido) {
        AccionResponseDTO dto = new AccionResponseDTO();
        dto.setIdAccion(accion.getIdAccion());
        dto.setIdPartido(accion.getIdPartido());
        dto.setIdPosesion(accion.getIdPosesion());
        dto.setEquipoAccion(accion.getEquipoAccion());
        dto.setTipoAtaque(accion.getTipoAtaque());
        dto.setOrigenAccion(accion.getOrigenAccion());
        dto.setEvento(accion.getEvento());
        dto.setDetalleFinalizacion(accion.getDetalleFinalizacion());
        dto.setZonaLanzamiento(accion.getZonaLanzamiento());
        dto.setDetalleEvento(accion.getDetalleEvento());
        dto.setCambioPosesion(accion.getCambioPosesion());
        
        // Información del partido
        dto.setNombreEquipoLocal(partido.getNombreEquipoLocal());
        dto.setNombreEquipoVisitante(partido.getNombreEquipoVisitante());
        
        return dto;
    }
}