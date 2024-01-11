import random

random.seed()

tytuly = open("data_gen/ksiazka/tytul.txt", "r")
opisy = open("data_gen/ksiazka/opisy.txt", "r")
output = open("data_gen/ksiazka/ksiazkaSQL.txt", "w")

for line in tytuly:
    tytul = line.strip()
    opis = opisy.readline().strip()
    autor_id = random.randint(1,14)
    wydawnictwo_id = random.randint(1, 5)

    opis = "'"+opis+"'" if (opis != '') else "NULL"

    output.write("INSERT INTO ksiazka (tytul, opis, autor_id, wydawnictwo_id) VALUES ('"+ tytul +"', "+ opis +", "+ str(autor_id) +", "+ str(wydawnictwo_id) +");\n")

