package com.example.wejam.verification;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
    public static final byte[] JPEG = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x11, 0x22};

    /** Listed in application-test.properties under wejam.admin.phones. */
    private static final String ADMIN_PHONE = "+919999999901";
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final String HOST_DOCUMENTS = "/api/v1/me/host-profile/documents";

    private VerificationTestSupport() {
    }

    /** PUTs to a presigned URL. Content-Length comes from the body (Java won't let you set it by hand). */
    public static int put(MvcTestResult start, byte[] bytes, String contentType) throws Exception {
        Map<String, String> signed = read(body(start), "$.headers");
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(read(body(start), "$.uploadUrl")))
                .PUT(HttpRequest.BodyPublishers.ofByteArray(bytes));
        signed.forEach((name, value) -> {
            if (!name.equalsIgnoreCase("content-length") && !name.equalsIgnoreCase("content-type")) {
                request.header(name, value);
            }
        });
        request.header("Content-Type", contentType);
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
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
