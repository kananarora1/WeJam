// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'venue_request_hosting_mode.dart';
import 'venue_request_sound_policy.dart';

part 'venue_request.g.dart';

@JsonSerializable()
class VenueRequest {
  const VenueRequest({
    required this.addressLine,
    required this.city,
    required this.fssaiNumber,
    required this.hostingMode,
    required this.latitude,
    required this.longitude,
    required this.name,
    required this.soundPolicy,
    this.description,
    this.houseRules,
    this.soundCurfew,
  });
  
  factory VenueRequest.fromJson(Map<String, Object?> json) => _$VenueRequestFromJson(json);
  
  final String addressLine;
  final String city;
  final String? description;

  /// FSSAI license number. Changing it resets verification to PENDING.
  final String fssaiNumber;
  final VenueRequestHostingMode hostingMode;
  final String? houseRules;
  final double latitude;
  final double longitude;
  final String name;

  /// Local time after which amplified sound must stop; null = no curfew
  final String? soundCurfew;
  final VenueRequestSoundPolicy soundPolicy;

  Map<String, Object?> toJson() => _$VenueRequestToJson(this);
}
