package com.HandballStats_Pro.handballstatspro.services;

import com.HandballStats_Pro.handballstatspro.dto.*;
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

import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccionService {

    private final AccionRepository accionRepository;
    private final PartidoRepository partidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PartidoService partidoService;

    public AccionService(AccionRepository accionRepository, 
                        PartidoRepository partidoRepository,
                        UsuarioRepository usuarioRepository,
                        PartidoService partidoService) {
        System.out.println("=== INICIALIZANDO AccionService ===");
        this.accionRepository = accionRepository;
        this.partidoRepository = partidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.partidoService = partidoService;
        System.out.println("✓ Servicio AccionService creado exitosamente");
    }

    @Transactional
    public AccionResponseDTO crearAccion(AccionDTO accionDTO) {
        System.out.println("=== INICIANDO PROCESO DE CREACIÓN DE ACCIÓN ===");
        System.out.println("Datos recibidos: " + accionDTO);

        System.out.println("PASO PREVIO: Convirtiendo Strings a Enums...");
        EquipoAccion equipoAccion = EquipoAccion.valueOf(accionDTO.getEquipoAccion());
        TipoAtaque tipoAtaque = TipoAtaque.valueOf(accionDTO.getTipoAtaque());
        OrigenAccion origenAccion = OrigenAccion.valueOf(accionDTO.getOrigenAccion());
        Evento evento = Evento.valueOf(accionDTO.getEvento());
        DetalleFinalizacion detalleFinalizacion = (accionDTO.getDetalleFinalizacion() != null) ? DetalleFinalizacion.valueOf(accionDTO.getDetalleFinalizacion()) : null;
        ZonaLanzamiento zonaLanzamiento = (accionDTO.getZonaLanzamiento() != null) ? ZonaLanzamiento.valueOf(accionDTO.getZonaLanzamiento()) : null;
        DetalleEvento detalleEvento = (accionDTO.getDetalleEvento() != null) ? DetalleEvento.valueOf(accionDTO.getDetalleEvento()) : null;
        System.out.println("✓ Conversión completada.");

        System.out.println("PASO 1: Verificando existencia del partido ID: " + accionDTO.getIdPartido());
        Partido partido = partidoRepository.findById(accionDTO.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(accionDTO.getIdPartido())));
        System.out.println("✓ Partido encontrado: " + partido.getNombreEquipoLocal() + " vs " + partido.getNombreEquipoVisitante());

        System.out.println("PASO 2: Verificando permisos de usuario");
        if (!tienePermisoCrearAccion(partido)) {
            System.out.println("ERROR: Usuario sin permisos para crear acciones en este partido");
            throw new PermissionDeniedException();
        }
        System.out.println("✓ Usuario tiene permisos para crear acciones");

        System.out.println("PASO 3: Validando reglas de balonmano");
        validarReglamento(tipoAtaque, origenAccion, evento, detalleFinalizacion, zonaLanzamiento, detalleEvento);
        System.out.println("✓ Todas las reglas de balonmano son válidas");

        System.out.println("PASO 4: Calculando cambio de posesión");
        Boolean cambioPosesion = calcularCambioPosesion(evento, detalleEvento);
        System.out.println("✓ Cambio de posesión calculado: " + cambioPosesion);

        System.out.println("PASO 5: Validando lógica secuencial");
        validarLogicaSecuencial(accionDTO.getIdPartido(), origenAccion, equipoAccion);
        System.out.println("✓ Lógica secuencial válida");

        System.out.println("PASO 6: Creando entidad Accion");
        Accion accion = mapToEntity(accionDTO, equipoAccion, tipoAtaque, origenAccion, evento, detalleFinalizacion, zonaLanzamiento, detalleEvento, cambioPosesion);
        System.out.println("Entidad Accion creada: " + accion);
        Accion nuevaAccion = accionRepository.save(accion);
        System.out.println("✓ Acción guardada con ID: " + nuevaAccion.getIdAccion());

        System.out.println("=== PROCESO DE CREACIÓN COMPLETADO EXITOSAMENTE ===");
        return mapToResponseDTO(nuevaAccion, partido);
    }

    public List<AccionResponseDTO> listarAccionesPorPartido(Integer idPartido) {
        System.out.println("=== LISTANDO ACCIONES DEL PARTIDO ID: " + idPartido + " ===");
        
        // Verificar que el partido existe
        System.out.println("Buscando partido con ID: " + idPartido);
        Partido partido = partidoRepository.findById(idPartido)
                .orElseThrow(() -> {
                    System.out.println("ERROR: Partido no encontrado con ID: " + idPartido);
                    return new ResourceNotFoundException("Partido", "id", String.valueOf(idPartido));
                });
        System.out.println("✓ Partido encontrado: " + partido.getNombreEquipoLocal() + " vs " + partido.getNombreEquipoVisitante());

        // Verificar permisos
        System.out.println("Verificando permisos para acceder al partido");
        if (!partidoService.puedeAccederPartido(partido)) {
            System.out.println("ERROR: Usuario sin permisos para ver acciones de este partido");
            throw new PermissionDeniedException();
        }
        System.out.println("✓ Permisos verificados");

        System.out.println("Buscando acciones para el partido...");
        List<Accion> acciones = accionRepository.findByIdPartidoOrderByIdAccionAsc(idPartido);
        System.out.println("✓ Se encontraron " + acciones.size() + " acciones");

        System.out.println("Mapeando acciones a DTOs...");
        List<AccionResponseDTO> resultado = acciones.stream()
                .map(accion -> mapToResponseDTO(accion, partido))
                .collect(Collectors.toList());
        System.out.println("✓ Mapeo completado");

        return resultado;
    }

    public AccionResponseDTO obtenerAccionPorId(Integer idAccion) {
        System.out.println("=== OBTENIENDO ACCIÓN ID: " + idAccion + " ===");
        
        System.out.println("Buscando acción con ID: " + idAccion);
        Accion accion = accionRepository.findById(idAccion)
                .orElseThrow(() -> {
                    System.out.println("ERROR: Acción no encontrada con ID: " + idAccion);
                    return new ResourceNotFoundException("Accion", "id", String.valueOf(idAccion));
                });
        System.out.println("✓ Acción encontrada");

        System.out.println("Buscando partido asociado con ID: " + accion.getIdPartido());
        Partido partido = partidoRepository.findById(accion.getIdPartido())
                .orElseThrow(() -> {
                    System.out.println("ERROR: Partido no encontrado con ID: " + accion.getIdPartido());
                    return new ResourceNotFoundException("Partido", "id", String.valueOf(accion.getIdPartido()));
                });
        System.out.println("✓ Partido encontrado");

        // Verificar permisos
        System.out.println("Verificando permisos para acceder al partido");
        if (!partidoService.puedeAccederPartido(partido)) {
            System.out.println("ERROR: Usuario sin permisos para ver esta acción");
            throw new PermissionDeniedException();
        }
        System.out.println("✓ Permisos verificados");

        System.out.println("Mapeando acción a DTO...");
        AccionResponseDTO resultado = mapToResponseDTO(accion, partido);
        System.out.println("✓ Mapeo completado");

        return resultado;
    }

    @Transactional
    public AccionResponseDTO actualizarAccion(Integer idAccion, AccionUpdateDTO updateDTO) {
        System.out.println("=== INICIANDO ACTUALIZACIÓN DE ACCIÓN ID: " + idAccion + " ===");
        System.out.println("Datos de actualización: " + updateDTO);

        Accion accion = accionRepository.findById(idAccion)
                .orElseThrow(() -> new ResourceNotFoundException("Accion", "id", String.valueOf(idAccion)));
        System.out.println("✓ Acción encontrada");

        Partido partido = partidoRepository.findById(accion.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(accion.getIdPartido())));
        System.out.println("✓ Partido encontrado");

        System.out.println("PASO 1: Verificando permisos de edición");
        if (!puedeEditarAccion(accion)) {
            throw new PermissionDeniedException();
        }
        System.out.println("✓ Usuario tiene permisos para editar");

        System.out.println("Creando estado actualizado para validación...");
        EquipoAccion equipoAccion = (updateDTO.getEquipoAccion() != null) ? EquipoAccion.valueOf(updateDTO.getEquipoAccion()) : accion.getEquipoAccion();
        TipoAtaque tipoAtaque = (updateDTO.getTipoAtaque() != null) ? TipoAtaque.valueOf(updateDTO.getTipoAtaque()) : accion.getTipoAtaque();
        OrigenAccion origenAccion = (updateDTO.getOrigenAccion() != null) ? OrigenAccion.valueOf(updateDTO.getOrigenAccion()) : accion.getOrigenAccion();
        Evento evento = (updateDTO.getEvento() != null) ? Evento.valueOf(updateDTO.getEvento()) : accion.getEvento();
        DetalleFinalizacion detalleFinalizacion = (updateDTO.getDetalleFinalizacion() != null) ? DetalleFinalizacion.valueOf(updateDTO.getDetalleFinalizacion()) : accion.getDetalleFinalizacion();
        ZonaLanzamiento zonaLanzamiento = (updateDTO.getZonaLanzamiento() != null) ? ZonaLanzamiento.valueOf(updateDTO.getZonaLanzamiento()) : accion.getZonaLanzamiento();
        DetalleEvento detalleEvento = (updateDTO.getDetalleEvento() != null) ? DetalleEvento.valueOf(updateDTO.getDetalleEvento()) : accion.getDetalleEvento();
        System.out.println("✓ Estado actualizado creado.");

        System.out.println("PASO 2: Validando reglas de balonmano");
        validarReglamento(tipoAtaque, origenAccion, evento, detalleFinalizacion, zonaLanzamiento, detalleEvento);
        System.out.println("✓ Reglas de balonmano válidas");

        System.out.println("PASO 3: Calculando cambio de posesión");
        Boolean cambioPosesion = calcularCambioPosesion(evento, detalleEvento);
        System.out.println("✓ Cambio de posesión calculado: " + cambioPosesion);

        System.out.println("PASO 4: Actualizando campos de la acción");
        if (updateDTO.getIdPosesion() != null) accion.setIdPosesion(updateDTO.getIdPosesion());
        accion.setEquipoAccion(equipoAccion);
        accion.setTipoAtaque(tipoAtaque);
        accion.setOrigenAccion(origenAccion);
        accion.setEvento(evento);
        accion.setDetalleFinalizacion(detalleFinalizacion);
        accion.setZonaLanzamiento(zonaLanzamiento);
        accion.setDetalleEvento(detalleEvento);
        accion.setCambioPosesion(cambioPosesion);
        System.out.println("✓ Campos actualizados");

        Accion accionActualizada = accionRepository.save(accion);
        System.out.println("✓ Acción actualizada exitosamente");

        System.out.println("=== ACTUALIZACIÓN COMPLETADA ===");
        return mapToResponseDTO(accionActualizada, partido);
    }

    @Transactional
    public void eliminarAccion(Integer idAccion) {
        System.out.println("=== INICIANDO ELIMINACIÓN DE ACCIÓN ID: " + idAccion + " ===");

        System.out.println("Buscando acción a eliminar...");
        Accion accion = accionRepository.findById(idAccion)
                .orElseThrow(() -> {
                    System.out.println("ERROR: Acción no encontrada con ID: " + idAccion);
                    return new ResourceNotFoundException("Accion", "id", String.valueOf(idAccion));
                });
        System.out.println("✓ Acción encontrada");

        // Verificar permisos de eliminación
        System.out.println("PASO 1: Verificando permisos de eliminación");
        if (!puedeEditarAccion(accion)) {
            System.out.println("ERROR: Usuario sin permisos para eliminar esta acción");
            throw new PermissionDeniedException();
        }
        System.out.println("✓ Usuario tiene permisos para eliminar");

        System.out.println("Eliminando acción...");
        accionRepository.delete(accion);
        System.out.println("✓ Acción eliminada exitosamente");
        System.out.println("=== ELIMINACIÓN COMPLETADA ===");
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    private void validarReglamento(TipoAtaque tipoAtaque, OrigenAccion origenAccion, Evento evento, DetalleFinalizacion detalleFinalizacion, ZonaLanzamiento zonaLanzamiento, DetalleEvento detalleEvento) {
        System.out.println("--- Validando Regla 1: Caso especial de 7 metros ---");
        validarRegla1_CasoEspecial7Metros(origenAccion, detalleFinalizacion, tipoAtaque, zonaLanzamiento);

        System.out.println("--- Validando Regla 2: Lógica del tipo de ataque ---");
        validarRegla2_LogicaTipoAtaque(tipoAtaque, detalleFinalizacion);

        System.out.println("--- Validando Regla 3: Lógica del evento principal ---");
        validarRegla3_LogicaEventoPrincipal(evento, detalleFinalizacion, zonaLanzamiento, detalleEvento);
    }

    private void validarRegla1_CasoEspecial7Metros(OrigenAccion origenAccion, DetalleFinalizacion detalleFinalizacion, TipoAtaque tipoAtaque, ZonaLanzamiento zonaLanzamiento) {
        boolean origenEs7m = (origenAccion == OrigenAccion._7m);
        boolean detalleEs7m = (detalleFinalizacion == DetalleFinalizacion._7m);
        boolean zonaEs7m = (zonaLanzamiento == ZonaLanzamiento._7m);

        // Si el origen es un 7 metros...
        if (origenEs7m) {
            System.out.println("  → Origen es 7m, verificando consistencia...");
            if (!detalleEs7m) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Si origen_accion es '_7m', detalle_finalizacion también debe ser '_7m'.");
            }
            if (!zonaEs7m) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Si origen_accion es '_7m', zona_lanzamiento debe ser '_7m'.");
            }
            if (tipoAtaque != TipoAtaque.Posicional) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Si origen_accion es '_7m', tipo_ataque debe ser 'Posicional'.");
            }
        }

        // Comprobaciones inversas para asegurar que los valores de 7m no se usen incorrectamente
        if (detalleEs7m && !origenEs7m) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Si detalle_finalizacion es '_7m', el origen_accion también debe ser '_7m'.");
        }
        
        if (zonaEs7m && !origenEs7m) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "La zona_lanzamiento '_7m' solo puede usarse si origen_accion es '_7m'.");
        }
    }

    private void validarRegla2_LogicaTipoAtaque(TipoAtaque tipoAtaque, DetalleFinalizacion detalleFinalizacion) {
        Set<DetalleFinalizacion> detallesContraataque = Set.of(
                DetalleFinalizacion.Contragol,
                DetalleFinalizacion.Primera_Oleada,
                DetalleFinalizacion.Segunda_Oleada,
                DetalleFinalizacion.Tercera_Oleada
        );

        if (tipoAtaque == TipoAtaque.Contraataque) {
            System.out.println("  → Tipo ataque es Contraataque, verificando detalle_finalizacion");
            if (detalleFinalizacion == null || !detallesContraataque.contains(detalleFinalizacion)) {
                System.out.println("  ERROR: Si tipo_ataque es Contraataque, detalle_finalizacion debe ser Contragol, 1ª oleada, 2ª oleada o 3ª oleada");
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Si tipo_ataque es 'Contraataque', detalle_finalizacion debe ser 'Contragol', '1ª oleada', '2ª oleada' o '3ª oleada'");
            }
            System.out.println("  ✓ Tipo ataque Contraataque válido");
        }

        if (tipoAtaque == TipoAtaque.Posicional) {
            System.out.println("  → Tipo ataque es Posicional, verificando que no sea detalle de contraataque");
            if (detalleFinalizacion != null && detallesContraataque.contains(detalleFinalizacion)) {
                System.out.println("  ERROR: Si tipo_ataque es Posicional, detalle_finalizacion no puede ser de contraataque");
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Si tipo_ataque es 'Posicional', detalle_finalizacion no puede ser 'Contragol', '1ª oleada', '2ª oleada' o '3ª oleada'");
            }
            System.out.println("  ✓ Tipo ataque Posicional válido");
        }
    }

    private void validarRegla3_LogicaEventoPrincipal(Evento evento, DetalleFinalizacion detalleFinalizacion, ZonaLanzamiento zonaLanzamiento, DetalleEvento detalleEvento) {
        System.out.println("  → Validando evento: " + evento);

        switch (evento) {
            case Gol:
                System.out.println("    Evento es Gol - verificando campos obligatorios y nulos");
                if (detalleFinalizacion == null) {
                    System.out.println("    ERROR: Para Gol, detalle_finalizacion es obligatorio");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Gol', detalle_finalizacion es obligatorio");
                }
                if (zonaLanzamiento == null) {
                    System.out.println("    ERROR: Para Gol, zona_lanzamiento es obligatorio");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Gol', zona_lanzamiento es obligatorio");
                }
                if (detalleEvento != null) {
                    System.out.println("    ERROR: Para Gol, detalle_evento debe ser nulo");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Gol', detalle_evento debe ser nulo");
                }
                System.out.println("    ✓ Evento Gol válido");
                break;

            case Lanzamiento_Parado:
                System.out.println("    Evento es Lanzamiento_Parado - verificando campos");
                if (detalleFinalizacion == null) {
                    System.out.println("    ERROR: Para Lanzamiento_Parado, detalle_finalizacion es obligatorio");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Lanzamiento_Parado', detalle_finalizacion es obligatorio");
                }
                if (zonaLanzamiento == null) {
                    System.out.println("    ERROR: Para Lanzamiento_Parado, zona_lanzamiento es obligatorio");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Lanzamiento_Parado', zona_lanzamiento es obligatorio");
                }
                if (detalleEvento == null) {
                    System.out.println("    ERROR: Para Lanzamiento_Parado, detalle_evento es obligatorio");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Lanzamiento_Parado', detalle_evento es obligatorio");
                }
                if (detalleEvento != DetalleEvento.Parada_Portero && detalleEvento != DetalleEvento.Bloqueo_Defensor) {
                    System.out.println("    ERROR: Para Lanzamiento_Parado, detalle_evento debe ser Parada_Portero o Bloqueo_Defensor");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Lanzamiento_Parado', detalle_evento debe ser 'Parada_Portero' o 'Bloqueo_Defensor'");
                }
                System.out.println("    ✓ Evento Lanzamiento_Parado válido");
                break;

            case Lanzamiento_Fuera:
                System.out.println("    Evento es Lanzamiento_Fuera - verificando campos");
                if (detalleFinalizacion == null) {
                    System.out.println("    ERROR: Para Lanzamiento_Fuera, detalle_finalizacion es obligatorio");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Lanzamiento_Fuera', detalle_finalizacion es obligatorio");
                }
                if (zonaLanzamiento == null) {
                    System.out.println("    ERROR: Para Lanzamiento_Fuera, zona_lanzamiento es obligatorio");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Lanzamiento_Fuera', zona_lanzamiento es obligatorio");
                }
                if (detalleEvento == null) {
                    System.out.println("    ERROR: Para Lanzamiento_Fuera, detalle_evento es obligatorio");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Lanzamiento_Fuera', detalle_evento es obligatorio");
                }
                if (detalleEvento != DetalleEvento.Palo && detalleEvento != DetalleEvento.Fuera_Directo) {
                    System.out.println("    ERROR: Para Lanzamiento_Fuera, detalle_evento debe ser Palo o Fuera_Directo");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Lanzamiento_Fuera', detalle_evento debe ser 'Palo' o 'Fuera_Directo'");
                }
                System.out.println("    ✓ Evento Lanzamiento_Fuera válido");
                break;

            case Perdida:
                System.out.println("    Evento es Perdida - verificando campos");
                if (detalleFinalizacion != null) {
                    System.out.println("    ERROR: Para Perdida, detalle_finalizacion debe ser nulo");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Perdida', detalle_finalizacion debe ser nulo");
                }
                if (zonaLanzamiento != null) {
                    System.out.println("    ERROR: Para Perdida, zona_lanzamiento debe ser nulo");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Perdida', zona_lanzamiento debe ser nulo");
                }
                if (detalleEvento == null) {
                    System.out.println("    ERROR: Para Perdida, detalle_evento es obligatorio");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Perdida', detalle_evento es obligatorio");
                }
                Set<DetalleEvento> detallesValidosPerdida = Set.of(
                        DetalleEvento.Pasos, DetalleEvento.Dobles, DetalleEvento.FaltaAtaque,
                        DetalleEvento.Pasivo, DetalleEvento.InvasionArea, DetalleEvento.Robo,
                        DetalleEvento.Pie, DetalleEvento.BalonFuera
                );
                if (!detallesValidosPerdida.contains(detalleEvento)) {
                    System.out.println("    ERROR: Para Perdida, detalle_evento no es válido");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para evento 'Perdida', detalle_evento debe ser uno de: Pasos, Dobles, FaltaAtaque, Pasivo, InvasionArea, Robo, Pie, BalonFuera");
                }
                System.out.println("    ✓ Evento Perdida válido");
                break;
        }
    }

    private Boolean calcularCambioPosesion(Evento evento, DetalleEvento detalleEvento) {
        System.out.println("  → Calculando cambio de posesión para evento: " + evento + ", detalle: " + detalleEvento);
        
        // cambio_posesion será false solo en estos casos específicos
        boolean noHayCambio = (evento == Evento.Lanzamiento_Parado && detalleEvento == DetalleEvento.Parada_Portero) ||
                             (evento == Evento.Lanzamiento_Parado && detalleEvento == DetalleEvento.Bloqueo_Defensor) ||
                             (evento == Evento.Lanzamiento_Fuera && detalleEvento == DetalleEvento.Palo);
        
        Boolean resultado = !noHayCambio;
        System.out.println("    Resultado: " + resultado + " (no cambio: " + noHayCambio + ")");
        return resultado;
    }

    private void validarLogicaSecuencial(Integer idPartido, OrigenAccion origen, EquipoAccion equipoAccionActual) {
        if (origen == OrigenAccion._7m) {
            System.out.println("  → Origen es 7m, no aplica validación secuencial");
            return;
        }

        System.out.println("Buscando última acción del partido...");
        Optional<Accion> accionAnteriorOpt = accionRepository.findUltimaAccionDelPartido(idPartido);

        if (accionAnteriorOpt.isPresent()) {
            Accion accionAnterior = accionAnteriorOpt.get();
            System.out.println("    Acción anterior encontrada ID: " + accionAnterior.getIdAccion() + ", cambio_posesion: " + accionAnterior.getCambioPosesion() + ", equipo: " + accionAnterior.getEquipoAccion());

            // Si la acción anterior NO tuvo cambio de posesión...
            if (!accionAnterior.getCambioPosesion()) {
                System.out.println("  → No hubo cambio de posesión. Verificando consistencia de equipo.");
                // ...el equipo de la acción actual DEBE ser el mismo que el de la acción anterior.
                if (accionAnterior.getEquipoAccion() != equipoAccionActual) {
                    System.out.println("    ERROR: La acción anterior no tuvo cambio de posesión, por lo que el equipo debe ser el mismo.");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Conflicto de posesión: La acción anterior no tuvo cambio de posesión, el equipo debe ser el mismo.");
                }
            }
        }

        if (origen == OrigenAccion.Juego_Continuado) {
            System.out.println("  → Origen es Juego_Continuado, verificando acción anterior");
            if (accionAnteriorOpt.isPresent()) {
                Accion accionAnterior = accionAnteriorOpt.get();
                System.out.println("    Acción anterior encontrada ID: " + accionAnterior.getIdAccion() + ", cambio_posesion: " + accionAnterior.getCambioPosesion());
                if (!accionAnterior.getCambioPosesion()) {
                    System.out.println("    ERROR: Para Juego_Continuado, la acción anterior debe tener cambio_posesion = true");
                    throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para origen_accion 'Juego_Continuado', la acción anterior debe tener cambio_posesion = true");
                }
            } else {
                System.out.println("    No hay acción anterior, válido para inicio de posesión");
            }
            System.out.println("    ✓ Juego_Continuado válido");
        }

        if (origen == OrigenAccion.Rebote_Directo || origen == OrigenAccion.Rebote_Indirecto) {
            System.out.println("  → Origen es rebote, verificando acción anterior");
            if (accionAnteriorOpt.isEmpty()) {
                System.out.println("    ERROR: Para rebote debe existir una acción anterior");
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para origen_accion de rebote debe existir una acción anterior");
            }

            Accion accionAnterior = accionAnteriorOpt.get();
            System.out.println("    Acción anterior encontrada ID: " + accionAnterior.getIdAccion() + ", cambio_posesion: " + accionAnterior.getCambioPosesion());
            if (accionAnterior.getCambioPosesion()) {
                System.out.println("    ERROR: Para rebote, la acción anterior debe tener cambio_posesion = false");
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Para origen_accion de rebote, la acción anterior debe tener cambio_posesion = false");
            }
            System.out.println("    ✓ Rebote válido");
        }
    }

    private boolean tienePermisoCrearAccion(Partido partido) {
        System.out.println("Verificando permisos para crear acción...");
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        System.out.println("  → Usuario actual: " + usuario.getEmail() + ", rol: " + rol);

        if ("ROLE_Admin".equals(rol)) {
            System.out.println("    Usuario es Admin, tiene todos los permisos");
            return true;
        }

        System.out.println("Verificando acceso al partido...");
        boolean puedeAcceder = partidoService.puedeAccederPartido(partido);
        System.out.println("  → Resultado verificación acceso: " + puedeAcceder);
        return puedeAcceder;
    }

    private boolean puedeEditarAccion(Accion accion) {
        System.out.println("Verificando permisos para editar acción...");
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        System.out.println("  → Usuario actual: " + usuario.getEmail() + ", rol: " + rol);

        if ("ROLE_Admin".equals(rol)) {
            System.out.println("    Usuario es Admin, puede editar cualquier acción");
            return true;
        }

        // Verificar si es la última acción del partido
        System.out.println("Buscando última acción del partido...");
        Optional<Accion> ultimaAccionOpt = accionRepository.findUltimaAccionDelPartido(accion.getIdPartido());
        if (ultimaAccionOpt.isPresent()) {
            System.out.println("  → Última acción encontrada ID: " + ultimaAccionOpt.get().getIdAccion());
            if (ultimaAccionOpt.get().getIdAccion().equals(accion.getIdAccion())) {
                System.out.println("    Es la última acción del partido, verificando permisos del partido");
                Partido partido = partidoRepository.findById(accion.getIdPartido()).get();
                boolean puedeAcceder = partidoService.puedeAccederPartido(partido);
                System.out.println("  → Resultado verificación acceso: " + puedeAcceder);
                return puedeAcceder;
            }
        }

        System.out.println("    No es la última acción y usuario no es Admin");
        return false;
    }

    // MÉTODOS DE UTILIDAD

    private Usuario obtenerUsuarioActual() {
        System.out.println("Obteniendo usuario actual...");
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        System.out.println("  → Username: " + username);
        
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> {
                    System.out.println("ERROR: Usuario no encontrado con email: " + username);
                    return new ResourceNotFoundException("Usuario", "email", username);
                });
        System.out.println("  → Usuario encontrado: " + usuario.getEmail());
        return usuario;
    }

    private String obtenerRolUsuario() {
        System.out.println("Obteniendo rol del usuario actual...");
        String rol = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        System.out.println("  → Rol obtenido: " + rol);
        return rol;
    }

    private Accion mapToEntity(AccionDTO dto, EquipoAccion equipoAccion, TipoAtaque tipoAtaque, OrigenAccion origenAccion, Evento evento, DetalleFinalizacion detalleFinalizacion, ZonaLanzamiento zonaLanzamiento, DetalleEvento detalleEvento, Boolean cambioPosesion) {
        System.out.println("Mapeando DTO a entidad Accion...");
        Accion accion = new Accion();
        accion.setIdPartido(dto.getIdPartido());
        accion.setIdPosesion(dto.getIdPosesion());
        accion.setEquipoAccion(equipoAccion);
        accion.setTipoAtaque(tipoAtaque);
        accion.setOrigenAccion(origenAccion);
        accion.setEvento(evento);
        accion.setDetalleFinalizacion(detalleFinalizacion);
        accion.setZonaLanzamiento(zonaLanzamiento);
        accion.setDetalleEvento(detalleEvento);
        accion.setCambioPosesion(cambioPosesion);
        System.out.println("  → Entidad Accion mapeada: " + accion);
        return accion;
    }

    private AccionResponseDTO mapToResponseDTO(Accion accion, Partido partido) {
        System.out.println("Mapeando entidad Accion a ResponseDTO...");
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
        dto.setNombreEquipoLocal(partido.getNombreEquipoLocal());
        dto.setNombreEquipoVisitante(partido.getNombreEquipoVisitante());
        System.out.println("  → ResponseDTO mapeado: " + dto);
        return dto;
    }

    private AccionDTO crearDTOParaValidacion(Accion accionOriginal, AccionUpdateDTO updateDTO) {
        System.out.println("Creando DTO temporal para validación...");
        AccionDTO dto = new AccionDTO();
        dto.setIdPartido(accionOriginal.getIdPartido());
        dto.setIdPosesion(updateDTO.getIdPosesion() != null ? updateDTO.getIdPosesion() : accionOriginal.getIdPosesion());
        dto.setEquipoAccion(updateDTO.getEquipoAccion() != null ? updateDTO.getEquipoAccion() : accionOriginal.getEquipoAccion());
        dto.setTipoAtaque(updateDTO.getTipoAtaque() != null ? updateDTO.getTipoAtaque() : accionOriginal.getTipoAtaque());
        dto.setOrigenAccion(updateDTO.getOrigenAccion() != null ? updateDTO.getOrigenAccion() : accionOriginal.getOrigenAccion());
        dto.setEvento(updateDTO.getEvento() != null ? updateDTO.getEvento() : accionOriginal.getEvento());
        dto.setDetalleFinalizacion(updateDTO.getDetalleFinalizacion() != null ? updateDTO.getDetalleFinalizacion() : accionOriginal.getDetalleFinalizacion());
        dto.setZonaLanzamiento(updateDTO.getZonaLanzamiento() != null ? updateDTO.getZonaLanzamiento() : accionOriginal.getZonaLanzamiento());
        dto.setDetalleEvento(updateDTO.getDetalleEvento() != null ? updateDTO.getDetalleEvento() : accionOriginal.getDetalleEvento());
        System.out.println("  → DTO temporal creado: " + dto);
        return dto;
    }

    private void actualizarCamposAccion(Accion accion, AccionUpdateDTO updateDTO, Boolean cambioPosesion) {
        System.out.println("Actualizando campos de la acción...");
        if (updateDTO.getIdPosesion() != null) {
            System.out.println("  → Actualizando idPosesion: " + updateDTO.getIdPosesion());
            accion.setIdPosesion(updateDTO.getIdPosesion());
        }
        if (updateDTO.getEquipoAccion() != null) {
            System.out.println("  → Actualizando equipoAccion: " + updateDTO.getEquipoAccion());
            accion.setEquipoAccion(updateDTO.getEquipoAccion());
        }
        if (updateDTO.getTipoAtaque() != null) {
            System.out.println("  → Actualizando tipoAtaque: " + updateDTO.getTipoAtaque());
            accion.setTipoAtaque(updateDTO.getTipoAtaque());
        }
        if (updateDTO.getOrigenAccion() != null) {
            System.out.println("  → Actualizando origenAccion: " + updateDTO.getOrigenAccion());
            accion.setOrigenAccion(updateDTO.getOrigenAccion());
        }
        if (updateDTO.getEvento() != null) {
            System.out.println("  → Actualizando evento: " + updateDTO.getEvento());
            accion.setEvento(updateDTO.getEvento());
        }
        System.out.println("  → Actualizando detalleFinalizacion: " + updateDTO.getDetalleFinalizacion());
        accion.setDetalleFinalizacion(updateDTO.getDetalleFinalizacion());
        System.out.println("  → Actualizando zonaLanzamiento: " + updateDTO.getZonaLanzamiento());
        accion.setZonaLanzamiento(updateDTO.getZonaLanzamiento());
        System.out.println("  → Actualizando detalleEvento: " + updateDTO.getDetalleEvento());
        accion.setDetalleEvento(updateDTO.getDetalleEvento());
        System.out.println("  → Actualizando cambioPosesion: " + cambioPosesion);
        accion.setCambioPosesion(cambioPosesion);
        System.out.println("✓ Campos actualizados");
    }
}