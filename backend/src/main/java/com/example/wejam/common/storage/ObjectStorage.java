package com.example.wejam.common.storage;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Private-bucket operations. File bytes never pass through the backend: apps use presigned URLs. */
@Service
public class ObjectStorage {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final StorageProperties properties;

    public ObjectStorage(S3Client s3, S3Presigner presigner, StorageProperties properties) {
        this.s3 = s3;
        this.presigner = presigner;
        this.properties = properties;
    }

    /** Content-Type and Content-Length are part of the signature, so storage rejects any other file. */
    public PresignedUpload presignUpload(String key, String contentType, long contentLength) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(properties.privateBucket())
                .key(key)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();
        PresignedPutObjectRequest presigned = presigner.presignPutObject(p -> p
                .signatureDuration(properties.uploadUrlTtl())
                .putObjectRequest(put));
        Map<String, String> headers = presigned.signedHeaders().entrySet().stream()
                .filter(e -> !e.getKey().equalsIgnoreCase("host"))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(",", e.getValue())));
        return new PresignedUpload(presigned.url().toString(), headers, presigned.expiration());
    }

    public String presignDownload(String key) {
        GetObjectRequest get = GetObjectRequest.builder().bucket(properties.privateBucket()).key(key).build();
        return presigner.presignGetObject(p -> p.signatureDuration(properties.downloadUrlTtl()).getObjectRequest(get))
                .url().toString();
    }

    public Instant downloadUrlExpiry() {
        return Instant.now().plus(properties.downloadUrlTtl());
    }

    public Optional<StoredObject> head(String key) {
        try {
            HeadObjectResponse head = s3.headObject(HeadObjectRequest.builder()
                    .bucket(properties.privateBucket()).key(key).build());
            return Optional.of(new StoredObject(head.contentLength(), head.contentType()));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    public void delete(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(properties.privateBucket()).key(key).build());
    }

    /**
     * Delete the object only once the surrounding DB transaction has committed: if the transaction rolled back
     * after the object was gone, a row would point at nothing. Outside a transaction, deletes immediately.
     */
    public void deleteAfterCommit(String key) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            delete(key);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                delete(key);
            }
        });
    }
}
