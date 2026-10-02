// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'pending_invite_response.g.dart';

@JsonSerializable()
class PendingInviteResponse {
  const PendingInviteResponse({
    required this.invitedAt,
    required this.maskedPhone,
    required this.userId,
  });
  
  factory PendingInviteResponse.fromJson(Map<String, Object?> json) => _$PendingInviteResponseFromJson(json);
  
  final DateTime invitedAt;
  final String maskedPhone;
  final String userId;

  Map<String, Object?> toJson() => _$PendingInviteResponseToJson(this);
}
