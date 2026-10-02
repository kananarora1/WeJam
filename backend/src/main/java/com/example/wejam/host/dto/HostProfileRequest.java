package com.example.wejam.host.dto;

import com.example.wejam.common.model.Genre;
import com.example.wejam.host.model.GroupKind;
import com.example.wejam.host.model.HostType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

/** Full replacement. For INDIVIDUAL, groupKind/groupName are ignored; for GROUP both are required. */
public record HostProfileRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull HostType type,
        @Schema(nullable = true, description = "Required when type is GROUP")
        GroupKind groupKind,
        @Schema(nullable = true, example = "The Low Notes", description = "Required when type is GROUP")
        @Size(max = 80) String groupName,
        @Schema(nullable = true, example = "Four friends running slow blues nights in Indiranagar since 2024.")
        @Size(max = 200) String bio,
        @Schema(nullable = true, example = "Indiranagar")
        @Size(max = 80) String area,
        @Schema(nullable = true, example = "thelownotes", description = "Instagram username, with or without @")
        @Pattern(regexp = "@?[A-Za-z0-9._]{1,30}", message = "must be a valid Instagram username") String instagramHandle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Size(max = 5) Set<@NotNull Genre> genres,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Size(max = 5) List<@Valid @NotNull MediaLinkDto> mediaLinks) {
}
