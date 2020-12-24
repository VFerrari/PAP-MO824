package solutions;

@SuppressWarnings("serial")
public class PAP_Solution extends Solution<Integer[]> {
	public PAP_Solution() {
		super();
	}
	
	public PAP_Solution(Solution<Integer[]> sol) {
		super(sol);
	}
	
	@Override
	public String toString() {
		String out = "Solution: cost=[" + cost + "], size=[" + this.size() + "], elements=[";
		for(Integer[] e : this) {
			out += " (" + e[0] + "," + e[1] + "),";
		}
		out += "]";
		return out;
	}
}
