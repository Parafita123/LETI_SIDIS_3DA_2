package com.psoft2024._5.grupo1.projeto_psoft.dto;

public class ConsultasReportDTO {

        private int year;
        private int month;
        private long totalAppointments;
        private long cancelledAppointments;
        private long rescheduledAppointments;

        public ConsultasReportDTO() {}

        public ConsultasReportDTO(int year, int month, long totalAppointments, long cancelledAppointments, long rescheduledAppointments) {
            this.year = year;
            this.month = month;
            this.totalAppointments = totalAppointments;
            this.cancelledAppointments = cancelledAppointments;
            this.rescheduledAppointments = rescheduledAppointments;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public long getTotalAppointments() {
            return totalAppointments;
        }

        public void setTotalAppointments(long totalAppointments) {
            this.totalAppointments = totalAppointments;
        }

        public long getCancelledAppointments() {
            return cancelledAppointments;
        }

        public void setCancelledAppointments(long cancelledAppointments) {
            this.cancelledAppointments = cancelledAppointments;
        }

        public long getRescheduledAppointments() {
            return rescheduledAppointments;
        }

        public void setRescheduledAppointments(long rescheduledAppointments) {
            this.rescheduledAppointments = rescheduledAppointments;
        }
    }
