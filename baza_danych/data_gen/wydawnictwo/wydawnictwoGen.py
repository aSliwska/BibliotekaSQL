import random

random.seed()

input = open("data_gen/wydawnictwo/wydaw.txt", "r")
output = open("data_gen/wydawnictwo/wydawSQL.txt", "w")

for line in input:
    name = line.strip()
    data_zalozenia = str(random.randint(1950, 2015)) + "-" + str(random.randint(1, 12)) + "-" + str(random.randint(1, 28))
    output.write("INSERT INTO wydawnictwo (nazwa, data_zalozenia) VALUES ('"+name+"', '"+data_zalozenia+"');\n")