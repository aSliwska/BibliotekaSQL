CREATE SCHEMA projekt;
SET search_path TO projekt;
SET DATESTYLE TO EUROPEAN;
SET statement_timeout TO '10s';

------------------------------------------------------------- tabele

CREATE TABLE czytelnik (
    czytelnik_id SERIAL,
    imie VARCHAR(32) NOT NULL,
    nazwisko VARCHAR(32) NOT NULL,
    email VARCHAR(32) NOT NULL,
    telefon VARCHAR(32),
    data_rejestracji DATE NOT NULL,
    CONSTRAINT czytelnik_pk PRIMARY KEY(czytelnik_id)
);

CREATE TABLE wplata (
    wplata_id SERIAL,
    data_wplaty TIMESTAMP NOT NULL,
    kwota NUMERIC(7, 2) NOT NULL,
    czytelnik_id INTEGER NOT NULL,
    CHECK (kwota > 0),
    CONSTRAINT wplata_pk PRIMARY KEY(wplata_id),
    CONSTRAINT wplata_czytelnik_id_fk FOREIGN KEY(czytelnik_id) REFERENCES czytelnik(czytelnik_id) ON DELETE CASCADE
);

CREATE TABLE autor (
    autor_id SERIAL,
    imie VARCHAR(32) NOT NULL,
    nazwisko VARCHAR(32) NOT NULL,
    CONSTRAINT autor_pk PRIMARY KEY(autor_id)
);

CREATE TABLE wydawnictwo (
    wydawnictwo_id SERIAL,
    nazwa VARCHAR(32) NOT NULL,
    data_zalozenia DATE NOT NULL,
    CONSTRAINT wydawnictwo_pk PRIMARY KEY(wydawnictwo_id)
);

CREATE TABLE ksiazka (
    ksiazka_id SERIAL,
    tytul VARCHAR(128) NOT NULL,
    opis VARCHAR,
    autor_id INTEGER NOT NULL,
    wydawnictwo_id INTEGER NOT NULL,
    CONSTRAINT ksiazka_pk PRIMARY KEY(ksiazka_id),
    CONSTRAINT ksiazka_autor_id_fk FOREIGN KEY(autor_id) REFERENCES autor(autor_id) ON DELETE CASCADE,
    CONSTRAINT ksiazka_wydawnictwo_id_fk FOREIGN KEY(wydawnictwo_id) REFERENCES wydawnictwo(wydawnictwo_id) ON DELETE CASCADE
);

CREATE TABLE egzemplarz (
    egzemplarz_id SERIAL,
    ksiazka_id INTEGER NOT NULL,
    CONSTRAINT egzemplarz_pk PRIMARY KEY(egzemplarz_id),
    CONSTRAINT egzemplarz_ksiazka_id_fk FOREIGN KEY(ksiazka_id) REFERENCES ksiazka(ksiazka_id) ON DELETE CASCADE
);

CREATE TABLE wypozyczenie (
    wypozyczenie_id SERIAL,
    data_wypozyczenia TIMESTAMP NOT NULL,
    data_zwrotu TIMESTAMP,
    egzemplarz_id INTEGER, -- null to tylko usuniety egzemplarz
    czytelnik_id INTEGER NOT NULL,
    CHECK (data_wypozyczenia < data_zwrotu),
    CONSTRAINT wypozyczenie_pk PRIMARY KEY(wypozyczenie_id),
    CONSTRAINT wypozyczenie_egzemplarz_id_fk FOREIGN KEY(egzemplarz_id) REFERENCES egzemplarz(egzemplarz_id) ON DELETE SET NULL,
    CONSTRAINT wypozyczenie_czytelnik_id_fk FOREIGN KEY(czytelnik_id) REFERENCES czytelnik(czytelnik_id) ON DELETE CASCADE
);

CREATE TABLE gatunek (
    gatunek_id SERIAL,
    gatunek_rodzic_id INTEGER,
    nazwa VARCHAR(32) NOT NULL,
    CONSTRAINT gatunek_pk PRIMARY KEY(gatunek_id),
    CONSTRAINT gatunek_gatunek_rodzic_id_fk FOREIGN KEY(gatunek_rodzic_id) REFERENCES gatunek(gatunek_id)
);

