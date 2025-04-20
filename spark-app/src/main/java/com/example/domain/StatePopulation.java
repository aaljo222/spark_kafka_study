package com.example.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;

@Data
@Document(collection = "state_population")
public class StatePopulation {
    @Id
    private String id;
    private String state;
    private Integer population;
}

