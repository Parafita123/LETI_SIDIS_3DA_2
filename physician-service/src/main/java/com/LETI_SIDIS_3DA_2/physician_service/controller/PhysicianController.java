package com.LETI_SIDIS_3DA_2.physician_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.LETI_SIDIS_3DA_2.physician_service.command.dto.RegisterPhysicianDTO;
import com.LETI_SIDIS_3DA_2.physician_service.command.service.PhysicianCommandService;
import com.LETI_SIDIS_3DA_2.physician_service.exception.DuplicateResourceException;
import com.LETI_SIDIS_3DA_2.physician_service.exception.ResourceNotFoundException;
import com.LETI_SIDIS_3DA_2.physician_service.query.dto.PhysicianBasicInfoDTO;
import com.LETI_SIDIS_3DA_2.physician_service.query.dto.PhysicianOutputDTO;
import com.LETI_SIDIS_3DA_2.physician_service.query.service.PhysicianQueryService;
import com.LETI_SIDIS_3DA_2.physician_service.service.FileStorageService;
import org.springframework.core.io.Resource;
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
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/physicians")
public class PhysicianController {

    private final PhysicianCommandService commandService;
    private final PhysicianQueryService queryService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    public PhysicianController(PhysicianCommandService commandService,
                               PhysicianQueryService queryService,
                               FileStorageService fileStorageService,
                               ObjectMapper objectMapper) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhysicianOutputDTO> registerPhysician(
            @RequestPart("physicianData") String physicianDataString,
            @RequestPart(name = "profilePhoto", required = false) MultipartFile profilePhotoFile
    ) {
        try {
            RegisterPhysicianDTO physicianDTO =
                    objectMapper.readValue(physicianDataString, RegisterPhysicianDTO.class);

            PhysicianOutputDTO dto = commandService.registerPhysician(physicianDTO, profilePhotoFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid physician data format: " + e.getMessage(), e);
        } catch (DuplicateResourceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage(), e);
        }
    }

    @GetMapping("/photo/{filename:.+}")
    public ResponseEntity<Resource> getPhysicianProfilePhoto(@PathVariable String filename) {
        try {
            Resource fileResource = fileStorageService.loadPhysicianPhotoAsResource(filename);

            String contentType = "application/octet-stream";
            try {
                contentType = Files.probeContentType(fileResource.getFile().toPath());
            } catch (IOException | NullPointerException ex) {
                System.err.println("Could not determine file type for " + filename
                        + ". Defaulting to " + contentType);
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + fileResource.getFilename() + "\"")
                    .body(fileResource);

        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Photo not found: " + filename, ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not serve photo: " + filename, ex);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT') or isAuthenticated()")
    public ResponseEntity<?> getPhysicianDetailsById(@PathVariable Long id,
                                                     Authentication authentication) {

        boolean isAdmin = false;
        if (authentication != null && authentication.isAuthenticated()) {
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        if (isAdmin) {
            Optional<PhysicianOutputDTO> opt = queryService.getPhysicianById(id);
            PhysicianOutputDTO dto = opt.orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Physician not found with ID: " + id));
            return ResponseEntity.ok(dto);

        } else {
            Optional<PhysicianBasicInfoDTO> optBasic = queryService.getPhysicianBasicInfoById(id);
            PhysicianBasicInfoDTO basicDto = optBasic.orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Physician not found with ID: " + id));
            return ResponseEntity.ok(basicDto);
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PhysicianBasicInfoDTO>> searchPhysiciansForPatient(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialty
    ) {
        List<PhysicianBasicInfoDTO> list =
                queryService.searchPhysiciansForPatient(name, specialty);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhysicianOutputDTO> updatePhysician(
            @PathVariable Long id,
            @RequestBody RegisterPhysicianDTO physicianDTO
    ) {
        try {
            PhysicianOutputDTO updatedDto = commandService.updatePhysician(id, physicianDTO);
            return ResponseEntity.ok(updatedDto);
        } catch (ResourceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (DuplicateResourceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
