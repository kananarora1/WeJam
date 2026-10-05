// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:dio/dio.dart';
import 'package:retrofit/retrofit.dart';

import '../models/document_response.dart';
import '../models/document_upload_request.dart';
import '../models/document_upload_response.dart';
import '../models/host_invite_response.dart';
import '../models/host_profile_request.dart';
import '../models/host_profile_response.dart';
import '../models/host_verification_request.dart';
import '../models/host_verification_response.dart';
import '../models/invite_member_request.dart';
import '../models/pending_invite_response.dart';

part 'hosts_client.g.dart';

@RestApi()
abstract class HostsClient {
  factory HostsClient(Dio dio, {String? baseUrl}) = _HostsClient;

  @GET('/api/v1/host-profiles/{profileId}')
  Future<HostProfileResponse> getHostProfile({
    @Path('profileId') required String profileId,
  });

  @GET('/api/v1/me/host-invites')
  Future<List<HostInviteResponse>> myHostInvites();

  @POST('/api/v1/me/host-invites/{hostProfileId}/accept')
  Future<void> acceptHostInvite({
    @Path('hostProfileId') required String hostProfileId,
  });

  @POST('/api/v1/me/host-invites/{hostProfileId}/decline')
  Future<void> declineHostInvite({
    @Path('hostProfileId') required String hostProfileId,
  });

  @DELETE('/api/v1/me/host-memberships/{hostProfileId}')
  Future<void> leaveHostGroup({
    @Path('hostProfileId') required String hostProfileId,
  });

  @GET('/api/v1/me/host-profile')
  Future<HostProfileResponse> getMyHostProfile();

  @PUT('/api/v1/me/host-profile')
  Future<HostProfileResponse> saveMyHostProfile({
    @Body() required HostProfileRequest body,
  });

  @POST('/api/v1/me/host-profile/documents')
  Future<DocumentUploadResponse> startHostDocumentUpload({
    @Body() required DocumentUploadRequest body,
  });

  @DELETE('/api/v1/me/host-profile/documents/{documentId}')
  Future<void> deleteHostDocument({
    @Path('documentId') required String documentId,
  });

  @POST('/api/v1/me/host-profile/documents/{documentId}/confirm')
  Future<DocumentResponse> confirmHostDocument({
    @Path('documentId') required String documentId,
  });

  @GET('/api/v1/me/host-profile/invites')
  Future<List<PendingInviteResponse>> pendingInvites();

  @POST('/api/v1/me/host-profile/invites')
  Future<PendingInviteResponse> inviteMember({
    @Body() required InviteMemberRequest body,
  });

  @DELETE('/api/v1/me/host-profile/members/{userId}')
  Future<void> removeMember({
    @Path('userId') required String userId,
  });

  @GET('/api/v1/me/host-profile/verification')
  Future<HostVerificationResponse> myHostVerification();

  @POST('/api/v1/me/host-profile/verification')
  Future<HostVerificationResponse> requestHostVerification({
    @Body() required HostVerificationRequest body,
  });
}
