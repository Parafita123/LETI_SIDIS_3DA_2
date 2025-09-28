package com.psoft.clinic.appointmentsmanagement.api;

import com.psoft.clinic.model.DailySlots;
import lombok.Getter;

import java.util.List;

@Getter
public class AvailableSlotsView {
    private String date;
    private String dayOfWeek;
    private boolean closed;
    private List<String> slots;

    public AvailableSlotsView(DailySlots dto) {
        this.date = dto.getDate();
        this.dayOfWeek = dto.getDayOfWeek();
        this.closed = dto.isClosed();
        this.slots = dto.getSlots();
    }

}
