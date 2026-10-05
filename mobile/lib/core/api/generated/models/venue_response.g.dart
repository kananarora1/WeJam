// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'venue_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VenueResponse _$VenueResponseFromJson(Map<String, dynamic> json) =>
    VenueResponse(
      addressLine: json['addressLine'] as String,
      city: json['city'] as String,
      description: json['description'] as String?,
      fssaiNumber: json['fssaiNumber'] as String,
      hostingMode: VenueResponseHostingMode.fromJson(
        json['hostingMode'] as String,
      ),
      id: json['id'] as String,
      latitude: (json['latitude'] as num).toDouble(),
      longitude: (json['longitude'] as num).toDouble(),
      name: json['name'] as String,
      rejectionReason: json['rejectionReason'] as String?,
      spaces: (json['spaces'] as List<dynamic>)
          .map((e) => SpaceResponse.fromJson(e as Map<String, dynamic>))
          .toList(),
      verificationIssues: (json['verificationIssues'] as List<dynamic>)
          .map((e) => VenueResponseVerificationIssues.fromJson(e as String))
          .toList(),
      verificationStatus: VenueResponseVerificationStatus.fromJson(
        json['verificationStatus'] as String,
      ),
    );

Map<String, dynamic> _$VenueResponseToJson(
  VenueResponse instance,
) => <String, dynamic>{
  'addressLine': instance.addressLine,
  'city': instance.city,
  'description': instance.description,
  'fssaiNumber': instance.fssaiNumber,
  'hostingMode': _$VenueResponseHostingModeEnumMap[instance.hostingMode]!,
  'id': instance.id,
  'latitude': instance.latitude,
  'longitude': instance.longitude,
  'name': instance.name,
  'rejectionReason': instance.rejectionReason,
  'spaces': instance.spaces,
  'verificationIssues': instance.verificationIssues
      .map((e) => _$VenueResponseVerificationIssuesEnumMap[e]!)
      .toList(),
  'verificationStatus':
      _$VenueResponseVerificationStatusEnumMap[instance.verificationStatus]!,
};

const _$VenueResponseHostingModeEnumMap = {
  VenueResponseHostingMode.open: 'OPEN',
  VenueResponseHostingMode.selfOnly: 'SELF_ONLY',
  VenueResponseHostingMode.$unknown: r'$unknown',
};

const _$VenueResponseVerificationIssuesEnumMap = {
  VenueResponseVerificationIssues.fssaiNumber: 'FSSAI_NUMBER',
  VenueResponseVerificationIssues.fssaiCertificate: 'FSSAI_CERTIFICATE',
  VenueResponseVerificationIssues.leaseAgreement: 'LEASE_AGREEMENT',
  VenueResponseVerificationIssues.venueDetails: 'VENUE_DETAILS',
  VenueResponseVerificationIssues.$unknown: r'$unknown',
};

const _$VenueResponseVerificationStatusEnumMap = {
  VenueResponseVerificationStatus.pending: 'PENDING',
  VenueResponseVerificationStatus.verified: 'VERIFIED',
  VenueResponseVerificationStatus.rejected: 'REJECTED',
  VenueResponseVerificationStatus.$unknown: r'$unknown',
};
