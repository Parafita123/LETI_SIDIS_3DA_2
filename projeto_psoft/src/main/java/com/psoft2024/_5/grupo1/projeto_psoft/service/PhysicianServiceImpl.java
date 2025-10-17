package com.psoft2024._5.grupo1.projeto_psoft.service;

import com.psoft2024._5.grupo1.projeto_psoft.domain.Consulta;
import com.psoft2024._5.grupo1.projeto_psoft.domain.Department;
import com.psoft2024._5.grupo1.projeto_psoft.domain.Physician;
import com.psoft2024._5.grupo1.projeto_psoft.domain.Specialization;
import com.psoft2024._5.grupo1.projeto_psoft.dto.PhysicianBasicInfoDTO;
import com.psoft2024._5.grupo1.projeto_psoft.dto.PhysicianOutputDTO;
import com.psoft2024._5.grupo1.projeto_psoft.dto.RegisterPhysicianDTO;
import com.psoft2024._5.grupo1.projeto_psoft.dto.AvailableSlotDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import com.psoft2024._5.grupo1.projeto_psoft.exception.DuplicateResourceException;
import com.psoft2024._5.grupo1.projeto_psoft.exception.ResourceNotFoundException;
import com.psoft2024._5.grupo1.projeto_psoft.repository.ConsultasRepository;
import com.psoft2024._5.grupo1.projeto_psoft.repository.DepartmentRepository;
import com.psoft2024._5.grupo1.projeto_psoft.repository.PhysicianRepository;
import com.psoft2024._5.grupo1.projeto_psoft.repository.SpecializationRepository;
import com.psoft2024._5.grupo1.projeto_psoft.dto.PhysicianAppointmentCountDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.jpa.domain.Specification; // Mantém este
import jakarta.persistence.criteria.Predicate; // Mantém este

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException; // Para erro específico de parse
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhysicianServiceImpl implements PhysicianService {

    private static final int CONSULTATION_DURATION_MINUTES = 60; // Duração da consulta em si
    private static final int INTERVAL_BETWEEN_APPOINTMENTS_MINUTES = 5; // Intervalo após cada consulta
    private static final int SLOT_SEARCH_DAYS_RANGE = 10;
    private static final int MAX_MONTHS_IN_FUTURE = 3;

    private final PhysicianRepository physicianRepository;
    private final SpecializationRepository specializationRepository;
    private final DepartmentRepository departmentRepository;
    private final FileStorageService fileStorageService;// Injetado
    private final ConsultasRepository consultasRepository;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    public PhysicianServiceImpl(PhysicianRepository physicianRepository,
                                SpecializationRepository specializationRepository,
                                DepartmentRepository departmentRepository,
                                FileStorageService fileStorageService,
                                ConsultasRepository consultasRepository) {
        this.physicianRepository = physicianRepository;
        this.specializationRepository = specializationRepository;
        this.departmentRepository = departmentRepository;
        this.fileStorageService = fileStorageService;
        this.consultasRepository = consultasRepository;
    }

    // Método convertToDTO (já estava correto, apenas para referência)
    private PhysicianOutputDTO convertToDTO(Physician physician) {
        if (physician == null) {
            return null;
        }
        String photoUrl = null;
        if (physician.getProfilePhotoPath() != null && !physician.getProfilePhotoPath().isEmpty()) {
            photoUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .pathSegment("api", "physicians", "photo", physician.getProfilePhotoPath())
                    .toUriString();
        }
        return new PhysicianOutputDTO(
                physician.getId(),
                physician.getFullName(),
                physician.getSpecialty() != null ? physician.getSpecialty().getName() : null,
                physician.getDepartment() != null ? physician.getDepartment().getAcronym() : null,
                physician.getEmail(),
                physician.getPhoneNumber(),
                physician.getAddress(),
                physician.getWorkStartTime(),
                physician.getWorkEndTime(),
                physician.getOptionalDescription(),
                photoUrl
        );
    }

    // Método convertToBasicDTO (já estava correto)
    private PhysicianBasicInfoDTO convertToBasicDTO(Physician physician) {
        if (physician == null) {
            return null;
        }
        return new PhysicianBasicInfoDTO(
                physician.getFullName(),
                physician.getSpecialty() != null ? physician.getSpecialty().getName() : null
        );
    }

    @Override
    @Transactional
    public PhysicianOutputDTO registerPhysician(RegisterPhysicianDTO physicianDTO, MultipartFile profilePhotoFile) {
        // Validações de unicidade (email, telefone)
        if (physicianRepository.findByEmail(physicianDTO.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email já existe: " + physicianDTO.getEmail());
        }
        if (physicianRepository.findByPhoneNumber(physicianDTO.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("Número de telefone já existe: " + physicianDTO.getPhoneNumber());
        }

        Specialization specialty = specializationRepository.findById(physicianDTO.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with ID: " + physicianDTO.getSpecialtyId()));
        Department department = departmentRepository.findById(physicianDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + physicianDTO.getDepartmentId()));

        // Converter horas ANTES de criar a entidade Physician
        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(physicianDTO.getWorkStartTime(), timeFormatter);
            endTime = LocalTime.parse(physicianDTO.getWorkEndTime(), timeFormatter);
        } catch (DateTimeParseException e) { // Erro específico para parse de data/hora
            throw new RuntimeException("Invalid time format for working hours. Use HH:mm. Details: " + e.getMessage(), e);
        }

        // Guardar a foto (se existir) e obter o nome do ficheiro
        String profilePhotoFileName = fileStorageService.storePhysicianProfilePhoto(profilePhotoFile);

        // Criar a entidade Physician com os dados corretos
        Physician physician = new Physician(
                physicianDTO.getFullName(),
                specialty,
                department,
                physicianDTO.getEmail(),
                physicianDTO.getPhoneNumber(),
                physicianDTO.getAddress(),
                startTime, // Passa LocalTime
                endTime,   // Passa LocalTime
                physicianDTO.getOptionalDescription()
        );

        if (profilePhotoFileName != null) {
            physician.setProfilePhotoPath(profilePhotoFileName); // Define o caminho/nome da foto na entidade
        }

        Physician savedPhysician = physicianRepository.save(physician);
        return convertToDTO(savedPhysician);
    }

    // --- Métodos de Leitura (GET) ---
    @Override
    @Transactional(readOnly = true)
    public Optional<PhysicianOutputDTO> getPhysicianById(Long id) {
        return physicianRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhysicianBasicInfoDTO> getPhysicianBasicInfoById(Long id) {
        return physicianRepository.findById(id).map(this::convertToBasicDTO);
    }

    // --- Métodos de Pesquisa (Search) ---
    @Override
    @Transactional(readOnly = true)
    public List<PhysicianOutputDTO> searchPhysicians(String name, String specialtyName) {
        Specification<Physician> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + name.toLowerCase() + "%"));
            }
            if (specialtyName != null && !specialtyName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.join("specialty").get("name")), specialtyName.toLowerCase()));
            }
            if (predicates.isEmpty()) return criteriaBuilder.conjunction(); // Retorna todos se não houver predicados
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return physicianRepository.findAll(spec).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhysicianBasicInfoDTO> searchPhysiciansForPatient(String name, String specialtyName) {
        Specification<Physician> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + name.toLowerCase() + "%"));
            }
            if (specialtyName != null && !specialtyName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.join("specialty").get("name")), specialtyName.toLowerCase()));
            }
            if (predicates.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return physicianRepository.findAll(spec).stream()
                .map(this::convertToBasicDTO)
                .collect(Collectors.toList());
    }

    // --- Método de Atualização (Update) ---
    @Override
    @Transactional
    public PhysicianOutputDTO updatePhysician(Long id, RegisterPhysicianDTO physicianDTO) {
        // NOTA: Este método NÃO lida com atualização de foto.
        // Para atualizar a foto, seria necessário um endpoint e método de serviço dedicados,
        // ou modificar este para também aceitar um MultipartFile opcional.

        Physician physicianToUpdate = physicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Physician not found with ID: " + id));

        // Validar unicidade de email (se mudou e já existe noutro)
        if (!physicianToUpdate.getEmail().equalsIgnoreCase(physicianDTO.getEmail()) &&
                physicianRepository.findByEmail(physicianDTO.getEmail()).filter(p -> !p.getId().equals(id)).isPresent()) {
            throw new DuplicateResourceException("Email já existe para outro médico: " + physicianDTO.getEmail());
        }

        // Validar unicidade de telefone (se mudou e já existe noutro)
        if (!physicianToUpdate.getPhoneNumber().equals(physicianDTO.getPhoneNumber()) &&
                physicianRepository.findByPhoneNumber(physicianDTO.getPhoneNumber()).filter(p -> !p.getId().equals(id)).isPresent()) {
            throw new DuplicateResourceException("Número de telefone já existe para outro médico: " + physicianDTO.getPhoneNumber());
        }

        Specialization specialty = specializationRepository.findById(physicianDTO.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with ID: " + physicianDTO.getSpecialtyId()));
        Department department = departmentRepository.findById(physicianDTO.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + physicianDTO.getDepartmentId()));

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(physicianDTO.getWorkStartTime(), timeFormatter);
            endTime = LocalTime.parse(physicianDTO.getWorkEndTime(), timeFormatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid time format for working hours. Use HH:mm. Details: " + e.getMessage(), e);
        }

        // Atualizar campos
        physicianToUpdate.setFullName(physicianDTO.getFullName());
        physicianToUpdate.setSpecialty(specialty);
        physicianToUpdate.setDepartment(department);
        physicianToUpdate.setEmail(physicianDTO.getEmail());
        physicianToUpdate.setPhoneNumber(physicianDTO.getPhoneNumber());
        physicianToUpdate.setAddress(physicianDTO.getAddress());
        physicianToUpdate.setWorkStartTime(startTime);
        physicianToUpdate.setWorkEndTime(endTime);
        physicianToUpdate.setOptionalDescription(physicianDTO.getOptionalDescription());
        // O profilePhotoPath não é atualizado aqui. Precisaria de lógica separada.

        Physician updatedPhysician = physicianRepository.save(physicianToUpdate);
        return convertToDTO(updatedPhysician);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhysicianAppointmentCountDTO> getTop5PhysiciansByAppointments() {
        Pageable topFive = PageRequest.of(0, 5); // Pega a primeira página (0) com 5 elementos
        return consultasRepository.findTopPhysiciansByAppointmentCount(topFive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableSlotDTO> getAvailableSlots(Long physicianId, LocalDate startDate) {
        Physician physician = physicianRepository.findById(physicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Physician not found with ID: " + physicianId));

        LocalTime physicianWorkStartTime = physician.getWorkStartTime();
        LocalTime physicianWorkEndTime = physician.getWorkEndTime();

        if (physicianWorkStartTime == null || physicianWorkEndTime == null || physicianWorkStartTime.isAfter(physicianWorkEndTime)) {
            return Collections.emptyList();
        }

        List<AvailableSlotDTO> allAvailableSlots = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate maxSearchDate = today.plusMonths(MAX_MONTHS_IN_FUTURE);

        if (startDate.isBefore(today) || startDate.isAfter(maxSearchDate)) {
            // Se startDate é antes de hoje, ou se ultrapassa o limite, não retorna slots.
            // Se startDate for hoje, a lógica de `earliestSlotToday` tratará de filtrar o passado.
            if(!startDate.isEqual(today) && startDate.isBefore(today)) return Collections.emptyList();
            if(startDate.isAfter(maxSearchDate)) return Collections.emptyList();
        }

        for (int i = 0; i <= SLOT_SEARCH_DAYS_RANGE; i++) {
            LocalDate currentDateToSearch = startDate.plusDays(i);
            if (currentDateToSearch.isAfter(maxSearchDate)) {
                break;
            }

            LocalDateTime dayStartBoundary = currentDateToSearch.atStartOfDay();
            LocalDateTime dayEndBoundary = currentDateToSearch.atTime(LocalTime.MAX);
            List<Consulta> existingAppointmentsOnDate = consultasRepository
                    .findByPhysicianAndDateTimeBetween(physician, dayStartBoundary, dayEndBoundary);

            LocalTime currentPotentialSlotStart = physicianWorkStartTime;

            if (currentDateToSearch.isEqual(today)) {
                LocalTime now = LocalTime.now();
                // Só considera slots que começam pelo menos, por exemplo, 1 hora a partir de agora,
                // e alinhados com a granularidade da duração da consulta.
                LocalTime earliestPossibleStart = now.plusHours(1).withSecond(0).withNano(0);
                if (earliestPossibleStart.getMinute() % CONSULTATION_DURATION_MINUTES != 0) {
                    int minutesToAdd = CONSULTATION_DURATION_MINUTES - (earliestPossibleStart.getMinute() % CONSULTATION_DURATION_MINUTES);
                    earliestPossibleStart = earliestPossibleStart.plusMinutes(minutesToAdd).withMinute(0); // Ajusta para a próxima meia hora ou hora cheia
                }


                if (currentPotentialSlotStart.isBefore(earliestPossibleStart)) {
                    currentPotentialSlotStart = earliestPossibleStart;
                }
            }

            // Loop para gerar slots
            while (true) {
                LocalDateTime slotStartDateTime = currentDateToSearch.atTime(currentPotentialSlotStart);
                LocalDateTime slotEndDateTime = slotStartDateTime.plusMinutes(CONSULTATION_DURATION_MINUTES);

                // Verifica se o fim do slot (CONSULTA_DURATION_MINUTES) ultrapassa o horário de trabalho
                if (slotEndDateTime.toLocalTime().isAfter(physicianWorkEndTime)) {
                    break; // Não cabem mais slots neste dia
                }

                boolean isOverlapping = false;
                for (Consulta existingApp : existingAppointmentsOnDate) {
                    LocalDateTime existingAppStartTime = existingApp.getDateTime();
                    LocalDateTime existingAppEndTime = existingAppStartTime.plusMinutes(existingApp.getDuration());

                    // Verifica sobreposição considerando o intervalo *após* a consulta existente
                    // Um novo slot não pode começar antes do fim de uma consulta existente + o intervalo
                    LocalDateTime existingAppBlockEnd = existingAppEndTime.plusMinutes(INTERVAL_BETWEEN_APPOINTMENTS_MINUTES);

                    // Novo slot (slotStartDateTime até slotEndDateTime)
                    // Bloco ocupado por consulta existente (existingAppStartTime até existingAppBlockEnd)
                    if (slotStartDateTime.isBefore(existingAppBlockEnd) && slotEndDateTime.isAfter(existingAppStartTime)) {
                        isOverlapping = true;
                        break;
                    }
                }

                if (!isOverlapping) {
                    allAvailableSlots.add(new AvailableSlotDTO(currentDateToSearch, currentPotentialSlotStart));
                }

                // Avança para o início do próximo slot potencial:
                // fim do slot atual + intervalo
                currentPotentialSlotStart = currentPotentialSlotStart.plusMinutes(CONSULTATION_DURATION_MINUTES + INTERVAL_BETWEEN_APPOINTMENTS_MINUTES);
            }
        }
        return allAvailableSlots;
    }
}