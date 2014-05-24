/**
 * This class implements a regular expression compiler using the
 * following rules:
 *
 * E -> D
 * E -> DE
 * D -> T
 * D -> T|D
 * T -> F
 * T -> F*
 * T -> F+
 * T -> F?
 * F -> \v
 * F -> v
 * F -> .
 * F -> []L]
 * F -> [L]
 * F -> (E)
 * L -> v
 * L -> vL
 *
 * Where E is an expression, T is a term, F is a factor, L is a list
 * and v is a vocab item (a literal).
 *
 * This program will accept a regex from the command line, and then
 * output the compiled FSM to stdout.
 *
 * Dan Collins 1183446
 * Severin Mahoney Marsh 1181754
 */
import java.util.ArrayList;

public class Compiler {
	class Node {
		String literal;
		int next1;
		int next2;

		public Node(String lit, int n1, int n2) {
			literal = lit;
			next1 = n1;
			next2 = n2;
		}

		public void setNext1(int n1) {
			next1 = n1;
		}

		public void setNext2(int n2) {
			next2 = n2;
		}

		public int getNext1() {
			return next1;
		}

		public int getNext2() {
			return next2;
		}

		public String toString() {
			return String.format("%s\n%d\n%d", literal, next1, next2);
			//return String.format("%s,%d,%d", literal, next1, next2);
		}
	}

	private String exp;
	private int index;
	private int state;
	private ArrayList<Node> fsm;

	public static char[] SPECIAL_CHARS = {'\\', '*', '+', '?', '|',
										  '.', '(', ')', '[', ']'}; 

	public Compiler() {
		fsm = new ArrayList<Node>();
	}

	public void setExpression(String exp) {
		this.exp = exp;
	}

	public void compile() throws IllegalArgumentException {
		int initial;

		index = 0;
		state = 1;

		try {
			// Create a placeholder state for the initial state.
			// We'll update it later.
			setState(0, "NULL", 0, 0);
			initial = expression();
			setState(0, initial, initial);
		} catch (StringIndexOutOfBoundsException e) {
			error();
		}

		if (index != exp.length())
			error();

		// Point back to the start of the FSM
		setState(state, "NULL", 0, 0);
	}

	private int expression() throws IllegalArgumentException {
		int r;

		r = disjunction();

		// Test if we've reached the end of the pattern
		if (index == exp.length())
			return r;

		// Recursive if a valid character follows
		if (isVocab(exp.charAt(index)) ||
			exp.charAt(index) == '\\' ||
			exp.charAt(index) == '(' ||
			exp.charAt(index) == '.' ||
			exp.charAt(index) == '[')
			r = expression();

		return r;
	}

	private int disjunction() throws IllegalArgumentException {
		int r, s1, e1, s2, e2;
		
		// Get start and end states for the term
		s1 = term();
		e1 = state-1;
		r = s1;

		// Test if we've reached the end of the pattern
		if (index == exp.length())
			return r;

		// Recurse if this is a disjunction
		if (exp.charAt(index) == '|') {
			// Consume |
			index++;

			// Create a branching machine to point to the start of
			// the term, and the start of the disjunction
			setState(state, "NULL", s1, state+1);
			r = state;
			state++;

			// Get the start and end states for the disjunction
			s2 = disjunction();
			e2 = state-1;

			// Update the branching machine to point to the start of
			// the term and the new disjunction
			setState(r, s1, s2);

			// Create an end state for this machine
			setState(state, "NULL", state+1, state+1);
			state++;

			// Point both terms to the end state
			setState(e1, state-1, state-1);
			setState(e2, state-1, state-1);
		}

		// Return the start state of this machine
		return r;
	}

	private int term() throws IllegalArgumentException {
		int r, start;

		// Save the state pointing to this one
		start = state-1;

		r = factor();

		// Test if we've reached the end of the pattern, and if we
		// have return the start state for the factor machine
		if (index == exp.length())
			return r;

		// Zero or more
		if (exp.charAt(index) == '*') {
			// Create a branching state that points to the factor
			// and the state after the factor.
			setState(state, "NULL", r, state+1);
			r = state;
			state++;

			// Consume *
			index++;
		}

		// One or more
		else if (exp.charAt(index) == '+') {
			// Create a branching state that points to the factor,
			// and the state after the factor
			setState(state, "NULL", r, state+1);
			state++;
			
			// Consume +
			index++;
		}

		// Zero or one
		else if (exp.charAt(index) == '?') {
			// Create a branching state that points to the factor,
			// and the state after the factor
			setState(state, "NULL", r, state+1);
			r = state;
			state++;

			// Create an end state for this machine
			setState(state, "NULL", state+1, state+1);
			state++;

			// Point the factor to the end state
			setState(r, state-1, state-1);
			r = state-1;

			index++;
		}

		// Return the start state of this machine
		return r;
	}

