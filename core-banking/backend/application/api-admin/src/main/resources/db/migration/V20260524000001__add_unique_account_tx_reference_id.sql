-- account_tx.reference_id 에 UNIQUE 제약을 추가합니다.
--
-- 배경:
--   AccountCommandImpl 의 deposit / withdraw / transfer 는 referenceId 로 멱등성을 보장하지만
--   DB 레벨 UNIQUE 제약이 없으면 동시 요청(TOCTOU) 시 중복 거래가 삽입될 수 있습니다.
--   NULL 허용(매수·배당 등 referenceId 없는 거래)이 필요하므로 NULLS NOT DISTINCT 를 제외하고
--   부분 인덱스(WHERE reference_id IS NOT NULL)로만 유니크를 보장합니다.

CREATE UNIQUE INDEX uq_account_tx_reference_id
    ON public.account_tx (reference_id)
    WHERE reference_id IS NOT NULL;
