package com.planitsquare.holidaykeeper.holiday.entity;

import com.planitsquare.holidaykeeper.country.Country;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** 공휴일 데이터 저장 */
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String localName;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country countryCode;

    private boolean fixed;
    private boolean global;
    @ElementCollection
    private List<String> counties;

    private Integer launchYear;
    @ElementCollection
    private List<String> types;


    @Column(name = "holiday_year")
    private int year;
    @PrePersist
    public void prePersist()
    {
        this.year = this.date.getYear();
    }
}