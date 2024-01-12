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