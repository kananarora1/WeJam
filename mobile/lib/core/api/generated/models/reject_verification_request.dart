// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'reject_verification_request_issues.dart';

part 'reject_verification_request.g.dart';

@JsonSerializable()
class RejectVerificationRequest {
  const RejectVerificationRequest({
    required this.reason,
    this.issues,
  });
  
  factory RejectVerificationRequest.fromJson(Map<String, Object?> json) => _$RejectVerificationRequestFromJson(json);
  
  /// Items the venue must fix; shown flagged on their side
  final List<RejectVerificationRequestIssues?>? issues;

  /// Sent verbatim to the venue
  final String reason;

  Map<String, Object?> toJson() => _$RejectVerificationRequestToJson(this);
}
