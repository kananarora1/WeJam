// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'host_invite_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HostInviteResponse _$HostInviteResponseFromJson(Map<String, dynamic> json) =>
    HostInviteResponse(
      groupKind: HostInviteResponseGroupKind.fromJson(
        json['groupKind'] as String,
      ),
      groupName: json['groupName'] as String,
      hostProfileId: json['hostProfileId'] as String,
      invitedAt: DateTime.parse(json['invitedAt'] as String),
      invitedByName: json['invitedByName'] as String?,
    );

Map<String, dynamic> _$HostInviteResponseToJson(HostInviteResponse instance) =>
    <String, dynamic>{
      'groupKind': _$HostInviteResponseGroupKindEnumMap[instance.groupKind]!,
      'groupName': instance.groupName,
      'hostProfileId': instance.hostProfileId,
      'invitedAt': instance.invitedAt.toIso8601String(),
      'invitedByName': instance.invitedByName,
    };

const _$HostInviteResponseGroupKindEnumMap = {
  HostInviteResponseGroupKind.band: 'BAND',
  HostInviteResponseGroupKind.friends: 'FRIENDS',
  HostInviteResponseGroupKind.community: 'COMMUNITY',
  HostInviteResponseGroupKind.$unknown: r'$unknown',
};
