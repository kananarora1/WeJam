package com.example.wejam.venue.dto;

import com.example.wejam.venue.model.Space;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record SpaceResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int capacity) {

    public static SpaceResponse from(Space space) {
        return new SpaceResponse(space.getId(), space.getName(), space.getCapacity());
    }
}
