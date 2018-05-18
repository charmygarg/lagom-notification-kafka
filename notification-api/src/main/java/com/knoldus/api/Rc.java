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
public class Rc {
    String rc_number;
    String date_of_expiry;
    Integer vid;
}
