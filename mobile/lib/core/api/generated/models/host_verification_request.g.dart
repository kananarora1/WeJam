// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'host_verification_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HostVerificationRequest _$HostVerificationRequestFromJson(
  Map<String, dynamic> json,
) => HostVerificationRequest(
  idType: HostVerificationRequestIdType.fromJson(json['idType'] as String),
);

Map<String, dynamic> _$HostVerificationRequestToJson(
  HostVerificationRequest instance,
) => <String, dynamic>{
  'idType': _$HostVerificationRequestIdTypeEnumMap[instance.idType]!,
};

const _$HostVerificationRequestIdTypeEnumMap = {
  HostVerificationRequestIdType.collegeId: 'COLLEGE_ID',
  HostVerificationRequestIdType.companyId: 'COMPANY_ID',
  HostVerificationRequestIdType.$unknown: r'$unknown',
};
