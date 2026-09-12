-- 300,000 demo rows.
-- H2 SYSTEM_RANGE를 사용해서 압축된 형태로 대량 데이터를 생성한다.
-- 2026-05-01부터 10일 범위에 주문을 분산한다.

INSERT INTO orders (id, ordered_at, product_id, amount, status)
SELECT
    x AS id,
    DATEADD('SECOND', MOD(x, 864000), TIMESTAMP '2026-05-01 00:00:00') AS ordered_at,
    CAST(MOD(x, 1000) + 1 AS BIGINT) AS product_id,
    CAST((MOD(x * 37, 200000) + 1000) / 10.0 AS NUMERIC(19, 2)) AS amount,
    CASE
        WHEN MOD(x, 20) = 0 THEN 'CANCELLED'
        ELSE 'PAID'
    END AS status
FROM SYSTEM_RANGE(1, 300000);
