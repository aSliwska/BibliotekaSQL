------------------------------------------------------------- select

------ formularze:
-- zapytanie budowane jest w kodzie aplikacji na podstawie wprowadzonych przez uzytkownika danych do formy wyszukiwania

-- czytelnicy - przykladowe dla wypełnionych pól imie='%a', nazwisko='%a': 
SELECT c.czytelnik_id, c.imie, c.nazwisko, c.email, c.telefon, c.data_rejestracji, 
    bc.balans, cs.ilosc_aktualnie_wypozyczonych, cs.ilosc_aktualnie_po_terminie, sb.czy_jest_zbanowany 
FROM czytelnik c LEFT JOIN balans_czytelnika bc USING (czytelnik_id) 
    LEFT JOIN czytelnik_statystyki cs USING (czytelnik_id) LEFT JOIN status_bana sb USING (czytelnik_id) 
WHERE c.imie LIKE '%a' AND c.nazwisko LIKE '%a' 
GROUP BY c.czytelnik_id, c.imie, c.nazwisko, c.email, c.telefon, c.data_rejestracji, 
    bc.balans, cs.ilosc_aktualnie_wypozyczonych, cs.ilosc_aktualnie_po_terminie, sb.czy_jest_zbanowany 
ORDER BY c.czytelnik_id;

-- wplaty: dla nazwisko czytelnika='Kowalski'
SELECT w.wplata_id, w.data_wplaty, w.kwota, w.czytelnik_id 
FROM wplata w JOIN czytelnik c USING (czytelnik_id) 
WHERE c.nazwisko LIKE 'Kowalski' 
ORDER BY w.wplata_id;

-- wypozyczenia: dla id egzemplarza=1, nazwisko czytelnika='Kowalska'
SELECT w.wypozyczenie_id, w.egzemplarz_id, w.czytelnik_id, w.data_wypozyczenia, w.data_zwrotu, sw.dni_nad_limitem 
FROM wypozyczenie w JOIN czytelnik c USING (czytelnik_id) LEFT JOIN status_wypozyczenia sw USING (wypozyczenie_id) 
WHERE w.egzemplarz_id = 1 AND c.nazwisko LIKE 'Kowalska' 
GROUP BY w.wypozyczenie_id, w.egzemplarz_id, w.czytelnik_id, w.data_wypozyczenia, w.data_zwrotu, sw.dni_nad_limitem 
ORDER BY w.wypozyczenie_id;

-- egzemplarze: dla tytul ksiazki = '%ra%'
SELECT e.egzemplarz_id, e.ksiazka_id 
FROM egzemplarz e JOIN ksiazka k USING (ksiazka_id) 
WHERE k.tytul LIKE '%ra%' 
ORDER BY e.egzemplarz_id;


-- ksiazki: dla nazwa wydawnictwa = 'Miłość Czytania', nazwisko autora = '%i'
SELECT k.ksiazka_id, k.tytul, k.opis, k.wydawnictwo_id, k.autor_id, ks.ilosc_egzemplarzy_w_systemie, ks.ilosc_dostepnych_egzemplarzy 
FROM ksiazka k JOIN autor a USING (autor_id) JOIN wydawnictwo w USING (wydawnictwo_id) LEFT JOIN egzemplarz e USING (ksiazka_id) 
    LEFT JOIN ksiazka_statystyki ks USING (ksiazka_id) 
WHERE w.nazwa LIKE 'Miłość Czytania' AND a.nazwisko LIKE '%i' GROUP BY k.ksiazka_id, k.tytul, k.opis, k.wydawnictwo_id, k.autor_id, 
    ks.ilosc_egzemplarzy_w_systemie, ks.ilosc_dostepnych_egzemplarzy 
ORDER BY k.ksiazka_id;

-- autorzy: dla imie = 'Władysława'
SELECT a.autor_id, a.imie, a.nazwisko 
FROM autor a 
WHERE a.imie LIKE 'Władysława' 
ORDER BY a.autor_id;

-- wydawnictwa: bez wartości wprowadzonych
SELECT w.wydawnictwo_id, w.nazwa, w.data_zalozenia 
FROM wydawnictwo w 
ORDER BY w.wydawnictwo_id;

-- gatunki: dla id = 3
SELECT g.gatunek_id, g.nazwa 
FROM gatunek g 
WHERE g.gatunek_id = 3 
ORDER BY g.gatunek_id;

-- gatunek_ksiazki: dla tytul ksiazki = 'Jutro'
SELECT gk.ksiazka_id, gk.gatunek_id 
FROM gatunek_ksiazki gk JOIN ksiazka k USING (ksiazka_id) JOIN gatunek g USING (gatunek_id) 
WHERE k.tytul LIKE 'Jutro' 
ORDER BY gk.ksiazka_id;

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







------------------------------------------------------------- insert
-- czytelnik
INSERT INTO czytelnik (imie, nazwisko, email, telefon, data_rejestracji) VALUES (?, ?, ?, ?, NOW()::DATE)

-- egzemplarze - zapytanie wywolywane tyle razy ile uzytkownik sprecyzuje w polu "Ilosc egzemplarzy"
INSERT INTO egzemplarz (ksiazka_id) VALUES (?);

-- ksiazka
INSERT INTO ksiazka (tytul, opis, wydawnictwo_id, autor_id) VALUES (?, ?, ?, ?);

-- autor
INSERT INTO autor (imie, nazwisko) VALUES (?, ?);

-- wydawnictwo
INSERT INTO wydawnictwo (nazwa, data_zalozenia) VALUES (?, ?);

-- gatunek
INSERT INTO gatunek (gatunek_rodzic_id, nazwa) VALUES (?, ?);

-- gatunek_ksiazki
INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (?, ?);

-- wypozyczenie
INSERT INTO wypozyczenie (egzemplarz_id, czytelnik_id, data_wypozyczenia, data_zwrotu) VALUES (?, ?, NOW()::TIMESTAMP, NULL);

-- wplata
INSERT INTO wplata (czytelnik_id, kwota, data_wplaty) VALUES (?, ?, NOW()::TIMESTAMP);






------------------------------------------------------------- update
-- zwroc ksiazke
UPDATE wypozyczenie 
SET data_zwrotu = NOW()::TIMESTAMP
WHERE czytelnik_id = ? AND egzemplarz_id = ? AND data_zwrotu IS NULL;


-- kazdy update wywolywany przez forme do edytowania jest generowany na podstawie uzupelnionych przez uzytkownika pol
-- pola niezapelnione nie sa edytowane, kluczy glownych nie mozna edytowac.
-- przyklad edycji czytelnika o id = 50, kiedy zapelnione sa tylko pola email = 'izielinska56@qmail.com' i telefon = '123456789':
UPDATE czytelnik SET email = 'izielinska56@qmail.com', telefon = '123456789' WHERE czytelnik_id = 50;
-- i tym podobne dla reszty tabel






------------------------------------------------------------- delete
-- zapytanie delete generowane jest automatycznie przez program (zawsze szukanie po _id)
DELETE FROM czytelnik WHERE czytelnik_id = ?;
-- takie same dla reszty tabel