CREATE TABLE gatunek_ksiazki (
    ksiazka_id INTEGER NOT NULL,
    gatunek_id INTEGER NOT NULL,
    CONSTRAINT gatunek_ksiazki_pk PRIMARY KEY(ksiazka_id, gatunek_id),
    CONSTRAINT gatunek_ksiazki_ksiazka_id_fk FOREIGN KEY(ksiazka_id) REFERENCES ksiazka(ksiazka_id) ON DELETE CASCADE,
    CONSTRAINT gatunek_ksiazki_gatunek_id_fk FOREIGN KEY(gatunek_id) REFERENCES gatunek(gatunek_id) ON DELETE CASCADE
);

------------------------------------------------------------- wypelnienie
-- inserts.sql






------------------------------------------------------------- widoki 
-- najnowsze wypozyczenie egzemplarza
CREATE VIEW najnowsze_wypozyczenie AS
WITH najnowsza_data_wypozyczenia AS (
    SELECT egzemplarz_id, MAX(data_wypozyczenia) AS data_wypozyczenia
    FROM wypozyczenie
    GROUP BY egzemplarz_id
)
SELECT *
FROM wypozyczenie JOIN najnowsza_data_wypozyczenia USING (egzemplarz_id, data_wypozyczenia);

-- dostepne egzemplarze
CREATE VIEW dostepne_egzemplarze AS
SELECT egzemplarz_id, ksiazka_id
FROM egzemplarz LEFT JOIN najnowsze_wypozyczenie USING (egzemplarz_id)
WHERE data_zwrotu IS NOT NULL OR data_wypozyczenia IS NULL;

-- ilosc egzemplarzy kazdej ksiazki w systemie oraz ile jest dostepnych aktualnie
CREATE VIEW ksiazka_statystyki AS 
WITH policzone_dostepne_egzemplarze AS (
    SELECT ksiazka_id, COUNT(DISTINCT egzemplarz_id) AS ilosc_dostepnych_egzemplarzy
    FROM ksiazka LEFT JOIN dostepne_egzemplarze USING (ksiazka_id)
    GROUP BY ksiazka_id
)
SELECT ksiazka_id, COUNT(DISTINCT egzemplarz_id) AS ilosc_egzemplarzy_w_systemie, ilosc_dostepnych_egzemplarzy
FROM (ksiazka LEFT JOIN policzone_dostepne_egzemplarze USING(ksiazka_id)) LEFT JOIN egzemplarz USING (ksiazka_id)
GROUP BY ksiazka_id, ilosc_dostepnych_egzemplarzy;



-- wypozyczenia ktore trwaly wiecej niz 30 dni
CREATE VIEW przedluzone_wypozyczenia AS
WITH wypozyczenie_do_dzisiaj AS (
    SELECT wypozyczenie_id, czytelnik_id, data_wypozyczenia, CASE WHEN data_zwrotu IS NULL THEN NOW()::TIMESTAMP ELSE data_zwrotu END AS data_zwrotu
    FROM wypozyczenie 
)
SELECT wypozyczenie_id, czytelnik_id, (EXTRACT(epoch FROM AGE(data_zwrotu, data_wypozyczenia))/86400)::INTEGER AS dlugosc_wypozyczenia
FROM wypozyczenie_do_dzisiaj 
WHERE (EXTRACT(epoch FROM AGE(data_zwrotu, data_wypozyczenia))/86400)::INTEGER > 30;

