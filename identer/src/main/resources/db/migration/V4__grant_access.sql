DO $$
    BEGIN
        IF EXISTS
            ( SELECT 1 from pg_roles where rolname='cloudsqliamuser')
        THEN
            GRANT ALL PRIVILEGES ON TABLE public.identifikator TO cloudsqliamuser;
        END IF ;
    END
$$ ;
