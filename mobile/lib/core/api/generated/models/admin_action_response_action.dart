// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

@JsonEnum()
enum AdminActionResponseAction {
  @JsonValue('VENUE_APPROVED')
  venueApproved('VENUE_APPROVED'),
  @JsonValue('VENUE_REJECTED')
  venueRejected('VENUE_REJECTED'),
  @JsonValue('HOST_APPROVED')
  hostApproved('HOST_APPROVED'),
  @JsonValue('HOST_REJECTED')
  hostRejected('HOST_REJECTED'),
  /// Default value for all unparsed values, allows backward compatibility when adding new values on the backend.
  $unknown(null);

  const AdminActionResponseAction(this.json);

  factory AdminActionResponseAction.fromJson(String json) => values.firstWhere(
        (e) => e.json == json,
        orElse: () => $unknown,
      );

  final String? json;

  @override
  String toString() => json?.toString() ?? super.toString();
  /// Returns all defined enum values excluding the $unknown value.
  static List<AdminActionResponseAction> get $valuesDefined => values.where((value) => value != $unknown).toList();
}
