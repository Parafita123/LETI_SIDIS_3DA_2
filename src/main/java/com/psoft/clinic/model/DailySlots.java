package com.psoft.clinic.model;

import java.util.List;

public class DailySlots {
    private String date;           // formato dd/MM/yyyy
    private String dayOfWeek;      // ex: "Segunda-feira"
    private boolean closed;        // true se a clínica estiver fechada (domingo)
    private List<String> slots;    // lista de horários livres (formato HH:mm)

    public DailySlots() { }

    public DailySlots(String date, String dayOfWeek, boolean closed, List<String> slots) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.closed = closed;
        this.slots = slots;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public boolean isClosed() {
        return closed;
    }
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public List<String> getSlots() {
        return slots;
    }
    public void setSlots(List<String> slots) {
        this.slots = slots;
    }
}
