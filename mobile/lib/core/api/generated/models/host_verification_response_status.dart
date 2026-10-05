// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

/// Optional perk: NOT_REQUESTED is a normal state, not a problem
@JsonEnum()
enum HostVerificationResponseStatus {
  @JsonValue('NOT_REQUESTED')
  notRequested('NOT_REQUESTED'),
  @JsonValue('PENDING')
  pending('PENDING'),
  @JsonValue('VERIFIED')
  verified('VERIFIED'),
  @JsonValue('REJECTED')
  rejected('REJECTED'),
  /// Default value for all unparsed values, allows backward compatibility when adding new values on the backend.
  $unknown(null);

  const HostVerificationResponseStatus(this.json);

  factory HostVerificationResponseStatus.fromJson(String json) => values.firstWhere(
        (e) => e.json == json,
        orElse: () => $unknown,
      );

  final String? json;

  @override
  String toString() => json?.toString() ?? super.toString();
  /// Returns all defined enum values excluding the $unknown value.
  static List<HostVerificationResponseStatus> get $valuesDefined => values.where((value) => value != $unknown).toList();
}
