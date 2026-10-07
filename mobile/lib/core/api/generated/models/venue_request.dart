// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'venue_request_hosting_mode.dart';

part 'venue_request.g.dart';

@JsonSerializable()
class VenueRequest {
  const VenueRequest({
    required this.addressLine,
    required this.city,
    required this.fssaiLicenseName,
    required this.fssaiNumber,
    required this.fssaiValidUntil,
    required this.hostingMode,
    required this.latitude,
    required this.longitude,
    required this.name,
    this.description,
  });
  
  factory VenueRequest.fromJson(Map<String, Object?> json) => _$VenueRequestFromJson(json);
  
  final String addressLine;
  final String city;
  final String? description;

  /// Business name printed on the license. Changing it resets verification to PENDING.
  final String fssaiLicenseName;

  /// FSSAI license number. Changing it resets verification to PENDING.
  final String fssaiNumber;

  /// Expiry date on the license; can't be in the past. Changing it (e.g. a renewal) resets verification to PENDING.
  final DateTime fssaiValidUntil;
  final VenueRequestHostingMode hostingMode;
  final double latitude;
  final double longitude;
  final String name;

  Map<String, Object?> toJson() => _$VenueRequestToJson(this);
}
