import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
		// Parsing the fsm goes here
		// Null here is the branching state
		String[] consumables = new String[]{null, "a", "b", null, "c", null};
		int[] states1 = new int[]{1, 3, 5, 2, 5, -1};
		int[] states2 = new int[]{-1, -1, -1, 4, -1, -1};

		// The loop that tries all strings goes here
		String str = "This yam has abbs of steel.";
		
		// Loop to search the string
		boolean sucess = false;
		for (int i = 0; i < str.length(); i++){
			Dequeue<Integer> deque = new Dequeue<Integer>();
			// Null here is the scan
			deque.push(null);
			// Add first state to the deque
			deque.push(states1[0]);
			for (int offset = 0; i + offset < str.length(); offset++){
				// I use a string instead of a char (or Character) as I'm skittish about using
				// String.equals(char)
				// This could likely be improved
				String character = str.substring(i+offset, i+offset+1);
				while (true){
					Integer state = deque.pop();
					System.out.println(state);
					// If scan (null) move scan, increment offset and continue
					if (state == null) {
						// Leave the deque empty if the scan was the only item left
						// This signals the upper loop to break
						if (!deque.isEmpty()){
							deque.unshift(null);
						}
						break;
					// Check if sucess 
					// Now only handles the case where the begining state is sucess
					} else if (state.equals(consumables.length - 1)){
						sucess = true;
						break;
					// If branching push states
					} else if (consumables[state] == null){
						if (states1[state] == consumables.length - 1 
								|| states2[state] == consumables.length - 1){
							sucess = true;
							break;
						}
						deque.push(states1[state]);
						deque.push(states2[state]);
					// Other consumables go here (Like WILD)
					// If correct character unshift state
					} else if (consumables[state].equals(character)) {
						if (states1[state] == consumables.length - 1) {
							sucess = true;
							break;
						}
						System.out.println("Found "+character);
						deque.unshift(states1[state]);
					}
				}
				// Victory
				if (sucess) {
					break;
				// If the deque is empty this substring has failed
				} else if (deque.isEmpty()){
					break;
				}
			}
			// Output the satisfactory line
			if (sucess) {
				System.out.println(str);
				break;
			}
		}
	}
	
	/*
	 * Parses a file and creates a FSM
	 * Returns an Object array where 
	 * 	[0] is the literals (String[])
	 * 	[1] is the first states (Integer[])
	 * 	[2] is the second states (Integer[])
	 * Unfortunately I've been using a lot of ruby recently.
	 */
	private static Object[] parseFSM(BufferedReader in) throws NumberFormatException, IOException{
		ArrayList<String> consumables = new ArrayList<String>();
		ArrayList<Integer> states1 = new ArrayList<Integer>();
		ArrayList<Integer> states2 = new ArrayList<Integer>();
		String line = in.readLine();
		while (line != null){
			// Parse literal
			consumables.add(line);
			line = in.readLine();
			// Check for failure
			if (line == null){
				throw new RuntimeException("The FSM is malformed! Lines%3=1!");
			}
			// Parse first state
			states1.add(Integer.parseInt(line));
			line = in.readLine();
			// Check for failure
			if (line == null){
				throw new RuntimeException("The FSM is malformed! Lines%3=2!");
			}
			// Parse second state
			states2.add(Integer.parseInt(line));
			line = in.readLine();
		}
		// 'ArrayList.toArray(T[] a) => T[]' looks like a hack to me.
		// Admittedly returning an Object array is also a hack.
		return new Object[]{consumables.toArray(new String[0]), 
					states1.toArray(new Integer[0]), states2.toArray(new Integer[0])};
	}
}
