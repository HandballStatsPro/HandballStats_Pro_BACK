package com.HandballStats_Pro.handballstatspro.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

@Data
public class ClubUpdateDTO {
    
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @Size(max = 50, message = "La ciudad no puede exceder los 50 caracteres")
    private String ciudad;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacionClub;

}