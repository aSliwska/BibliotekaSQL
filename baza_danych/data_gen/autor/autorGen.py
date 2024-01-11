input = open("data_gen/autor/autorzy.txt", "r")
output = open("data_gen/autor/autorzySQL.txt", "w")

femNames = input.readline().strip().split(";")
maleNames = input.readline().strip().split(";")
femSur = input.readline().strip().split(";")
maleSur = input.readline().strip().split(";")

for i in range(7):
    output.write("INSERT INTO autor (imie, nazwisko) VALUES ('"+femNames[i] + "', '" +femSur[i]+"');\n")
    output.write("INSERT INTO autor (imie, nazwisko) VALUES ('"+maleNames[i] + "', '" +maleSur[i]+"');\n")