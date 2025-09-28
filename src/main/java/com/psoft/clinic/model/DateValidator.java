package com.psoft.clinic.model;

import com.psoft.clinic.exceptions.InvalidDateException;

import java.time.LocalDate;
import java.time.Year;

public class DateValidator {

    public static LocalDate validateDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            throw new InvalidDateException("Data não informada. Formato esperado: dd/MM/yyyy.");
        }

        String[] parts = dateStr.split("/");
        if (parts.length != 3) {
            throw new InvalidDateException(
                    "Formato inválido: '" + dateStr + "'. Use dd/MM/yyyy."
            );
        }

        int day, month, year;
        try {
            day   = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
            year  = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new InvalidDateException(
                    "Data contém caracteres inválidos: '" + dateStr + "'. Use apenas números."
            );
        }

        if (month < 1 || month > 12) {
            throw new InvalidDateException(
                    "Mês inválido: " + month + ". Deve estar entre 1 e 12."
            );
        }

        int maxDay;
        switch (month) {
            case  1: case  3: case  5: case  7:
            case  8: case 10: case 12:
                maxDay = 31; break;
            case  4: case  6: case  9: case 11:
                maxDay = 30; break;
            case  2:
                maxDay = Year.isLeap(year) ? 29 : 28; break;
            default:
                // Já tratado acima, mas só por garantia
                throw new InvalidDateException("Erro interno de validação de mês.");
        }

        if (day < 1 || day > maxDay) {
            throw new InvalidDateException(
                    "Dia inválido: " + day + " para o mês " + month +
                            ". Deve estar entre 1 e " + maxDay + "."
            );
        }

        LocalDate date;
        try {
            date = LocalDate.of(year, month, day);
        } catch (Exception e) {
            throw new InvalidDateException(
                    "Data inválida: '" + dateStr + "'. Verifique valores."
            );
        }

        if (date.isAfter(LocalDate.now())) {
            throw new InvalidDateException(
                    "Data no futuro não permitida: '" + dateStr + "'."
            );
        }

        return date;
    }
}
