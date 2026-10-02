// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'space_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SpaceResponse _$SpaceResponseFromJson(Map<String, dynamic> json) =>
    SpaceResponse(
      capacity: (json['capacity'] as num).toInt(),
      houseRules: json['houseRules'] as String?,
      id: json['id'] as String,
      name: json['name'] as String,
      soundCurfew: json['soundCurfew'] as String?,
      soundPolicy: SpaceResponseSoundPolicy.fromJson(
        json['soundPolicy'] as String,
      ),
    );

Map<String, dynamic> _$SpaceResponseToJson(SpaceResponse instance) =>
    <String, dynamic>{
      'capacity': instance.capacity,
      'houseRules': instance.houseRules,
      'id': instance.id,
      'name': instance.name,
      'soundCurfew': instance.soundCurfew,
      'soundPolicy': _$SpaceResponseSoundPolicyEnumMap[instance.soundPolicy]!,
    };

const _$SpaceResponseSoundPolicyEnumMap = {
  SpaceResponseSoundPolicy.acousticOnly: 'ACOUSTIC_ONLY',
  SpaceResponseSoundPolicy.amplifiedAllowed: 'AMPLIFIED_ALLOWED',
  SpaceResponseSoundPolicy.$unknown: r'$unknown',
};
