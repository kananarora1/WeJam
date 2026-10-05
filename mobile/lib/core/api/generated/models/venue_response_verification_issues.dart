// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

/// What to fix after a rejection; empty otherwise
@JsonEnum()
enum VenueResponseVerificationIssues {
  @JsonValue('FSSAI_NUMBER')
  fssaiNumber('FSSAI_NUMBER'),
  @JsonValue('FSSAI_CERTIFICATE')
  fssaiCertificate('FSSAI_CERTIFICATE'),
  @JsonValue('LEASE_AGREEMENT')
  leaseAgreement('LEASE_AGREEMENT'),
  @JsonValue('VENUE_DETAILS')
  venueDetails('VENUE_DETAILS'),
  /// Default value for all unparsed values, allows backward compatibility when adding new values on the backend.
  $unknown(null);

  const VenueResponseVerificationIssues(this.json);

  factory VenueResponseVerificationIssues.fromJson(String json) => values.firstWhere(
        (e) => e.json == json,
        orElse: () => $unknown,
      );

  final String? json;

  @override
  String toString() => json?.toString() ?? super.toString();
  /// Returns all defined enum values excluding the $unknown value.
  static List<VenueResponseVerificationIssues> get $valuesDefined => values.where((value) => value != $unknown).toList();
}
