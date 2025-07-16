package com.HandballStats_Pro.handballstatspro.controllers;

import com.HandballStats_Pro.handballstatspro.dto.AccionDTO;
import com.HandballStats_Pro.handballstatspro.dto.AccionResponseDTO;
import com.HandballStats_Pro.handballstatspro.dto.AccionUpdateDTO;
import com.HandballStats_Pro.handballstatspro.services.AccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/acciones")
@RequiredArgsConstructor
public class AccionController {
    
    private final AccionService accionService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<AccionResponseDTO> crearAccion(@Valid @RequestBody AccionDTO accionDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accionService.crearAccion(accionDTO));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<AccionResponseDTO> obtenerAccion(@PathVariable Integer id) {
        return ResponseEntity.ok(accionService.obtenerAccionPorId(id));
    }
    
    @GetMapping("/partido/{idPartido}")
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<List<AccionResponseDTO>> listarAccionesPorPartido(@PathVariable Integer idPartido) {
        return ResponseEntity.ok(accionService.listarAccionesPorPartido(idPartido));
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<AccionResponseDTO> actualizarAccion(@PathVariable Integer id, 
                                                              @Valid @RequestBody AccionUpdateDTO accionUpdateDTO) {
        return ResponseEntity.ok(accionService.actualizarAccion(id, accionUpdateDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<Void> eliminarAccion(@PathVariable Integer id) {
        accionService.eliminarAccion(id);
        return ResponseEntity.noContent().build();
    }
}