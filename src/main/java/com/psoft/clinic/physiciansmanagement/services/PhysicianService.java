package com.psoft.clinic.physiciansmanagement.services;

import com.psoft.clinic.exceptions.InvalidImageFormatException;
import com.psoft.clinic.model.BaseUser;
import com.psoft.clinic.department.model.Department;
import com.psoft.clinic.exceptions.UsernameAlreadyExistsException;
import com.psoft.clinic.model.*;
import com.psoft.clinic.physiciansmanagement.model.Physician;
import com.psoft.clinic.physiciansmanagement.repository.SpringBootPhysicianRepository;
import com.psoft.clinic.department.repository.SpringBootDepartmentRepository;
import com.psoft.clinic.speciality.model.Speciality;
import com.psoft.clinic.speciality.repository.SpringBootSpecialityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PhysicianService {

    private final SpringBootPhysicianRepository physicianRepository;
    private final SpringBootDepartmentRepository departmentRepository;
    private final SpringBootSpecialityRepository specialityRepository;
    private final PasswordEncoder passwordEncoder;

    private final Path rootLocation = Paths.get(
            System.getProperty("user.dir"), "src", "main", "resources", "physician"
    );

    public Physician create(CreatePhysicianRequest request, MultipartFile file) {
        String username = request.getUsername();

        if (physicianRepository.findByBaseUserUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException(username);
        }

        Department dept = departmentRepository
                .findBySigla(request.getDepartmentSigla())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Department não encontrado: " + request.getDepartmentSigla()));

        Speciality spec = specialityRepository
                .findByName(request.getSpeciality())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Speciality não encontrada: " + request.getSpeciality()));

        String uploadDir = rootLocation.toFile().getAbsolutePath();
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filename = null;
        String extension = null;

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null
                    || !Set.of("image/jpeg", "image/png", "image/jpg")
                    .contains(contentType.toLowerCase())) {
                throw new InvalidImageFormatException(
                        "Apenas são permitidos ficheiros JPG, JPEG ou PNG"
                );
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename
                        .substring(originalFilename.lastIndexOf('.') + 1)
                        .toLowerCase();
            }
            if (extension == null || !Set.of("jpg", "jpeg", "png").contains(extension)) {
                throw new InvalidImageFormatException(
                        "Extensão inválida: " + extension + ". Só JPG, JPEG e PNG são permitidos."
                );
            }

            filename = username + "." + extension;
            Path destination = Paths.get(uploadDir, filename);
            try {
                file.transferTo(destination.toFile());
            } catch (IOException e) {
                throw new RuntimeException("Falha ao gravar ficheiro de imagem em " + destination, e);
            }
        }

        BaseUser baseUser = new BaseUser();
        baseUser.setUsername(username);
        baseUser.setFullName(request.getFullName());
        baseUser.setPassword(passwordEncoder.encode(request.getPassword()));
        baseUser.setRole(Role.USER_PHYSICIAN);

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail(request.getEmail());

        Address address = new Address();
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setZip(request.getZip());
        address.setCountry(request.getCountry());
        contactInfo.setAddress(address);

        Phone phone = new Phone(
                request.getPhoneNumber(),
                request.getPhoneType() != null ? request.getPhoneType() : PhoneType.HOME
        );
        contactInfo.setPhones(List.of(phone));

        WorkingHours wh = new WorkingHours();
        wh.setStartTime(LocalTime.parse(request.getStartTime()));
        wh.setEndTime(LocalTime.parse(request.getEndTime()));

        Physician physician = new Physician();
        physician.setBaseUser(baseUser);
        physician.setDepartment(dept);
        physician.setSpeciality(spec);
        physician.setContactInfo(contactInfo);
        physician.setWorkingHours(wh);

        if (filename != null) {
            physician.setImage(filename);
        }
        return physicianRepository.save(physician);
    }


    @Transactional
    public Physician updatePhysician(Long id,
                                     UpdatePhysicianRequest req,
                                     MultipartFile file) {
        boolean hasFields = req != null && (
                req.getFullName() != null ||
                        req.getPassword() != null ||
                        req.getEmail() != null ||
                        req.getStreet() != null ||
                        req.getCity() != null ||
                        req.getDistrict() != null ||
                        req.getZip() != null ||
                        req.getCountry() != null ||
                        req.getPhoneNumber() != null ||
                        req.getPhoneType() != null ||
                        req.getDepartmentSigla() != null ||
                        req.getSpeciality() != null ||
                        req.getStartTime() != null ||
                        req.getEndTime() != null
        );
        boolean hasFile = file != null && !file.isEmpty();
        if (!hasFields && !hasFile) {
            try {
                throw new BadRequestException("Nenhum campo ou ficheiro para atualizar.");
            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            }
        }

        Physician p = physicianRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Physician não encontrado: " + id));

        if (hasFields) {
            if (req.getFullName() != null)
                p.getBaseUser().setFullName(req.getFullName());
            if (req.getPassword() != null)
                p.getBaseUser().setPassword(passwordEncoder.encode(req.getPassword()));

            if (req.getEmail() != null)
                p.getContactInfo().setEmail(req.getEmail());
            var addr = p.getContactInfo().getAddress();
            if (req.getStreet() != null)   addr.setStreet(req.getStreet());
            if (req.getCity() != null)     addr.setCity(req.getCity());
            if (req.getDistrict() != null) addr.setDistrict(req.getDistrict());
            if (req.getZip() != null)      addr.setZip(req.getZip());
            if (req.getCountry() != null)  addr.setCountry(req.getCountry());

            if (req.getPhoneNumber() != null || req.getPhoneType() != null) {
                var phone = p.getContactInfo().getPhones().get(0);
                if (req.getPhoneNumber() != null) phone.setNumber(req.getPhoneNumber());
                if (req.getPhoneType()   != null) phone.setType(PhoneType.valueOf(req.getPhoneType()));
            }

            if (req.getDepartmentSigla() != null) {
                var dept = departmentRepository.findBySigla(req.getDepartmentSigla())
                        .orElseThrow(() -> new EntityNotFoundException("Department não encontrado: " + req.getDepartmentSigla()));
                p.setDepartment(dept);
            }
            if (req.getSpeciality() != null) {
                var spec = specialityRepository.findByName(req.getSpeciality())
                        .orElseThrow(() -> new EntityNotFoundException("Speciality não encontrada: " + req.getSpeciality()));
                p.setSpeciality(spec);
            }

            var wh = p.getWorkingHours();
            if (req.getStartTime() != null) wh.setStartTime(LocalTime.parse(req.getStartTime()));
            if (req.getEndTime()   != null) wh.setEndTime(LocalTime.parse(req.getEndTime()));
        }

        if (hasFile) {
            String uploadDir = rootLocation.toFile().getAbsolutePath();
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            var ct = file.getContentType();
            if (ct == null || !Set.of("image/jpeg","image/png","image/jpg").contains(ct.toLowerCase())) {
                throw new InvalidImageFormatException("Apenas JPG, JPEG ou PNG permitidos.");
            }
            var orig = file.getOriginalFilename();
            var ext = ((orig != null) && orig.contains(".")) ? orig.substring(orig.lastIndexOf('.') + 1).toLowerCase() : null;
            if (ext == null || !Set.of("jpg","jpeg","png").contains(ext)) {
                throw new InvalidImageFormatException("Extensão inválida: " + ext + ".");
            }
            var filename = p.getBaseUser().getUsername() + "." + ext;
            var dest = Path.of(uploadDir, filename);
            try {
                file.transferTo(dest.toFile());
            } catch (IOException e) {
                throw new RuntimeException("Falha ao gravar imagem", e);
            }
            p.setImage(filename);
        }

        p.setModifiedAt(Instant.now());
        return physicianRepository.save(p);
    }


public Optional<Physician> findByUsername(String username) {
        return physicianRepository.findByBaseUserUsername(username);
    }
    public Optional<Physician> findById(Long id) {
        return physicianRepository.findById(id);
    }
    public List<Physician> searchByFullNameOrSpeciality(String term) {
        return physicianRepository.searchByFullNameOrSpeciality(term);
    }
}
