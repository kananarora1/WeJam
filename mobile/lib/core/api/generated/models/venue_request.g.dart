// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'venue_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VenueRequest _$VenueRequestFromJson(Map<String, dynamic> json) => VenueRequest(
  addressLine: json['addressLine'] as String,
  city: json['city'] as String,
  fssaiNumber: json['fssaiNumber'] as String,
  hostingMode: VenueRequestHostingMode.fromJson(json['hostingMode'] as String),
  latitude: (json['latitude'] as num).toDouble(),
  longitude: (json['longitude'] as num).toDouble(),
  name: json['name'] as String,
  soundPolicy: VenueRequestSoundPolicy.fromJson(json['soundPolicy'] as String),
  description: json['description'] as String?,
  houseRules: json['houseRules'] as String?,
  soundCurfew: json['soundCurfew'] as String?,
);

Map<String, dynamic> _$VenueRequestToJson(VenueRequest instance) =>
    <String, dynamic>{
      'addressLine': instance.addressLine,
      'city': instance.city,
      'description': instance.description,
      'fssaiNumber': instance.fssaiNumber,
      'hostingMode': _$VenueRequestHostingModeEnumMap[instance.hostingMode]!,
      'houseRules': instance.houseRules,
      'latitude': instance.latitude,
      'longitude': instance.longitude,
      'name': instance.name,
      'soundCurfew': instance.soundCurfew,
      'soundPolicy': _$VenueRequestSoundPolicyEnumMap[instance.soundPolicy]!,
    };

const _$VenueRequestHostingModeEnumMap = {
  VenueRequestHostingMode.open: 'OPEN',
  VenueRequestHostingMode.selfOnly: 'SELF_ONLY',
  VenueRequestHostingMode.$unknown: r'$unknown',
};

const _$VenueRequestSoundPolicyEnumMap = {
  VenueRequestSoundPolicy.acousticOnly: 'ACOUSTIC_ONLY',
  VenueRequestSoundPolicy.amplifiedAllowed: 'AMPLIFIED_ALLOWED',
  VenueRequestSoundPolicy.$unknown: r'$unknown',
};
