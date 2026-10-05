ALTER TABLE user_roles DROP CONSTRAINT user_roles_role_check;
ALTER TABLE user_roles ADD CONSTRAINT user_roles_role_check
    CHECK (role IN ('USER', 'HOST', 'VENUE_ADMIN', 'PLATFORM_ADMIN'));

-- When the venue last entered PENDING (creation, FSSAI change, resubmit). The review queue is oldest-first.
ALTER TABLE venues ADD COLUMN verification_requested_at timestamptz NOT NULL DEFAULT now();
UPDATE venues SET verification_requested_at = created_at;

-- Admin queue: WHERE verification_status = ? ORDER BY verification_requested_at, id
CREATE INDEX venues_verification_queue_idx ON venues (verification_status, verification_requested_at, id);

-- Every platform-admin decision, written in the same transaction as the decision itself. Read-only via the API.
CREATE TABLE admin_actions (
    id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id    uuid        NOT NULL REFERENCES users (id),
    action      text        NOT NULL CHECK (action IN ('VENUE_APPROVED', 'VENUE_REJECTED')),
    target_type text        NOT NULL CHECK (target_type IN ('VENUE')),
    target_id   uuid        NOT NULL,
    reason      text,
    created_at  timestamptz NOT NULL DEFAULT now()
);

-- Action log, newest first, keyset-paginated on (created_at, id).
CREATE INDEX admin_actions_created_idx ON admin_actions (created_at DESC, id DESC);
