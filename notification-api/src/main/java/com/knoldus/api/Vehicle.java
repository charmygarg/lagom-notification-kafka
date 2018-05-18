package com.knoldus.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import javax.annotation.concurrent.Immutable;

@Value
@Builder
@Immutable
@JsonDeserialize
@AllArgsConstructor
public class Vehicle {
    Integer vid;
    String car_number;
    String rc_number;
    String puc_number ;
    String chasis_number;
    String insurance;
    String id;
}
