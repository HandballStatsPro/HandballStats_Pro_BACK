package com.HandballStats_Pro.handballstatspro.controllers;

import com.HandballStats_Pro.handballstatspro.dto.ClubDTO;
import com.HandballStats_Pro.handballstatspro.dto.ClubResponseDTO;
import com.HandballStats_Pro.handballstatspro.dto.ClubUpdateDTO;
import com.HandballStats_Pro.handballstatspro.dto.UsuarioClubDTO;
import com.HandballStats_Pro.handballstatspro.exceptions.PermissionDeniedException;
import com.HandballStats_Pro.handballstatspro.exceptions.ResourceNotFoundException;
import com.HandballStats_Pro.handballstatspro.services.ClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/club")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','GestorClub')")
    public ResponseEntity<ClubResponseDTO> crearClub(@Valid @RequestBody ClubDTO clubDTO) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(clubService.crearClub(clubDTO));
        } catch (ResourceNotFoundException | PermissionDeniedException e) {
            throw e; // Manejado por GlobalExceptionHandler
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin','GestorClub')")
    public ResponseEntity<List<ClubResponseDTO>> listarClubs() {
        return ResponseEntity.ok(clubService.listarClubs());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','GestorClub')")
    public ResponseEntity<ClubResponseDTO> obtenerClub(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clubService.obtenerClubPorId(id));
        } catch (ResourceNotFoundException | PermissionDeniedException e) {
            throw e;
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','GestorClub')")
    public ResponseEntity<ClubResponseDTO> actualizarClub(
            @PathVariable Long id,
            @Valid @RequestBody ClubUpdateDTO clubUpdateDTO) {
        try {
            return ResponseEntity.ok(clubService.actualizarClub(id, clubUpdateDTO));
        } catch (ResourceNotFoundException | PermissionDeniedException e) {
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> eliminarClub(@PathVariable Long id) {
        try {
            clubService.eliminarClub(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            throw e;
        }
    }

    @PostMapping("/asignarUsuario")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> asignarUsuarioAClub(@Valid @RequestBody UsuarioClubDTO dto) {
        try {
            clubService.asignarUsuarioAClub(dto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ResourceNotFoundException e) {
            throw e;
        }
    }

    @DeleteMapping("/{idClub}/gestores/{idUsuario}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> removeGestor(
        @PathVariable Long idClub,
        @PathVariable Long idUsuario
    ) {
        clubService.removeGestor(idClub, idUsuario);
        return ResponseEntity.noContent().build();
    }

}