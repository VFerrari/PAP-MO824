# %%
import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy
import pandas
import seaborn as sn

logplot = True

# %%
T = pandas.read_csv("objectiveFunction.csv")
np = len(T)
ns = len(T.columns)

# %% Minimal performance per solver.

minperf = numpy.zeros(np)

T = T.to_numpy()
    
for k in range(np):
    T[k, :] = 1.05*max(T[k]) - T[k,:]
    minperf[k] = min(T[k])

# %% Compute ratios and divide by smallest element in each row.

r = numpy.zeros((np, ns))

vec  = numpy.vectorize(lambda x: max(0,x))
for k in range(np):
    r[k, :] = vec(T[k, :] / minperf[k])


if logplot:
    r = numpy.log2(r, where=(r != 0))

max_ratio = r.max()

for k in range(ns):
    r[:, k] = numpy.sort(r[ :, k])


# %% Plot stair graphs with markers.

n = []

for k in range(1, np + 1):
    n.append(k / np)

# %%
plt.figure(figsize = (8, 5))
plt.plot(r[:, 0], n, color = '#3CB371', linewidth = 1, label = 'GUROBI')
plt.plot(r[:, 1], n, color = '#FFD700', linewidth = 1, label = 'GUROBI_ALT')
plt.plot(r[:, 2], n, color = '#4B0082', linewidth = 1, label = 'GRASP')
plt.plot(r[:, 3], n, color = '#FF6347', linewidth = 1, label = 'GRASP+LINEAR')
plt.plot(r[:, 4], n, color = '#4169E1', linewidth = 1, label = 'GRASP+LINEAR+ALPHA2')
plt.plot(r[:, 5], n, color = '#094951', linewidth = 1, label = 'GRASP+LINEAR+ALPHA3')
plt.ylabel('Probabilidade (%)')
plt.xlabel('$log_2(r)$')
sn.despine(left = True, bottom = True)
plt.grid(True, axis = 'x')
plt.grid(True, axis = 'y')
plt.legend()
plt.savefig('objectiveFunctionLog.eps', dpi = 1500, transparent = True, bbox_inches = 'tight')
plt.show()
# %%



