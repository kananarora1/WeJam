// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'host_profile_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HostProfileResponse _$HostProfileResponseFromJson(Map<String, dynamic> json) =>
    HostProfileResponse(
      area: json['area'] as String?,
      bio: json['bio'] as String?,
      displayName: json['displayName'] as String?,
      genres: (json['genres'] as List<dynamic>)
          .map((e) => HostProfileResponseGenres.fromJson(e as String))
          .toList(),
      groupKind: json['groupKind'] == null
          ? null
          : HostProfileResponseGroupKind.fromJson(json['groupKind'] as String),
      id: json['id'] as String,
      instagramHandle: json['instagramHandle'] as String?,
      mediaLinks: (json['mediaLinks'] as List<dynamic>)
          .map((e) => MediaLinkDto.fromJson(e as Map<String, dynamic>))
          .toList(),
      type: HostProfileResponseType.fromJson(json['type'] as String),
    );

Map<String, dynamic> _$HostProfileResponseToJson(
  HostProfileResponse instance,
) => <String, dynamic>{
  'area': instance.area,
  'bio': instance.bio,
  'displayName': instance.displayName,
  'genres': instance.genres
      .map((e) => _$HostProfileResponseGenresEnumMap[e]!)
      .toList(),
  'groupKind': _$HostProfileResponseGroupKindEnumMap[instance.groupKind],
  'id': instance.id,
  'instagramHandle': instance.instagramHandle,
  'mediaLinks': instance.mediaLinks,
  'type': _$HostProfileResponseTypeEnumMap[instance.type]!,
};

const _$HostProfileResponseGenresEnumMap = {
  HostProfileResponseGenres.blues: 'BLUES',
  HostProfileResponseGenres.jazz: 'JAZZ',
  HostProfileResponseGenres.rock: 'ROCK',
  HostProfileResponseGenres.indie: 'INDIE',
  HostProfileResponseGenres.pop: 'POP',
  HostProfileResponseGenres.folk: 'FOLK',
  HostProfileResponseGenres.soul: 'SOUL',
  HostProfileResponseGenres.funk: 'FUNK',
  HostProfileResponseGenres.sufi: 'SUFI',
  HostProfileResponseGenres.bollywood: 'BOLLYWOOD',
  HostProfileResponseGenres.indianClassical: 'INDIAN_CLASSICAL',
  HostProfileResponseGenres.westernClassical: 'WESTERN_CLASSICAL',
  HostProfileResponseGenres.fusion: 'FUSION',
  HostProfileResponseGenres.hipHop: 'HIP_HOP',
  HostProfileResponseGenres.electronic: 'ELECTRONIC',
  HostProfileResponseGenres.metal: 'METAL',
  HostProfileResponseGenres.acousticCovers: 'ACOUSTIC_COVERS',
  HostProfileResponseGenres.$unknown: r'$unknown',
};

const _$HostProfileResponseGroupKindEnumMap = {
  HostProfileResponseGroupKind.band: 'BAND',
  HostProfileResponseGroupKind.friends: 'FRIENDS',
  HostProfileResponseGroupKind.community: 'COMMUNITY',
  HostProfileResponseGroupKind.$unknown: r'$unknown',
};

const _$HostProfileResponseTypeEnumMap = {
  HostProfileResponseType.individual: 'INDIVIDUAL',
  HostProfileResponseType.group: 'GROUP',
  HostProfileResponseType.$unknown: r'$unknown',
};
