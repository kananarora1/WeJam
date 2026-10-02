// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

@JsonEnum()
enum HostProfileRequestGenres {
  @JsonValue('BLUES')
  blues('BLUES'),
  @JsonValue('JAZZ')
  jazz('JAZZ'),
  @JsonValue('ROCK')
  rock('ROCK'),
  @JsonValue('INDIE')
  indie('INDIE'),
  @JsonValue('POP')
  pop('POP'),
  @JsonValue('FOLK')
  folk('FOLK'),
  @JsonValue('SOUL')
  soul('SOUL'),
  @JsonValue('FUNK')
  funk('FUNK'),
  @JsonValue('SUFI')
  sufi('SUFI'),
  @JsonValue('BOLLYWOOD')
  bollywood('BOLLYWOOD'),
  @JsonValue('INDIAN_CLASSICAL')
  indianClassical('INDIAN_CLASSICAL'),
  @JsonValue('WESTERN_CLASSICAL')
  westernClassical('WESTERN_CLASSICAL'),
  @JsonValue('FUSION')
  fusion('FUSION'),
  @JsonValue('HIP_HOP')
  hipHop('HIP_HOP'),
  @JsonValue('ELECTRONIC')
  electronic('ELECTRONIC'),
  @JsonValue('METAL')
  metal('METAL'),
  @JsonValue('ACOUSTIC_COVERS')
  acousticCovers('ACOUSTIC_COVERS'),
  /// Default value for all unparsed values, allows backward compatibility when adding new values on the backend.
  $unknown(null);

  const HostProfileRequestGenres(this.json);

  factory HostProfileRequestGenres.fromJson(String json) => values.firstWhere(
        (e) => e.json == json,
        orElse: () => $unknown,
      );

  final String? json;

  @override
  String toString() => json?.toString() ?? super.toString();
  /// Returns all defined enum values excluding the $unknown value.
  static List<HostProfileRequestGenres> get $valuesDefined => values.where((value) => value != $unknown).toList();
}
