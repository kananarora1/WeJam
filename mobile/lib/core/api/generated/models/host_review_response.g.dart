// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'host_review_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HostReviewResponse _$HostReviewResponseFromJson(Map<String, dynamic> json) =>
    HostReviewResponse(
      documents: (json['documents'] as List<dynamic>)
          .map((e) => DocumentResponse.fromJson(e as Map<String, dynamic>))
          .toList(),
      idType: json['idType'] == null
          ? null
          : HostReviewResponseIdType.fromJson(json['idType'] as String),
      ownerName: json['ownerName'] as String?,
      ownerPhone: json['ownerPhone'] as String?,
      profile: HostProfileResponse.fromJson(
        json['profile'] as Map<String, dynamic>,
      ),
      rejectionReason: json['rejectionReason'] as String?,
      requestedAt: json['requestedAt'] == null
          ? null
          : DateTime.parse(json['requestedAt'] as String),
      status: HostReviewResponseStatus.fromJson(json['status'] as String),
    );

Map<String, dynamic> _$HostReviewResponseToJson(HostReviewResponse instance) =>
    <String, dynamic>{
      'documents': instance.documents,
      'idType': _$HostReviewResponseIdTypeEnumMap[instance.idType],
      'ownerName': instance.ownerName,
      'ownerPhone': instance.ownerPhone,
      'profile': instance.profile,
      'rejectionReason': instance.rejectionReason,
      'requestedAt': instance.requestedAt?.toIso8601String(),
      'status': _$HostReviewResponseStatusEnumMap[instance.status]!,
    };

const _$HostReviewResponseIdTypeEnumMap = {
  HostReviewResponseIdType.collegeId: 'COLLEGE_ID',
  HostReviewResponseIdType.companyId: 'COMPANY_ID',
  HostReviewResponseIdType.$unknown: r'$unknown',
};

const _$HostReviewResponseStatusEnumMap = {
  HostReviewResponseStatus.notRequested: 'NOT_REQUESTED',
  HostReviewResponseStatus.pending: 'PENDING',
  HostReviewResponseStatus.verified: 'VERIFIED',
  HostReviewResponseStatus.rejected: 'REJECTED',
  HostReviewResponseStatus.$unknown: r'$unknown',
};
