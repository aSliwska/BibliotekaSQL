------ raporty:
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


------ profile:

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