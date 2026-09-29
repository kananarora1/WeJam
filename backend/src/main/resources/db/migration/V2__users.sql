CREATE TABLE users (
    id           uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid text        NOT NULL UNIQUE,
    phone        text        UNIQUE,
    display_name text,
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
    user_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role    text NOT NULL CHECK (role IN ('USER', 'HOST', 'VENUE_ADMIN')),
    PRIMARY KEY (user_id, role)
);
