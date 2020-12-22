package problems.pap.solvers;

import metaheuristics.grasp.AbstractGRASP;
import problems.pap.PAP_Inverse;
import solutions.Solution;
import problems.pap.PAP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.DoubleStream;

public class GRASP_PAP extends AbstractGRASP<Integer[]> {
	
	/**
	 * Bias Function enum.
	 * Provides bias functions for each enum value.
	 */
	private enum BiasFunction {
		RANDOM{
			@Override double bias(final Integer i) {
				return 1;
			}
		},
		LINEAR{
			@Override double bias(final Integer i) {
				return 1/(float)i;
			}
		},
		LOG{
			@Override double bias(final Integer i) {
				return 1/(Math.log(i+1));
			}
		},
		EXP{
			@Override double bias(final Integer i) {
				return Math.pow(Math.E, -i);
			}
		},
		POL{
			@Override double bias(final Integer i) {
				return Math.pow(i, -2);
			}
		};
		
		abstract double bias(final Integer i);
	}
	
	/**
	 * Value to represent bias function.
	 * Can be random, linear, logarithmic, exponential and polynomial.
	 * Default grasp uses RANDOM bias. 
	 */
	private final BiasFunction bF;
	
	/**
	 * Constructor for the GRASP_PAP class. An inverse PAP objective function is
	 * passed as argument for the superclass constructor.
	 * 
	 * @param alpha
	 *            The GRASP greediness-randomness parameter (within the range
	 *            [0,1])
	 * @param iterations
	 *            The number of iterations which the GRASP will be executed.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @param constructionType
	 * 				Type of construction to be used.
	 * @param  rpgP
	 * 				Number of iterations needed to change from random
	 * 				to greedy when using the random plus greedy construction.
	 * @throws IOException
	 *             necessary for I/O operations.
	 */
	public GRASP_PAP(Double alpha, 
					 Integer iterations, 
					 String filename,
					 BiasFunction bF,
					 Construction constructionType,
					 int rpgP) throws IOException {
		super(new PAP_Inverse(filename), alpha, iterations, constructionType, rpgP);
		this.bF = bF;
	} 
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#makeCL()
	 */
	@Override
	public ArrayList<Integer[]> makeCL() {
		int p=0, d=0, t=0; 
		
		ArrayList<Integer[]> _CL = new ArrayList<Integer[]>();
		for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
			Integer[] cand = {p,d,t};
			
			if (((PAP)ObjFunction).r[p][t]) {
				_CL.add(cand);
			}
		}

