package com.psoft.clinic.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Embeddable
@NoArgsConstructor
public class ContactInfo {

    @Getter @Setter
    @Column(unique = true, nullable = false)
    private String email;

    @Embedded
    @Getter @Setter
    private Address address;

    @ElementCollection
    @CollectionTable(
            name = "physician_phones",
            joinColumns = @JoinColumn(
                    name = "physician_id",
                    foreignKey = @ForeignKey(
                            name = "fk_physician_phones_physician",
                            foreignKeyDefinition =
                                    "FOREIGN KEY (physician_id) REFERENCES physician(id) ON DELETE CASCADE"
                    )
            )
    )
    @Getter @Setter
    private List<Phone> phones;
}


