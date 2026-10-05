-- Optional host verification (CLAUDE.md §5.12, design 4.3). Never required: nothing gates on it, and only a
-- positive "verified" flag is public. NOT_REQUESTED until the host asks; then PENDING → VERIFIED | REJECTED.
ALTER TABLE host_profiles
    ADD COLUMN verification_status       text        NOT NULL DEFAULT 'NOT_REQUESTED'
        CHECK (verification_status IN ('NOT_REQUESTED', 'PENDING', 'VERIFIED', 'REJECTED')),
    ADD COLUMN id_type                   text        CHECK (id_type IN ('COLLEGE_ID', 'COMPANY_ID')),
    ADD COLUMN verification_requested_at timestamptz,
    ADD COLUMN rejection_reason          text,
    -- Once requested, we always know what kind of ID and since when.
    ADD CONSTRAINT host_profiles_verification_request_check
        CHECK ((verification_status = 'NOT_REQUESTED') = (id_type IS NULL AND verification_requested_at IS NULL)),
    ADD CONSTRAINT host_profiles_rejection_reason_check
        CHECK ((verification_status = 'REJECTED') = (rejection_reason IS NOT NULL));

-- Admin queue: WHERE verification_status = ? ORDER BY verification_requested_at, id
CREATE INDEX host_profiles_verification_queue_idx ON host_profiles (verification_status, verification_requested_at, id);

ALTER TABLE verification_documents DROP CONSTRAINT verification_documents_doc_type_check;
ALTER TABLE verification_documents
    ADD CONSTRAINT verification_documents_doc_type_check
        CHECK (doc_type IN ('FSSAI_CERTIFICATE', 'LEASE_AGREEMENT', 'ID_FRONT', 'ID_BACK')),
    -- Venue documents are business records; ID sides belong to host profiles only.
    ADD CONSTRAINT verification_documents_owner_doc_type_check
        CHECK ((owner_type = 'VENUE') = (doc_type IN ('FSSAI_CERTIFICATE', 'LEASE_AGREEMENT'))),
    -- Host IDs are deleted 30 days after review (design 4.3), or 30 days after upload if never submitted.
    -- NULL = kept (venue documents, or an ID under review).
    ADD COLUMN delete_after timestamptz;

-- Cleanup job: WHERE delete_after < ?  and  WHERE status = 'PENDING_UPLOAD' AND created_at < ?
CREATE INDEX verification_documents_delete_after_idx
    ON verification_documents (delete_after) WHERE delete_after IS NOT NULL;
CREATE INDEX verification_documents_pending_upload_idx
    ON verification_documents (created_at) WHERE status = 'PENDING_UPLOAD';

ALTER TABLE admin_actions DROP CONSTRAINT admin_actions_action_check;
ALTER TABLE admin_actions ADD CONSTRAINT admin_actions_action_check
    CHECK (action IN ('VENUE_APPROVED', 'VENUE_REJECTED', 'HOST_APPROVED', 'HOST_REJECTED'));
ALTER TABLE admin_actions DROP CONSTRAINT admin_actions_target_type_check;
ALTER TABLE admin_actions ADD CONSTRAINT admin_actions_target_type_check
    CHECK (target_type IN ('VENUE', 'HOST_PROFILE'));
