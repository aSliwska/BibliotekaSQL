import random
from unidecode import unidecode

random.seed()

input = open("data_gen/czytelnik/imiona.txt", "r")
output = open("data_gen/czytelnik/imionaSQL.txt", "w")

femNames = input.readline().strip().split(";")
maleNames = input.readline().strip().split(";")
femSur = input.readline().strip().split(";")
maleSur = input.readline().strip().split(";")

for i in range(50):
    
    if (random.randint(0, 1) == 0): # girl
        name = random.choice(femNames)
        surname = random.choice(femSur)
    else: # boy
        name = random.choice(maleNames)
        surname = random.choice(maleSur)

    email = unidecode(name[0].lower()) + unidecode(surname.lower()) + str(random.randint(1, 99)) + "@qmail.com"
    telefon = ''.join([str(random.randint(0, 9)) for _ in range(9)])
    data_rejestracji = str(random.randint(2020, 2023)) + "-" + str(random.randint(1, 12)) + "-" + str(random.randint(1, 28))
    
    output.write("INSERT INTO czytelnik(imie, nazwisko, email, telefon, data_rejestracji) VALUES ('" + name + "', '" + surname + "', '" + email + "', '" + telefon + "', '" + data_rejestracji +"');\n")
