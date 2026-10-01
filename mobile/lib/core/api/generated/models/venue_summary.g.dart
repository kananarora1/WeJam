// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'venue_summary.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VenueSummary _$VenueSummaryFromJson(Map<String, dynamic> json) => VenueSummary(
  city: json['city'] as String,
  id: json['id'] as String,
  name: json['name'] as String,
  spaceCount: (json['spaceCount'] as num).toInt(),
  verificationStatus: VenueSummaryVerificationStatus.fromJson(
    json['verificationStatus'] as String,
  ),
);

Map<String, dynamic> _$VenueSummaryToJson(VenueSummary instance) =>
    <String, dynamic>{
      'city': instance.city,
      'id': instance.id,
      'name': instance.name,
      'spaceCount': instance.spaceCount,
      'verificationStatus':
          _$VenueSummaryVerificationStatusEnumMap[instance.verificationStatus]!,
    };

const _$VenueSummaryVerificationStatusEnumMap = {
  VenueSummaryVerificationStatus.pending: 'PENDING',
  VenueSummaryVerificationStatus.verified: 'VERIFIED',
  VenueSummaryVerificationStatus.rejected: 'REJECTED',
  VenueSummaryVerificationStatus.$unknown: r'$unknown',
};
