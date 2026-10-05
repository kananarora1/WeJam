// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'admin_action_response.dart';

part 'admin_action_page.g.dart';

@JsonSerializable()
class AdminActionPage {
  const AdminActionPage({
    required this.items,
    required this.nextBefore,
    required this.nextBeforeId,
  });
  
  factory AdminActionPage.fromJson(Map<String, Object?> json) => _$AdminActionPageFromJson(json);
  
  final List<AdminActionResponse> items;
  final DateTime? nextBefore;
  final String? nextBeforeId;

  Map<String, Object?> toJson() => _$AdminActionPageToJson(this);
}
