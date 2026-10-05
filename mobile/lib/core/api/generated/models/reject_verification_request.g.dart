// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reject_verification_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RejectVerificationRequest _$RejectVerificationRequestFromJson(
  Map<String, dynamic> json,
) => RejectVerificationRequest(
  reason: json['reason'] as String,
  issues: (json['issues'] as List<dynamic>?)
      ?.map(
        (e) => e == null
            ? null
            : RejectVerificationRequestIssues.fromJson(e as String),
      )
      .toList(),
);

Map<String, dynamic> _$RejectVerificationRequestToJson(
  RejectVerificationRequest instance,
) => <String, dynamic>{
  'issues': instance.issues
      ?.map((e) => _$RejectVerificationRequestIssuesEnumMap[e])
      .toList(),
  'reason': instance.reason,
};

const _$RejectVerificationRequestIssuesEnumMap = {
  RejectVerificationRequestIssues.fssaiNumber: 'FSSAI_NUMBER',
  RejectVerificationRequestIssues.fssaiCertificate: 'FSSAI_CERTIFICATE',
  RejectVerificationRequestIssues.leaseAgreement: 'LEASE_AGREEMENT',
  RejectVerificationRequestIssues.venueDetails: 'VENUE_DETAILS',
  RejectVerificationRequestIssues.$unknown: r'$unknown',
};
