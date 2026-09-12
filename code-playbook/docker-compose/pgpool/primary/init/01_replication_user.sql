-- primary/init/01_replication_user.sql
-- PRIMARY 최초 기동 시 자동 실행 - 복제 유저 생성

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'replicator') THEN
    CREATE USER replicator WITH REPLICATION ENCRYPTED PASSWORD 'replicator_secret';
  END IF;
END
$$;
