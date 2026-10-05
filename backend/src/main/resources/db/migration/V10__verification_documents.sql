-- Documents behind a verification (CLAUDE.md §5.12). Polymorphic owner: venues now, host profiles next.
-- Bytes live in the private bucket; only the key is stored here.
CREATE TABLE verification_documents (
    id           uuid        PRIMARY KEY,
    owner_type   text        NOT NULL CHECK (owner_type IN ('VENUE', 'HOST_PROFILE')),
    owner_id     uuid        NOT NULL,
    doc_type     text        NOT NULL CHECK (doc_type IN ('FSSAI_CERTIFICATE', 'LEASE_AGREEMENT')),
    storage_key  text        NOT NULL UNIQUE,
    content_type text        NOT NULL CHECK (content_type IN ('image/jpeg', 'image/png', 'application/pdf')),
    size_bytes   bigint      NOT NULL CHECK (size_bytes BETWEEN 1 AND 10485760),
    status       text        NOT NULL CHECK (status IN ('PENDING_UPLOAD', 'UPLOADED')),
    created_at   timestamptz NOT NULL DEFAULT now(),
    uploaded_at  timestamptz,
    CHECK ((status = 'UPLOADED') = (uploaded_at IS NOT NULL))
);

-- At most one current document per type per owner; also serves "list an owner's documents".
CREATE UNIQUE INDEX verification_documents_current_uq
    ON verification_documents (owner_type, owner_id, doc_type) WHERE status = 'UPLOADED';

-- What the admin flagged on rejection (design 9.3 / 7.6); cleared on approval, resubmit or a new FSSAI number.
ALTER TABLE venues ADD COLUMN verification_issues text[] NOT NULL DEFAULT '{}'
    CHECK (verification_issues <@ ARRAY['FSSAI_NUMBER', 'FSSAI_CERTIFICATE', 'LEASE_AGREEMENT', 'VENUE_DETAILS']::text[]);
