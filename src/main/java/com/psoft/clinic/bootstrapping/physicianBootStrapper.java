package com.psoft.clinic.bootstrapping;

import com.psoft.clinic.model.PhoneType;
import com.psoft.clinic.physiciansmanagement.services.CreatePhysicianRequest;
import com.psoft.clinic.physiciansmanagement.services.PhysicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
@Profile("bootstrap")
@RequiredArgsConstructor
@Order(3)
public class physicianBootStrapper implements CommandLineRunner {

    private final PhysicianService physicianService;

    @Override
    @Transactional
    public void run(String... args) {
        createIfNotExists("ana.silva", "Password1!", "Ana Silva", "CARD", "Cardiologia", "ana.silva@clinic.com",
                "Rua A, 100", "Lisboa", "Centro", "1000-100", "Portugal", "+351912345678", PhoneType.WORK, "08:00", "14:00");

        createIfNotExists("bruno.costa", "Password2!", "Bruno Costa", "DERM", "Dermatologia", "bruno.costa@clinic.com",
                "Rua B, 200", "Porto", "Aliados", "4000-100", "Portugal", "+351912345679", PhoneType.WORK, "09:00", "17:00");

        createIfNotExists("carla.santos", "Password3!", "Carla Santos", "ORTO", "Ortopedia", "carla.santos@clinic.com",
                "Rua C, 300", "Coimbra", "Baixa", "3000-100", "Portugal", "+351912345680", PhoneType.HOME, "08:30", "16:30");

        createIfNotExists("carla.santos", "Password3!", "Carla Santos", "ORTO", "Ortopedia", "carla.santos@clinic.com",
                "Rua C, 300", "Coimbra", "Baixa", "3000-100", "Portugal", "+351912345681", PhoneType.WORK, "08:30", "16:30");

        createIfNotExists("diogo.ferreira", "Password4!", "Diogo Ferreira", "PED", "Pediatria", "diogo.ferreira@clinic.com",
                "Rua D, 400", "Braga", "Sé", "4700-100", "Portugal", "+351912345682", PhoneType.WORK, "08:00", "15:00");

        createIfNotExists("elaine.gomes", "Password5!", "Elaine Gomes", "GAST", "Gastroenterologia", "elaine.gomes@clinic.com",
                "Rua E, 500", "Faro", "Centro", "8000-100", "Portugal", "+351912345683", PhoneType.WORK, "10:00", "18:00");

        createIfNotExists("fabio.martins", "Password6!", "Fábio Martins", "NEURO", "Neurologia", "fabio.martins@clinic.com",
                "Rua F, 600", "Aveiro", "Centro", "3800-100", "Portugal", "+351912345684", PhoneType.WORK, "09:30", "17:30");

        createIfNotExists("patricia.almeida", "Password7!", "Patrícia Almeida", "OBG", "Obstetrícia", "patricia.almeida@clinic.com",
                "Rua G, 700", "Viseu", "Baixa", "3500-100", "Portugal", "+351912345685", PhoneType.WORK, "08:00", "14:00");

        createIfNotExists("ricardo.rodrigues", "Password8!", "Ricardo Rodrigues", "PSIQ", "Psiquiatria", "ricardo.rodrigues@clinic.com",
                "Rua H, 800", "Évora", "Centro", "7000-100", "Portugal", "+351912345686", PhoneType.WORK, "11:00", "19:00");

        createIfNotExists("susana.sousa", "Password9!", "Susana Sousa", "ONCO", "Oncologia", "susana.sousa@clinic.com",
                "Rua I, 900", "Leiria", "Lis", "2400-100", "Portugal", "+351912345687", PhoneType.WORK, "08:30", "16:30");

        createIfNotExists("tiago.pereira", "Password10!", "Tiago Pereira", "ENDO", "Endocrinologia", "tiago.pereira@clinic.com",
                "Rua J, 1000", "Viana do Castelo", "Centro", "4900-100", "Portugal", "+351912345688", PhoneType.WORK, "09:00", "17:00");
    }

    private static class EmptyMultipartFile implements MultipartFile {
        private final String name, originalFilename;
        private final byte[] content = new byte[0];

        public EmptyMultipartFile(String name, String originalFilename) {
            this.name = name;
            this.originalFilename = originalFilename;
        }
        @Override public String getName()              { return name; }
        @Override public String getOriginalFilename()  { return originalFilename; }
        @Override public String getContentType()       {
            // força um tipo válido para passar na validação
            return "image/jpeg";
        }
        @Override public boolean isEmpty()             { return true; }
        @Override public long getSize()                { return 0; }
        @Override public byte[] getBytes()             { return content; }
        @Override public InputStream getInputStream()  {
            return new ByteArrayInputStream(content);
        }
        @Override public void transferTo(File dest)    throws IOException { /* nop */ }
    }

    private void createIfNotExists(
            String username,
            String password,
            String fullName,
            String departmentSigla,
            String speciality,
            String email,
            String street,
            String district,
            String city,
            String zip,
            String country,
            String phoneNumber,
            PhoneType phoneType,
            String startTime,
            String endTime
    ) {
        if (physicianService.findByUsername(username).isEmpty()) {
            CreatePhysicianRequest req = new CreatePhysicianRequest();
            req.setUsername(username);
            req.setPassword(password);
            req.setFullName(fullName);
            req.setDepartmentSigla(departmentSigla);
            req.setSpeciality(speciality);
            req.setEmail(email);
            req.setStreet(street);
            req.setDistrict(district);
            req.setCity(city);
            req.setZip(zip);
            req.setCountry(country);
            req.setPhoneNumber(phoneNumber);
            req.setPhoneType(phoneType);
            req.setStartTime(startTime);
            req.setEndTime(endTime);

            MultipartFile emptyFile = new EmptyMultipartFile("file", "placeholder.jpg");
            physicianService.create(req, emptyFile);

        }
    }
}
