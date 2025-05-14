package com.HandballStats_Pro.handballstatspro.controllers;

import com.HandballStats_Pro.handballstatspro.dto.*;
import com.HandballStats_Pro.handballstatspro.services.EquipoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipo")
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    @PostMapping
    public ResponseEntity<EquipoResponseDTO> crearEquipo(@Valid @RequestBody EquipoDTO dto) {
        return ResponseEntity.ok(equipoService.crearEquipo(dto));
    }

    @GetMapping
    public ResponseEntity<List<EquipoResponseDTO>> listarEquipos() {
        return ResponseEntity.ok(equipoService.listarEquipos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipoResponseDTO> obtenerEquipo(@PathVariable Long id) {
        return ResponseEntity.ok(equipoService.obtenerEquipoPorId(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EquipoResponseDTO> actualizarEquipo(@PathVariable Long id, @RequestBody EquipoUpdateDTO dto) {
        return ResponseEntity.ok(equipoService.actualizarEquipo(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEquipo(@PathVariable Long id) {
        equipoService.eliminarEquipo(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/asignarUsuario")
    public ResponseEntity<Void> asignarEntrenador(@Valid @RequestBody UsuarioEquipoDTO dto) {
        equipoService.asignarEntrenadorAEquipo(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{idEquipo}/entrenadores/{idUsuario}")
    public ResponseEntity<Void> desasignarEntrenador(@PathVariable Long idEquipo, @PathVariable Long idUsuario) {
        equipoService.desasignarEntrenadorDeEquipo(idEquipo, idUsuario);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{idEquipo}/club/{idClub}")
    public ResponseEntity<Void> asignarEquipoAClub(@PathVariable Long idEquipo, @PathVariable Long idClub) {
        equipoService.asignarEquipoAClub(idEquipo, idClub);
        return ResponseEntity.ok().build();
    }
}