-- balans na koncie bibliotecznym czytelnika (1zl za dzien przedluzenia)
CREATE VIEW balans_czytelnika AS 
WITH suma_wplat AS (
    SELECT czytelnik_id, CASE WHEN SUM(kwota) IS NULL THEN 0 ELSE SUM(kwota) END as wplacone
    FROM czytelnik LEFT JOIN wplata USING (czytelnik_id)
    GROUP BY czytelnik_id
), suma_przedluzen AS (
    SELECT czytelnik_id, CASE WHEN SUM(dlugosc_wypozyczenia-30)*1 IS NULL THEN 0 ELSE SUM(dlugosc_wypozyczenia-30) END as dluzne
    FROM czytelnik LEFT JOIN przedluzone_wypozyczenia USING (czytelnik_id)
    GROUP BY czytelnik_id
)
SELECT czytelnik_id, CAST(wplacone-dluzne AS NUMERIC(7, 2)) AS balans
FROM (czytelnik LEFT JOIN suma_wplat USING (czytelnik_id)) LEFT JOIN suma_przedluzen USING (czytelnik_id);

-- ilosc ksiazek ktore ma wypozyczone + ilosc ksiazek ktorych jeszcze nie zwrocil a minal czas
CREATE VIEW czytelnik_statystyki AS
WITH policzone_wypozyczone AS (
    SELECT czytelnik_id, COUNT(DISTINCT egzemplarz_id) AS ilosc_wypozyczonych
    FROM wypozyczenie
    WHERE data_zwrotu IS NULL
    GROUP BY czytelnik_id
), policzone_po_terminie AS (
    SELECT czytelnik_id, COUNT(DISTINCT egzemplarz_id) AS ilosc_po_terminie
    FROM wypozyczenie
    WHERE data_zwrotu IS NULL AND wypozyczenie_id IN (SELECT wypozyczenie_id FROM przedluzone_wypozyczenia)
    GROUP BY czytelnik_id
)
SELECT czytelnik_id, CASE WHEN ilosc_wypozyczonych IS NULL THEN 0 ELSE ilosc_wypozyczonych END as ilosc_aktualnie_wypozyczonych, CASE WHEN ilosc_po_terminie IS NULL THEN 0 ELSE ilosc_po_terminie END as ilosc_aktualnie_po_terminie
FROM (czytelnik LEFT JOIN policzone_wypozyczone USING (czytelnik_id)) LEFT JOIN policzone_po_terminie USING (czytelnik_id);

-- zbanowani uzytkownicy ktorzy przetrzymali ksiazki w sumie ponad 365 dni
CREATE VIEW zbanowani_czytelnicy AS
SELECT czytelnik_id, SUM(dlugosc_wypozyczenia-30) AS suma_przedluzen
FROM przedluzone_wypozyczenia
GROUP BY czytelnik_id
HAVING SUM(dlugosc_wypozyczenia-30) > 365;

CREATE VIEW status_bana AS 
SELECT czytelnik_id, CASE WHEN suma_przedluzen IS NULL THEN 'nie' ELSE 'tak' END AS czy_jest_zbanowany
FROM czytelnik LEFT JOIN zbanowani_czytelnicy USING (czytelnik_id);

CREATE VIEW status_wypozyczenia AS 
SELECT wypozyczenie_id, CASE WHEN dlugosc_wypozyczenia IS NULL THEN 0 ELSE dlugosc_wypozyczenia-30 END AS dni_nad_limitem
FROM wypozyczenie LEFT JOIN przedluzone_wypozyczenia USING (wypozyczenie_id);


------------------------------------------------------------- funkcje
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





------------------------------------------------------------- selecty
------ ogolne:
-- najpopularniejszy gatunek w systemie
SELECT gatunek_id, nazwa, czestosc 
FROM czestosc_gatunkow_dla_ksiazek('SELECT ksiazka_id FROM ksiazka') JOIN gatunek USING (gatunek_id)
ORDER BY czestosc DESC;

-- wszystkie ksiazki za dlugo przetrzymywane i przez kogo
SELECT wypozyczenie_id, w.czytelnik_id, w.egzemplarz_id, dlugosc_wypozyczenia-30 AS dni_nad_limitem
FROM przedluzone_wypozyczenia JOIN wypozyczenie w USING (wypozyczenie_id)
WHERE w.data_zwrotu IS NULL;

