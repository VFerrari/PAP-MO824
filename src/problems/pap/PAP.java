package problems.pap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.Arrays;

import problems.Evaluator;
import solutions.Solution;

/**
 * The Professor Allocation Problem (PAP) consists of allocating classes
 * in time slots, and professors to such classes.
 * 
 * @author vferrari
 *
 */
public class PAP implements Evaluator<Integer[]> {
	/**
	 * Dimensions of the domain.
	 */
	public final Integer P, D, T;
	
	/**
	 * Constants of the problem
	 */
	public final Integer S, H;
	
	/**
	 * The workload of each class
	 */
	public Integer[] h;

	/**
	 * The rating of each professor on each subject.
	 */
	public Integer[][] A;
	
	/**
	 * The availability of each professor on each time slot. 
	 */
	public Boolean[][] r;
	
	/**
	 * The array of numbers representing the problem variables.
	 */
	public final Integer[][] variables;
	
	/**
	 * Array of classes per time slot
	 */
	public Integer[] timeSlots;
	
	/**
	 * Array of workloads for each professor.
	 */
	public Integer[] profWorkload;
	
	/**
	 * Array of time slots for each professor.
	 */
	public Integer[][] profSlots;
	
	public PAP(String filename) throws IOException {
		Integer[] vals = readInput(filename);
		P = vals[0];
		D = vals[1];
		T = vals[2];
		S = vals[3];
		H = vals[4];
		variables = allocateVariables();
		resetStatus();
	}
		
	/**
	 * Updates time slots and professor workloads: insertion.
	 */
	public void updateStatusAdd(Integer[] inCand) {
		int p = inCand[0];
		int d = inCand[1];
		int t = 0;
		
		// Time Slots
		for (int i=0; i<h[d]; i++) {
			for(; (timeSlots[t] >= S || !r[p][t] || profSlots[p][t] >= 0) && t < T; t++);
			assert(t < T): "Infeasible! No time slot to insert.";
			timeSlots[t]++;
			profSlots[p][t] = d;
		}
		
		// Professor
		profWorkload[p] += h[d];
	}
	
	/**
	 * Updates time slots and professor workloads: removal.
	 */
	public void updateStatusRm(Integer[] outCand) {
		int p = outCand[0];
		int d = outCand[1];
		int t = 0;
		
		// Time Slots
		for (t=0; t<T; t++) {
			if (profSlots[p][t] == d) {
				timeSlots[t]--;
				profSlots[p][t] = -1;
			}
		}
		
		// Professor
		profWorkload[p] -= h[d];
	}
	
	/**
	 * Checks if the (professor, class) set is feasible (checks ILP restrictions).
	 * @param cand (professor, class) set
	 * @return true if feasible, false otherwise.
	 */
	public boolean isFeasible(Integer[] cand) {
		boolean feasible = true;
		int p = cand[0];
		int d = cand[1];
		int t, sum;
		
		// Check if in solution.
		if(variables[p][d] == 1) {
			feasible = false;
		}
		
		// Check if workload handles class.
		else if(profWorkload[p] + h[d] > H) {
			feasible = false;
		}
		
		// Check if there are enough available time slots for the element.
		else {
			sum = 0;
			for(t=0; t<T; t++) {
				sum += (timeSlots[t] < S && r[p][t]) ? 1:0;
			}
			
			feasible = (sum >= h[d]);
			
		}
		
		return feasible;
	}
	
