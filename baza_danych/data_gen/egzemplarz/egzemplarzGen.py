import random

random.seed()

output = open("data_gen/egzemplarz/egzemplarzSQL.txt", "w")

for ksiazka_id in range(1, 61):
    for _ in range(random.randint(1, 5)):
        output.write("INSERT INTO egzemplarz (ksiazka_id) VALUES ("+ str(ksiazka_id) +");\n")