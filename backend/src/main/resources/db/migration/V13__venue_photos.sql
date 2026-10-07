-- Venue photos (design 7.1). Unlike verification documents these are public: the files live in the public
-- bucket. Only keys are stored; URLs are built per response, so a new host or a CDN needs no data change.
CREATE TABLE venue_photos (
    id           uuid        PRIMARY KEY,
    venue_id     uuid        NOT NULL REFERENCES venues (id) ON DELETE CASCADE,
    storage_key  text        NOT NULL UNIQUE,
    content_type text        NOT NULL CHECK (content_type IN ('image/jpeg', 'image/png', 'image/webp')),
    size_bytes   bigint      NOT NULL CHECK (size_bytes BETWEEN 1 AND 5242880),
    status       text        NOT NULL CHECK (status IN ('PENDING_UPLOAD', 'UPLOADED')),
    created_at   timestamptz NOT NULL DEFAULT now(),
    uploaded_at  timestamptz,
    CHECK ((status = 'UPLOADED') = (uploaded_at IS NOT NULL))
);

-- A venue's photos in upload order (the first is the cover); also serves the ON DELETE CASCADE.
CREATE INDEX venue_photos_venue_idx ON venue_photos (venue_id, uploaded_at, id);
-- Cleanup job: uploads started but never confirmed.
CREATE INDEX venue_photos_pending_upload_idx ON venue_photos (created_at) WHERE status = 'PENDING_UPLOAD';

-- Confirmed photos only. Written solely by atomic counter updates (ADR-026), never by saving the entity.
ALTER TABLE venues ADD COLUMN photo_count integer NOT NULL DEFAULT 0 CHECK (photo_count BETWEEN 0 AND 10);

-- Rejection can flag the photos (design 9.3).
ALTER TABLE venues DROP CONSTRAINT venues_verification_issues_check;
ALTER TABLE venues ADD CONSTRAINT venues_verification_issues_check CHECK (verification_issues
    <@ ARRAY['FSSAI_NUMBER', 'FSSAI_CERTIFICATE', 'LEASE_AGREEMENT', 'VENUE_DETAILS', 'PHOTOS']::text[]);