-- wszyscy czytelnicy ktorzy sa jeszcze cos dluzni bibliotece
SELECT czytelnik_id, balans
FROM balans_czytelnika
WHERE balans < 0
ORDER BY balans;

--==============================================--czytelnik--=====================================================--
-- ogolne dane, balans, czy jest zbanowany, ilosc aktualnie wypozyczonych ksiazek, ilosc aktualnie przetrzymywanych po terminie, ile ksiazek wypozyczyl w sumie, ile dni spoznil sie z oddawaniem ksiazek w sumie
WITH zsumowana_ilosc_wypozyczen AS (
    SELECT czytelnik_id, COUNT(*) AS ogolna_ilosc_wypozyczen
    FROM wypozyczenie 
    GROUP BY czytelnik_id
), zsumowana_ilosc_dni_nad_termin AS (
    SELECT czytelnik_id, SUM(dlugosc_wypozyczenia-30) as suma_przedluzen
    FROM przedluzone_wypozyczenia
    GROUP BY czytelnik_id
)
SELECT imie, nazwisko, email, telefon, data_rejestracji, balans, czy_jest_zbanowany, ilosc_aktualnie_wypozyczonych, ilosc_aktualnie_po_terminie, ogolna_ilosc_wypozyczen, suma_przedluzen
FROM czytelnik LEFT JOIN balans_czytelnika USING (czytelnik_id) LEFT JOIN status_bana USING (czytelnik_id) LEFT JOIN czytelnik_statystyki USING (czytelnik_id) LEFT JOIN zsumowana_ilosc_wypozyczen USING (czytelnik_id) LEFT JOIN zsumowana_ilosc_dni_nad_termin USING (czytelnik_id) 
WHERE czytelnik_id = ?;

-- jakie gatunki czyta
SELECT nazwa, czestosc
FROM czestosc_gatunkow_dla_ksiazek('SELECT DISTINCT ksiazka_id FROM ksiazka JOIN egzemplarz USING (ksiazka_id) JOIN wypozyczenie USING (egzemplarz_id) WHERE czytelnik_id = ?') JOIN gatunek USING (gatunek_id)
ORDER BY czestosc DESC;

--==============================================--ksiazka--=====================================================--
-- ogolne dane, ilosc wszystkich i dostepnych egzemplarzy
SELECT tytul, opis, wydawnictwo_id, autor_id, ilosc_egzemplarzy_w_systemie, ilosc_dostepnych_egzemplarzy
FROM ksiazka LEFT JOIN ksiazka_statystyki USING (ksiazka_id)
WHERE ksiazka_id = ?;

-- wszystkie drzewa gatunkow ksiazki
SELECT znajdz_drzewo_gatunku_wstecz(gatunek_id) AS drzewo
FROM gatunek_ksiazki
WHERE ksiazka_id = ?;

-- wszystkie egzemplarze i ich ostatnie wypozyczenia i dostepnosc
SELECT egzemplarz_id, wypozyczenie_id, czytelnik_id, data_wypozyczenia, data_zwrotu, CASE WHEN egzemplarz_id IN (SELECT egzemplarz_id FROM dostepne_egzemplarze) THEN 'tak' ELSE 'nie' END AS czy_egzemplarz_dostepny
FROM egzemplarz LEFT JOIN najnowsze_wypozyczenie USING (egzemplarz_id)
WHERE ksiazka_id = ?;

--==============================================--autor--=====================================================--
-- ogolne dane, ile tytulow autora, ile egzemplarzy
WITH statystyki_ksiazkowe AS (
    SELECT autor_id,
        CASE WHEN COUNT(DISTINCT ksiazka_id) IS NULL THEN 0 ELSE COUNT(DISTINCT ksiazka_id) END AS ilosc_ksiazek,
        CASE WHEN COUNT(DISTINCT egzemplarz_id) IS NULL THEN 0 ELSE COUNT(DISTINCT egzemplarz_id) END AS ilosc_egzemplarzy
    FROM ksiazka LEFT JOIN egzemplarz USING (ksiazka_id)
    GROUP BY autor_id
)
SELECT imie, nazwisko, ilosc_ksiazek, ilosc_egzemplarzy
FROM autor LEFT JOIN statystyki_ksiazkowe USING (autor_id)
WHERE autor_id = ?;

