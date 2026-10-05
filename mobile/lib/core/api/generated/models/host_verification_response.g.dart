// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'host_verification_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HostVerificationResponse _$HostVerificationResponseFromJson(
  Map<String, dynamic> json,
) => HostVerificationResponse(
  documents: (json['documents'] as List<dynamic>)
      .map((e) => DocumentResponse.fromJson(e as Map<String, dynamic>))
      .toList(),
  idType: json['idType'] == null
      ? null
      : HostVerificationResponseIdType.fromJson(json['idType'] as String),
  rejectionReason: json['rejectionReason'] as String?,
  requestedAt: json['requestedAt'] == null
      ? null
      : DateTime.parse(json['requestedAt'] as String),
  status: HostVerificationResponseStatus.fromJson(json['status'] as String),
);

Map<String, dynamic> _$HostVerificationResponseToJson(
  HostVerificationResponse instance,
) => <String, dynamic>{
  'documents': instance.documents,
  'idType': _$HostVerificationResponseIdTypeEnumMap[instance.idType],
  'rejectionReason': instance.rejectionReason,
  'requestedAt': instance.requestedAt?.toIso8601String(),
  'status': _$HostVerificationResponseStatusEnumMap[instance.status]!,
};

const _$HostVerificationResponseIdTypeEnumMap = {
  HostVerificationResponseIdType.collegeId: 'COLLEGE_ID',
  HostVerificationResponseIdType.companyId: 'COMPANY_ID',
  HostVerificationResponseIdType.$unknown: r'$unknown',
};

const _$HostVerificationResponseStatusEnumMap = {
  HostVerificationResponseStatus.notRequested: 'NOT_REQUESTED',
  HostVerificationResponseStatus.pending: 'PENDING',
  HostVerificationResponseStatus.verified: 'VERIFIED',
  HostVerificationResponseStatus.rejected: 'REJECTED',
  HostVerificationResponseStatus.$unknown: r'$unknown',
};