		return _CL;

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#makeRCL()
	 */
	@Override
	public ArrayList<Integer[]> makeRCL() {
		return new ArrayList<Integer[]>();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#updateCL()
	 */
	@Override
	public void updateCL() {
		for (Integer[] c : CL){
			if(((PAP)this.ObjFunction).isFeasible(c)){
				RCL.add(c);
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * -100*D cost, since there are D classes with penalty of -100 when not allocated.
	 */
	@Override
	public Solution<Integer[]> createEmptySol() {
		Solution<Integer[]> sol = new Solution<Integer[]>();
		sol.cost = -100.0*((PAP)ObjFunction).D;
		return sol;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * The local search operator developed for the QBFPT objective function is
	 * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
	 */
	@Override
	public Solution<Integer[]> localSearch(){
		return localSearchPAP();
	}
	
	/**
	 * Best-Improving local search.
	 */
	private Solution<Integer[]> localSearchPAP() {

		Double minDeltaCost;
		Integer[] bestCandIn = null, bestCandOut = null;
		double deltaCost;

		do {
			minDeltaCost = Double.POSITIVE_INFINITY;
			updateCL();
				
			// Evaluate insertions
			for (Integer[] candIn : RCL) {
				deltaCost = ObjFunction.evaluateInsertionCost(candIn, currentSol);
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = candIn;
					bestCandOut = null;
				}
			}
			// Evaluate removals
			for (Integer[] candOut : currentSol) {
				deltaCost = ObjFunction.evaluateRemovalCost(candOut, currentSol);
				if (deltaCost < minDeltaCost) {
					minDeltaCost = deltaCost;
					bestCandIn = null;
					bestCandOut = candOut;
				}
			}
			// Evaluate exchanges
			for (Integer[] candIn : CL) {
				for (Integer[] candOut : currentSol) {
					deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, currentSol);
					if (deltaCost < minDeltaCost) {
						minDeltaCost = deltaCost;
						bestCandIn = candIn;
						bestCandOut = candOut;
					}
				}
			}
			// Implement the best move, if it reduces the solution cost.
			if (minDeltaCost < -Double.MIN_VALUE) {
				if (bestCandOut != null) {
					currentSol.remove(bestCandOut);
					CL.add(bestCandOut);
				}
				if (bestCandIn != null) {
					currentSol.add(bestCandIn);
					CL.remove(bestCandIn);
					RCL.remove(bestCandIn);
				}
				ObjFunction.evaluate(currentSol);
				RCL.clear();
			}
		} while (minDeltaCost < -Double.MIN_VALUE);

		return null;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * The QBFPT random choice follows a bias function.
	 * Get the bias for each value, calculate probability, and choose element.
	 */
	@Override
	public Integer[] chooseRandom(){
		double probs[] = new double[RCL.size()];
		double totalBias = 0;
		int i;
		
		// Rank (sort) RCL
		RCL.sort(Comparator.comparingDouble(x -> ObjFunction.evaluateInsertionCost(x, currentSol)));
		
		// Get bias.
		for(i=0; i<RCL.size(); i++) {
			probs[i] = bF.bias(i+1);
			totalBias += probs[i];
		}
		
		// Calculate probabilities.
		final double biasSum = totalBias;
		probs = DoubleStream.of(probs).map(p->p/biasSum).toArray();
		
		// Get random value from weighted probs.
		int rndIndex=0;
		double rndValue = rng.nextDouble();
		for(i=0; i<RCL.size(); i++) {
			if(rndValue < probs[i]) {
				rndIndex = i;
				break;
			}
			rndValue -= probs[i];
		}
		
		return RCL.get(rndIndex);
	}
	
	/**
	 * Run GRASP for QBFPT.
	 */
	public static void run(double alpha, int maxIt, String filename,
						   BiasFunction biasType, Construction constrMethod, 
						   int rpgP, double maxTime) 
					   throws IOException {
		
		long startTime = System.currentTimeMillis();
		GRASP_PAP grasp = new GRASP_PAP(alpha,
										maxIt,
										filename, 
										biasType,
										constrMethod,
										rpgP);
		
		Solution<Integer[]> bestSol = grasp.solve(maxTime);
		System.out.println("maxVal = " + bestSol);

		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = "+(double)totalTime/(double)1000+" seg");		
	}
	
	public static void testAll(double alpha, int maxIt, 
							   BiasFunction biasType,
							   AbstractGRASP.Construction constrMethod, 
							   int rpgP, double maxTime) 
					   throws IOException {
		
        String[] inst = {"P50D50S1.pap", "P50D50S3.pap", "P50D50S5.pap", "P70D70S1.pap", "P70D70S3.pap", "P70D70S5.pap", "P70D100S6.pap", "P70D100S8.pap", "P70D100S10.pap", "P100D150S10.pap", "P100D150S15.pap", "P100D150S20.pap"};
		
		for(String file : inst) {
			GRASP_PAP.run(alpha, maxIt, file, biasType, 
						  constrMethod, rpgP, maxTime);
		}
	}
	
	/**
	 * A main method used for testing the GRASP metaheuristic.
	 */
	public static void main(String[] args) throws IOException {
		
		// Fixed parameters
		double maxTime = 1800.0;
		int maxIterations = 1000;
		int rpgP = 2;
		
		// Changeable parameters.
		double alpha1 = 0.25, alpha2 = 0.7;
		
		// 1 - Testing default/alpha1/random bias.
		GRASP_PAP.testAll(alpha1, maxIterations, 
						  BiasFunction.RANDOM,
						  AbstractGRASP.Construction.DEF, 
						  rpgP, maxTime);	
	}
}
