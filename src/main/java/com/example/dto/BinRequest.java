package com.example.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@Data
@NoArgsConstructor
@RegisterForReflection
@Schema(description = "Request DTO for BIN lookup")
public class BinRequest implements Serializable {

    @JsonProperty("accountRange")
    @Schema(
            description = "The first 6-9 digits of the card number",
            example = "585240",
            required = true
    )
    private String accountRange;

    @JsonCreator
    public BinRequest(@JsonProperty("accountRange") String accountRange) {
        this.accountRange = accountRange;
    }
}
