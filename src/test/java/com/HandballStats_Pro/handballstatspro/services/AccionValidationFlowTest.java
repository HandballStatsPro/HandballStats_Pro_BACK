package com.HandballStats_Pro.handballstatspro.services;

import com.HandballStats_Pro.handballstatspro.dto.AccionDTO;
import com.HandballStats_Pro.handballstatspro.entities.Accion;
import com.HandballStats_Pro.handballstatspro.entities.Partido;
import com.HandballStats_Pro.handballstatspro.enums.*;
import com.HandballStats_Pro.handballstatspro.exceptions.ApiException;
import com.HandballStats_Pro.handballstatspro.repositories.AccionRepository;
import com.HandballStats_Pro.handballstatspro.repositories.PartidoRepository;
import com.HandballStats_Pro.handballstatspro.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * TEST COMPLETO DE TODOS LOS FLUJOS POSIBLES DE VALIDACIÓN DE ACCIONES
 * 
 * Este test analiza sistemáticamente todos los casos posibles según el
 * Reglamento Maestro de Validación para `accion`.
 * 
 * Organizado por las 5 reglas principales:
 * 1. El Caso Especial de 7 Metros
 * 2. Lógica del Tipo de Ataque  
 * 3. Lógica del Evento Principal
 * 4. Lógica de Cambio de Posesión
 * 5. Lógica Secuencial
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("🎯 ANÁLISIS COMPLETO DE FLUJOS DE VALIDACIÓN DE ACCIONES")
public class AccionValidationFlowTest {

    @Mock
    private AccionRepository accionRepository;
    
    @Mock
    private PartidoRepository partidoRepository;
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private PartidoService partidoService;
    
    @InjectMocks
    private AccionService accionService;
    
    private Partido partidoMock;
    
    @BeforeEach
    void setUp() {
        System.out.println("\n🔥 ==> CONFIGURANDO TEST SETUP <== 🔥");
        
        partidoMock = new Partido();
        partidoMock.setIdPartido(1);
        partidoMock.setNombreEquipoLocal("Equipo Local");
        partidoMock.setNombreEquipoVisitante("Equipo Visitante");
        
        // Configurar mocks básicos
        when(partidoRepository.findById(anyInt())).thenReturn(Optional.of(partidoMock));
        when(partidoService.puedeAccederPartido(any())).thenReturn(true);
        when(accionRepository.save(any(Accion.class))).thenAnswer(invocation -> {
            Accion accion = invocation.getArgument(0);
            accion.setIdAccion(1);
            return accion;
        });
        
        System.out.println("✅ Setup completado");
    }
    
    /**
     * REGLA 1: EL CASO ESPECIAL DE 7 METROS
     */
    @Nested
    @DisplayName("📋 REGLA 1: Caso Especial de 7 Metros")
    class Regla1_CasoEspecial7Metros {
        
        @Test
        @DisplayName("✅ Caso válido: 7m con detalle_finalizacion='7m' y tipo_ataque='Posicional'")
        void test_7m_caso_valido() {
            System.out.println("\n🎯 TEST: 7m caso válido");
            
            AccionDTO accion = crearAccionBase();
            accion.setOrigenAccion(OrigenAccion._7m);
            accion.setDetalleFinalizacion(DetalleFinalizacion._7m);
            accion.setTipoAtaque(TipoAtaque.Posicional);
            accion.setEvento(Evento.Gol);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setCambioPosesion(true);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Caso 7m válido pasó todas las validaciones");
        }
        
