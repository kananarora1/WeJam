// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

/// Only HOST or VENUE_ADMIN are accepted
@JsonEnum()
enum AddRoleRequestRole {
  @JsonValue('USER')
  user('USER'),
  @JsonValue('HOST')
  host('HOST'),
  @JsonValue('VENUE_ADMIN')
  venueAdmin('VENUE_ADMIN'),
  /// Default value for all unparsed values, allows backward compatibility when adding new values on the backend.
  $unknown(null);

  const AddRoleRequestRole(this.json);

  factory AddRoleRequestRole.fromJson(String json) => values.firstWhere(
        (e) => e.json == json,
        orElse: () => $unknown,
      );

  final String? json;

  @override
  String toString() => json?.toString() ?? super.toString();
  /// Returns all defined enum values excluding the $unknown value.
  static List<AddRoleRequestRole> get $valuesDefined => values.where((value) => value != $unknown).toList();
}
