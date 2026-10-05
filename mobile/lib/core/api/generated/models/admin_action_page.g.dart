// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'admin_action_page.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AdminActionPage _$AdminActionPageFromJson(Map<String, dynamic> json) =>
    AdminActionPage(
      items: (json['items'] as List<dynamic>)
          .map((e) => AdminActionResponse.fromJson(e as Map<String, dynamic>))
          .toList(),
      nextBefore: json['nextBefore'] == null
          ? null
          : DateTime.parse(json['nextBefore'] as String),
      nextBeforeId: json['nextBeforeId'] as String?,
    );

Map<String, dynamic> _$AdminActionPageToJson(AdminActionPage instance) =>
    <String, dynamic>{
      'items': instance.items,
      'nextBefore': instance.nextBefore?.toIso8601String(),
      'nextBeforeId': instance.nextBeforeId,
    };
