/**
 * Accepts arg[0] a path to a Finite State Machine
 * Accepts arg[1] a path to a text
 * 		java Searcher FSM text
 * Uses the FSM to search the text for matches
 * Outputs each matching line once even if multiple matches exist on a line
 * Output is standard out
 *
 * Dan Collins 1183446
 * Severin Mahoney-Marsh 1181754
 */
public class Searcher {
	public static void main(String[] args){
		// Null here is the branching state
		Character[] consumables = new Character[]{null, 'a', 'b', null, 'c', null};
		int[] states1 = new int[]{1, 3, 5, 2, 5, -1};
		int[] states2 = new int[]{-1, -1, -1, 4, -1, -1};
		String str = "This yam has abbs of steel.";
		
		// Loop to search the string
		boolean sucess = false;
		for (int i = 0; i < str.length(); i++){
			Dequeue<Integer> deque = new Dequeue<Integer>();
			// Null here is the scan
			deque.push(null);
			// Add first state to the deque
			deque.push(states1[0]);
			int offset = 0;
			for (int offset = 0; i + offset < str.length(); offset++){
				Character c = str.charAt(i+offset);
				// Check if sucess
				// If branching push states
				// If correct character unshift state
				// If scan (null) move scan, increment offset and continue
			}
		}
	}
}
