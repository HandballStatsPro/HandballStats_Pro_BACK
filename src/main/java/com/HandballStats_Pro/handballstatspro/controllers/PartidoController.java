package com.HandballStats_Pro.handballstatspro.controllers;

import com.HandballStats_Pro.handballstatspro.dto.PartidoDTO;
import com.HandballStats_Pro.handballstatspro.dto.PartidoResponseDTO;
import com.HandballStats_Pro.handballstatspro.dto.PartidoUpdateDTO;
import com.HandballStats_Pro.handballstatspro.dto.EquipoResponseDTO;
import com.HandballStats_Pro.handballstatspro.services.PartidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/partidos")
@RequiredArgsConstructor
public class PartidoController {

    private final PartidoService partidoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<PartidoResponseDTO> crearPartido(@Valid @RequestBody PartidoDTO partidoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partidoService.crearPartido(partidoDTO));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<List<PartidoResponseDTO>> listarPartidos() {
        return ResponseEntity.ok(partidoService.listarPartidos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<PartidoResponseDTO> obtenerPartido(@PathVariable Integer id) {
        return ResponseEntity.ok(partidoService.obtenerPartidoPorId(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<PartidoResponseDTO> actualizarPartido(@PathVariable Integer id, @Valid @RequestBody PartidoUpdateDTO partidoUpdateDTO) {
        return ResponseEntity.ok(partidoService.actualizarPartido(id, partidoUpdateDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<Void> eliminarPartido(@PathVariable Integer id) {
        partidoService.eliminarPartido(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/equipo/{idEquipo}")
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<List<PartidoResponseDTO>> obtenerPartidosPorEquipo(@PathVariable Long idEquipo) {
        return ResponseEntity.ok(partidoService.obtenerPartidosPorEquipo(idEquipo));
    }

    @GetMapping("/fecha/{fecha}")
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<List<PartidoResponseDTO>> obtenerPartidosPorFecha(@PathVariable LocalDate fecha) {
        return ResponseEntity.ok(partidoService.obtenerPartidosPorFecha(fecha));
    }

    @GetMapping("/equipos-disponibles")
    @PreAuthorize("hasAnyRole('Admin', 'GestorClub', 'Entrenador')")
    public ResponseEntity<List<EquipoResponseDTO>> obtenerEquiposDisponibles() {
        return ResponseEntity.ok(partidoService.obtenerEquiposDisponibles());
    }
}