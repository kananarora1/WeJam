package com.example.wejam.host.dto;

import com.example.wejam.common.model.Genre;
import com.example.wejam.host.model.GroupKind;
import com.example.wejam.host.model.HostProfile;
import com.example.wejam.host.model.HostType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record HostProfileResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) HostType type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) GroupKind groupKind,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true,
                description = "Group name, or the owner's display name for an individual (null if not set yet)")
        String displayName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String bio,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String area,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String instagramHandle,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<Genre> genres,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<MediaLinkDto> mediaLinks,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Owner first (admin), then accepted members. Pending invites are never listed here.")
        List<HostMemberDto> members) {

    public static HostProfileResponse from(HostProfile profile, String ownerDisplayName, List<HostMemberDto> members) {
        String displayName = profile.getType() == HostType.GROUP ? profile.getGroupName() : ownerDisplayName;
        return new HostProfileResponse(profile.getId(), profile.getType(), profile.getGroupKind(), displayName,
                profile.getBio(), profile.getArea(), profile.getInstagramHandle(), profile.getGenres(),
                profile.getMediaLinks().stream().map(l -> new MediaLinkDto(l.url(), l.title())).toList(), members);
    }
}
