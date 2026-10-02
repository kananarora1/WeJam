// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'space_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SpaceRequest _$SpaceRequestFromJson(Map<String, dynamic> json) => SpaceRequest(
  capacity: (json['capacity'] as num).toInt(),
  name: json['name'] as String,
  soundPolicy: SpaceRequestSoundPolicy.fromJson(json['soundPolicy'] as String),
  houseRules: json['houseRules'] as String?,
  soundCurfew: json['soundCurfew'] as String?,
);

Map<String, dynamic> _$SpaceRequestToJson(SpaceRequest instance) =>
    <String, dynamic>{
      'capacity': instance.capacity,
      'houseRules': instance.houseRules,
      'name': instance.name,
      'soundCurfew': instance.soundCurfew,
      'soundPolicy': _$SpaceRequestSoundPolicyEnumMap[instance.soundPolicy]!,
    };

const _$SpaceRequestSoundPolicyEnumMap = {
  SpaceRequestSoundPolicy.acousticOnly: 'ACOUSTIC_ONLY',
  SpaceRequestSoundPolicy.amplifiedAllowed: 'AMPLIFIED_ALLOWED',
  SpaceRequestSoundPolicy.$unknown: r'$unknown',
};
