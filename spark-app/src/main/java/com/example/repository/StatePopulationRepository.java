package com.example.repository;

import com.example.domain.StatePopulation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StatePopulationRepository extends MongoRepository<StatePopulation, String> {
}
