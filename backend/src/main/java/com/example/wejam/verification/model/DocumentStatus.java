package com.example.wejam.verification.model;

public enum DocumentStatus {
    /** Row created and upload URL handed out; the file may never arrive. */
    PENDING_UPLOAD,
    /** Confirmed present in storage with the declared size and type. */
    UPLOADED
}
