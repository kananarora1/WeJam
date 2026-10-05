// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

@JsonEnum()
enum HostVerificationResponseIdType {
  @JsonValue('COLLEGE_ID')
  collegeId('COLLEGE_ID'),
  @JsonValue('COMPANY_ID')
  companyId('COMPANY_ID'),
  /// Default value for all unparsed values, allows backward compatibility when adding new values on the backend.
  $unknown(null);

  const HostVerificationResponseIdType(this.json);

  factory HostVerificationResponseIdType.fromJson(String json) => values.firstWhere(
        (e) => e.json == json,
        orElse: () => $unknown,
      );

  final String? json;

  @override
  String toString() => json?.toString() ?? super.toString();
  /// Returns all defined enum values excluding the $unknown value.
  static List<HostVerificationResponseIdType> get $valuesDefined => values.where((value) => value != $unknown).toList();
}
