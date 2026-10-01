// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'space_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SpaceResponse _$SpaceResponseFromJson(Map<String, dynamic> json) =>
    SpaceResponse(
      capacity: (json['capacity'] as num).toInt(),
      gear: (json['gear'] as List<dynamic>)
          .map((e) => GearItemDto.fromJson(e as Map<String, dynamic>))
          .toList(),
      id: json['id'] as String,
      name: json['name'] as String,
    );

Map<String, dynamic> _$SpaceResponseToJson(SpaceResponse instance) =>
    <String, dynamic>{
      'capacity': instance.capacity,
      'gear': instance.gear,
      'id': instance.id,
      'name': instance.name,
    };
