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
	public static void main(String[] args) throws IOException{
		// The FSM and reader
		String[] consumables;
		Integer[] states1;
		Integer[] states2;
		BufferedReader in;
		// Parsing the FSM goes here 
		try {
			Object[] fsm = parseFSM(new BufferedReader(new FileReader(args[0])));
			consumables = (String[])fsm[0];
			states1 = (Integer[])fsm[1];
			states2 = (Integer[])fsm[2];
			in = new BufferedReader(new FileReader(args[1]));
		} catch (Exception e){
			System.err.println("Setup Failed!");
			System.err.println(e.getMessage());
			e.printStackTrace();
			return;
		}
		// Unwieldy try catch for in.readLine()
		try {
			// String str = "This yam has abbs of steel.";
			String str = in.readLine();
			while (str != null){
				checkLine(consumables, states1, states2, str);
				// Get the next line
				str = in.readLine();
			}
		} catch (IOException e){
			System.err.println("Error while reading text.");
			System.err.println(e.getMessage());
			e.printStackTrace();
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

	private static void checkLine(String[] consumables, Integer[] states1, 
			Integer[] states2, String str){
		// Loop to search the string
		for (int i = 0; i < str.length(); i++){
			Dequeue<Integer> deque = new Dequeue<Integer>();
			// Null here is the scan
			deque.push(null);
			// Add first state to the deque
			deque.push(states1[0]);
			// Check the substring
			boolean sucess = checkSubstring(consumables, states1, states2, str, deque, i);
			// Output the satisfactory line
			if (sucess) {
				System.out.println(str);
				break;
			}
		}
	}

	private static boolean checkSubstring(String[] consumables, Integer[] states1,
			Integer[] states2, String str, Dequeue<Integer> deque, int i){
		for (int offset = 0; i + offset < str.length(); offset++){
			// I use a string instead of a char (or Character) as I'm skittish about using
			// String.equals(char)
			// This could likely be improved
			String character = str.substring(i+offset, i+offset+1);
			while (true){
				Integer state = deque.pop();
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
					return true;
				// If branching push states
				} else if (consumables[state].equals("")){
					if (states1[state].equals(consumables.length - 1) 
							|| states2[state].equals(consumables.length - 1)){
						return true;
					}
					deque.push(states1[state]);
					deque.push(states2[state]);
				// Other consumables go here (Like WILD)
				// If correct character unshift state
				} else if (consumables[state].equals(character)) {
					if (states1[state].equals(consumables.length - 1)) {
						return true;
					}
					deque.unshift(states1[state]);
				}
			}
			// If the deque is empty this substring has failed
			if (deque.isEmpty()){
				return false;
			}
		}
		// Ran out of characters, this substring has failed
		return false;
	}
}