-- gatunki w ktorych pisze
SELECT nazwa, czestosc 
FROM czestosc_gatunkow_dla_ksiazek('SELECT ksiazka_id FROM ksiazka WHERE autor_id = ?') JOIN gatunek USING (gatunek_id)
ORDER BY czestosc DESC;

--==============================================--wydawnictwo--=====================================================--
-- ogolne dane, ile ksiazek wydawnictwa jest
WITH statystyki_ksiazkowe AS (
    SELECT wydawnictwo_id, COUNT(DISTINCT ksiazka_id) AS ilosc_ksiazek
    FROM ksiazka
    GROUP BY wydawnictwo_id
)
SELECT nazwa, data_zalozenia, ilosc_ksiazek
FROM wydawnictwo LEFT JOIN statystyki_ksiazkowe USING (wydawnictwo_id)
WHERE wydawnictwo_id = ?;

-- gatunki wydawane przez wydawnictwo
SELECT nazwa, czestosc 
FROM czestosc_gatunkow_dla_ksiazek('SELECT ksiazka_id FROM ksiazka WHERE wydawnictwo_id = ?') JOIN gatunek USING (gatunek_id)
ORDER BY czestosc DESC;

--==============================================--gatunek--=====================================================--
-- ogolne dane, drzewo gatunku wstecz
SELECT nazwa, gatunek_rodzic_id, znajdz_drzewo_gatunku_wstecz(gatunek_id) AS drzewo
FROM gatunek 
WHERE gatunek_id = ?;

-- lista bezposrednich dzieci gatunku
SELECT gatunek_id, nazwa
FROM gatunek 
WHERE gatunek_rodzic_id = ?;

--==============================================--wplata--=====================================================--
-- ogolne dane
SELECT data_wplaty, kwota, czytelnik_id, imie, nazwisko
FROM wplata JOIN czytelnik USING (czytelnik_id)
WHERE wplata_id = ?;

--==============================================--wypozyczenie--=====================================================--
-- ogolne dane
SELECT egzemplarz_id, tytul, czytelnik_id, imie, nazwisko, data_wypozyczenia, data_zwrotu
FROM wypozyczenie JOIN czytelnik USING (czytelnik_id) LEFT JOIN egzemplarz USING (egzemplarz_id) LEFT JOIN ksiazka USING (ksiazka_id)
WHERE wypozyczenie_id = ?;

--==============================================--egzemplarz--=====================================================--
-- ogolne dane
SELECT ksiazka_id, tytul
FROM egzemplarz JOIN ksiazka USING (ksiazka_id)
WHERE egzemplarz_id = ?;

--==============================================--gatunek_ksiazki--=====================================================--
-- ogolne dane
SELECT ksiazka_id, tytul, gatunek_id, nazwa, znajdz_drzewo_gatunku_wstecz(gatunek_id) AS drzewo
FROM gatunek_ksiazki JOIN ksiazka USING (ksiazka_id) JOIN gatunek USING (gatunek_id)
WHERE ksiazka_id = ? AND gatunek_id = ?;







------------------------------------------------------------- triggery
-- sprawdzanie Duzej i malych liter dla gatunkow
CREATE OR REPLACE FUNCTION normalizuj_gatunek()
RETURNS TRIGGER AS $$
    BEGIN
        NEW.nazwa := UPPER(SUBSTR(NEW.nazwa, 1, 1)) || LOWER(SUBSTR(NEW.nazwa, 2));
        RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER normalizacja_gatunek BEFORE INSERT OR UPDATE ON gatunek 
FOR EACH ROW EXECUTE PROCEDURE normalizuj_gatunek();

