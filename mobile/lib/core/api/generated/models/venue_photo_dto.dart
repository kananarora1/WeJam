// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'venue_photo_dto.g.dart';

/// Upload order; the first is the cover. Public URLs, safe to cache.
@JsonSerializable()
class VenuePhotoDto {
  const VenuePhotoDto({
    required this.id,
    required this.url,
  });
  
  factory VenuePhotoDto.fromJson(Map<String, Object?> json) => _$VenuePhotoDtoFromJson(json);
  
  final String id;

  /// Permanent public URL; safe to cache
  final String url;

  Map<String, Object?> toJson() => _$VenuePhotoDtoToJson(this);
}
