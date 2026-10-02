package com.example.wejam.venue.dto;

import com.example.wejam.venue.model.SoundPolicy;
import com.example.wejam.venue.model.Space;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.UUID;

public record SpaceResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int capacity,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SoundPolicy soundPolicy,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true, type = "string", example = "23:00")
        @JsonFormat(pattern = "HH:mm") LocalTime soundCurfew,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String houseRules) {

    public static SpaceResponse from(Space space) {
        return new SpaceResponse(space.getId(), space.getName(), space.getCapacity(), space.getSoundPolicy(),
                space.getSoundCurfew(), space.getHouseRules());
    }
}
