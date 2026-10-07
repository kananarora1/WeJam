-- The rest of the FSSAI license (design 7.2): the business name printed on it and its expiry date.
-- Nullable only for venues created before this step; every create/update now requires both.
ALTER TABLE venues
    ADD COLUMN fssai_license_name text CHECK (char_length(fssai_license_name) BETWEEN 1 AND 120),
    ADD COLUMN fssai_valid_until  date,
    ADD CONSTRAINT venues_fssai_license_details_check
        CHECK ((fssai_license_name IS NULL) = (fssai_valid_until IS NULL));

-- Proof of space can be a rental lease or an owner agreement (design 7.3).
ALTER TABLE verification_documents DROP CONSTRAINT verification_documents_doc_type_check;
ALTER TABLE verification_documents DROP CONSTRAINT verification_documents_owner_doc_type_check;
ALTER TABLE verification_documents
    ADD CONSTRAINT verification_documents_doc_type_check
        CHECK (doc_type IN ('FSSAI_CERTIFICATE', 'LEASE_AGREEMENT', 'OWNER_AGREEMENT', 'ID_FRONT', 'ID_BACK')),
    ADD CONSTRAINT verification_documents_owner_doc_type_check
        CHECK ((owner_type = 'VENUE') = (doc_type IN ('FSSAI_CERTIFICATE', 'LEASE_AGREEMENT', 'OWNER_AGREEMENT')));
