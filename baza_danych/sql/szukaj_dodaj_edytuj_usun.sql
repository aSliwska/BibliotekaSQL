------------------------------------------------------------- selecty
-- zapytanie budowane jest w kodzie aplikacji na podstawie wprowadzonych przez
-- uzytkownika danych do formy wyszukiwania. 

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

------------------------------------------------------------- inserty
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

------------------------------------------------------------- update'y
-- zwroc ksiazke
UPDATE wypozyczenie 
SET data_zwrotu = NOW()::TIMESTAMP
WHERE czytelnik_id = ? AND egzemplarz_id = ? AND data_zwrotu IS NULL;


-- kazdy update wywolywany przez forme do edytowania jest generowany na podstawie uzupelnionych przez uzytkownika pol
-- pola niezapelnione nie sa edytowane, kluczy glownych nie mozna edytowac.
-- przyklad edycji czytelnika o id = 50, kiedy zapelnione sa tylko pola email = 'izielinska56@qmail.com' i telefon = '123456789':
UPDATE czytelnik SET email = 'izielinska56@qmail.com', telefon = '123456789' WHERE czytelnik_id = 50;
-- i tym podobne dla reszty tabel

------------------------------------------------------------- delete'y
-- zapytanie delete generowane jest automatycznie przez program (zawsze szukanie po _id)
DELETE FROM czytelnik WHERE czytelnik_id = ?;
-- takie same dla reszty tabel