package com.example.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SensorState {
    public double sum;
    public long count;

    public double avg() {
        return count == 0 ? 0 : sum / count;
    }
}
