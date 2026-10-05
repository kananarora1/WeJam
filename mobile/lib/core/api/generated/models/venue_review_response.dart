// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'venue_response.dart';
import 'verification_checks.dart';

part 'venue_review_response.g.dart';

@JsonSerializable()
class VenueReviewResponse {
  const VenueReviewResponse({
    required this.checks,
    required this.ownerName,
    required this.ownerPhone,
    required this.venue,
  });
  
  factory VenueReviewResponse.fromJson(Map<String, Object?> json) => _$VenueReviewResponseFromJson(json);
  
  final VerificationChecks checks;
  final String? ownerName;
  final String? ownerPhone;
  final VenueResponse venue;

  Map<String, Object?> toJson() => _$VenueReviewResponseToJson(this);
}
