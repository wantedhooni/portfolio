CREATE TABLE IF NOT EXISTS public.users
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    email
    TEXT
    NOT
    NULL
    UNIQUE,
    name
    TEXT
    NOT
    NULL,
    created_at
    TIMESTAMPTZ
    NOT
    NULL
    DEFAULT
    now
(
),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );

ALTER TABLE public.users REPLICA IDENTITY FULL;

INSERT INTO public.users (email, name)
VALUES ('alice@example.com', 'Alice'),
       ('bob@example.com', 'Bob') ON CONFLICT (email) DO NOTHING;


/*
 SELECT *
FROM pg_publication_tables
WHERE pubname = 'debezium_outbox_publication';

DROP PUBLICATION IF EXISTS debezium_outbox_publication;

SELECT pg_drop_replication_slot('debezium_outbox_slot');


DROP PUBLICATION IF EXISTS debezium_outbox_publication;

CREATE PUBLICATION debezium_outbox_publication
FOR TABLE public.outboxevent;

SELECT slot_name, active
FROM pg_replication_slots;

SELECT *
FROM pg_publication_tables
WHERE pubname = 'debezium_outbox_publication';
 */