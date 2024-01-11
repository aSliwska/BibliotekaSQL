import random
from datetime import timedelta
from datetime import datetime

def random_date(start, end):
    delta = end - start
    int_delta = (delta.days * 24 * 60 * 60) + delta.seconds
    random_second = random.randrange(int_delta)
    return start + timedelta(seconds=random_second)

random.seed()

input = open("data_gen/czytelnik/imionaSQL.txt", "r")
output = open("data_gen/wypozyczenie/wypozyczenieSQL.txt", "w")

daty_rejestracji = [datetime(2000, 1, 1) for _ in range(51)]
j = 1
for line in input:
    daty_rejestracji[j] = datetime.strptime(line.split("'")[9] + " 08:00:00", '%Y-%m-%d %H:%M:%S')
    j += 1

for i in range(1, 166):
    egzemplarz_id = str(i)
    ilosc_wypozyczen = random.randint(0, 3)

    newest = datetime(2000, 1, 1) 
    
    for k in range(ilosc_wypozyczen):
        czytelnik = random.randint(1, 50)
        newest = newest if(newest > daty_rejestracji[czytelnik]) else daty_rejestracji[czytelnik]
        data_wypozyczenia = random_date(newest, datetime.now()) # musi byc nowsza niz data rejestracji uzytkownika i ostatnia data zwrotu
        data_zwrotu = "NULL" if (k == ilosc_wypozyczen-1 and random.randint(0, 1) == 0) else random_date(data_wypozyczenia, datetime.now()) # musi byc nowsza niz data wypozyczenia, starsza niz dzisiejsza (inaczej null)
        # wszystkie dluzsze niz miesiac musza byc pozniej oplacone - populate with sql function?
        newest = data_zwrotu
        data_zwrotu = "'" + str(data_zwrotu) + "'" if (data_zwrotu != "NULL") else data_zwrotu
        output.write("INSERT INTO wypozyczenie (egzemplarz_id, czytelnik_id, data_wypozyczenia, data_zwrotu) VALUES ("+ egzemplarz_id + ", " + str(czytelnik) + ", '" + str(data_wypozyczenia) + "', " + data_zwrotu +");\n") 