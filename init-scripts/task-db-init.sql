DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'task') THEN
        CREATE DATABASE task WITH OWNER taskapp;
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'taskapp') THEN
        CREATE ROLE taskapp WITH LOGIN PASSWORD 'taskpassword';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.role_table_grants 
                    WHERE grantee = 'taskapp' AND table_schema = 'public' AND privilege_type = 'CREATE') THEN
        GRANT CREATE ON SCHEMA public TO taskapp;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.role_table_grants 
                    WHERE grantee = 'taskapp' AND table_schema = 'public' AND privilege_type = 'ALL') THEN
        GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO taskapp;
    END IF;
END $$;
