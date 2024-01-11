import random
from datetime import timedelta
from datetime import datetime
import math

def random_date(start, end):
    delta = end - start
    int_delta = (delta.days * 24 * 60 * 60) + delta.seconds
    random_second = random.randrange(int_delta)
    return start + timedelta(seconds=random_second)

random.seed()

input = open("data_gen/wypozyczenie/wypozyczenieSQL.txt", "r")
output = open("data_gen/wplata/wplataSQL.txt", "w")

for line in input:
    if( line.split(" ")[12] != "NULL);\n"):
        data_zwrotu = datetime.strptime(line.split("'")[3], '%Y-%m-%d %H:%M:%S')
        data_wypozyczenia = datetime.strptime(line.split("'")[1], '%Y-%m-%d %H:%M:%S')
        czytelnik_id = line.split(" ")[9].strip(",")

        data_wplaty = random_date(data_zwrotu, data_zwrotu + timedelta(days=10))
        delta = data_zwrotu - data_wypozyczenia
        if (delta.days > 30):
            kwota = (delta.days - 30) * 1.00 + random.random() * 10 # 1zl per day + additional money for calculating balance in sql later
            if (random.randint(0,5) != 0): #chance they haven't payed: 1/6
                output.write("INSERT INTO wplata (data_wplaty, kwota, czytelnik_id) VALUES ('"+ str(data_wplaty) + "', " + f'{kwota:.2f}' + ", " + czytelnik_id +");\n")
