// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'admin_action_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AdminActionResponse _$AdminActionResponseFromJson(Map<String, dynamic> json) =>
    AdminActionResponse(
      action: AdminActionResponseAction.fromJson(json['action'] as String),
      adminId: json['adminId'] as String,
      adminName: json['adminName'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
      id: json['id'] as String,
      reason: json['reason'] as String?,
      targetId: json['targetId'] as String,
      targetType: AdminActionResponseTargetType.fromJson(
        json['targetType'] as String,
      ),
    );

Map<String, dynamic> _$AdminActionResponseToJson(
  AdminActionResponse instance,
) => <String, dynamic>{
  'action': _$AdminActionResponseActionEnumMap[instance.action]!,
  'adminId': instance.adminId,
  'adminName': instance.adminName,
  'createdAt': instance.createdAt.toIso8601String(),
  'id': instance.id,
  'reason': instance.reason,
  'targetId': instance.targetId,
  'targetType': _$AdminActionResponseTargetTypeEnumMap[instance.targetType]!,
};

const _$AdminActionResponseActionEnumMap = {
  AdminActionResponseAction.venueApproved: 'VENUE_APPROVED',
  AdminActionResponseAction.venueRejected: 'VENUE_REJECTED',
  AdminActionResponseAction.hostApproved: 'HOST_APPROVED',
  AdminActionResponseAction.hostRejected: 'HOST_REJECTED',
  AdminActionResponseAction.$unknown: r'$unknown',
};

const _$AdminActionResponseTargetTypeEnumMap = {
  AdminActionResponseTargetType.venue: 'VENUE',
  AdminActionResponseTargetType.hostProfile: 'HOST_PROFILE',
  AdminActionResponseTargetType.$unknown: r'$unknown',
};
