// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pending_invite_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PendingInviteResponse _$PendingInviteResponseFromJson(
  Map<String, dynamic> json,
) => PendingInviteResponse(
  invitedAt: DateTime.parse(json['invitedAt'] as String),
  maskedPhone: json['maskedPhone'] as String,
  userId: json['userId'] as String,
);

Map<String, dynamic> _$PendingInviteResponseToJson(
  PendingInviteResponse instance,
) => <String, dynamic>{
  'invitedAt': instance.invitedAt.toIso8601String(),
  'maskedPhone': instance.maskedPhone,
  'userId': instance.userId,
};
