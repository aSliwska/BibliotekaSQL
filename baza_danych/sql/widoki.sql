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