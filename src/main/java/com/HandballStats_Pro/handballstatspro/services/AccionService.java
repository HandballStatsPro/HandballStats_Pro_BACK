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
        this.accionRepository = accionRepository;
        this.partidoRepository = partidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.partidoService = partidoService;
    }

    @Transactional
    public AccionResponseDTO crearAccion(AccionDTO accionDTO) {
        System.out.println("=== INICIANDO PROCESO DE CREACIÓN DE ACCIÓN ===");
        System.out.println("Datos recibidos: " + accionDTO);

        // 1. Verificar que el partido existe
        System.out.println("PASO 1: Verificando existencia del partido ID: " + accionDTO.getIdPartido());
        Partido partido = partidoRepository.findById(accionDTO.getIdPartido())
                .orElseThrow(() -> {
                    System.out.println("ERROR: Partido no encontrado con ID: " + accionDTO.getIdPartido());
                    return new ResourceNotFoundException("Partido", "id", String.valueOf(accionDTO.getIdPartido()));
                });
        System.out.println("✓ Partido encontrado: " + partido.getNombreEquipoLocal() + " vs " + partido.getNombreEquipoVisitante());

        // 2. Verificar permisos
        System.out.println("PASO 2: Verificando permisos de usuario");
        if (!tienePermisoCrearAccion(partido)) {
            System.out.println("ERROR: Usuario sin permisos para crear acciones en este partido");
            throw new PermissionDeniedException();
        }
        System.out.println("✓ Usuario tiene permisos para crear acciones");

        // 3. Validar reglas de balonmano
        System.out.println("PASO 3: Validando reglas de balonmano");
        validarReglamento(accionDTO);
        System.out.println("✓ Todas las reglas de balonmano son válidas");

        // 4. Calcular cambio de posesión
        System.out.println("PASO 4: Calculando cambio de posesión");
        Boolean cambioPosesion = calcularCambioPosesion(accionDTO.getEvento(), accionDTO.getDetalleEvento());
        accionDTO.setCambioPosesion(cambioPosesion);
        System.out.println("✓ Cambio de posesión calculado: " + cambioPosesion);

        // 5. Validar lógica secuencial
        System.out.println("PASO 5: Validando lógica secuencial");
        validarLogicaSecuencial(accionDTO);
        System.out.println("✓ Lógica secuencial válida");

        // 6. Crear y guardar la acción
        System.out.println("PASO 6: Creando entidad Accion");
        Accion accion = mapToEntity(accionDTO);
        Accion nuevaAccion = accionRepository.save(accion);
        System.out.println("✓ Acción guardada con ID: " + nuevaAccion.getIdAccion());

        System.out.println("=== PROCESO DE CREACIÓN COMPLETADO EXITOSAMENTE ===");
        return mapToResponseDTO(nuevaAccion, partido);
    }

    public List<AccionResponseDTO> listarAccionesPorPartido(Integer idPartido) {
        System.out.println("=== LISTANDO ACCIONES DEL PARTIDO ID: " + idPartido + " ===");
        
        // Verificar que el partido existe
        Partido partido = partidoRepository.findById(idPartido)
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(idPartido)));

        // Verificar permisos
        if (!partidoService.puedeAccederPartido(partido)) {
            System.out.println("ERROR: Usuario sin permisos para ver acciones de este partido");
            throw new PermissionDeniedException();
        }

        List<Accion> acciones = accionRepository.findByIdPartidoOrderByIdAccionAsc(idPartido);
        System.out.println("✓ Se encontraron " + acciones.size() + " acciones");

        return acciones.stream()
                .map(accion -> mapToResponseDTO(accion, partido))
                .collect(Collectors.toList());
    }

    public AccionResponseDTO obtenerAccionPorId(Integer idAccion) {
        System.out.println("=== OBTENIENDO ACCIÓN ID: " + idAccion + " ===");
        
        Accion accion = accionRepository.findById(idAccion)
                .orElseThrow(() -> new ResourceNotFoundException("Accion", "id", String.valueOf(idAccion)));

        Partido partido = partidoRepository.findById(accion.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(accion.getIdPartido())));

        // Verificar permisos
        if (!partidoService.puedeAccederPartido(partido)) {
            System.out.println("ERROR: Usuario sin permisos para ver esta acción");
            throw new PermissionDeniedException();
        }

        System.out.println("✓ Acción encontrada y permisos verificados");
        return mapToResponseDTO(accion, partido);
    }

    @Transactional
    public AccionResponseDTO actualizarAccion(Integer idAccion, AccionUpdateDTO updateDTO) {
        System.out.println("=== INICIANDO ACTUALIZACIÓN DE ACCIÓN ID: " + idAccion + " ===");
        System.out.println("Datos de actualización: " + updateDTO);

        Accion accion = accionRepository.findById(idAccion)
                .orElseThrow(() -> new ResourceNotFoundException("Accion", "id", String.valueOf(idAccion)));

        Partido partido = partidoRepository.findById(accion.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido", "id", String.valueOf(accion.getIdPartido())));

        // Verificar permisos de edición
        System.out.println("PASO 1: Verificando permisos de edición");
        if (!puedeEditarAccion(accion)) {
            System.out.println("ERROR: Usuario sin permisos para editar esta acción");
            throw new PermissionDeniedException();
        }
        System.out.println("✓ Usuario tiene permisos para editar");

        // Crear DTO temporal para validación
        AccionDTO dtoParaValidacion = crearDTOParaValidacion(accion, updateDTO);
        
        // Validar reglas de balonmano
        System.out.println("PASO 2: Validando reglas de balonmano");
        validarReglamento(dtoParaValidacion);
        System.out.println("✓ Reglas de balonmano válidas");

        // Calcular cambio de posesión
        System.out.println("PASO 3: Calculando cambio de posesión");
        Boolean cambioPosesion = calcularCambioPosesion(dtoParaValidacion.getEvento(), dtoParaValidacion.getDetalleEvento());
        System.out.println("✓ Cambio de posesión calculado: " + cambioPosesion);

        // Actualizar campos
        System.out.println("PASO 4: Actualizando campos de la acción");
        actualizarCamposAccion(accion, updateDTO, cambioPosesion);
        
        Accion accionActualizada = accionRepository.save(accion);
        System.out.println("✓ Acción actualizada exitosamente");

        System.out.println("=== ACTUALIZACIÓN COMPLETADA ===");
        return mapToResponseDTO(accionActualizada, partido);
    }

    @Transactional
    public void eliminarAccion(Integer idAccion) {
        System.out.println("=== INICIANDO ELIMINACIÓN DE ACCIÓN ID: " + idAccion + " ===");

        Accion accion = accionRepository.findById(idAccion)
                .orElseThrow(() -> new ResourceNotFoundException("Accion", "id", String.valueOf(idAccion)));

        // Verificar permisos de eliminación
        System.out.println("PASO 1: Verificando permisos de eliminación");
        if (!puedeEditarAccion(accion)) {
            System.out.println("ERROR: Usuario sin permisos para eliminar esta acción");
            throw new PermissionDeniedException();
        }
        System.out.println("✓ Usuario tiene permisos para eliminar");

        accionRepository.delete(accion);
        System.out.println("✓ Acción eliminada exitosamente");
        System.out.println("=== ELIMINACIÓN COMPLETADA ===");
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    private void validarReglamento(AccionDTO accionDTO) {
        System.out.println("--- Validando Regla 1: Caso especial de 7 metros ---");
        validarRegla1_CasoEspecial7Metros(accionDTO);
        
        System.out.println("--- Validando Regla 2: Lógica del tipo de ataque ---");
        validarRegla2_LogicaTipoAtaque(accionDTO);
        
        System.out.println("--- Validando Regla 3: Lógica del evento principal ---");
        validarRegla3_LogicaEventoPrincipal(accionDTO);
    }

    private void validarRegla1_CasoEspecial7Metros(AccionDTO accionDTO) {
        if (accionDTO.getOrigenAccion() == OrigenAccion._7m) {
            System.out.println("  → Origen es 7m, verificando detalle_finalizacion");
            if (accionDTO.getDetalleFinalizacion() != DetalleFinalizacion._7m) {
                System.out.println("  ERROR: Si origen_accion es 7m, detalle_finalizacion debe ser 7m");
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Si origen_accion es '7m', detalle_finalizacion debe ser '7m'");
            }
            System.out.println("  → Origen es 7m, verificando tipo_ataque");
            if (accionDTO.getTipoAtaque() != TipoAtaque.Posicional) {
                System.out.println("  ERROR: Si origen_accion es 7m, tipo_ataque debe ser Posicional");
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Si origen_accion es '7m', tipo_ataque debe ser 'Posicional'");
            }
            System.out.println("  ✓ Regla 7m válida");
        }

        if (accionDTO.getDetalleFinalizacion() == DetalleFinalizacion._7m) {
            System.out.println("  → Detalle_finalizacion es 7m, verificando origen");
            if (accionDTO.getOrigenAccion() != OrigenAccion._7m) {
                System.out.println("  ERROR: Si detalle_finalizacion es 7m, origen_accion debe ser 7m");
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Si detalle_finalizacion es '7m', origen_accion debe ser '7m'");
            }
            System.out.println("  ✓ Regla 7m inversa válida");
        }
    }

    private void validarRegla2_LogicaTipoAtaque(AccionDTO accionDTO) {
        Set<DetalleFinalizacion> detallesContraataque = Set.of(
            DetalleFinalizacion.Contragol,
            DetalleFinalizacion._1ª_oleada,
            DetalleFinalizacion._2ª_oleada,
            DetalleFinalizacion._3ª_oleada
        );

        if (accionDTO.getTipoAtaque() == TipoAtaque.Contraataque) {
            System.out.println("  → Tipo ataque es Contraataque, verificando detalle_finalizacion");
            if (accionDTO.getDetalleFinalizacion() == null || !detallesContraataque.contains(accionDTO.getDetalleFinalizacion())) {
                System.out.println("  ERROR: Si tipo_ataque es Contraataque, detalle_finalizacion debe ser Contragol, 1ª oleada, 2ª oleada o 3ª oleada");
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Si tipo_ataque es 'Contraataque', detalle_finalizacion debe ser 'Contragol', '1ª oleada', '2ª oleada' o '3ª oleada'");
            }
            System.out.println("  ✓ Tipo ataque Contraataque válido");
        }

        if (accionDTO.getTipoAtaque() == TipoAtaque.Posicional) {
            System.out.println("  → Tipo ataque es Posicional, verificando que no sea detalle de contraataque");
            if (accionDTO.getDetalleFinalizacion() != null && detallesContraataque.contains(accionDTO.getDetalleFinalizacion())) {
                System.out.println("  ERROR: Si tipo_ataque es Posicional, detalle_finalizacion no puede ser de contraataque");
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Si tipo_ataque es 'Posicional', detalle_finalizacion no puede ser 'Contragol', '1ª oleada', '2ª oleada' o '3ª oleada'");
            }
            System.out.println("  ✓ Tipo ataque Posicional válido");
        }
    }

    private void validarRegla3_LogicaEventoPrincipal(AccionDTO accionDTO) {
        Evento evento = accionDTO.getEvento();
        DetalleFinalizacion detalleFinalizacion = accionDTO.getDetalleFinalizacion();
        ZonaLanzamiento zonaLanzamiento = accionDTO.getZonaLanzamiento();
        DetalleEvento detalleEvento = accionDTO.getDetalleEvento();

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

    private void validarLogicaSecuencial(AccionDTO accionDTO) {
        OrigenAccion origen = accionDTO.getOrigenAccion();
        
        // El origen 7m no se rige por la regla secuencial
        if (origen == OrigenAccion._7m) {
            System.out.println("  → Origen es 7m, no aplica validación secuencial");
            return;
        }

        Optional<Accion> accionAnteriorOpt = accionRepository.findUltimaAccionDelPartido(accionDTO.getIdPartido());
        
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
            if (!accionAnteriorOpt.isPresent()) {
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
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        System.out.println("  → Verificando permisos para usuario: " + usuario.getEmail() + ", rol: " + rol);

        if ("ROLE_Admin".equals(rol)) {
            System.out.println("    Usuario es Admin, tiene todos los permisos");
            return true;
        }

        return partidoService.puedeAccederPartido(partido);
    }

    private boolean puedeEditarAccion(Accion accion) {
        Usuario usuario = obtenerUsuarioActual();
        String rol = obtenerRolUsuario();
        
        System.out.println("  → Verificando permisos de edición para usuario: " + usuario.getEmail() + ", rol: " + rol);

        if ("ROLE_Admin".equals(rol)) {
            System.out.println("    Usuario es Admin, puede editar cualquier acción");
            return true;
        }

        // Verificar si es la última acción del partido
        Optional<Accion> ultimaAccionOpt = accionRepository.findUltimaAccionDelPartido(accion.getIdPartido());
        if (ultimaAccionOpt.isPresent() && ultimaAccionOpt.get().getIdAccion().equals(accion.getIdAccion())) {
            System.out.println("    Es la última acción del partido, verificando permisos del partido");
            Partido partido = partidoRepository.findById(accion.getIdPartido()).get();
            return partidoService.puedeAccederPartido(partido);
        }

        System.out.println("    No es la última acción y usuario no es Admin");
        return false;
    }

    // MÉTODOS DE UTILIDAD

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

    private Accion mapToEntity(AccionDTO dto) {
        Accion accion = new Accion();
        accion.setIdPartido(dto.getIdPartido());
        accion.setIdPosesion(dto.getIdPosesion());
        accion.setEquipoAccion(dto.getEquipoAccion());
        accion.setTipoAtaque(dto.getTipoAtaque());
        accion.setOrigenAccion(dto.getOrigenAccion());
        accion.setEvento(dto.getEvento());
        accion.setDetalleFinalizacion(dto.getDetalleFinalizacion());
        accion.setZonaLanzamiento(dto.getZonaLanzamiento());
        accion.setDetalleEvento(dto.getDetalleEvento());
        accion.setCambioPosesion(dto.getCambioPosesion());
        return accion;
    }

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
        dto.setNombreEquipoLocal(partido.getNombreEquipoLocal());
        dto.setNombreEquipoVisitante(partido.getNombreEquipoVisitante());
        return dto;
    }

    private AccionDTO crearDTOParaValidacion(Accion accionOriginal, AccionUpdateDTO updateDTO) {
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
        return dto;
    }

    private void actualizarCamposAccion(Accion accion, AccionUpdateDTO updateDTO, Boolean cambioPosesion) {
        if (updateDTO.getIdPosesion() != null) accion.setIdPosesion(updateDTO.getIdPosesion());
        if (updateDTO.getEquipoAccion() != null) accion.setEquipoAccion(updateDTO.getEquipoAccion());
        if (updateDTO.getTipoAtaque() != null) accion.setTipoAtaque(updateDTO.getTipoAtaque());
        if (updateDTO.getOrigenAccion() != null) accion.setOrigenAccion(updateDTO.getOrigenAccion());
        if (updateDTO.getEvento() != null) accion.setEvento(updateDTO.getEvento());
        accion.setDetalleFinalizacion(updateDTO.getDetalleFinalizacion());
        accion.setZonaLanzamiento(updateDTO.getZonaLanzamiento());
        accion.setDetalleEvento(updateDTO.getDetalleEvento());
        accion.setCambioPosesion(cambioPosesion);
    }
}