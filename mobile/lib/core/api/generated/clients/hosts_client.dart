// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:dio/dio.dart';
import 'package:retrofit/retrofit.dart';

import '../models/host_profile_request.dart';
import '../models/host_profile_response.dart';

part 'hosts_client.g.dart';

@RestApi()
abstract class HostsClient {
  factory HostsClient(Dio dio, {String? baseUrl}) = _HostsClient;

  @GET('/api/v1/host-profiles/{profileId}')
  Future<HostProfileResponse> getHostProfile({
    @Path('profileId') required String profileId,
  });

  @GET('/api/v1/me/host-profile')
  Future<HostProfileResponse> getMyHostProfile();

  @PUT('/api/v1/me/host-profile')
  Future<HostProfileResponse> saveMyHostProfile({
    @Body() required HostProfileRequest body,
  });
}
