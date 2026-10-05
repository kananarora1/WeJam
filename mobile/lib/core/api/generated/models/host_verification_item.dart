// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'host_verification_item_id_type.dart';
import 'host_verification_item_status.dart';
import 'host_verification_item_type.dart';

part 'host_verification_item.g.dart';

@JsonSerializable()
class HostVerificationItem {
  const HostVerificationItem({
    required this.displayName,
    required this.hostProfileId,
    required this.idType,
    required this.ownerName,
    required this.requestedAt,
    required this.status,
    required this.type,
  });
  
  factory HostVerificationItem.fromJson(Map<String, Object?> json) => _$HostVerificationItemFromJson(json);
  
  /// Group name, or the owner's display name for an individual
  final String? displayName;
  final String hostProfileId;
  final HostVerificationItemIdType idType;
  final String? ownerName;
  final DateTime requestedAt;
  final HostVerificationItemStatus status;
  final HostVerificationItemType type;

  Map<String, Object?> toJson() => _$HostVerificationItemToJson(this);
}
