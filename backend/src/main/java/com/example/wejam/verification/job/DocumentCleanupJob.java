package com.example.wejam.verification.job;

import com.example.wejam.common.storage.ObjectStorage;
import com.example.wejam.verification.config.VerificationProperties;
import com.example.wejam.verification.model.VerificationDocument;
import com.example.wejam.verification.repository.VerificationDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

/**
 * Deletes host IDs past their retention (design 4.3: "deleted 30 days after review") and uploads that were
 * started but never confirmed.
 *
 * <p>Order matters: file first, row second (the opposite of user-initiated deletes, which drop the file after
 * commit). If we crash in between, the row is still there and the next run retries — deleting a missing file
 * is a no-op. The other order could leave an ID file that no row points to, which would never be deleted.
 */
@Component
public class DocumentCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(DocumentCleanupJob.class);
    static final int BATCH_SIZE = 100;

    private final VerificationDocumentRepository documents;
    private final ObjectStorage storage;
    private final TransactionTemplate transaction;
    private final VerificationProperties properties;

    public DocumentCleanupJob(VerificationDocumentRepository documents, ObjectStorage storage,
                              TransactionTemplate transaction, VerificationProperties properties) {
        this.documents = documents;
        this.storage = storage;
        this.transaction = transaction;
        this.properties = properties;
    }

    /** Returns how many documents were deleted. Safe to run anywhere, any number of times, concurrently. */
    @Scheduled(fixedDelayString = "${wejam.verification.cleanup-interval}", initialDelayString = "1m")
    public int run() {
        int total = 0;
        int deleted;
        do {
            // One short transaction per batch, so row locks are held for one batch's file deletes at most.
            deleted = transaction.execute(status -> deleteBatch());
            total += deleted;
        } while (deleted == BATCH_SIZE);
        if (total > 0) {
            log.info("Deleted {} expired or abandoned verification documents", total);
        }
        return total;
    }

    private int deleteBatch() {
        Instant now = Instant.now();
        List<VerificationDocument> due = documents.lockDueForDeletion(now,
                now.minus(properties.abandonedUploadAge()), BATCH_SIZE);
        due.forEach(d -> storage.delete(d.getStorageKey()));
        documents.deleteAllInBatch(due);
        return due.size();
    }
}
