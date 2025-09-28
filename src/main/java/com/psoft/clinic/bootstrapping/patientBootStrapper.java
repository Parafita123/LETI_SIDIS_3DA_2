package com.psoft.clinic.bootstrapping;

import com.psoft.clinic.model.PhoneType;
import com.psoft.clinic.patientmanagement.repository.SpringBootPatientRepository;
import com.psoft.clinic.patientmanagement.services.CreatePatientRequest;
import com.psoft.clinic.patientmanagement.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("bootstrap")
@RequiredArgsConstructor
@Order(5)
public class patientBootStrapper implements CommandLineRunner {

    private final PatientService patientService;
    private final SpringBootPatientRepository patientRepository;

    @Override
    @Transactional
    public void run(String... args){
        createIfNotExists(
                "alice.santos",
                "Alice Santos",
                null,
                "senha123",
                "12/03/1985",
                "Rua das Flores, 123",
                "Lisboa",
                "Lisboa",
                "1000-200",
                "Portugal",
                "+351912345678",
                PhoneType.MOBILE,
                "alice.santos@example.com",
                "SaúdeMais",
                "SM-987654",
                "true",
                "FEMALE"
        );
        createIfNotExists(
                "bruno.pereira",
                "Bruno Pereira",
                "147258369",
                "segredo456",
                "25/07/1990",
                "Av. da Liberdade, 45",
                "Porto",
                "Porto",
                "4000-150",
                "Portugal",
                "+351919876543",
                PhoneType.HOME,
                "bruno.pereira@example.com",
                "ClinInsure",
                "CI-123789",
                "true",
                "MALE"
        );
        createIfNotExists(
                "carla.melo",
                "Carla Melo",
                "654987321",
                "minhaSenha789",
                "05/11/1978",
                "Rua do Mercado, 9",
                "Coimbra",
                "Coimbra",
                "3000-500",
                "Portugal",
                "+351913333444",
                PhoneType.WORK,
                "carla.melo@example.com",
                "VidaSaúde",
                "VS-456321",
                "true",
                "FEMALE"
        );
        createIfNotExists(
                "diana.azevedo",
                "Diana Azevedo",
                "789123456",
                "dianaPass123",
                "14/02/1988",
                "Rua do Sol, 77",
                "Braga",
                "Braga",
                "4700-300",
                "Portugal",
                "+351912000111",
                PhoneType.MOBILE,
                "diana.azevedo@example.com",
                "CuidarBem",
                "CB-112233",
                "true",
                "FEMALE"
        );
        createIfNotExists(
                "eduardo.lima",
                "Eduardo Lima",
                "321654987",
                "eduardoSeg789",
                "30/09/1975",
                "Av. 25 de Abril, 101",
                "Faro",
                "Faro",
                "8000-150",
                "Portugal",
                "+351913000222",
                PhoneType.HOME,
                "eduardo.lima@example.com",
                "SaúdeTotal",
                "ST-445566",
                "true",
                "MALE"
        );
        createIfNotExists(
                "fernanda.rosa",
                "Fernanda Rosa",
                "000000000",
                "rosaPass456",
                "21/06/1992",
                "Travessa do Poço, 12",
                "Setúbal",
                "Setúbal",
                "2900-100",
                "Portugal",
                "+351914000333",
                PhoneType.WORK,
                "fernanda.rosa@example.com",
                "VidaFácil",
                "VF-778899",
                "true",
                "FEMALE"
        );
        createIfNotExists(
                "gustavo.alves",
                "Gustavo Alves",
                "987654321",
                "gusta12345",
                "08/12/1980",
                "Rua Nova, 5",
                "Aveiro",
                "Aveiro",
                "3800-250",
                "Portugal",
                "+351915000444",
                PhoneType.MOBILE,
                "gustavo.alves@example.com",
                "ClinSaúde",
                "CS-334455",
                "true",
                "MALE"
        );
        createIfNotExists(
                "helena.silva",
                "Helena Silva",
                "123456789",
                "helenaPwd321",
                "17/03/1983",
                "Largo do Carmo, 23",
                "Évora",
                "Évora",
                "7000-180",
                "Portugal",
                "+351916000555",
                PhoneType.HOME,
                "helena.silva@example.com",
                "SeguroMais",
                "SM-556677",
                "true",
                "FEMALE"

        );
    }

    private void createIfNotExists(
            String username,
            String fullName,
            String nsns,
            String password,
            String dateOfBirth,      // dd/MM/yyyy
            String street,
            String city,
            String district,
            String zip,
            String country,
            String phoneNumber,
            PhoneType phoneType,
            String email,
            String insuranceProvider,
            String policyNumber,
            String consent,
            String gender
    ) {
        if (patientRepository.existsByBaseUserUsername(username)) {
            return;
        }
        CreatePatientRequest req = new CreatePatientRequest();
        req.setUsername(username);
        req.setFullName(fullName);
        req.setNsns(nsns);
        req.setPassword(password);
        req.setDateOfBirth(dateOfBirth);
        req.setStreet(street);
        req.setCity(city);
        req.setDistrict(district);
        req.setZip(zip);
        req.setCountry(country);
        req.setPhoneNumber(phoneNumber);
        req.setPhoneType(phoneType);
        req.setEmail(email);
        req.setInsuranceProvider(insuranceProvider);
        req.setPolicyNumber(policyNumber);
        req.setConsent(consent);
        req.setGender(gender);

        patientService.create(req,null);
    }
}
