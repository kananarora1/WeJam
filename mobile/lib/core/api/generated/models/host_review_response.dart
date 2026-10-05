// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'document_response.dart';
import 'host_profile_response.dart';
import 'host_review_response_id_type.dart';
import 'host_review_response_status.dart';

part 'host_review_response.g.dart';

@JsonSerializable()
class HostReviewResponse {
  const HostReviewResponse({
    required this.documents,
    required this.idType,
    required this.ownerName,
    required this.ownerPhone,
    required this.profile,
    required this.rejectionReason,
    required this.requestedAt,
    required this.status,
  });
  
  factory HostReviewResponse.fromJson(Map<String, Object?> json) => _$HostReviewResponseFromJson(json);
  
  /// Uploaded ID sides with 5-minute download links
  final List<DocumentResponse> documents;
  final HostReviewResponseIdType? idType;
  final String? ownerName;
  final String? ownerPhone;
  final HostProfileResponse profile;
  final String? rejectionReason;
  final DateTime? requestedAt;
  final HostReviewResponseStatus status;

  Map<String, Object?> toJson() => _$HostReviewResponseToJson(this);
}