        @Test
        @DisplayName("❌ Error: 7m con detalle_finalizacion incorrecto")
        void test_7m_detalle_incorrecto() {
            System.out.println("\n🎯 TEST: 7m con detalle incorrecto");
            
            AccionDTO accion = crearAccionBase();
            accion.setOrigenAccion(OrigenAccion._7m);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote); // ❌ Incorrecto
            accion.setTipoAtaque(TipoAtaque.Posicional);
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("INVALID_7M_DETAIL", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
        
        @Test
        @DisplayName("❌ Error: 7m con tipo_ataque incorrecto")
        void test_7m_tipo_ataque_incorrecto() {
            System.out.println("\n🎯 TEST: 7m con tipo_ataque incorrecto");
            
            AccionDTO accion = crearAccionBase();
            accion.setOrigenAccion(OrigenAccion._7m);
            accion.setDetalleFinalizacion(DetalleFinalizacion._7m);
            accion.setTipoAtaque(TipoAtaque.Contraataque); // ❌ Incorrecto
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("INVALID_7M_TYPE", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
        
        @Test
        @DisplayName("❌ Error: detalle_finalizacion='7m' sin origen_accion='7m'")
        void test_detalle_7m_sin_origen_7m() {
            System.out.println("\n🎯 TEST: detalle_finalizacion='7m' sin origen='7m'");
            
            AccionDTO accion = crearAccionBase();
            accion.setOrigenAccion(OrigenAccion.Juego_Continuado); // ❌ Incorrecto
            accion.setDetalleFinalizacion(DetalleFinalizacion._7m);
            accion.setTipoAtaque(TipoAtaque.Posicional);
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("INVALID_7M_ORIGIN", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
    }
    
    /**
     * REGLA 2: LÓGICA DEL TIPO DE ATAQUE
     */
    @Nested
    @DisplayName("📋 REGLA 2: Lógica del Tipo de Ataque")
    class Regla2_TipoAtaque {
        
        @Test
        @DisplayName("✅ Contraataque válido con Contragol")
        void test_contraataque_contragol_valido() {
            System.out.println("\n🎯 TEST: Contraataque con Contragol");
            
            AccionDTO accion = crearAccionBase();
            accion.setTipoAtaque(TipoAtaque.Contraataque);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Contragol);
            accion.setEvento(Evento.Gol);
            accion.setZonaLanzamiento(ZonaLanzamiento.Izquierda);
            accion.setCambioPosesion(true);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Contraataque con Contragol válido");
        }
        
        @Test
        @DisplayName("✅ Contraataque válido con 1ª oleada")
        void test_contraataque_1a_oleada_valido() {
            System.out.println("\n🎯 TEST: Contraataque con 1ª oleada");
            
            AccionDTO accion = crearAccionBase();
            accion.setTipoAtaque(TipoAtaque.Contraataque);
            accion.setDetalleFinalizacion(DetalleFinalizacion._1a_oleada);
            accion.setEvento(Evento.Gol);
            accion.setZonaLanzamiento(ZonaLanzamiento.Izquierda);
            accion.setCambioPosesion(true);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Contraataque con 1ª oleada válido");
        }
        
        @Test
        @DisplayName("❌ Error: Contraataque con detalle posicional")
        void test_contraataque_detalle_posicional_error() {
            System.out.println("\n🎯 TEST: Contraataque con detalle posicional");
            
            AccionDTO accion = crearAccionBase();
            accion.setTipoAtaque(TipoAtaque.Contraataque);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote); // ❌ Incorrecto para contraataque
            accion.setEvento(Evento.Gol);
            accion.setZonaLanzamiento(ZonaLanzamiento.Izquierda);
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("INVALID_COUNTERATTACK_DETAIL", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
        
        @Test
        @DisplayName("✅ Posicional válido con Pivote")
        void test_posicional_pivote_valido() {
            System.out.println("\n🎯 TEST: Posicional con Pivote");
            
            AccionDTO accion = crearAccionBase();
            accion.setTipoAtaque(TipoAtaque.Posicional);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setEvento(Evento.Gol);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setCambioPosesion(true);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Posicional con Pivote válido");
        }
        
        @Test
        @DisplayName("❌ Error: Posicional con detalle de contraataque")
        void test_posicional_detalle_contraataque_error() {
            System.out.println("\n🎯 TEST: Posicional con detalle de contraataque");
            
            AccionDTO accion = crearAccionBase();
            accion.setTipoAtaque(TipoAtaque.Posicional);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Contragol); // ❌ Incorrecto para posicional
            accion.setEvento(Evento.Gol);
            accion.setZonaLanzamiento(ZonaLanzamiento.Izquierda);
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("INVALID_POSITIONAL_DETAIL", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
    }
    
    /**
     * REGLA 3: LÓGICA DEL EVENTO PRINCIPAL
     */
    @Nested
    @DisplayName("📋 REGLA 3: Lógica del Evento Principal")
    class Regla3_EventoPrincipal {
        
        @Test
        @DisplayName("✅ Gol válido con campos obligatorios")
        void test_gol_valido() {
            System.out.println("\n🎯 TEST: Gol válido");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Gol);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setDetalleEvento(null); // Debe ser nulo para gol
            accion.setCambioPosesion(true);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Gol válido pasó validación");
        }
        
        @Test
        @DisplayName("❌ Error: Gol sin detalle_finalizacion")
        void test_gol_sin_detalle_finalizacion() {
            System.out.println("\n🎯 TEST: Gol sin detalle_finalizacion");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Gol);
            accion.setDetalleFinalizacion(null); // ❌ Obligatorio para gol
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setDetalleEvento(null);
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("GOAL_REQUIRED_FIELDS", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
        
        @Test
        @DisplayName("❌ Error: Gol con detalle_evento no nulo")
        void test_gol_con_detalle_evento() {
            System.out.println("\n🎯 TEST: Gol con detalle_evento");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Gol);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setDetalleEvento(DetalleEvento.Parada_Portero); // ❌ Debe ser nulo para gol
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("GOAL_INVALID_DETAIL", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
        
        @Test
        @DisplayName("✅ Lanzamiento_Parado válido con Parada_Portero")
        void test_lanzamiento_parado_parada_portero_valido() {
            System.out.println("\n🎯 TEST: Lanzamiento_Parado con Parada_Portero");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Lanzamiento_Parado);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setDetalleEvento(DetalleEvento.Parada_Portero);
            accion.setCambioPosesion(false); // No cambia posesión por parada
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Lanzamiento_Parado con Parada_Portero válido");
        }
        
        @Test
        @DisplayName("✅ Lanzamiento_Parado válido con Bloqueo_Defensor")
        void test_lanzamiento_parado_bloqueo_defensor_valido() {
            System.out.println("\n🎯 TEST: Lanzamiento_Parado con Bloqueo_Defensor");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Lanzamiento_Parado);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Extremos);
            accion.setZonaLanzamiento(ZonaLanzamiento.Izquierda);
            accion.setDetalleEvento(DetalleEvento.Bloqueo_Defensor);
            accion.setCambioPosesion(false); // No cambia posesión por bloqueo
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Lanzamiento_Parado con Bloqueo_Defensor válido");
        }
        
        @Test
        @DisplayName("❌ Error: Lanzamiento_Parado con detalle_evento inválido")
        void test_lanzamiento_parado_detalle_evento_invalido() {
            System.out.println("\n🎯 TEST: Lanzamiento_Parado con detalle_evento inválido");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Lanzamiento_Parado);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setDetalleEvento(DetalleEvento.Palo); // ❌ Solo válido para Lanzamiento_Fuera
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("SHOT_STOPPED_INVALID_DETAIL", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
        
        @Test
        @DisplayName("✅ Lanzamiento_Fuera válido con Palo")
        void test_lanzamiento_fuera_palo_valido() {
            System.out.println("\n🎯 TEST: Lanzamiento_Fuera con Palo");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Lanzamiento_Fuera);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Lanzamiento_Exterior);
            accion.setZonaLanzamiento(ZonaLanzamiento.Izquierda);
            accion.setDetalleEvento(DetalleEvento.Palo);
            accion.setCambioPosesion(false); // No cambia posesión por palo
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Lanzamiento_Fuera con Palo válido");
        }
        
        @Test
        @DisplayName("✅ Lanzamiento_Fuera válido con Fuera_Directo")
        void test_lanzamiento_fuera_fuera_directo_valido() {
            System.out.println("\n🎯 TEST: Lanzamiento_Fuera con Fuera_Directo");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Lanzamiento_Fuera);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Penetracion);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setDetalleEvento(DetalleEvento.Fuera_Directo);
            accion.setCambioPosesion(true); // Sí cambia posesión por fuera directo
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Lanzamiento_Fuera con Fuera_Directo válido");
        }
        
        @Test
        @DisplayName("✅ Perdida válida con Pasos")
        void test_perdida_pasos_valido() {
            System.out.println("\n🎯 TEST: Perdida con Pasos");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Perdida);
            accion.setDetalleFinalizacion(null); // Debe ser nulo para perdida
            accion.setZonaLanzamiento(null); // Debe ser nulo para perdida
            accion.setDetalleEvento(DetalleEvento.Pasos);
            accion.setCambioPosesion(true); // Sí cambia posesión por perdida
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Perdida con Pasos válida");
        }
        
