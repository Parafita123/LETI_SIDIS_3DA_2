package com.psoft.clinic.bootstrapping;

import com.psoft.clinic.appointmentsmanagement.services.AppointmentService;
import com.psoft.clinic.appointmentsmanagement.services.CreateAppointmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.psoft.clinic.appointmentsmanagement.services.AppointmentService.DATE_FMT;
import static com.psoft.clinic.appointmentsmanagement.services.AppointmentService.TIME_FMT;




@Component
@Profile("bootstrap")
@RequiredArgsConstructor
@Order(6)
public class appointmentBootStrapper implements CommandLineRunner {

    private final AppointmentService appointmentService;

    @Override
    @Transactional
    public void run(String... args) {
        String number = "2025";

        // Ana Silva
        createIfNotExists("02/07/" + number, "09:00",    "FOLLOW_UP",  "Alice Santos", "Ana Silva",
                "Revisão pós-operatória");
        createIfNotExists("02/07/" + number, "09:50",    "FOLLOW_UP",  "Eduardo Lima", "Ana Silva",
                "Acompanhamento de medicação");
        createIfNotExists("03/07/" + number, "09:25",    "FIRST_TIME", "Bruno Pereira","Ana Silva",
                "Consulta inicial e anamnese");
        createIfNotExists("05/07/" + number, "10:25",    "FOLLOW_UP",  "Carla Melo",   "Ana Silva",
                "Avaliação de resultados de exame");
        createIfNotExists("08/07/" + number, "10:50",    "FIRST_TIME", "Diana Azevedo","Ana Silva",
                "Primeira consulta de rotina");

        // Bruno Costa
        createIfNotExists("03/07/" + number, "09:00",    "FIRST_TIME", "Eduardo Lima", "Bruno Costa",
                "Consulta inicial de cardio");
        createIfNotExists("04/07/" + number, "10:25",    "FOLLOW_UP",  "Fernanda Rosa","Bruno Costa",
                "Revisão de pressão arterial");
        createIfNotExists("07/07/" + number, "09:50",    "FIRST_TIME", "Gustavo Alves","Bruno Costa",
                "Exames de rotina completos");
        createIfNotExists("09/07/" + number, "10:50",    "FOLLOW_UP",  "Helena Silva", "Bruno Costa",
                "Acompanhamento de sintomas");

        // Carla Santos
        createIfNotExists("04/07/" + number, "09:25",    "FOLLOW_UP",  "Miguel Sousa", "Carla Santos",
                "Avaliação de dor lombar");
        createIfNotExists("05/07/" + number, "10:50",    "FIRST_TIME", "Alice Santos", "Carla Santos",
                "Primeira consulta ortopédica");
        createIfNotExists("08/07/" + number, "09:00",    "FOLLOW_UP",  "Bruno Pereira","Carla Santos",
                "Controle pós-fisioterapia");
        createIfNotExists("10/07/" + number, "09:50",    "FIRST_TIME", "Carla Melo",   "Carla Santos",
                "Exame inicial de mobilidade");

        // Diogo Ferreira
        createIfNotExists("05/07/" + number, "09:00",    "FIRST_TIME", "Diana Azevedo","Diogo Ferreira",
                "Consulta inicial ginecológica");
        createIfNotExists("07/07/" + number, "09:25",    "FOLLOW_UP",  "Eduardo Lima", "Diogo Ferreira",
                "Acompanhamento de exame Papanicolau");
        createIfNotExists("09/07/" + number, "10:25",    "FIRST_TIME", "Fernanda Rosa","Diogo Ferreira",
                "Avaliação de rotina feminina");
        createIfNotExists("11/07/" + number, "09:50",    "FOLLOW_UP",  "Gustavo Alves","Diogo Ferreira",
                "Revisão de resultados laboratoriais");

        // Elaine Gomes
        createIfNotExists("07/07/" + number, "09:00",    "FOLLOW_UP",  "Helena Silva", "Elaine Gomes",
                "Monitorização de hipertensão");
        createIfNotExists("09/07/" + number, "09:25",    "FIRST_TIME", "Miguel Sousa", "Elaine Gomes",
                "Consulta nutricional inicial");
        createIfNotExists("10/07/" + number, "09:50",    "FOLLOW_UP",  "Beatriz Rocha","Elaine Gomes",
                "Acompanhamento dietético");

        // Fábio Martins
        createIfNotExists("07/07/" + number, "10:25",    "FIRST_TIME", "Alice Santos", "Fábio Martins",
                "Consulta pediátrica inicial");
        createIfNotExists("10/07/" + number, "09:25",    "FOLLOW_UP",  "Bruno Pereira","Fábio Martins",
                "Revisão de vacinações");

        // Patrícia Almeida
        createIfNotExists("08/07/" + number, "09:00",    "FIRST_TIME", "Diana Azevedo","Patrícia Almeida",
                "Consulta dermatológica inicial");
        createIfNotExists("10/07/" + number, "10:25",    "FOLLOW_UP",  "Eduardo Lima", "Patrícia Almeida",
                "Acompanhamento de tratamento de pele");
        createIfNotExists("11/07/" + number, "09:25",    "FIRST_TIME", "Beatriz Rocha","Patrícia Almeida",
                "Exame inicial de sinais vitais");
    }

    private void createIfNotExists(
            String date,
            String startTime,
            String consultationType,
            String patientFullName,
            String physicianFullName,
            String notes        // <— nota passada aqui
    ) {
        LocalDate ld = LocalDate.parse(date, DATE_FMT);
        LocalTime lt = LocalTime.parse(startTime, TIME_FMT);

        boolean exists = appointmentService.exists(ld, lt, patientFullName, physicianFullName);
        if (!exists) {
            CreateAppointmentRequest req = new CreateAppointmentRequest(
                    date,
                    startTime,
                    consultationType,
                    patientFullName,
                    physicianFullName,
                    notes
            );
            appointmentService.create(req);
        }
    }
}
