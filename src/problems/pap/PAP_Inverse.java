package problems.pap;

import java.io.IOException;

/**
 * Class representing the inverse of the Professor Allocation Problem
 * ({@link PAP}), which is used since the GRASP is set by
 * default as a minimization procedure.
 * 
 * @author vferrari
 */
public class PAP_Inverse extends PAP {
	
	/**
	 * Constructor for the PAP_Inverse class.
	 * 
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public PAP_Inverse(String filename) throws IOException {
		super(filename);
	}
	
	
	/* (non-Javadoc)
	 * @see problems.qbf.QBFPT#evaluate()
	 */
	@Override
	public Double evaluatePAP() {
		return -super.evaluatePAP();
	}
	
	/* (non-Javadoc)
	 * @see problems.qbf.QBFPT#evaluateInsertion(int)
	 */
	@Override
	public Double evaluateInsertionPAP(Integer[] i) {
		return -super.evaluateInsertionPAP(i);
	}
	
	/* (non-Javadoc)
	 * @see problems.qbf.QBFPT#evaluateRemoval(int)
	 */
	@Override
	public Double evaluateRemovalPAP(Integer[] i) {
		return -super.evaluateRemovalPAP(i);
	}
	
	/* (non-Javadoc)
	 * @see problems.qbf.QBFPT#evaluateExchange(int, int)
	 */
	@Override
	public Double evaluateExchangePAP(Integer[] in, Integer[] out) {
		return -super.evaluateExchangePAP(in,out);
	}
}
