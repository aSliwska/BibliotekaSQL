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
    CONSTRAINT gatunek_gatunek_rodzic_id_fk FOREIGN KEY(gatunek_rodzic_id) REFERENCES gatunek(gatunek_id) ON DELETE SET NULL
);

CREATE TABLE gatunek_ksiazki (
    ksiazka_id INTEGER NOT NULL,
    gatunek_id INTEGER NOT NULL,
    CONSTRAINT gatunek_ksiazki_pk PRIMARY KEY(ksiazka_id, gatunek_id),
    CONSTRAINT gatunek_ksiazki_ksiazka_id_fk FOREIGN KEY(ksiazka_id) REFERENCES ksiazka(ksiazka_id) ON DELETE CASCADE,
    CONSTRAINT gatunek_ksiazki_gatunek_id_fk FOREIGN KEY(gatunek_id) REFERENCES gatunek(gatunek_id) ON DELETE CASCADE
);