-- normalizacja imion i nazwisk autorow i czytelnikow
-- sprawdz czy maja tylko litery
CREATE OR REPLACE FUNCTION normalizuj_imie_nazwisko()
RETURNS TRIGGER AS $$
    BEGIN
        IF (NEW.imie !~ '^[[:alpha:]]+$' OR NEW.nazwisko !~ '^[[:alpha:]]+$') THEN
            RAISE EXCEPTION 'Imie i nazwisko mogą zawierać tylko litery.';
            RETURN NULL;
        END IF;

        NEW.imie := INITCAP(NEW.imie);
        NEW.nazwisko := INITCAP(NEW.nazwisko);
        RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER normalizacja_autor BEFORE INSERT OR UPDATE ON autor 
FOR EACH ROW EXECUTE PROCEDURE normalizuj_imie_nazwisko();

CREATE TRIGGER normalizacja_czytelnik BEFORE INSERT OR UPDATE ON czytelnik 
FOR EACH ROW EXECUTE PROCEDURE normalizuj_imie_nazwisko();

-- INSERT
-- nie mozna wypozyczyc null ksiazki
-- & blokuj wypozyczenie jezeli uzytkownik jest zadluzony albo zbanowany
-- & blokada wypozyczenia jezeli ksiazka jest niedostepna 
-- daty nie moga byc z przyszlosci
-- data_wypozyczenia musi byc nowsza niz data rejestracji uzytkownika i ostatnia data zwrotu 
-- data_zwrotu musi byc starsza niz kolejne wypozyczenie tej ksiazki
CREATE OR REPLACE FUNCTION sprawdz_nowe_wypozyczenie()
RETURNS TRIGGER AS $$
    BEGIN
        IF NEW.egzemplarz_id IS NULL THEN 
            RAISE EXCEPTION 'Pole egzemplarza nie może być puste.';

        ELSIF NEW.czytelnik_id IN (SELECT czytelnik_id FROM balans_czytelnika WHERE balans < 0) THEN 
            RAISE EXCEPTION 'Zadłużony użytkownik nie może wypożyczać książek.';

        ELSIF NEW.czytelnik_id IN (SELECT czytelnik_id FROM zbanowani_czytelnicy) THEN 
            RAISE EXCEPTION 'Zbanowany użytkownik nie może wypożyczać książek.';

        ELSIF NEW.data_wypozyczenia::DATE < (SELECT data_rejestracji FROM czytelnik WHERE czytelnik_id = NEW.czytelnik_id) THEN
            RAISE EXCEPTION 'Data wypożyczenia jest starsza niż data rejestracji użytkownika.';

        ELSIF NEW.data_wypozyczenia > NOW()::TIMESTAMP OR NEW.data_zwrotu > NOW()::TIMESTAMP THEN
            RAISE EXCEPTION 'Nie można wprowadzać przyszłych dat.';

        ELSIF EXISTS (
            SELECT 1 FROM wypozyczenie 
            WHERE egzemplarz_id = NEW.egzemplarz_id AND (
                (NEW.data_zwrotu IS NULL AND (data_zwrotu IS NULL OR data_zwrotu > NEW.data_wypozyczenia))
                OR
                (NEW.data_zwrotu IS NOT NULL AND ((data_zwrotu IS NULL OR data_zwrotu > NEW.data_wypozyczenia) AND (NEW.data_zwrotu > data_wypozyczenia)))
            ) 
        ) THEN
            RAISE EXCEPTION 'Wypożyczenie zahacza czasowo o inne wypożyczenie tego samego egzemplarza.';

        ELSE 
            RETURN NEW;
        END IF;

        RETURN NULL;
    END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER egzemplarz_nowe_wypozyczenie BEFORE INSERT ON wypozyczenie 
FOR EACH ROW EXECUTE PROCEDURE sprawdz_nowe_wypozyczenie();

