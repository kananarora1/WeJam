// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'host_verification_item.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HostVerificationItem _$HostVerificationItemFromJson(
  Map<String, dynamic> json,
) => HostVerificationItem(
  displayName: json['displayName'] as String?,
  hostProfileId: json['hostProfileId'] as String,
  idType: HostVerificationItemIdType.fromJson(json['idType'] as String),
  ownerName: json['ownerName'] as String?,
  requestedAt: DateTime.parse(json['requestedAt'] as String),
  status: HostVerificationItemStatus.fromJson(json['status'] as String),
  type: HostVerificationItemType.fromJson(json['type'] as String),
);

Map<String, dynamic> _$HostVerificationItemToJson(
  HostVerificationItem instance,
) => <String, dynamic>{
  'displayName': instance.displayName,
  'hostProfileId': instance.hostProfileId,
  'idType': _$HostVerificationItemIdTypeEnumMap[instance.idType]!,
  'ownerName': instance.ownerName,
  'requestedAt': instance.requestedAt.toIso8601String(),
  'status': _$HostVerificationItemStatusEnumMap[instance.status]!,
  'type': _$HostVerificationItemTypeEnumMap[instance.type]!,
};

const _$HostVerificationItemIdTypeEnumMap = {
  HostVerificationItemIdType.collegeId: 'COLLEGE_ID',
  HostVerificationItemIdType.companyId: 'COMPANY_ID',
  HostVerificationItemIdType.$unknown: r'$unknown',
};

const _$HostVerificationItemStatusEnumMap = {
  HostVerificationItemStatus.notRequested: 'NOT_REQUESTED',
  HostVerificationItemStatus.pending: 'PENDING',
  HostVerificationItemStatus.verified: 'VERIFIED',
  HostVerificationItemStatus.rejected: 'REJECTED',
  HostVerificationItemStatus.$unknown: r'$unknown',
};

const _$HostVerificationItemTypeEnumMap = {
  HostVerificationItemType.individual: 'INDIVIDUAL',
  HostVerificationItemType.group: 'GROUP',
  HostVerificationItemType.$unknown: r'$unknown',
};
