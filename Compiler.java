/**
 * This class implements a regular expression compiler using the
 * following rules:
 *
 * E -> T
 * E -> TE
 * T -> F
 * T -> F*
 * T -> F+
 * T -> F?
 * T -> F|T
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
		char literal;
		int next1;
		int next2;

		public Node(char lit, int n1, int n2) {
			literal = lit;
			next1 = n1;
			next2 = n2;
		}

		public String toString() {
			return String.format("%c,%d,%d", literal, next1, next2);
		}
	}

	private String exp;
	private int index;
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
		index = 0;

		try {
			expression();
		} catch (StringIndexOutOfBoundsException e) {
			error();
		}

		if (index != exp.length())
			error();
	}

	private void expression() throws IllegalArgumentException {
		System.out.print("E ");

		term();

		// Test if we've reached the end of the pattern
		if (index == exp.length())
			return;

		// Recursive if a valid character follows
		if (isVocab(exp.charAt(index)) ||
			exp.charAt(index) == '\\' ||
			exp.charAt(index) == '(' ||
			exp.charAt(index) == '.' ||
			exp.charAt(index) == '[' ||
			exp.charAt(index) == '|')
			expression();
	}

	private void term() throws IllegalArgumentException {
		System.out.print("T ");

		factor();

		// Test if we've reached the end of the pattern
		if (index == exp.length())
			return;

		// Zero or more
		if (exp.charAt(index) == '*') {
			System.out.println("Consuming *");
			index++;
		}

		// One or more
		else if (exp.charAt(index) == '+') {
			System.out.println("Consuming +");
			index++;
		}

		// Zero or one
		else if (exp.charAt(index) == '?') {
			System.out.println("Consuming ?");
			index++;
		}

		// Disjuction
		else if (exp.charAt(index) == '|') {
			System.out.println("Consuming |");
			index++;

			term();
		}
	}

	private void factor() throws IllegalArgumentException {
		System.out.print("F ");
		// Escaped characters
		if (exp.charAt(index) == '\\') {
			System.out.println("Consuming \\");
			index++;
			System.out.printf("Literal %c\n", exp.charAt(index));
			index++;
		}

		// Literals
		else if (isVocab(exp.charAt(index))) {
			System.out.printf("Literal %c\n", exp.charAt(index));
			index++;
		}

		// Any literal
		else if (exp.charAt(index) == '.') {
			System.out.println("Consuming .");
			index++;
		}

		// List of literals
		else if (exp.charAt(index) == '[') {
			System.out.println("Consuming [");
			index++;
			if (exp.charAt(index) == ']') {
				System.out.println("Literal ]");
				index++;
			}
			list();
			if (exp.charAt(index) == ']') {
				System.out.println("Consuming ]");
				index++;
			} else
				error();
		}

		// Nested expression
		else if (exp.charAt(index) == '(') {
			System.out.println("Consuming (");
			index++;
			expression();
			if (exp.charAt(index) == ')') {
				System.out.println("Consuming )");
				index++;
			} else
				error();
		}

		// This isn't a factor!
		else
			error();
	}

	private void list() throws IllegalArgumentException {
		System.out.print("L ");

		if (exp.charAt(index) != ']') {
			System.out.printf("Literal %c\n", exp.charAt(index));
			index++;
			list();
		}
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
			System.err.println("This program requires a regex as an argument!");
			return;
		}

		Compiler c = new Compiler();
		c.setExpression(args[0]);
		try {
			c.compile();
		} catch (IllegalArgumentException e) {
			System.out.println();
			System.err.println(e.getMessage());
		}

		//fsm = c.getFSM();
		//for (Node n : fsm) {
		//	System.out.println(n.toString());
		//}
	}
}
