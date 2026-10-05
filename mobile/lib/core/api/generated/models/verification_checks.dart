// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'verification_checks.g.dart';

@JsonSerializable()
class VerificationChecks {
  const VerificationChecks({
    required this.fssaiStructureLooksValid,
    required this.otherVenuesWithSameFssai,
  });
  
  factory VerificationChecks.fromJson(Map<String, Object?> json) => _$VerificationChecksFromJson(json);
  
  /// 14 digits, licence type 1/2, state code 01–38 (offline heuristic, not the registry)
  final bool fssaiStructureLooksValid;

  /// Other venues using the same FSSAI number
  final int otherVenuesWithSameFssai;

  Map<String, Object?> toJson() => _$VerificationChecksToJson(this);
}
