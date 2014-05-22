/*
 * I got sick of my bash script not working, so I've come up with
 * this program to test the compiler class.  The expressions.txt file
 * should now be a CSV file with (expression,valid) where expression
 * is the expression to test, and valid is true if it should parse and
 * compile as valid regex.
 *
 * Dan Collins 2014
 * 1183446
 */
import java.nio.charset.*;
import java.nio.file.*;
import java.io.*;
import java.util.ArrayList;

class CompilerTest {
	public static void main(String[] args) {
		Path p;
		BufferedReader r;
		ArrayList<String> l;
		
		String inLine;
		String[] data;
		String exp;
		boolean valid, pass;

		Compiler c;

		if (args.length < 1) {
			System.err.println("Invalid input arguments!");
			return;
		}

		p = Paths.get(args[0]).toAbsolutePath();

		try {
			r = Files.newBufferedReader(p,
										Charset.forName("US-ASCII"));
		} catch (IOException e) {
			System.err.println("Failed to open file.");
			return;
		}

		l = new ArrayList<String>();
		c = new Compiler();

		try {
			while ((inLine = r.readLine()) != null) {
				data = inLine.split(",");
				if (data.length != 2)
					continue;

				exp = data[0];
				valid = data[1].equals("true") ? true : false;

				c.setExpression(exp);

				try {
					c.compile();
					pass = true;
				} catch (IllegalArgumentException e) {
					pass = false;
				}

				if (pass != valid)
					l.add(exp);
			}
		} catch (IOException e) {
			System.err.println("Error reading line from file.");
		}

		System.out.println("Failed tests:");
		for (String s : l) {
			System.out.println(s);
		}
	}
}