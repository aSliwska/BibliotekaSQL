-- zwraca tabele wszystkich gatunkow i nad-gatunkow danej ksiazki (powinny byc DISTINCT)
CREATE OR REPLACE FUNCTION wszystkie_gatunki_ksiazki(id_ksiazki INTEGER) 
RETURNS TABLE(gatunek_id INTEGER) AS 
$$
    BEGIN
        RETURN QUERY
        WITH RECURSIVE wszystkie_gatunki AS (
            SELECT gk.gatunek_id FROM gatunek_ksiazki gk WHERE gk.ksiazka_id = id_ksiazki

            UNION

            SELECT gatunek_rodzic_id
            FROM gatunek JOIN wszystkie_gatunki USING (gatunek_id)
            WHERE gatunek_rodzic_id IS NOT NULL
        )
        SELECT * FROM wszystkie_gatunki;

    END;
$$ LANGUAGE 'plpgsql';

-- zwraca czestosc wystepowania kazdego gatunku dla id ksiazek zwracanych w przekazanym query
CREATE OR REPLACE FUNCTION czestosc_gatunkow_dla_ksiazek(ksiazki_query VARCHAR)
RETURNS TABLE(gatunek_id INTEGER, czestosc INTEGER) AS 
$$
    DECLARE
        id_ksiazki INTEGER;
    BEGIN
        CREATE TEMP TABLE rec (gatunek_id INTEGER NOT NULL);

        FOR id_ksiazki IN EXECUTE ksiazki_query 
        LOOP
            INSERT INTO rec SELECT * FROM wszystkie_gatunki_ksiazki(id_ksiazki);
        END LOOP;

        RETURN QUERY
        SELECT rec.gatunek_id, COUNT(*)::INTEGER AS czestosc
        FROM rec
        GROUP BY rec.gatunek_id;

        DROP TABLE rec;
    END;
$$ LANGUAGE 'plpgsql';

-- zwraca drzewo gatunku wstecz
CREATE OR REPLACE FUNCTION znajdz_drzewo_gatunku_wstecz(id_gatunku INTEGER)
RETURNS VARCHAR AS 
$$
    DECLARE
        wynik VARCHAR;
    BEGIN
        WITH RECURSIVE drzewo_gatunku_wstecz AS (
            SELECT g.gatunek_id, g.gatunek_rodzic_id, 1 AS poziom, g.nazwa::VARCHAR AS drzewo 
            FROM gatunek g WHERE g.gatunek_id = id_gatunku

            UNION ALL

            SELECT new.gatunek_id, new.gatunek_rodzic_id, poziom+1, new.nazwa||' -> '||drzewo
            FROM gatunek new
            JOIN drzewo_gatunku_wstecz old ON (new.gatunek_id = old.gatunek_rodzic_id)
        )
        SELECT INTO wynik drzewo 
        FROM drzewo_gatunku_wstecz
        ORDER BY poziom DESC
        LIMIT 1;

        RETURN wynik;
    END;
$$ LANGUAGE 'plpgsql';