// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'invite_member_request.g.dart';

@JsonSerializable()
class InviteMemberRequest {
  const InviteMemberRequest({
    required this.phoneNumber,
  });
  
  factory InviteMemberRequest.fromJson(Map<String, Object?> json) => _$InviteMemberRequestFromJson(json);
  
  final String phoneNumber;

  Map<String, Object?> toJson() => _$InviteMemberRequestToJson(this);
}
