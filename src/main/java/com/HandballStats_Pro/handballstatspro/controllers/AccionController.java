package com.HandballStats_Pro.handballstatspro.controllers;

import com.HandballStats_Pro.handballstatspro.dto.AccionDTO;
import com.HandballStats_Pro.handballstatspro.dto.AccionResponseDTO;
import com.HandballStats_Pro.handballstatspro.dto.AccionUpdateDTO;
import com.HandballStats_Pro.handballstatspro.services.AccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/acciones")
@CrossOrigin(origins = "*")
public class AccionController {

    private final AccionService accionService;

    public AccionController(AccionService accionService) {
        this.accionService = accionService;
    }

    /**
     * Crear una nueva acción
     */
    @PostMapping
    public ResponseEntity<AccionResponseDTO> crearAccion(@Valid @RequestBody AccionDTO accionDTO) {
        System.out.println("=== ENDPOINT: POST /api/acciones ===");
        AccionResponseDTO nuevaAccion = accionService.crearAccion(accionDTO);
        return new ResponseEntity<>(nuevaAccion, HttpStatus.CREATED);
    }

    /**
     * Listar todas las acciones de un partido específico
     */
    @GetMapping("/partido/{idPartido}")
    public ResponseEntity<List<AccionResponseDTO>> listarAccionesPorPartido(@PathVariable Integer idPartido) {
        System.out.println("=== ENDPOINT: GET /api/acciones/partido/" + idPartido + " ===");
        List<AccionResponseDTO> acciones = accionService.listarAccionesPorPartido(idPartido);
        return ResponseEntity.ok(acciones);
    }

    /**
     * Obtener una acción específica por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccionResponseDTO> obtenerAccionPorId(@PathVariable Integer id) {
        System.out.println("=== ENDPOINT: GET /api/acciones/" + id + " ===");
        AccionResponseDTO accion = accionService.obtenerAccionPorId(id);
        return ResponseEntity.ok(accion);
    }

    /**
     * Actualizar una acción existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccionResponseDTO> actualizarAccion(
            @PathVariable Integer id, 
            @RequestBody AccionUpdateDTO updateDTO) {
        System.out.println("=== ENDPOINT: PUT /api/acciones/" + id + " ===");
        AccionResponseDTO accionActualizada = accionService.actualizarAccion(id, updateDTO);
        return ResponseEntity.ok(accionActualizada);
    }

    /**
     * Eliminar una acción
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAccion(@PathVariable Integer id) {
        System.out.println("=== ENDPOINT: DELETE /api/acciones/" + id + " ===");
        accionService.eliminarAccion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint adicional para obtener información útil para crear acciones
     * Retorna los valores posibles de los enums para el frontend
     */
    @GetMapping("/enums")
    public ResponseEntity<Object> obtenerEnums() {
        System.out.println("=== ENDPOINT: GET /api/acciones/enums ===");
        
        return ResponseEntity.ok(new Object() {
            public final String[] equipoAccion = {"LOCAL", "VISITANTE"};
            public final String[] tipoAtaque = {"Posicional", "Contraataque"};
            public final String[] origenAccion = {"Juego_Continuado", "Rebote_Directo", "Rebote_Indirecto", "_7m"};
            public final String[] evento = {"Gol", "Lanzamiento_Parado", "Lanzamiento_Fuera", "Perdida"};
            public final String[] detalleFinalizacion = {"Lanzamiento_Exterior", "Pivote", "Penetracion", "Extremo", "_7m", "Contragol", "_1ª_oleada", "_2ª_oleada", "_3ª_oleada"};
            public final String[] zonaLanzamiento = {"Izquierda", "Centro", "Derecha"};
            public final String[] detalleEvento = {"Parada_Portero", "Bloqueo_Defensor", "Palo", "Fuera_Directo", "Pasos", "Dobles", "FaltaAtaque", "Pasivo", "InvasionArea", "Robo", "Pie", "BalonFuera"};
        });
    }
}