DO $$
    BEGIN
        IF EXISTS
            ( SELECT 1 from pg_roles where rolname='cloudsqliamuser')
        THEN
            GRANT ALL PRIVILEGES ON TABLE public.flyway_schema_history TO cloudsqliamuser;
        END IF ;
    END
$$ ;
