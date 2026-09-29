package com.example.wejam;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public final class TestAuth {

    private TestAuth() {
    }

    public static String randomUid() {
        return "uid-" + UUID.randomUUID();
    }

    /** Logs in through the real exchange endpoint using the fake Firebase verifier. */
    public static String accessTokenFor(MockMvcTester mvc, String firebaseUid) {
        return JsonPath.read(body(exchange(mvc, "fake:" + firebaseUid)), "$.accessToken");
    }

    public static MvcTestResult exchange(MockMvcTester mvc, String firebaseIdToken) {
        return mvc.post().uri("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firebaseIdToken\":\"" + firebaseIdToken + "\"}")
                .exchange();
    }

    public static String body(MvcTestResult result) {
        try {
            return result.getResponse().getContentAsString();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }
}
