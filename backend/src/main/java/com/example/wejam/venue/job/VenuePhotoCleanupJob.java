package com.example.wejam.venue.job;

import com.example.wejam.common.storage.Bucket;
import com.example.wejam.common.storage.ObjectStorage;
import com.example.wejam.common.storage.StorageProperties;
import com.example.wejam.venue.model.VenuePhoto;
import com.example.wejam.venue.repository.VenuePhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

/**
 * Deletes photo uploads that were started but never confirmed (the file may or may not have landed).
 * Same shape as DocumentCleanupJob: locked batches, file first, then row.
 */
@Component
public class VenuePhotoCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(VenuePhotoCleanupJob.class);
    static final int BATCH_SIZE = 100;

    private final VenuePhotoRepository photos;
    private final ObjectStorage storage;
    private final TransactionTemplate transaction;
    private final StorageProperties properties;

    public VenuePhotoCleanupJob(VenuePhotoRepository photos, ObjectStorage storage, TransactionTemplate transaction,
                                StorageProperties properties) {
        this.photos = photos;
        this.storage = storage;
        this.transaction = transaction;
        this.properties = properties;
    }

    /** Returns how many abandoned uploads were deleted. */
    @Scheduled(fixedDelayString = "${wejam.jobs.cleanup-interval}", initialDelayString = "1m")
    public int run() {
        int total = 0;
        int deleted;
        do {
            deleted = transaction.execute(status -> deleteBatch());
            total += deleted;
        } while (deleted == BATCH_SIZE);
        if (total > 0) {
            log.info("Deleted {} abandoned venue photo uploads", total);
        }
        return total;
    }

    private int deleteBatch() {
        List<VenuePhoto> abandoned = photos.lockAbandoned(Instant.now().minus(properties.abandonedUploadAge()),
                BATCH_SIZE);
        abandoned.forEach(p -> storage.delete(Bucket.PUBLIC, p.getStorageKey()));
        photos.deleteAllInBatch(abandoned);
        return abandoned.size();
    }
}
