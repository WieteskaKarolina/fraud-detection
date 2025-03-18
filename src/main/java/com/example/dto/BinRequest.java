package com.example.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@RegisterForReflection
public class BinRequest implements Serializable {

    @JsonProperty("accountRange")
    private String accountRange;

    @JsonCreator
    public BinRequest(@JsonProperty("accountRange") String accountRange) {
        this.accountRange = accountRange;
    }
}
