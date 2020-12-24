package solutions;

@SuppressWarnings("serial")
public class PAP_Solution extends Solution<Integer[]> {
	public int len = 2;
	
	public PAP_Solution() {
		super();
	}
	
	public PAP_Solution(Solution<Integer[]> sol) {
		super(sol);
	}
	
	@Override
	public String toString() {
		String out = "Solution: cost=[" + cost + "], size=[" + this.size() + "], elements=[";
		
		// Add elements
		for(Integer[] e : this) {
			out += " (" + e[0];
			for (int i=1; i<len; i++) {
				out += "," + e[i];
			}
			out += "),";
		}
		out += "]";
		return out;
	}
}
