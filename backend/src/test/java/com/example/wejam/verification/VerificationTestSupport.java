package com.example.wejam.verification;

import com.example.wejam.TestUploads;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.nio.charset.StandardCharsets;

import static com.example.wejam.TestAuth.accessTokenWithRole;
import static com.example.wejam.TestAuth.bearer;
import static com.example.wejam.TestAuth.body;
import static com.example.wejam.TestAuth.exchange;
import static com.example.wejam.TestAuth.randomUid;
import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;

/** Uploads like the app does (real HTTP PUTs to MinIO) and the host verification steps tests share. */
public final class VerificationTestSupport {

    public static final byte[] PDF = "%PDF-1.4 fake scan".getBytes(StandardCharsets.UTF_8);
    public static final byte[] JPEG = TestUploads.JPEG;

    /** Listed in application-test.properties under wejam.admin.phones. */
    private static final String ADMIN_PHONE = "+919999999901";
    private static final String HOST_DOCUMENTS = "/api/v1/me/host-profile/documents";

    private VerificationTestSupport() {
    }

    public static int put(MvcTestResult start, byte[] bytes, String contentType) throws Exception {
        return TestUploads.put(start, bytes, contentType);
    }

    public static String adminToken(MockMvcTester mvc) {
        return read(body(exchange(mvc, "fake:platform-admin:" + ADMIN_PHONE)), "$.accessToken");
    }

    /** A new user with a group host profile; returns their token. */
    public static String newHost(MockMvcTester mvc) {
        String token = accessTokenWithRole(mvc, randomUid(), "HOST");
        assertThat(send(mvc, mvc.put().uri("/api/v1/me/host-profile"), token, """
                {"type":"GROUP","groupKind":"BAND","groupName":"The Low Notes","genres":["BLUES"],"mediaLinks":[]}
                """)).hasStatusOk();
        return token;
    }

    public static String hostProfileId(MockMvcTester mvc, String token) {
        return read(body(get(mvc, "/api/v1/me/host-profile", token)), "$.id");
    }

    public static MvcTestResult startHostUpload(MockMvcTester mvc, String token, String type, String contentType,
                                                long size) {
        return send(mvc, mvc.post().uri(HOST_DOCUMENTS), token,
                "{\"type\":\"%s\",\"contentType\":\"%s\",\"sizeBytes\":%d}".formatted(type, contentType, size));
    }

    /** Start → PUT → confirm; returns the confirm result. */
    public static MvcTestResult uploadHostId(MockMvcTester mvc, String token, String type) throws Exception {
        MvcTestResult start = startHostUpload(mvc, token, type, "image/jpeg", JPEG.length);
        assertThat(start).hasStatus(201);
        assertThat(put(start, JPEG, "image/jpeg")).isEqualTo(200);
        return send(mvc, mvc.post().uri(HOST_DOCUMENTS + "/{id}/confirm", (String) read(body(start), "$.documentId")),
                token, null);
    }

    public static MvcTestResult requestVerification(MockMvcTester mvc, String token, String idType) {
        return send(mvc, mvc.post().uri("/api/v1/me/host-profile/verification"), token,
                "{\"idType\":\"" + idType + "\"}");
    }

    /** A host with an ID front uploaded and a pending request; returns their token. */
    public static String pendingHost(MockMvcTester mvc) throws Exception {
        String token = newHost(mvc);
        assertThat(uploadHostId(mvc, token, "ID_FRONT")).hasStatusOk();
        assertThat(requestVerification(mvc, token, "COLLEGE_ID")).hasStatusOk();
        return token;
    }

    public static MvcTestResult get(MockMvcTester mvc, String uri, String token) {
        return mvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, bearer(token)).exchange();
    }

    public static MvcTestResult post(MockMvcTester mvc, String uri, String token, String json) {
        return send(mvc, mvc.post().uri(uri), token, json);
    }

    private static MvcTestResult send(MockMvcTester mvc, MockMvcTester.MockMvcRequestBuilder request, String token,
                                      String json) {
        request.header(HttpHeaders.AUTHORIZATION, bearer(token));
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return request.exchange();
    }
}
