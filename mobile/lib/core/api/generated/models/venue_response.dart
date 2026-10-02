// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'space_response.dart';
import 'venue_response_hosting_mode.dart';
import 'venue_response_verification_status.dart';

part 'venue_response.g.dart';

@JsonSerializable()
class VenueResponse {
  const VenueResponse({
    required this.addressLine,
    required this.city,
    required this.description,
    required this.fssaiNumber,
    required this.hostingMode,
    required this.id,
    required this.latitude,
    required this.longitude,
    required this.name,
    required this.rejectionReason,
    required this.spaces,
    required this.verificationStatus,
  });
  
  factory VenueResponse.fromJson(Map<String, Object?> json) => _$VenueResponseFromJson(json);
  
  final String addressLine;
  final String city;
  final String? description;
  final String fssaiNumber;
  final VenueResponseHostingMode hostingMode;
  final String id;
  final double latitude;
  final double longitude;
  final String name;
  final String? rejectionReason;
  final List<SpaceResponse> spaces;
  final VenueResponseVerificationStatus verificationStatus;

  Map<String, Object?> toJson() => _$VenueResponseToJson(this);
}
