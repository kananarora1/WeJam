// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'space_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SpaceRequest _$SpaceRequestFromJson(Map<String, dynamic> json) => SpaceRequest(
  capacity: (json['capacity'] as num).toInt(),
  gear: (json['gear'] as List<dynamic>)
      .map((e) => GearItemDto.fromJson(e as Map<String, dynamic>))
      .toList(),
  name: json['name'] as String,
);

Map<String, dynamic> _$SpaceRequestToJson(SpaceRequest instance) =>
    <String, dynamic>{
      'capacity': instance.capacity,
      'gear': instance.gear,
      'name': instance.name,
    };
