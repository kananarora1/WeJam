// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'reject_host_verification_request.g.dart';

@JsonSerializable()
class RejectHostVerificationRequest {
  const RejectHostVerificationRequest({
    required this.reason,
  });
  
  factory RejectHostVerificationRequest.fromJson(Map<String, Object?> json) => _$RejectHostVerificationRequestFromJson(json);
  
  /// Sent verbatim to the host; never shown to anyone else
  final String reason;

  Map<String, Object?> toJson() => _$RejectHostVerificationRequestToJson(this);
}
