package com.HandballStats_Pro.handballstatspro.controllers;

import com.HandballStats_Pro.handballstatspro.config.UserDetailsImpl;
import com.HandballStats_Pro.handballstatspro.dto.AuthResponse;
import com.HandballStats_Pro.handballstatspro.dto.LoginRequest;
import com.HandballStats_Pro.handballstatspro.dto.UsuarioDTO;
import com.HandballStats_Pro.handballstatspro.entities.Usuario;
import com.HandballStats_Pro.handballstatspro.services.JwtService;
import com.HandballStats_Pro.handballstatspro.services.UserDetailsServiceImpl;
import com.HandballStats_Pro.handballstatspro.services.UsuarioService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Usuario> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(userDetails.getUsername());
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(@RequestBody UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioService.crearUsuario(usuarioDTO);
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail()); 
        String token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(token, usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getContraseña()
                )
            );
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Usuario usuario = usuarioService.obtenerUsuarioPorEmail(userDetails.getUsername());
            String token = jwtService.generateToken(userDetails);
            
            return ResponseEntity.ok(new AuthResponse(token, usuario));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
    }
}