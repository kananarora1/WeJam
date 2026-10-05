// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'venue_verification_item.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VenueVerificationItem _$VenueVerificationItemFromJson(
  Map<String, dynamic> json,
) => VenueVerificationItem(
  checks: VerificationChecks.fromJson(json['checks'] as Map<String, dynamic>),
  city: json['city'] as String,
  fssaiNumber: json['fssaiNumber'] as String,
  name: json['name'] as String,
  ownerName: json['ownerName'] as String?,
  requestedAt: DateTime.parse(json['requestedAt'] as String),
  status: VenueVerificationItemStatus.fromJson(json['status'] as String),
  venueId: json['venueId'] as String,
);

Map<String, dynamic> _$VenueVerificationItemToJson(
  VenueVerificationItem instance,
) => <String, dynamic>{
  'checks': instance.checks,
  'city': instance.city,
  'fssaiNumber': instance.fssaiNumber,
  'name': instance.name,
  'ownerName': instance.ownerName,
  'requestedAt': instance.requestedAt.toIso8601String(),
  'status': _$VenueVerificationItemStatusEnumMap[instance.status]!,
  'venueId': instance.venueId,
};

const _$VenueVerificationItemStatusEnumMap = {
  VenueVerificationItemStatus.pending: 'PENDING',
  VenueVerificationItemStatus.verified: 'VERIFIED',
  VenueVerificationItemStatus.rejected: 'REJECTED',
  VenueVerificationItemStatus.$unknown: r'$unknown',
};
