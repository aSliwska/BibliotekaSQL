import random

random.seed()

output = open("data_gen/gatunek_ksiazki/gatunek_ksiazkiSQL.txt", "w")

for i in range(1, 51):
    ksiazka = str(i)
    gatunek = str(random.randint(2,31))
    output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES ("+ ksiazka +", "+ gatunek +");\n")

    gatunek2 = str(random.randint(2,31))
    if (random.randint(0,1) == 0 and gatunek2 != gatunek):
        output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES ("+ ksiazka +", "+ gatunek2 +");\n")

    gatunek3 = str(random.randint(2,31))
    if (random.randint(0,2) != 0 and gatunek3 != gatunek and gatunek2 != gatunek3):
        output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES ("+ ksiazka +", "+ gatunek3 +");\n")

output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (51, 33);\n")
output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (52, 33);\n")
output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (53, 34);\n")
output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (54, 36);\n")
output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (55, 37);\n")
output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (56, 38);\n")
output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (57, 39);\n")
output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (58, 42);\n")
output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (59, 42);\n")
output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (60, 41);\n")
output.write("INSERT INTO gatunek_ksiazki (ksiazka_id, gatunek_id) VALUES (61, 43);\n")