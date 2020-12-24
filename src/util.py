
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
    
def read_solution(filename):
    sol = []
    
    # Read
    with open(filename, 'r') as fp:
        data = fp.readlines()
    
    data=data[1:]
    for lin in data:
        lin = lin[1:-2].split(",")
        sol.append(tuple(map(int, lin)))
        
    return sol

def sol_to_vars(inst, sol):
    x = np.zeros((inst['P'], inst['D']))
    z = np.zeros((inst['P'], inst['T']))
    
    for s in sol:
        p,d,t = s
        x[p][d] = 1
        z[p][t] = 1
    
    return x,z

def check_viability(inst, sol):
    x,z = sol_to_vars(inst, sol)
    is_ok = True

    # Restriction 2.2
    if not (x.sum(axis=0) <= 1).all():
        print("Professor per class violation! Infeasible.")
        print("Professors per class:")
        print(x.sum(axis=0))
        #return False
        is_ok = False
    
    # Restriction 2.3
    if not (z.sum(axis=0) <= inst['S']).all():
        print("Class violation! Infeasible.")
        print("Rooms available:", inst['S'])
        print("Classes per slot:")
        print(z.sum(axis=0))
        #return False
        is_ok = False

    # Restriction 2.4
    if not (z <= inst['r']).all():
        print("Availability violation! Infeasible.")
        where = np.where(z > inst['r'])
        print(list(zip(where[0], where[1])))
        #return False
        is_ok = False
    
    # Restriction 2.5
    if not (z.sum(axis=1) <= inst['H']).all():
        print("Workload violation! Infeasible.")
        print("Max workload:", inst['H'])
        print("Actual workload per prof:")
        print(z.sum(axis=1))
        #return False
        is_ok = False
    
    # Restriction 2.6
    # For convenience, h[d] becomes h[p][d], with h[*][d] being equal.
    inst['h'] = np.array([list(inst['h'])]*inst['P'])
    if not (z.sum(axis=1) == (inst['h'] * x).sum(axis=1)).all():
        print("Coupling violation! Infeasible.")
        print("Slots per professor:")
        print(z.sum(axis=1))
        
        print("Professor workload:")
        print((inst['h'] * x).sum(axis=1))
        #return False
        is_ok = False
    
    return is_ok

if __name__ == "__main__":
    
    # Valid argument
    assert len(argv) == 3, "Please insert both instance and solution files, in that order."
    assert exists(argv[1]), "Instance does not exist!"
    assert exists(argv[2]), "Solution does not exist!"
    
    PAP = read_instance(argv[1])
    sol = read_solution(argv[2])
    
    if check_viability(PAP, sol):
        print("Solution is feasible!")
