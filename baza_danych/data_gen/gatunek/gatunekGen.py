input = open("data_gen/gatunek/gatunki_hierarchia.txt", "r")
output = open("data_gen/gatunek/gatunkiSQL.txt", "w")

lastParent = [-1 for _ in range(6)] # lastParent[numberOfTabs] = last genre id that had this many tabs in front of it
id = 1

for line in input:
    tabNumber = line.count('\t')
    lastParent[tabNumber] = id

    gatunek_rodzic_id = "NULL" if (tabNumber == 0) else str(lastParent[tabNumber-1])
    nazwa = line.strip()
    output.write("INSERT INTO gatunek (gatunek_rodzic_id, nazwa) VALUES (" + gatunek_rodzic_id + ", '" + nazwa + "');\n")

    id += 1