        @Test
        @DisplayName("❌ Error: Perdida con campos no nulos")
        void test_perdida_campos_no_nulos_error() {
            System.out.println("\n🎯 TEST: Perdida con campos no nulos");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Perdida);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote); // ❌ Debe ser nulo
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro); // ❌ Debe ser nulo
            accion.setDetalleEvento(DetalleEvento.Pasos);
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("TURNOVER_INVALID_FIELDS", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
    }
    
    /**
     * REGLA 4: LÓGICA DE CAMBIO DE POSESIÓN
     */
    @Nested
    @DisplayName("📋 REGLA 4: Lógica de Cambio de Posesión")
    class Regla4_CambioPosesion {
        
        @Test
        @DisplayName("✅ Parada_Portero NO cambia posesión")
        void test_parada_portero_no_cambia_posesion() {
            System.out.println("\n🎯 TEST: Parada_Portero NO cambia posesión");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Lanzamiento_Parado);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setDetalleEvento(DetalleEvento.Parada_Portero);
            accion.setCambioPosesion(false); // Correcto: NO cambia
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Parada_Portero correctamente NO cambia posesión");
        }
        
        @Test
        @DisplayName("❌ Error: Parada_Portero mal configurada como cambio de posesión")
        void test_parada_portero_mal_configurada() {
            System.out.println("\n🎯 TEST: Parada_Portero mal configurada");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Lanzamiento_Parado);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setDetalleEvento(DetalleEvento.Parada_Portero);
            accion.setCambioPosesion(true); // ❌ Incorrecto: debería ser false
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("INVALID_POSSESSION_CHANGE", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
        
        @Test
        @DisplayName("✅ Bloqueo_Defensor NO cambia posesión")
        void test_bloqueo_defensor_no_cambia_posesion() {
            System.out.println("\n🎯 TEST: Bloqueo_Defensor NO cambia posesión");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Lanzamiento_Parado);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Lanzamiento_Exterior);
            accion.setZonaLanzamiento(ZonaLanzamiento.Izquierda);
            accion.setDetalleEvento(DetalleEvento.Bloqueo_Defensor);
            accion.setCambioPosesion(false); // Correcto: NO cambia
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Bloqueo_Defensor correctamente NO cambia posesión");
        }
        
        @Test
        @DisplayName("✅ Palo NO cambia posesión")
        void test_palo_no_cambia_posesion() {
            System.out.println("\n🎯 TEST: Palo NO cambia posesión");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Lanzamiento_Fuera);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Lanzamiento_Exterior);
            accion.setZonaLanzamiento(ZonaLanzamiento.Izquierda);
            accion.setDetalleEvento(DetalleEvento.Palo);
            accion.setCambioPosesion(false); // Correcto: NO cambia
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Palo correctamente NO cambia posesión");
        }
        
        @Test
        @DisplayName("✅ Gol SÍ cambia posesión")
        void test_gol_si_cambia_posesion() {
            System.out.println("\n🎯 TEST: Gol SÍ cambia posesión");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Gol);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setDetalleEvento(null);
            accion.setCambioPosesion(true); // Correcto: SÍ cambia
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Gol correctamente SÍ cambia posesión");
        }
        
        @Test
        @DisplayName("✅ Fuera_Directo SÍ cambia posesión")
        void test_fuera_directo_si_cambia_posesion() {
            System.out.println("\n🎯 TEST: Fuera_Directo SÍ cambia posesión");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Lanzamiento_Fuera);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Penetracion);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setDetalleEvento(DetalleEvento.Fuera_Directo);
            accion.setCambioPosesion(true); // Correcto: SÍ cambia
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Fuera_Directo correctamente SÍ cambia posesión");
        }
        
        @Test
        @DisplayName("✅ Perdida SÍ cambia posesión")
        void test_perdida_si_cambia_posesion() {
            System.out.println("\n🎯 TEST: Perdida SÍ cambia posesión");
            
            AccionDTO accion = crearAccionBase();
            accion.setEvento(Evento.Perdida);
            accion.setDetalleFinalizacion(null);
            accion.setZonaLanzamiento(null);
            accion.setDetalleEvento(DetalleEvento.Robo);
            accion.setCambioPosesion(true); // Correcto: SÍ cambia
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Perdida correctamente SÍ cambia posesión");
        }
    }
    
    /**
     * REGLA 5: LÓGICA SECUENCIAL
     */
    @Nested
    @DisplayName("📋 REGLA 5: Lógica Secuencial")
    class Regla5_LogicaSecuencial {
        
        @Test
        @DisplayName("✅ Primera acción del partido con Juego_Continuado")
        void test_primera_accion_juego_continuado() {
            System.out.println("\n🎯 TEST: Primera acción del partido");
            
            // No hay acciones anteriores
            when(accionRepository.findLastActionInMatch(anyInt())).thenReturn(Optional.empty());
            
            AccionDTO accion = crearAccionBase();
            accion.setOrigenAccion(OrigenAccion.Juego_Continuado); // Correcto para primera acción
            accion.setEvento(Evento.Gol);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setCambioPosesion(true);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Primera acción válida con Juego_Continuado");
        }
        
        @Test
        @DisplayName("❌ Error: Primera acción con Rebote")
        void test_primera_accion_rebote_error() {
            System.out.println("\n🎯 TEST: Primera acción con Rebote (error)");
            
            // No hay acciones anteriores
            when(accionRepository.findLastActionInMatch(anyInt())).thenReturn(Optional.empty());
            
            AccionDTO accion = crearAccionBase();
            accion.setOrigenAccion(OrigenAccion.Rebote_directo); // ❌ Incorrecto para primera acción
            accion.setEvento(Evento.Gol);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("INVALID_FIRST_ACTION", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
        
        @Test
        @DisplayName("✅ Rebote_directo después de acción sin cambio de posesión")
        void test_rebote_directo_secuencia_valida() {
            System.out.println("\n🎯 TEST: Rebote_directo después de acción sin cambio");
            
            // Configurar acción anterior que NO cambia posesión
            Accion accionAnterior = new Accion();
            accionAnterior.setIdAccion(1);
            accionAnterior.setCambioPosesion(false); // Correcto para rebote
            accionAnterior.setEvento(Evento.Lanzamiento_Parado);
            when(accionRepository.findLastActionInMatch(anyInt())).thenReturn(Optional.of(accionAnterior));
            
            AccionDTO accion = crearAccionBase();
            accion.setOrigenAccion(OrigenAccion.Rebote_directo);
            accion.setEvento(Evento.Gol);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setCambioPosesion(true);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Rebote_directo válido después de acción sin cambio de posesión");
        }
        
        @Test
        @DisplayName("❌ Error: Rebote_directo después de acción con cambio de posesión")
        void test_rebote_directo_secuencia_invalida() {
            System.out.println("\n🎯 TEST: Rebote_directo después de cambio de posesión (error)");
            
            // Configurar acción anterior que SÍ cambia posesión
            Accion accionAnterior = new Accion();
            accionAnterior.setIdAccion(1);
            accionAnterior.setCambioPosesion(true); // ❌ Incorrecto para rebote
            accionAnterior.setEvento(Evento.Gol);
            when(accionRepository.findLastActionInMatch(anyInt())).thenReturn(Optional.of(accionAnterior));
            
            AccionDTO accion = crearAccionBase();
            accion.setOrigenAccion(OrigenAccion.Rebote_directo);
            accion.setEvento(Evento.Gol);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("INVALID_REBOUND_SEQUENCE", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
        
        @Test
        @DisplayName("✅ Juego_Continuado después de acción con cambio de posesión")
        void test_juego_continuado_secuencia_valida() {
            System.out.println("\n🎯 TEST: Juego_Continuado después de cambio de posesión");
            
            // Configurar acción anterior que SÍ cambia posesión
            Accion accionAnterior = new Accion();
            accionAnterior.setIdAccion(1);
            accionAnterior.setCambioPosesion(true); // Correcto para juego continuado
            accionAnterior.setEvento(Evento.Gol);
            when(accionRepository.findLastActionInMatch(anyInt())).thenReturn(Optional.of(accionAnterior));
            
            AccionDTO accion = crearAccionBase();
            accion.setOrigenAccion(OrigenAccion.Juego_Continuado);
            accion.setEvento(Evento.Perdida);
            accion.setDetalleFinalizacion(null);
            accion.setZonaLanzamiento(null);
            accion.setDetalleEvento(DetalleEvento.Pasos);
            accion.setCambioPosesion(true);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ Juego_Continuado válido después de cambio de posesión");
        }
        
        @Test
        @DisplayName("❌ Error: Juego_Continuado después de acción sin cambio de posesión")
        void test_juego_continuado_secuencia_invalida() {
            System.out.println("\n🎯 TEST: Juego_Continuado después de NO cambio (error)");
            
            // Configurar acción anterior que NO cambia posesión
            Accion accionAnterior = new Accion();
            accionAnterior.setIdAccion(1);
            accionAnterior.setCambioPosesion(false); // ❌ Incorrecto para juego continuado
            accionAnterior.setEvento(Evento.Lanzamiento_Parado);
            when(accionRepository.findLastActionInMatch(anyInt())).thenReturn(Optional.of(accionAnterior));
            
            AccionDTO accion = crearAccionBase();
            accion.setOrigenAccion(OrigenAccion.Juego_Continuado);
            accion.setEvento(Evento.Gol);
            accion.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            
            ApiException exception = assertThrows(ApiException.class, 
                () -> accionService.crearAccion(accion));
            
            assertEquals("INVALID_CONTINUOUS_GAME_SEQUENCE", exception.getErrorCode());
            System.out.println("✅ Error detectado correctamente: " + exception.getMessage());
        }
        
        @Test
        @DisplayName("✅ 7m no sigue reglas secuenciales")
        void test_7m_no_sigue_reglas_secuenciales() {
            System.out.println("\n🎯 TEST: 7m exento de reglas secuenciales");
            
            // Configurar acción anterior cualquiera (no importa para 7m)
            Accion accionAnterior = new Accion();
            accionAnterior.setIdAccion(1);
            accionAnterior.setCambioPosesion(true); // No importa para 7m
            accionAnterior.setEvento(Evento.Gol);
            when(accionRepository.findLastActionInMatch(anyInt())).thenReturn(Optional.of(accionAnterior));
            
            AccionDTO accion = crearAccionBase();
            accion.setOrigenAccion(OrigenAccion._7m); // Exento de reglas secuenciales
            accion.setDetalleFinalizacion(DetalleFinalizacion._7m);
            accion.setTipoAtaque(TipoAtaque.Posicional);
            accion.setEvento(Evento.Gol);
            accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion.setCambioPosesion(true);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion));
            System.out.println("✅ 7m correctamente exento de reglas secuenciales");
        }
    }
    
    /**
     * CASOS EDGE Y COMBINACIONES COMPLEJAS
     */
    @Nested
    @DisplayName("🎭 CASOS EDGE Y COMBINACIONES COMPLEJAS")
    class CasosEdgeYCombinacionesComplejas {
        
        @Test
        @DisplayName("🎯 Flujo completo: Secuencia realista de acciones")
        void test_flujo_completo_secuencia_realista() {
            System.out.println("\n🎯 TEST: Flujo completo de secuencia realista");
            
            // ACCIÓN 1: Inicio de partido (Juego_Continuado)
            System.out.println("   📌 Acción 1: Inicio de posesión");
            when(accionRepository.findLastActionInMatch(anyInt())).thenReturn(Optional.empty());
            
            AccionDTO accion1 = crearAccionBase();
            accion1.setOrigenAccion(OrigenAccion.Juego_Continuado);
            accion1.setEvento(Evento.Lanzamiento_Parado);
            accion1.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion1.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion1.setDetalleEvento(DetalleEvento.Parada_Portero);
            accion1.setCambioPosesion(false);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion1));
            System.out.println("   ✅ Acción 1 válida");
            
            // ACCIÓN 2: Rebote directo (después de parada)
            System.out.println("   📌 Acción 2: Rebote directo");
            Accion accionAnterior1 = new Accion();
            accionAnterior1.setCambioPosesion(false);
            when(accionRepository.findLastActionInMatch(anyInt())).thenReturn(Optional.of(accionAnterior1));
            
            AccionDTO accion2 = crearAccionBase();
            accion2.setOrigenAccion(OrigenAccion.Rebote_directo);
            accion2.setEvento(Evento.Gol);
            accion2.setDetalleFinalizacion(DetalleFinalizacion.Pivote);
            accion2.setZonaLanzamiento(ZonaLanzamiento.Centro);
            accion2.setCambioPosesion(true);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion2));
            System.out.println("   ✅ Acción 2 válida");
            
            // ACCIÓN 3: Nueva posesión (Juego_Continuado tras gol)
            System.out.println("   📌 Acción 3: Nueva posesión tras gol");
            Accion accionAnterior2 = new Accion();
            accionAnterior2.setCambioPosesion(true);
            when(accionRepository.findLastActionInMatch(anyInt())).thenReturn(Optional.of(accionAnterior2));
            
            AccionDTO accion3 = crearAccionBase();
            accion3.setOrigenAccion(OrigenAccion.Juego_Continuado);
            accion3.setTipoAtaque(TipoAtaque.Contraataque);
            accion3.setEvento(Evento.Gol);
            accion3.setDetalleFinalizacion(DetalleFinalizacion.Contragol);
            accion3.setZonaLanzamiento(ZonaLanzamiento.Izquierda);
            accion3.setCambioPosesion(true);
            
            assertDoesNotThrow(() -> accionService.crearAccion(accion3));
            System.out.println("   ✅ Acción 3 válida");
            
            System.out.println("🎉 Flujo completo de secuencia realista exitoso");
        }
        
        @Test
        @DisplayName("🎯 Todos los tipos de DetalleEvento para Perdida")
        void test_todos_detalle_evento_perdida() {
            System.out.println("\n🎯 TEST: Todos los tipos de DetalleEvento para Perdida");
            
            DetalleEvento[] detallesValidos = {
                DetalleEvento.Pasos, DetalleEvento.Dobles, DetalleEvento.FaltaAtaque,
                DetalleEvento.Pasivo, DetalleEvento.InvasionArea, DetalleEvento.Robo,
                DetalleEvento.Pie, DetalleEvento.BalonFuera
            };
            
            for (DetalleEvento detalle : detallesValidos) {
                System.out.println("   🔍 Probando " + detalle);
                
                AccionDTO accion = crearAccionBase();
                accion.setEvento(Evento.Perdida);
                accion.setDetalleFinalizacion(null);
                accion.setZonaLanzamiento(null);
                accion.setDetalleEvento(detalle);
                accion.setCambioPosesion(true);
                
                assertDoesNotThrow(() -> accionService.crearAccion(accion));
                System.out.println("   ✅ " + detalle + " válido");
            }
            
            System.out.println("🎉 Todos los DetalleEvento para Perdida son válidos");
        }
        
        @Test
        @DisplayName("🎯 Todas las combinaciones de DetalleFinalizacion con TipoAtaque")
        void test_todas_combinaciones_detalle_finalizacion_tipo_ataque() {
            System.out.println("\n🎯 TEST: Combinaciones DetalleFinalizacion con TipoAtaque");
            
            // Combinaciones válidas para Contraataque
            DetalleFinalizacion[] contraataqueValidos = {
                DetalleFinalizacion.Contragol, DetalleFinalizacion._1a_oleada,
                DetalleFinalizacion._2a_oleada, DetalleFinalizacion._3a_oleada
            };
            
            for (DetalleFinalizacion detalle : contraataqueValidos) {
                System.out.println("   🔍 Contraataque + " + detalle);
                
                AccionDTO accion = crearAccionBase();
                accion.setTipoAtaque(TipoAtaque.Contraataque);
                accion.setDetalleFinalizacion(detalle);
                accion.setEvento(Evento.Gol);
                accion.setZonaLanzamiento(ZonaLanzamiento.Izquierda);
                accion.setCambioPosesion(true);
                
                assertDoesNotThrow(() -> accionService.crearAccion(accion));
                System.out.println("   ✅ Contraataque + " + detalle + " válido");
            }
            
            // Combinaciones válidas para Posicional
            DetalleFinalizacion[] posicionalValidos = {
                DetalleFinalizacion.Lanzamiento_Exterior, DetalleFinalizacion.Pivote,
                DetalleFinalizacion.Penetracion, DetalleFinalizacion.Extremos,
                DetalleFinalizacion._7m
            };
            
            for (DetalleFinalizacion detalle : posicionalValidos) {
                System.out.println("   🔍 Posicional + " + detalle);
                
                AccionDTO accion = crearAccionBase();
                accion.setTipoAtaque(TipoAtaque.Posicional);
                accion.setDetalleFinalizacion(detalle);
                if (detalle == DetalleFinalizacion._7m) {
                    accion.setOrigenAccion(OrigenAccion._7m);
                }
                accion.setEvento(Evento.Gol);
                accion.setZonaLanzamiento(ZonaLanzamiento.Centro);
                accion.setCambioPosesion(true);
                
                assertDoesNotThrow(() -> accionService.crearAccion(accion));
                System.out.println("   ✅ Posicional + " + detalle + " válido");
            }
            
            System.out.println("🎉 Todas las combinaciones válidas pasaron");
        }
    }
    
    // MÉTODO AUXILIAR PARA CREAR ACCIÓN BASE
    private AccionDTO crearAccionBase() {
        AccionDTO accion = new AccionDTO();
        accion.setIdPartido(1);
        accion.setIdPosesion(1);
        accion.setEquipoAccion(EquipoAccion.LOCAL);
        accion.setTipoAtaque(TipoAtaque.Posicional);
        accion.setOrigenAccion(OrigenAccion.Juego_Continuado);
        return accion;
    }
}