-- UPDATE
-- daty nie moga byc z przyszlosci 
-- blokada wypozyczenia jezeli ksiazka jest niedostepna 
-- data_wypozyczenia musi byc nowsza niz data rejestracji uzytkownika i ostatnia data zwrotu 
-- data zwrotu musi byc starsza niz kolejne wypozyczenie tej ksiazki 
CREATE OR REPLACE FUNCTION sprawdz_edytowane_wypozyczenie()
RETURNS TRIGGER AS $$
    BEGIN
        IF NEW.data_wypozyczenia::DATE < (SELECT data_rejestracji FROM czytelnik WHERE czytelnik_id = NEW.czytelnik_id) THEN
            RAISE EXCEPTION 'Data wypożyczenia jest starsza niż data rejestracji użytkownika.';

        ELSIF NEW.data_wypozyczenia > NOW()::TIMESTAMP OR NEW.data_zwrotu > NOW()::TIMESTAMP THEN
            RAISE EXCEPTION 'Nie można wprowadzać przyszłych dat.';

        ELSIF EXISTS (
            SELECT 1 FROM wypozyczenie 
            WHERE egzemplarz_id = NEW.egzemplarz_id AND wypozyczenie_id != NEW.wypozyczenie_id AND (
                (NEW.data_zwrotu IS NULL AND (data_zwrotu IS NULL OR data_zwrotu > NEW.data_wypozyczenia))
                OR
                (NEW.data_zwrotu IS NOT NULL AND ((data_zwrotu IS NULL OR data_zwrotu > NEW.data_wypozyczenia) AND (NEW.data_zwrotu > data_wypozyczenia)))
            ) 
        ) THEN
            RAISE EXCEPTION 'Wypożyczenie zahacza czasowo o inne wypożyczenie tego samego egzemplarza.';

        ELSE 
            RETURN NEW;
        END IF;

        RETURN NULL;
    END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER egzemplarz_edytowane_wypozyczenie BEFORE UPDATE ON wypozyczenie 
FOR EACH ROW EXECUTE PROCEDURE sprawdz_edytowane_wypozyczenie();

-- check if email has one @ and nothing but .
CREATE OR REPLACE FUNCTION sprawdz_email()
RETURNS TRIGGER AS $$
    BEGIN
        IF (NEW.email !~ '^([[:alnum:]]|\.)+@([[:alpha:]]|\.)+$') THEN
            RAISE EXCEPTION 'Niepoprawny format emaila (może zawierać dokładnie jedno @ i kropki).';
            RETURN NULL;
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER poprawnosc_emaila BEFORE INSERT OR UPDATE ON czytelnik 
FOR EACH ROW EXECUTE PROCEDURE sprawdz_email();

-- check if telefon has only 9 digits
CREATE OR REPLACE FUNCTION sprawdz_telefon()
RETURNS TRIGGER AS $$
    BEGIN
        IF (NEW.telefon !~ '^[[:digit:]]+$' OR LENGTH(NEW.telefon) != 9) THEN
            RAISE EXCEPTION 'Niepoprawny numeru telefonu (potrzeba 9 cyfr bez innych znaków).';
            RETURN NULL;
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER poprawnosc_telefonu BEFORE INSERT OR UPDATE ON czytelnik 
FOR EACH ROW EXECUTE PROCEDURE sprawdz_telefon();

-- wplacana kwota musi byc wieksza od zera
CREATE OR REPLACE FUNCTION sprawdz_kwote()
RETURNS TRIGGER AS $$
    BEGIN
        IF (NEW.kwota <= 0) THEN
            RAISE EXCEPTION 'Kwota musi być większa od zera.';
            RETURN NULL;
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER kwota_wieksza_od_zera BEFORE INSERT OR UPDATE ON wplata 
FOR EACH ROW EXECUTE PROCEDURE sprawdz_kwote();






------------------------------------------------------------- inserty

------------------------------------------------------------- update'y
-- zwroc ksiazke
UPDATE wypozyczenie 
SET data_zwrotu = NOW()::TIMESTAMP
WHERE czytelnik_id = ? AND egzemplarz_id = ? AND data_zwrotu IS NULL;

------------------------------------------------------------- delete'y




-- wyswietl na profilu uzytkownika jego statystyki, balans, czy jest zbanowany, jakich ksiazek nie oddal i jakie przetrzymuje za dlugo
-- to samo z ksiazka i autorem i wydawnictwem i gatunkiem itp




























