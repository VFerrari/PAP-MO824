package solutions;

@SuppressWarnings("serial")
public class PAP_Solution extends Solution<Integer[]> {
	public int len = 2;
	public Integer[][] slots;
	
	public PAP_Solution() {
		super();
	}
	
	public PAP_Solution(Solution<Integer[]> sol) {
		super(sol);
	}
	
	/**
	 * Add slots to the solution.
	 * @param T Amount of slots available.
	 */
	public void addSlots(Integer T) {
		PAP_Solution copy = new PAP_Solution(this);
		this.clear();
		len = 3;
		
		for(Integer [] e :copy) {
			for(int t=0; t<T; t++) {
				if (slots[e[0]][t] == e[1]) {
					Integer [] newE = {e[0],e[1],t};
					this.add(newE);
				}
			}
		}
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
