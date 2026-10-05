// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'document_response.dart';
import 'host_verification_response_id_type.dart';
import 'host_verification_response_status.dart';

part 'host_verification_response.g.dart';

@JsonSerializable()
class HostVerificationResponse {
  const HostVerificationResponse({
    required this.documents,
    required this.idType,
    required this.rejectionReason,
    required this.requestedAt,
    required this.status,
  });
  
  factory HostVerificationResponse.fromJson(Map<String, Object?> json) => _$HostVerificationResponseFromJson(json);
  
  /// Uploaded ID sides with 5-minute download links and their deletion date
  final List<DocumentResponse> documents;
  final HostVerificationResponseIdType? idType;
  final String? rejectionReason;
  final DateTime? requestedAt;

  /// Optional perk: NOT_REQUESTED is a normal state, not a problem
  final HostVerificationResponseStatus status;

  Map<String, Object?> toJson() => _$HostVerificationResponseToJson(this);
}
