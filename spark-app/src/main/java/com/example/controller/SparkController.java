package com.example.controller;

import com.example.service.SparkDataGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SparkController {

    private final SparkDataGenerator generator;

    public SparkController(SparkDataGenerator generator) {
        this.generator = generator;
    }

    @GetMapping("/generate")
    public String generateData() {
        generator.generateAndSave();
        return "✅ MongoDB에 Spark 데이터 저장 완료!";
    }
}
