// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'venue_verification_item_status.dart';
import 'verification_checks.dart';

part 'venue_verification_item.g.dart';

@JsonSerializable()
class VenueVerificationItem {
  const VenueVerificationItem({
    required this.checks,
    required this.city,
    required this.fssaiNumber,
    required this.name,
    required this.ownerName,
    required this.requestedAt,
    required this.status,
    required this.venueId,
  });
  
  factory VenueVerificationItem.fromJson(Map<String, Object?> json) => _$VenueVerificationItemFromJson(json);
  
  final VerificationChecks checks;
  final String city;
  final String fssaiNumber;
  final String name;
  final String? ownerName;
  final DateTime requestedAt;
  final VenueVerificationItemStatus status;
  final String venueId;

  Map<String, Object?> toJson() => _$VenueVerificationItemToJson(this);
}
