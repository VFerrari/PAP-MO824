
from sys import argv
from os.path import exists
import numpy as np

# Example for reading instance.
def read_instance(filename):    
    PAP = {}
    
    # Read
    with open(filename, 'r') as fp:
        data = fp.readlines()
        
    # Scalars
    P = PAP['P'] = int(data[0][2:-1])
    D = PAP['D'] = int(data[1][2:-1])
    PAP['T'] = int(data[2][2:-1]) # NOTE: unnecessary, always 20
    PAP['S'] = int(data[3][2:-1])
    PAP['H'] = int(data[4][2:-1]) # NOTE: unnecessary, always 3
    
    # Advance
    data = data[6:]
    
    # Array
    h = np.empty(D, np.uint8)
    for i in range(D):
        h[i] = int(data[i][:-1])
    PAP['h'] = h
    
    # Advance
    data = data[D+1:]
    
    # 2D arrays
    a = data[:P]
    for i in range(P):
        a[i] = a[i][:-1].split("\t")
    PAP['a'] = np.array(a, np.uint8)
    
    # Advance and finish
    r = data[P+1:]
    for i in range(P):
        r[i] = r[i][:-1].split("\t")
    PAP['r'] = np.array(r, np.uint8)
    
    return PAP
    

if __name__ == "__main__":
    
    # Valid argument
    assert len(argv) == 2, "No instance to be read!"
    assert exists(argv[1]), "Instance does not exist!"
    
    PAP = read_instance(argv[1])
    
    print(f"P: {PAP['P']}")
    print(f"D: {PAP['D']}")
    print(f"T: {PAP['T']}")
    print(f"S: {PAP['S']}")
    print(f"H: {PAP['H']}")
    
    print(f"h: {PAP['h']}")
    
    print(f"a: {PAP['a']}")
    print(f"r: {PAP['r']}")
