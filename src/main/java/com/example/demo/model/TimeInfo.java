package com.example.demo.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeInfo {
    private String localTime;  // текущее местное время
    private String utcTime;    // текущее UTC время в формате UFC (ISO 8601)
    private String timeDescription;
}