	private int factor() throws IllegalArgumentException {
		int r, l;

		r = state;

		// Escaped characters
		if (exp.charAt(index) == '\\') {
			// Consume \
			index++;

			// Update FSM to contain character
			setState(state, String.valueOf(exp.charAt(index)),
					 state+1, state+1);
			state++;

			// Consume escaped literal
			index++;
		}

		// Literals
		else if (isVocab(exp.charAt(index))) {
			// Update FSM to contain character
			setState(state, String.valueOf(exp.charAt(index)),
					 state+1, state+1);
			state++;

			// Consume literal
			index++;
		}

		// Any literal
		else if (exp.charAt(index) == '.') {
			// Update FSM to contain character
			setState(state, "WILD", state+1, state+1);
			state++;

			// Consume wild card
			index++;
		}

		// List of literals
		else if (exp.charAt(index) == '[') {
			// Consume [
			index++;

			// If the first character is ], then it should be in the
			// literal list.  The easiest way is to either have a null
			// state pointing to the start of the list, or having the
			// consuming ']' state pointing to the start of the list.
			// Is is a waste of a state..!
			if (exp.charAt(index) == ']') {
				// Consume ]
				index++;

				// Create a state to consume the ]
				setState(state, "]", state+1, state+1);
				r = state;
				state++;
			}

			// Figure out where the list starts (and also build the
			// rest of the list FSM).
			r = list(state+1);

			// Make sure the list was valid
			if (exp.charAt(index) == ']') {
				// Consume ]
				index++;
			} else
				error();
		}

		// Nested expression
		else if (exp.charAt(index) == '(') {
			index++;
			r = expression();
			if (exp.charAt(index) == ')') {
				index++;
			} else
				error();
		}

		// This isn't a factor!
		else
			error();

		// Return the start state of this machine
		return r;
	}

	private int list(int entry) throws IllegalArgumentException {
		int r, e1, s2;

		// If this list is done, we just want to point to the entry
		// point of the previous list
		r = entry;
		
		// The end state for the previous list
		e1 = state-1;

		if (exp.charAt(index) != ']') {
			// Make sure there is a character available.  It is an
			// error for there not to be!
			if (index == exp.length())
				error();

			// Create a branching machine to point to the start of the
			// previous list and the start of the new list
			setState(state, "NULL", entry, state+1);
			r = state;
			state++;

			// Create a state to consume the literal
			setState(state, String.valueOf(exp.charAt(index)),
					 state+1, state+1);
			s2 = state;
			state++;
			index++;

			// Create an end state for this machine
			setState(state, "NULL", state+1, state+1);
			state++;

			// Point the exit state of the previous machine to the
			// end state
			setState(e1, state-1, state-1);

			// Try to recurse.  This will just return the value we
			// pass if there is no further list
			r = list(r);
		}

		return r;
	}

	private void setState(int state, String s, int n1, int n2) {
		Node n = new Node(s, n1, n2);
		
		// State will replace an existing state
		if (state < fsm.size()) {
			try {
				fsm.remove(state);
				fsm.add(state, n);
			} catch (IndexOutOfBoundsException e) {
				System.err.println("This shouldn't happen..!");
				e.printStackTrace();
				System.exit(-1);
			}
		}

		// State is a new state
		else if (state == fsm.size()) {
			fsm.add(n);
		}

		// This is invalid
		else {
			System.err.println("State value is too large for list!");
			System.err.printf("state: %d, fsm.size(): %d\n", state,
							  fsm.size());
			System.exit(-1);
		}
	}

	private void setState(int state, int n1, int n2) {
		Node n = fsm.remove(state);

		// If both new links point to the same place, we handle it
		// a bit differently.
		if (n1 == n2) {
			// If the links are the same, then set both
			if (n.getNext1() == n.getNext2()) {
				n.setNext1(n1);
				n.setNext2(n1);
			}

			// Otherwise, just set the second link
			else
				n.setNext2(n1);
		} else {
			n.setNext1(n1);
			n.setNext2(n2);
		}

		fsm.add(state, n);
	}

	private boolean isVocab(char c) {
		for (char s : SPECIAL_CHARS) {
			if (s == c)
				return false;
		}
		
		return true;
	}

	private void error() throws IllegalArgumentException {
		String message = String.format("%s is an invalid regex!",
									   exp);
		throw new IllegalArgumentException(message);
	}

	public ArrayList<Node> getFSM() {
		return this.fsm;
	}

	public static void main(String args[]) {
		ArrayList<Node> fsm;

		if (args.length < 1) {
			System.err.println("This program requires a regex as an" +
							   "argument!");
			return;
		}

		Compiler c = new Compiler();
		c.setExpression(args[0]);
		try {
			c.compile();
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		fsm = c.getFSM();
		for (int i = 0; i < fsm.size(); i++) {
			//System.out.printf("%d: %s\n", i, fsm.get(i));
			System.out.printf("%s\n", fsm.get(i));
		}
	}
}
