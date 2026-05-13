ALTER TABLE post
    ADD COLUMN IF NOT EXISTS user_id BIGINT,
    ADD COLUMN IF NOT EXISTS summary VARCHAR(500) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS category VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    ADD COLUMN IF NOT EXISTS view_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS published_at TIMESTAMP WITHOUT TIME ZONE;

WITH post_seed AS (
    SELECT
        post_idx,
        ((post_idx - 1) % 30) + 1 AS user_idx,
        CASE ((post_idx - 1) % 5) + 1
            WHEN 1 THEN 'ACCOUNT_GUIDE'
            WHEN 2 THEN 'BANK_NOTICE'
            WHEN 3 THEN 'TRANSACTION_TIP'
            WHEN 4 THEN 'SECURITY'
            ELSE 'SERVICE_UPDATE'
        END AS topic
    FROM generate_series(1, 120) AS seed(post_idx)
),
post_payload AS (
    SELECT
        concat('demo.user', lpad(user_idx::text, 2, '0'), '@example.com') AS email,
        concat('[', topic, '] Demo banking post ', lpad(post_idx::text, 3, '0')) AS title,
        concat('Demo summary for banking post ', lpad(post_idx::text, 3, '0'), '.') AS summary,
        concat(
            'This is demo content for banking post ',
            lpad(post_idx::text, 3, '0'),
            '. It is seeded for list, detail, search, and pagination scenarios.'
        ) AS content,
        topic AS category,
        CASE
            WHEN post_idx % 17 = 0 THEN 'DRAFT'
            WHEN post_idx % 19 = 0 THEN 'ARCHIVED'
            ELSE 'PUBLISHED'
        END AS status,
        (post_idx * 13 + user_idx * 7)::bigint AS view_count,
        now() - (post_idx * interval '6 hours') AS published_at
    FROM post_seed
)
INSERT INTO post (
    created_at,
    updated_at,
    user_id,
    title,
    summary,
    content,
    category,
    status,
    view_count,
    published_at
)
SELECT
    post_payload.published_at,
    post_payload.published_at,
    "users".id,
    post_payload.title,
    post_payload.summary,
    post_payload.content,
    post_payload.category,
    post_payload.status,
    post_payload.view_count,
    CASE
        WHEN post_payload.status = 'PUBLISHED' THEN post_payload.published_at
        ELSE NULL
    END
FROM post_payload
JOIN "users" ON "users".email = post_payload.email
WHERE NOT EXISTS (
    SELECT 1
    FROM post existing_post
    WHERE existing_post.title = post_payload.title
);
