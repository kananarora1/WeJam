// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:dio/dio.dart';
import 'package:retrofit/retrofit.dart';

import '../models/add_role_request.dart';
import '../models/me_response.dart';
import '../models/token_response.dart';
import '../models/update_profile_request.dart';

part 'me_client.g.dart';

@RestApi()
abstract class MeClient {
  factory MeClient(Dio dio, {String? baseUrl}) = _MeClient;

  @GET('/api/v1/me')
  Future<MeResponse> me();

  @PATCH('/api/v1/me')
  Future<MeResponse> updateProfile({
    @Body() required UpdateProfileRequest body,
  });

  @POST('/api/v1/me/roles')
  Future<TokenResponse> addRole({
    @Body() required AddRoleRequest body,
  });
}
