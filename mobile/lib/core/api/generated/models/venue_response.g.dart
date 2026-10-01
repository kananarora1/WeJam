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
      id: json['id'] as String,
      latitude: (json['latitude'] as num).toDouble(),
      longitude: (json['longitude'] as num).toDouble(),
      name: json['name'] as String,
      spaces: (json['spaces'] as List<dynamic>)
          .map((e) => SpaceResponse.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$VenueResponseToJson(VenueResponse instance) =>
    <String, dynamic>{
      'addressLine': instance.addressLine,
      'city': instance.city,
      'description': instance.description,
      'id': instance.id,
      'latitude': instance.latitude,
      'longitude': instance.longitude,
      'name': instance.name,
      'spaces': instance.spaces,
    };