	/**
	 * Evaluates the value of a solution by transforming it into a vector. .
	 * 
	 * @param sol
	 *            the solution which will be evaluated.
	 */
	public void setVariables(Solution<Integer[]> sol) {

		resetVariables();
		if (!sol.isEmpty()) {
			for (Integer[] elem : sol) {
				variables[elem[0]][elem[1]] = 1;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#getDomainSize()
	 */
	@Override
	public Integer getDomainSize() {
		return P*D;
	}
	
	/**
	 * {@inheritDoc} In the case of a PAP, the evaluation correspond to
	 * computing the objective function, adding the ratings of the allocated
	 * professors on their respective classes, with a penalty for each class
	 * that is not allocated.  A better way to evaluate this
	 * function when at most two variables are modified is given by methods
	 * {@link #evaluateInsertionPAP(int[])}, {@link #evaluateRemovalPAP(int[])} 
	 * and {@link #evaluateExchangePAP(int[],int[])}.
	 * 
	 * @return The evaluation of the PAP.
	 */
	@Override
	public Double evaluate(Solution<Integer[]> sol) {

		setVariables(sol);
		return sol.cost = evaluatePAP();

	}
	
	/**
	 * Evaluates a PAP by calculating the objective function.
	 * \sum(p,d) ApdXpd - 100(1-\sum(p)Xpd)
	 * 
	 * @return The value of the PAP.
	 */
	public Double evaluatePAP() {
		double sum = 0, aux=0;

		for (int j = 0; j < D; j++) {
			aux = 0;
			for (int i = 0; i < P; i++) {
				aux +=  A[i][j] * variables[i][j];
			}
			sum += (aux == 0) ? (-100):aux;
		}

		return (double) sum;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateInsertionCost(java.lang.Object,
	 * solutions.Solution)
	 */
	@Override
	public Double evaluateInsertionCost(Integer[] elem, Solution<Integer[]> sol) {

		setVariables(sol);
		return evaluateInsertionPAP(elem);

	}
	
	/**
	 * Determines the contribution to the PAP objective function from the
	 * insertion of an element.
	 * 
	 * @param i
	 *            Element being inserted into the solution.
	 * @return The variation of the objective function resulting from the
	 *         insertion.
	 */
	public Double evaluateInsertionPAP(Integer[] i) {
		int d = i[1];
		
		// If there is already a professor in this class, return lowest possible value.
		for(int p=0; p<P; p++)
			if (variables[p][d] == 1)
				return 0.0;

		return evaluateContributionPAP(i);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateInsertionCost(java.lang.Object,
	 * solutions.Solution)
	 */
	@Override
	public Double evaluateRemovalCost(Integer[] elem, Solution<Integer[]> sol) {

		setVariables(sol);
		return evaluateRemovalPAP(elem);

	}
	
	/**
	 * Determines the contribution to the PAP objective function from the
	 * removal of an element.
	 * 
	 * @param i
	 *            Element being removed from the solution.
	 * @return The variation of the objective function resulting from the
	 *         insertion.
	 */
	public Double evaluateRemovalPAP(Integer[] i) {

		if (variables[i[0]][i[1]] == 0)
			return 0.0;

		return -evaluateContributionPAP(i);

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see problems.Evaluator#evaluateExchangeCost(java.lang.Object,
	 * java.lang.Object, solutions.Solution)
	 */
	@Override
	public Double evaluateExchangeCost(Integer[] elemIn, Integer[] elemOut, Solution<Integer[]> sol) {

		setVariables(sol);
		return evaluateExchangePAP(elemIn, elemOut);

	}
	
	/**
	 * Determines the contribution to the PAP objective function from the
	 * exchange of two elements one belonging to the solution and the other not.
	 * 
	 * @param in
	 *            The index of the element that is considered entering the
	 *            solution.
	 * @param out
	 *            The index of the element that is considered exiting the
	 *            solution.
	 * @return The variation of the objective function resulting from the
	 *         exchange.
	 */
	public Double evaluateExchangePAP(Integer[] in, Integer[] out) {

		if (in[0] == out[0] && in[1] == out[1])
			return 0.0;
		if (variables[in[0]][in[1]] == 1)
			return evaluateRemovalPAP(out);
		if (variables[out[0]][out[1]] == 0)
			return evaluateInsertionPAP(in);

		return evaluateContributionPAP(in) - evaluateContributionPAP(out);
	}
	
	/**
	 * Determines the contribution to the PAP objective function from the
	 * insertion of an element. This method is faster than evaluating the whole
	 * solution, since it just considers one class, and one professor. This 
	 * method is different from {@link #evaluateInsertionPAP(int[])}, since it 
	 * disregards the fact that the element might already be in the solution.
	 * 
	 * @param i
	 *            Element for contribution calculation.
	 * @return the variation of the objective function resulting from the
	 *         insertion.
	 */
	private Double evaluateContributionPAP(Integer[] i) {
		return 100.0 + A[i[0]][i[1]];
	}
	
	/**
	 * Responsible for setting the PAP function parameters by reading the
	 * necessary input from an external file. This method reads the dimensions
	 * of the problem variables, the individual constants, the matrix {@link A},
	 * the subject workload and professor availiability.
	 * 
	 * @param filename
	 *            Name of the file containing the input for setting the black
	 *            box function.
	 * @return The problem constants and dimensions.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	protected Integer[] readInput(String filename) throws IOException {

		Reader fileInst = new BufferedReader(new FileReader(filename));
		StreamTokenizer stok = new StreamTokenizer(fileInst);
		
		Integer problemValues[] = new Integer[5];
		
		for(int i=0; i<5; i++) {
			stok.nextToken();
			stok.nextToken();
			problemValues[i] = (int)stok.nval;
		}
		final int P = problemValues[0];
		final int D = problemValues[1];
		final int T = problemValues[2];
		
		// Workload
		stok.nextToken();
		h = new Integer[D];
		for (int i = 0; i < D; i++) {
			stok.nextToken();
			h[i] = (int) stok.nval;
		}
		
		// Ratings
		stok.nextToken();
		A = new Integer[P][D];
		for (int i = 0; i < P; i++) {
			for (int j = 0; j < D; j++) {
				stok.nextToken();
				A[i][j] = (int) stok.nval;
			}
		}
		
		// Availiability
		stok.nextToken();
		r = new Boolean[P][T];
		for (int i = 0; i < P; i++) {
			for (int j = 0; j < T; j++) {
				stok.nextToken();
				r[i][j] = ((int) stok.nval) == 1;
			}
		}

		return problemValues;
	}
	
	/**
	 * Reserving the required memory for storing the values of the domain
	 * variables.
	 * 
	 * @return a pointer to the array of domain variables.
	 */
	protected Integer[][] allocateVariables() {
		Integer[][] _variables = new Integer[P][D];
		return _variables;
	}

	/**
	 * Reset the domain variables to their default values.
	 */
	public void resetVariables() {
		for(Integer [] row : variables) {
			Arrays.fill(row, 0);
		}
	}
	
	/**
	 * Resets status to 0
	 */
	public void resetStatus() {
		
		// Fill time slots
		timeSlots = new Integer[T];
		Arrays.fill(timeSlots, 0);
		
		// Fill professor slots
		profSlots = new Integer[P][T];
		for(Integer[] row : profSlots) {
			Arrays.fill(row, -1);
		}
		
		// Fill professor workload
		profWorkload = new Integer[P];
		Arrays.fill(profWorkload, 0);
	}

	
	/**
	 * Prints matrix {@link A}.
	 */
	public void printMatrix() {

		for (int i = 0; i < P; i++) {
			for (int j = 0; j < D; j++) {
				System.out.print(A[i][j] + "\t");
			}
			System.out.println();
		}

	}
}
