package com.example.wejam.venue.controller;

import com.example.wejam.common.web.CurrentUser;
import com.example.wejam.venue.dto.PhotoUploadRequest;
import com.example.wejam.venue.dto.PhotoUploadResponse;
import com.example.wejam.venue.dto.VenuePhotoDto;
import com.example.wejam.venue.service.VenuePhotoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Photos are listed on the venue itself ({@code VenueResponse.photos}). */
@Tag(name = "Venues")
@RestController
@RequestMapping("/api/v1/venues/{venueId}/photos")
class VenuePhotoController {

    private final VenuePhotoService photoService;

    VenuePhotoController(VenuePhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PhotoUploadResponse startVenuePhotoUpload(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId,
                                              @Valid @RequestBody PhotoUploadRequest request) {
        return photoService.startUpload(CurrentUser.id(jwt), venueId, request);
    }

    @PostMapping("/{photoId}/confirm")
    VenuePhotoDto confirmVenuePhoto(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId,
                                    @PathVariable UUID photoId) {
        return photoService.confirm(CurrentUser.id(jwt), venueId, photoId);
    }

    @DeleteMapping("/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteVenuePhoto(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID venueId, @PathVariable UUID photoId) {
        photoService.delete(CurrentUser.id(jwt), venueId, photoId);
    }
}
