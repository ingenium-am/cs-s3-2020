i = input("Set number: ")           # print prompt and get input
n = int(i)                          # parse input as an int
arr = [n for n in range(n + 1)]     # make array of ints
print("Nums:", arr)                 # print Nums (as array)
print("Sum:", sum(arr))             # print Sum
