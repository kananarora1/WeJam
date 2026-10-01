// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'venue_summary_verification_status.dart';

part 'venue_summary.g.dart';

@JsonSerializable()
class VenueSummary {
  const VenueSummary({
    required this.city,
    required this.id,
    required this.name,
    required this.spaceCount,
    required this.verificationStatus,
  });
  
  factory VenueSummary.fromJson(Map<String, Object?> json) => _$VenueSummaryFromJson(json);
  
  final String city;
  final String id;
  final String name;
  final int spaceCount;
  final VenueSummaryVerificationStatus verificationStatus;

  Map<String, Object?> toJson() => _$VenueSummaryToJson(this);
}
