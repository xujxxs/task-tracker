DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'profile') THEN
        CREATE DATABASE profile WITH OWNER profileapp;
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'profileapp') THEN
        CREATE ROLE profileapp WITH LOGIN PASSWORD 'profilepassword';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.role_table_grants 
                    WHERE grantee = 'profileapp' AND table_schema = 'public' AND privilege_type = 'CREATE') THEN
        GRANT CREATE ON SCHEMA public TO profileapp;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.role_table_grants 
                    WHERE grantee = 'profileapp' AND table_schema = 'public' AND privilege_type = 'ALL') THEN
        GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO profileapp;
    END IF;
END $$;
