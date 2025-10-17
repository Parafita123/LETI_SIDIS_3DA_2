// src/main/java/com/psoft2024/_5/grupo1/projeto_psoft/controller/PhysicianController.java
package com.LETI_SIDIS_3DA_2.physician_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.LETI_SIDIS_3DA_2.physician_service.dto.*;
import com.LETI_SIDIS_3DA_2.physician_service.exception.DuplicateResourceException;
import com.LETI_SIDIS_3DA_2.physician_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA_2.physician_service.service.FileStorageService;
import com.LETI_SIDIS_3DA_2.physician_service.service.PhysicianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/physicians")
public class PhysicianController {

    private final PhysicianService physicianService;
    private final FileStorageService fileStorageService; // NOVO: Injeta o serviço de ficheiros
    private final ObjectMapper objectMapper;

    @Autowired
    public PhysicianController(PhysicianService physicianService,
                               FileStorageService fileStorageService,
                               ObjectMapper objectMapper) {
        this.physicianService = physicianService;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhysicianOutputDTO> registerPhysician(
            @RequestPart("physicianData") String physicianDataString, // Dados do médico como string JSON
            @RequestPart(name = "profilePhoto", required = false) MultipartFile profilePhotoFile // Ficheiro da foto (opcional)
    ) {
        try {
            // Deserializa a string JSON para RegisterPhysicianDTO
            RegisterPhysicianDTO physicianDTO = objectMapper.readValue(physicianDataString, RegisterPhysicianDTO.class);

            PhysicianOutputDTO dto = physicianService.registerPhysician(physicianDTO, profilePhotoFile);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (JsonProcessingException e) { // Erro ao deserializar JSON
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid physician data format: " + e.getMessage(), e);
        } catch (DuplicateResourceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (ResourceNotFoundException e) { // Ex: Specialty/Department not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (RuntimeException e) { // Outros erros (ex: problema ao guardar ficheiro)
            // Logar a exceção original para debug no servidor
            // logger.error("Error registering physician with photo", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage(), e);
        }
    }


    // Endpoint para mostro a foto de perfil do médico no postman
    @GetMapping("/photo/{filename:.+}") // :.+ é para garantir que a extensão do ficheiro é capturada
    public ResponseEntity<Resource> getPhysicianProfilePhoto(@PathVariable String filename) {
        try {
            Resource fileResource = fileStorageService.loadPhysicianPhotoAsResource(filename);
            // Tenta determinar o content type
            String contentType = "application/octet-stream"; // Default
            try {
                contentType = Files.probeContentType(fileResource.getFile().toPath());
            } catch (IOException | NullPointerException ex) {
                // Loga o erro, mas continua com o default
                System.err.println("Could not determine file type for " + filename + ". Defaulting to " + contentType);
            }
            if (contentType == null) { // Fallback adicional
                contentType = "application/octet-stream";
            }


            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileResource.getFilename() + "\"") // "inline" tenta mostrar no browser
                    .body(fileResource);
        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found: " + filename, ex);
        } catch (RuntimeException ex) { // Ex: problema ao ler o ficheiro
            // logger.error("Error serving physician photo", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not serve photo: " + filename, ex);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT') or isAuthenticated()")

    public ResponseEntity<?> getPhysicianDetailsById(@PathVariable Long id, Authentication authentication) {
        // Verifica se o utilizador está autenticado e se é Admin
        boolean isAdmin = false;
        if (authentication != null && authentication.isAuthenticated()) {
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        }

        if (isAdmin) {
            Optional<PhysicianOutputDTO> opt = physicianService.getPhysicianById(id);
            PhysicianOutputDTO dto = opt.orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Physician not found with ID: " + id));
            // Adicionar links HATEOAS se DTO estender RepresentationModel
            // dto.add(linkTo(methodOn(PhysicianController.class).getPhysicianDetailsById(id, authentication)).withSelfRel());
            return ResponseEntity.ok(dto);
        } else { // Paciente ou outro utilizador autenticado (ou anónimo se permitido)
            Optional<PhysicianBasicInfoDTO> optBasic = physicianService.getPhysicianBasicInfoById(id);
            PhysicianBasicInfoDTO basicDto = optBasic.orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Physician not found with ID: " + id));
            // Adicionar links HATEOAS se DTO estender RepresentationModel (para PhysicianBasicInfoDTO)
            // basicDto.add(linkTo(methodOn(PhysicianController.class).getPhysicianDetailsById(id, authentication)).withSelfRel());
            return ResponseEntity.ok(basicDto);
        }
    }

    // UC: Pesquisa de médicos por nome ou especialidade (APENAS PARA PACIENTES)
    @GetMapping("/search") // Mudei o path para ser mais específico para esta funcionalidade de pesquisa de paciente
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PhysicianBasicInfoDTO>> searchPhysiciansForPatient(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialty
    ) {
        List<PhysicianBasicInfoDTO> list = physicianService.searchPhysiciansForPatient(name, specialty);
        return ResponseEntity.ok(list);
    }


    // UC: As an Administrator, I want to update a physician’s data.
    // PUT /api/physicians/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhysicianOutputDTO> updatePhysician(
            @PathVariable Long id,
            @RequestBody RegisterPhysicianDTO physicianDTO
    ) {
        try {
            PhysicianOutputDTO updatedDto = physicianService.updatePhysician(id, physicianDTO);
            return ResponseEntity.ok(updatedDto); // Retorna 200 OK com o médico atualizado
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (DuplicateResourceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (RuntimeException e) { // Outros erros (ex: formato de data inválido)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}

