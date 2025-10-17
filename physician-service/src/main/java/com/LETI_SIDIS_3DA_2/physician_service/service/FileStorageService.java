package com.LETI_SIDIS_3DA_2.physician_service.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storePhysicianProfilePhoto(MultipartFile file); // Retorna o nome do ficheiro guardado
    Resource loadPhysicianPhotoAsResource(String filename);
    void deletePhysicianProfilePhoto(String filename);
}
