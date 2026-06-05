CREATE TABLE IF NOT EXISTS public.purchase_order
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    customer_id
    BIGINT
    NOT
    NULL,
    product_name
    VARCHAR
(
    255
) NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR
(
    50
) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );

CREATE TABLE IF NOT EXISTS public.outboxevent
(
    id
    UUID
    PRIMARY
    KEY,
    aggregatetype
    VARCHAR
(
    255
) NOT NULL,
    aggregateid VARCHAR
(
    255
) NOT NULL,
    type VARCHAR
(
    255
) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );

ALTER TABLE public.purchase_order REPLICA IDENTITY FULL;
ALTER TABLE public.outboxevent REPLICA IDENTITY FULL;