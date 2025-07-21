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
import org.springframework.http.HttpStatus;
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
        System.out.println("🔥 ==> INICIO CREACIÓN DE ACCIÓN <== 🔥");
        System.out.println("📊 Datos recibidos: " + accionDTO);
        
        // Verificar que el partido existe
        System.out.println("🔍 Verificando existencia del partido ID: " + accionDTO.getIdPartido());
        Partido partido = partidoRepository.findById(accionDTO.getIdPartido())
                .orElseThrow(() -> {
                    System.out.println("❌ ERROR: Partido no encontrado con ID: " + accionDTO.getIdPartido());
                    return new ResourceNotFoundException("Partido", "id", String.valueOf(accionDTO.getIdPartido()));
                });
        System.out.println("✅ Partido encontrado: " + partido.getNombreEquipoLocal() + " vs " + partido.getNombreEquipoVisitante());
        
        // Verificar permisos sobre el partido
        System.out.println("🔐 Verificando permisos de acceso al partido...");
        if (!partidoService.puedeAccederPartido(partido)) {
            System.out.println("❌ ERROR: Permisos denegados para acceder al partido");
            throw new PermissionDeniedException();
        }
        System.out.println("✅ Permisos verificados correctamente");
        
        // Aplicar todas las reglas de validación
        System.out.println("📋 Iniciando proceso de validación de reglas...");
        validarAccion(accionDTO);
        System.out.println("✅ Todas las reglas de validación pasaron correctamente");
        
        // Crear la acción
        System.out.println("💾 Creando nueva acción en la base de datos...");
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
        System.out.println("✅ Acción guardada exitosamente con ID: " + nuevaAccion.getIdAccion());
        
        AccionResponseDTO response = mapToResponseDTO(nuevaAccion, partido);
        System.out.println("🎉 ==> FIN CREACIÓN DE ACCIÓN EXITOSA <== 🎉");
        return response;
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
        System.out.println("🔥 ==> INICIO ACTUALIZACIÓN DE ACCIÓN <== 🔥");
        System.out.println("📊 ID de acción a actualizar: " + id);
        System.out.println("📊 Datos de actualización: " + dto);
        
        Accion accion = accionRepository.findById(id)
                .orElseThrow(() -> {
                    System.out.println("❌ ERROR: Acción no encontrada con ID: " + id);
                    return new ResourceNotFoundException("Accion", "id", String.valueOf(id));
                });
        System.out.println("✅ Acción encontrada: " + accion);
        
        Partido partido = partidoRepository.findById(accion.getIdPartido())
                .orElseThrow(() -> {
                    System.out.println("❌ ERROR: Partido no encontrado con ID: " + accion.getIdPartido());
                    return new ResourceNotFoundException("Partido", "id", String.valueOf(accion.getIdPartido()));
                });
        
        // Verificar permisos sobre el partido
        System.out.println("🔐 Verificando permisos de acceso al partido...");
        if (!partidoService.puedeAccederPartido(partido)) {
            System.out.println("❌ ERROR: Permisos denegados para actualizar la acción");
            throw new PermissionDeniedException();
        }
        System.out.println("✅ Permisos verificados correctamente");
        
        // Crear DTO temporal para validación
        System.out.println("🔄 Creando DTO temporal con valores combinados para validación...");
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
        System.out.println("📊 DTO temporal creado: " + tempDTO);
        
        // Validar los cambios
        System.out.println("📋 Iniciando proceso de validación de actualización...");
        validarAccion(tempDTO);
        System.out.println("✅ Todas las reglas de validación pasaron correctamente para la actualización");
        
        // Actualizar campos
        System.out.println("💾 Aplicando cambios a la acción...");
        if (dto.getEquipoAccion() != null) {
            System.out.println("🔄 Actualizando equipoAccion: " + accion.getEquipoAccion() + " -> " + dto.getEquipoAccion());
            accion.setEquipoAccion(dto.getEquipoAccion());
        }
        if (dto.getTipoAtaque() != null) {
            System.out.println("🔄 Actualizando tipoAtaque: " + accion.getTipoAtaque() + " -> " + dto.getTipoAtaque());
            accion.setTipoAtaque(dto.getTipoAtaque());
        }
        if (dto.getOrigenAccion() != null) {
            System.out.println("🔄 Actualizando origenAccion: " + accion.getOrigenAccion() + " -> " + dto.getOrigenAccion());
            accion.setOrigenAccion(dto.getOrigenAccion());
        }
        if (dto.getEvento() != null) {
            System.out.println("🔄 Actualizando evento: " + accion.getEvento() + " -> " + dto.getEvento());
            accion.setEvento(dto.getEvento());
        }
        if (dto.getDetalleFinalizacion() != null) {
            System.out.println("🔄 Actualizando detalleFinalizacion: " + accion.getDetalleFinalizacion() + " -> " + dto.getDetalleFinalizacion());
            accion.setDetalleFinalizacion(dto.getDetalleFinalizacion());
        }
        if (dto.getZonaLanzamiento() != null) {
            System.out.println("🔄 Actualizando zonaLanzamiento: " + accion.getZonaLanzamiento() + " -> " + dto.getZonaLanzamiento());
            accion.setZonaLanzamiento(dto.getZonaLanzamiento());
        }
        if (dto.getDetalleEvento() != null) {
            System.out.println("🔄 Actualizando detalleEvento: " + accion.getDetalleEvento() + " -> " + dto.getDetalleEvento());
            accion.setDetalleEvento(dto.getDetalleEvento());
        }
        if (dto.getCambioPosesion() != null) {
            System.out.println("🔄 Actualizando cambioPosesion: " + accion.getCambioPosesion() + " -> " + dto.getCambioPosesion());
            accion.setCambioPosesion(dto.getCambioPosesion());
        }
        
        Accion accionActualizada = accionRepository.save(accion);
        System.out.println("✅ Acción actualizada exitosamente");
        System.out.println("🎉 ==> FIN ACTUALIZACIÓN DE ACCIÓN EXITOSA <== 🎉");
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
        System.out.println("🔍 ==> INICIANDO VALIDACIÓN COMPLETA DE ACCIÓN <== 🔍");
        System.out.println("📊 Datos a validar: " + accionDTO);
        
        System.out.println("📋 [REGLA 1] Validando caso especial de 7 metros...");
        validarRegla1_7Metros(accionDTO);
        
        System.out.println("📋 [REGLA 2] Validando lógica del tipo de ataque...");
        validarRegla2_TipoAtaque(accionDTO);
        
        System.out.println("📋 [REGLA 3] Validando lógica del evento principal...");
        validarRegla3_EventoPrincipal(accionDTO);
        
        System.out.println("📋 [REGLA 4] Validando lógica de cambio de posesión...");
        validarRegla4_CambioPosesion(accionDTO);
        
        System.out.println("📋 [REGLA 5] Validando lógica secuencial...");
        validarRegla5_LogicaSecuencial(accionDTO);
        
        System.out.println("✅ ==> TODAS LAS VALIDACIONES COMPLETADAS EXITOSAMENTE <== ✅");
    }
    
    // Regla 1: El Caso Especial de 7 Metros
    private void validarRegla1_7Metros(AccionDTO accionDTO) {
        System.out.println("🎯 [REGLA 1] Validando caso especial de 7 metros");
        System.out.println("   📊 OrigenAccion: " + accionDTO.getOrigenAccion());
        System.out.println("   📊 DetalleFinalizacion: " + accionDTO.getDetalleFinalizacion());
        System.out.println("   📊 TipoAtaque: " + accionDTO.getTipoAtaque());
        
        if (accionDTO.getOrigenAccion() == OrigenAccion._7m) {
            System.out.println("   🔍 Detectado origen_accion = '7m' - Aplicando validaciones específicas");
            
            if (accionDTO.getDetalleFinalizacion() != DetalleFinalizacion._7m) {
                System.out.println("   ❌ ERROR: Si origen_accion es '7m', detalle_finalizacion debe ser '7m'");
                System.out.println("   💡 Valor esperado: " + DetalleFinalizacion._7m + ", valor actual: " + accionDTO.getDetalleFinalizacion());
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_7M_DETAIL", "Si el origen_accion es '7m', detalle_finalizacion debe ser '7m'");
            }
            System.out.println("   ✅ DetalleFinalizacion correcto para 7m");
            
            if (accionDTO.getTipoAtaque() != TipoAtaque.Posicional) {
                System.out.println("   ❌ ERROR: Si origen_accion es '7m', tipo_ataque debe ser 'Posicional'");
                System.out.println("   💡 Valor esperado: " + TipoAtaque.Posicional + ", valor actual: " + accionDTO.getTipoAtaque());
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_7M_TYPE", "Si el origen_accion es '7m', tipo_ataque debe ser 'Posicional'");
            }
            System.out.println("   ✅ TipoAtaque correcto para 7m");
        } else {
            System.out.println("   ℹ️ OrigenAccion no es '7m', continuando con validación inversa");
        }
        
        if (accionDTO.getDetalleFinalizacion() == DetalleFinalizacion._7m) {
            System.out.println("   🔍 Detectado detalle_finalizacion = '7m' - Validando origen_accion");
            
            if (accionDTO.getOrigenAccion() != OrigenAccion._7m) {
                System.out.println("   ❌ ERROR: Si detalle_finalizacion es '7m', origen_accion debe ser '7m'");
                System.out.println("   💡 Valor esperado: " + OrigenAccion._7m + ", valor actual: " + accionDTO.getOrigenAccion());
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_7M_ORIGIN", "Si detalle_finalizacion es '7m', origen_accion debe ser '7m'");
            }
            System.out.println("   ✅ OrigenAccion correcto para detalle_finalizacion '7m'");
        }
        
        System.out.println("   ✅ [REGLA 1] Validación de 7 metros completada exitosamente");
    }
    
    // Regla 2: Lógica del Tipo de Ataque
    private void validarRegla2_TipoAtaque(AccionDTO accionDTO) {
        System.out.println("⚡ [REGLA 2] Validando lógica del tipo de ataque");
        System.out.println("   📊 TipoAtaque: " + accionDTO.getTipoAtaque());
        System.out.println("   📊 DetalleFinalizacion: " + accionDTO.getDetalleFinalizacion());
        
        if (accionDTO.getTipoAtaque() == TipoAtaque.Contraataque) {
            System.out.println("   🔍 Detectado tipo_ataque = 'Contraataque' - Validando detalles permitidos");
            System.out.println("   💡 Detalles válidos para Contraataque: Contragol, 1ª oleada, 2ª oleada, 3ª oleada");
            
            if (accionDTO.getDetalleFinalizacion() != DetalleFinalizacion.Contragol && 
                accionDTO.getDetalleFinalizacion() != DetalleFinalizacion._1a_oleada && 
                accionDTO.getDetalleFinalizacion() != DetalleFinalizacion._2a_oleada && 
                accionDTO.getDetalleFinalizacion() != DetalleFinalizacion._3a_oleada) {
                System.out.println("   ❌ ERROR: Para tipo_ataque 'Contraataque', detalle_finalizacion debe ser uno de los valores específicos");
                System.out.println("   💡 Valor actual: " + accionDTO.getDetalleFinalizacion() + " (no válido para Contraataque)");
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_COUNTERATTACK_DETAIL", "Si tipo_ataque es 'Contraataque', detalle_finalizacion debe ser 'Contragol', '1ª oleada', '2ª oleada' o '3ª oleada'");
            }
            System.out.println("   ✅ DetalleFinalizacion válido para Contraataque: " + accionDTO.getDetalleFinalizacion());
        }
        
        if (accionDTO.getTipoAtaque() == TipoAtaque.Posicional) {
            System.out.println("   🔍 Detectado tipo_ataque = 'Posicional' - Validando detalles prohibidos");
            System.out.println("   💡 Detalles prohibidos para Posicional: Contragol, 1ª oleada, 2ª oleada, 3ª oleada");
            
            if (accionDTO.getDetalleFinalizacion() == DetalleFinalizacion.Contragol || 
                accionDTO.getDetalleFinalizacion() == DetalleFinalizacion._1a_oleada || 
                accionDTO.getDetalleFinalizacion() == DetalleFinalizacion._2a_oleada || 
                accionDTO.getDetalleFinalizacion() == DetalleFinalizacion._3a_oleada) {
                System.out.println("   ❌ ERROR: Para tipo_ataque 'Posicional', detalle_finalizacion no puede ser de contraataque");
                System.out.println("   💡 Valor actual: " + accionDTO.getDetalleFinalizacion() + " (prohibido para Posicional)");
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_POSITIONAL_DETAIL", "Si tipo_ataque es 'Posicional', detalle_finalizacion no puede ser 'Contragol', '1ª oleada', '2ª oleada' o '3ª oleada'");
            }
            System.out.println("   ✅ DetalleFinalizacion válido para Posicional: " + accionDTO.getDetalleFinalizacion());
        }
        
        System.out.println("   ✅ [REGLA 2] Validación de tipo de ataque completada exitosamente");
    }
    
    // Regla 3: Lógica del Evento Principal
    private void validarRegla3_EventoPrincipal(AccionDTO accionDTO) {
        System.out.println("🎪 [REGLA 3] Validando lógica del evento principal");
        System.out.println("   📊 Evento: " + accionDTO.getEvento());
        System.out.println("   📊 DetalleFinalizacion: " + accionDTO.getDetalleFinalizacion());
        System.out.println("   📊 ZonaLanzamiento: " + accionDTO.getZonaLanzamiento());
        System.out.println("   📊 DetalleEvento: " + accionDTO.getDetalleEvento());
        
        switch (accionDTO.getEvento()) {
            case Gol:
                System.out.println("   ⚽ Validando evento 'Gol'");
                if (accionDTO.getDetalleFinalizacion() == null || accionDTO.getZonaLanzamiento() == null) {
                    System.out.println("   ❌ ERROR: Para evento 'Gol', detalle_finalizacion y zona_lanzamiento son obligatorios");
                    System.out.println("   💡 DetalleFinalizacion: " + accionDTO.getDetalleFinalizacion() + " (debe ser no nulo)");
                    System.out.println("   💡 ZonaLanzamiento: " + accionDTO.getZonaLanzamiento() + " (debe ser no nulo)");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "GOAL_REQUIRED_FIELDS", "Para evento 'Gol', detalle_finalizacion y zona_lanzamiento son obligatorios");
                }
                if (accionDTO.getDetalleEvento() != null) {
                    System.out.println("   ❌ ERROR: Para evento 'Gol', detalle_evento debe ser nulo");
                    System.out.println("   💡 DetalleEvento actual: " + accionDTO.getDetalleEvento() + " (debe ser nulo)");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "GOAL_INVALID_DETAIL", "Para evento 'Gol', detalle_evento debe ser nulo");
                }
                System.out.println("   ✅ Evento 'Gol' validado correctamente");
                break;
                
            case Lanzamiento_Parado:
                System.out.println("   🛡️ Validando evento 'Lanzamiento_Parado'");
                if (accionDTO.getDetalleFinalizacion() == null || accionDTO.getZonaLanzamiento() == null) {
                    System.out.println("   ❌ ERROR: Para evento 'Lanzamiento_Parado', detalle_finalizacion y zona_lanzamiento son obligatorios");
                    System.out.println("   💡 DetalleFinalizacion: " + accionDTO.getDetalleFinalizacion() + " (debe ser no nulo)");
                    System.out.println("   💡 ZonaLanzamiento: " + accionDTO.getZonaLanzamiento() + " (debe ser no nulo)");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_STOPPED_REQUIRED_FIELDS", "Para evento 'Lanzamiento_Parado', detalle_finalizacion y zona_lanzamiento son obligatorios");
                }
                if (accionDTO.getDetalleEvento() == null) {
                    System.out.println("   ❌ ERROR: Para evento 'Lanzamiento_Parado', detalle_evento es obligatorio");
                    System.out.println("   💡 DetalleEvento: " + accionDTO.getDetalleEvento() + " (debe ser no nulo)");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_STOPPED_REQUIRED_DETAIL", "Para evento 'Lanzamiento_Parado', detalle_evento es obligatorio");
                }
                if (accionDTO.getDetalleEvento() != DetalleEvento.Parada_Portero && 
                    accionDTO.getDetalleEvento() != DetalleEvento.Bloqueo_Defensor) {
                    System.out.println("   ❌ ERROR: Para evento 'Lanzamiento_Parado', detalle_evento debe ser específico");
                    System.out.println("   💡 Valores válidos: Parada_Portero, Bloqueo_Defensor");
                    System.out.println("   💡 Valor actual: " + accionDTO.getDetalleEvento());
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_STOPPED_INVALID_DETAIL", "Para evento 'Lanzamiento_Parado', detalle_evento debe ser 'Parada_Portero' o 'Bloqueo_Defensor'");
                }
                System.out.println("   ✅ Evento 'Lanzamiento_Parado' validado correctamente");
                break;
                
            case Lanzamiento_Fuera:
                System.out.println("   🎯 Validando evento 'Lanzamiento_Fuera'");
                if (accionDTO.getDetalleFinalizacion() == null || accionDTO.getZonaLanzamiento() == null) {
                    System.out.println("   ❌ ERROR: Para evento 'Lanzamiento_Fuera', detalle_finalizacion y zona_lanzamiento son obligatorios");
                    System.out.println("   💡 DetalleFinalizacion: " + accionDTO.getDetalleFinalizacion() + " (debe ser no nulo)");
                    System.out.println("   💡 ZonaLanzamiento: " + accionDTO.getZonaLanzamiento() + " (debe ser no nulo)");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_MISSED_REQUIRED_FIELDS", "Para evento 'Lanzamiento_Fuera', detalle_finalizacion y zona_lanzamiento son obligatorios");
                }
                if (accionDTO.getDetalleEvento() == null) {
                    System.out.println("   ❌ ERROR: Para evento 'Lanzamiento_Fuera', detalle_evento es obligatorio");
                    System.out.println("   💡 DetalleEvento: " + accionDTO.getDetalleEvento() + " (debe ser no nulo)");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_MISSED_REQUIRED_DETAIL", "Para evento 'Lanzamiento_Fuera', detalle_evento es obligatorio");
                }
                if (accionDTO.getDetalleEvento() != DetalleEvento.Palo && 
                    accionDTO.getDetalleEvento() != DetalleEvento.Fuera_Directo) {
                    System.out.println("   ❌ ERROR: Para evento 'Lanzamiento_Fuera', detalle_evento debe ser específico");
                    System.out.println("   💡 Valores válidos: Palo, Fuera_Directo");
                    System.out.println("   💡 Valor actual: " + accionDTO.getDetalleEvento());
                    throw new ApiException(HttpStatus.BAD_REQUEST, "SHOT_MISSED_INVALID_DETAIL", "Para evento 'Lanzamiento_Fuera', detalle_evento debe ser 'Palo' o 'Fuera_Directo'");
                }
                System.out.println("   ✅ Evento 'Lanzamiento_Fuera' validado correctamente");
                break;
                
            case Perdida:
                System.out.println("   💥 Validando evento 'Perdida'");
                if (accionDTO.getDetalleFinalizacion() != null || accionDTO.getZonaLanzamiento() != null) {
                    System.out.println("   ❌ ERROR: Para evento 'Perdida', detalle_finalizacion y zona_lanzamiento deben ser nulos");
                    System.out.println("   💡 DetalleFinalizacion: " + accionDTO.getDetalleFinalizacion() + " (debe ser nulo)");
                    System.out.println("   💡 ZonaLanzamiento: " + accionDTO.getZonaLanzamiento() + " (debe ser nulo)");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "TURNOVER_INVALID_FIELDS", "Para evento 'Perdida', detalle_finalizacion y zona_lanzamiento deben ser nulos");
                }
                if (accionDTO.getDetalleEvento() == null) {
                    System.out.println("   ❌ ERROR: Para evento 'Perdida', detalle_evento es obligatorio");
                    System.out.println("   💡 DetalleEvento: " + accionDTO.getDetalleEvento() + " (debe ser no nulo)");
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
                    System.out.println("   ❌ ERROR: Para evento 'Perdida', detalle_evento debe ser uno de los valores específicos");
                    System.out.println("   💡 Valores válidos: Pasos, Dobles, FaltaAtaque, Pasivo, InvasionArea, Robo, Pie, BalonFuera");
                    System.out.println("   💡 Valor actual: " + accionDTO.getDetalleEvento());
                    throw new ApiException(HttpStatus.BAD_REQUEST, "TURNOVER_INVALID_DETAIL", "Para evento 'Perdida', detalle_evento debe ser uno de los valores válidos para pérdida");
                }
                System.out.println("   ✅ Evento 'Perdida' validado correctamente");
                break;
        }
        
        System.out.println("   ✅ [REGLA 3] Validación de evento principal completada exitosamente");
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
                    throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CONTINUOUS_GAME_SEQUENCE", "Para origen_accion 'Juego_Continuado', la acción anterior debe tener cambio_posesion = true");
                }
            }
        } else {
            // Si no hay acción anterior, solo 'Juego_Continuado' es válido (inicio de posesión)
            if (accionDTO.getOrigenAccion() != OrigenAccion.Juego_Continuado) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_FIRST_ACTION", "Para la primera acción del partido, origen_accion debe ser 'Juego_Continuado'");
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