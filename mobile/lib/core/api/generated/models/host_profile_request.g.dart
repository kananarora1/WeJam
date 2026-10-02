// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'host_profile_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HostProfileRequest _$HostProfileRequestFromJson(Map<String, dynamic> json) =>
    HostProfileRequest(
      genres: (json['genres'] as List<dynamic>)
          .map((e) => HostProfileRequestGenres.fromJson(e as String))
          .toList(),
      mediaLinks: (json['mediaLinks'] as List<dynamic>)
          .map((e) => MediaLinkDto.fromJson(e as Map<String, dynamic>))
          .toList(),
      type: HostProfileRequestType.fromJson(json['type'] as String),
      area: json['area'] as String?,
      bio: json['bio'] as String?,
      groupKind: json['groupKind'] == null
          ? null
          : HostProfileRequestGroupKind.fromJson(json['groupKind'] as String),
      groupName: json['groupName'] as String?,
      instagramHandle: json['instagramHandle'] as String?,
    );

Map<String, dynamic> _$HostProfileRequestToJson(HostProfileRequest instance) =>
    <String, dynamic>{
      'area': instance.area,
      'bio': instance.bio,
      'genres': instance.genres
          .map((e) => _$HostProfileRequestGenresEnumMap[e]!)
          .toList(),
      'groupKind': _$HostProfileRequestGroupKindEnumMap[instance.groupKind],
      'groupName': instance.groupName,
      'instagramHandle': instance.instagramHandle,
      'mediaLinks': instance.mediaLinks,
      'type': _$HostProfileRequestTypeEnumMap[instance.type]!,
    };

const _$HostProfileRequestGenresEnumMap = {
  HostProfileRequestGenres.blues: 'BLUES',
  HostProfileRequestGenres.jazz: 'JAZZ',
  HostProfileRequestGenres.rock: 'ROCK',
  HostProfileRequestGenres.indie: 'INDIE',
  HostProfileRequestGenres.pop: 'POP',
  HostProfileRequestGenres.folk: 'FOLK',
  HostProfileRequestGenres.soul: 'SOUL',
  HostProfileRequestGenres.funk: 'FUNK',
  HostProfileRequestGenres.sufi: 'SUFI',
  HostProfileRequestGenres.bollywood: 'BOLLYWOOD',
  HostProfileRequestGenres.indianClassical: 'INDIAN_CLASSICAL',
  HostProfileRequestGenres.westernClassical: 'WESTERN_CLASSICAL',
  HostProfileRequestGenres.fusion: 'FUSION',
  HostProfileRequestGenres.hipHop: 'HIP_HOP',
  HostProfileRequestGenres.electronic: 'ELECTRONIC',
  HostProfileRequestGenres.metal: 'METAL',
  HostProfileRequestGenres.acousticCovers: 'ACOUSTIC_COVERS',
  HostProfileRequestGenres.$unknown: r'$unknown',
};

const _$HostProfileRequestGroupKindEnumMap = {
  HostProfileRequestGroupKind.band: 'BAND',
  HostProfileRequestGroupKind.friends: 'FRIENDS',
  HostProfileRequestGroupKind.community: 'COMMUNITY',
  HostProfileRequestGroupKind.$unknown: r'$unknown',
};

const _$HostProfileRequestTypeEnumMap = {
  HostProfileRequestType.individual: 'INDIVIDUAL',
  HostProfileRequestType.group: 'GROUP',
  HostProfileRequestType.$unknown: r'$unknown',
};
