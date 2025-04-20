package com.example.service;

import com.example.domain.StatePopulation;
import com.example.repository.StatePopulationRepository;
import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SparkDataGenerator {

    private final StatePopulationRepository repository;

    public SparkDataGenerator(StatePopulationRepository repository) {
        this.repository = repository;
    }

    public void generateAndSave() {
        SparkConf conf = new SparkConf().setAppName("StatePopulationApp").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Tuple2<String, Integer>> stateData = Arrays.asList(
                new Tuple2<>("California", 268280590),
                new Tuple2<>("Montana", 7105432),
                new Tuple2<>("Utah", 20333580),
                new Tuple2<>("North Carolina", 68914016),
                new Tuple2<>("Georgia", 70021737),
                new Tuple2<>("Tennessee", 45494345),
                new Tuple2<>("Pennsylvania", 89376524),
                new Tuple2<>("Washington", 48931464),
                new Tuple2<>("Massachusetts", 46888171),
                new Tuple2<>("Kentucky", 30777934)
                // ... 100개 정도 복제하거나 랜덤 생성 가능
        );

        JavaRDD<Tuple2<String, Integer>> rdd = sc.parallelize(stateData);

        List<StatePopulation> entities = rdd.collect().stream().map(tuple -> {
            StatePopulation entity = new StatePopulation();
            entity.setId(UUID.randomUUID().toString());
            entity.setState(tuple._1);
            entity.setPopulation(tuple._2);
            return entity;
        }).collect(Collectors.toList());

        repository.saveAll(entities);

        sc.stop();
    }
}

