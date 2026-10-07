package com.example.wejam;

import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static com.example.wejam.TestAuth.body;
import static com.jayway.jsonpath.JsonPath.read;

/** Talks to MinIO the way the app does: real HTTP, no credentials. */
public final class TestUploads {

    public static final byte[] JPEG = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x11, 0x22};

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private TestUploads() {
    }

    /** PUTs to the presigned URL in an upload response. Content-Length comes from the body (Java won't let you set it by hand). */
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

    /** A plain GET with no signature — what anyone on the internet could do. */
    public static HttpResponse<byte[]> anonymousGet(String url) throws Exception {
        return HTTP.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofByteArray());
    }
}
