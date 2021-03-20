//
// EvoLudo Project
//
// Copyright 2010 Christoph Hauert
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// For publications in any form, you are kindly requested to attribute the
// author and project as follows:
//
//	Hauert, Christoph (<year>) EvoLudo Project, http://www.evoludo.org
//			(doi: <doi>[, <version>])
//
//	<doi>:	digital object identifier of the downloaded release (or the
//			most recent release if downloaded from github.com),
//	<year>:	year of release (or download), and
//	[, <version>]: optional version number (as reported in output header
//			or GUI console) to simplify replication of reported results.
//
// The formatting may be adjusted to comply with publisher requirements.
//

package org.evoludo.util;

import java.awt.Color;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Parser for command line options. Inspired by UNIX style command line options.
 * 
 * @author Christoph Hauert
 */
public class CLOParser {

	/**
	 * List of command line options available (after parsing).
	 * 
	 * @see #parseCLO(String[])
	 */
	List<CLOption> options = new ArrayList<CLOption>();

	/**
	 * List of providers of command line options.
	 * 
	 * @see CLOProvider
	 */
	Set<CLOProvider> providers;

	/**
	 * Logger for reporting errors, warnings or other information (optional).
	 * Without a <code>logger</code> parsing is quite (same as with logging level
	 * {@link java.util.logging.Level#OFF Level.OFF}).
	 * 
	 * @see Logger
	 */
	protected Logger logger;

	/**
	 * All output should be printed to <code>output</code> (defaults to
	 * <code>stdout</code>). This is only relevant for JRE applications (mainly
	 * simulations) and ignored by GWT.
	 */
	protected PrintStream output = System.out;

	/**
	 * New command line option parser. Register <code>provider</code> for supplying
	 * options through the {@link CLOParser} interface.
	 * 
	 * @param provider of command line options
	 */
	public CLOParser(CLOProvider provider) {
		this(new HashSet<CLOProvider>(Collections.singleton(provider)));
	}

	/**
	 * New command line option parser. Register the list of <code>providers</code>
	 * for supplying options through the {@link CLOParser} interface.
	 * 
	 * @param providers list of command line option providers
	 */
	public CLOParser(Set<CLOProvider> providers) {
		this.providers = providers;
	}

	/**
	 * Add <code>provider</code> to the list of <code>providers</code>.
	 * 
	 * @param provider to add to list of command line option providers
	 * @return <code>true</code> if provider added to set, <code>false</code> if
	 *         provider already exists or is null
	 */
	public boolean addCLOProvider(CLOProvider provider) {
		if (provider == null)
			return false;
		return providers.add(provider);
	}

	/**
	 * Remove <code>provider</code> from the list of <code>providers</code>.
	 *
	 * @param provider to remove from list of command line option providers
	 * @return <code>true</code> if provider removed from set, <code>false</code> if
	 *         provider did not exist or is null
	 */
	public boolean removeCLOProvider(CLOProvider provider) {
		if (provider == null)
			return false;
		return providers.remove(provider);
	}

	/**
	 * Clears current collection of <code>options</code>.
	 */
	public void clearCLO() {
		options.clear();
	}

	/**
	 * Initializes parser. Clears current collection of <code>options</code> and
	 * creates new list by contacting all registered <code>providers</code>. Options
	 * are stored in alphabetical order in the List <code>options</code>.
	 * 
	 * @see #clearCLO()
	 * @see #updateCLO()
	 * @see CLOProvider
	 */
	public void initCLO() {
		clearCLO();
		updateCLO();
	}

	/**
	 * Updates current collection of <code>options</code> by contacting all
	 * registered <code>providers</code>. Options are stored in alphabetical order
	 * in the List <code>options</code>.
	 * 
	 * @see CLOProvider
	 */
	public void updateCLO() {
		for (CLOProvider provider : providers)
			provider.collectCLO(this);
		// sort options
		Collections.sort(options);
	}

	/**
	 * Reset current collection of <code>options</code> to their respective
	 * defaults.
	 * 
	 * @see CLOProvider
	 */
	public void resetCLO() {
		for (CLOption option : options)
			option.reset();
	}

	/**
	 * Parses String array of command line arguments in two stages. In the first
	 * stage the array is checked for arguments to short options as well as
	 * (optional) arguments to long options separated by '='. In both cases the
	 * option and argument are split into separate entries. In the second stage the
	 * current list of <code>options</code> is consulted and if the name of an
	 * option matches parsed accordingly. For unknown options a warning is logged
	 * and the option is ignored.
	 * <p>
	 * <strong>Note:</strong> if an entry starts with a single '-' and parses as a
	 * number, vector or array, it is assumed to be just that. Otherwise it is
	 * assumed to be a short option and checked whether an argument follows.
	 * </p>
	 * 
	 * @param cloargs array of String with command line options
	 * @return <code>true</code> if parsing successful and <code>false</code> if
	 *         problems occurred
	 */
	public boolean parseCLO(String[] cloargs) {
		boolean success = true;
		// prepare options: process short options and split their (potential) argument
		// check if (optional) arguments are separated with '=' and split if needed
		ArrayList<String> clos = new ArrayList<String>();
		for (String arg : Arrays.asList(cloargs)) {
			if (arg.charAt(0) != '-' || (arg.charAt(0) == '-' && arg.length() == 1)) {
				// arg is argument, not an option
				// note: a single '-' is a valid option for --geometry (strong suppressor)
				clos.add(arg);
				continue;
			}
			if (arg.charAt(1) != '-') {
				// short option, number, array or matrix; if parses as matrix, assume it is one
				// note: parseMatrix returns null if problems parsing arg arise
				if (CLOParser.parseMatrix(arg) != null) {
					clos.add(arg);
					continue;
				}
				// doesn't look like a number; treat as short option
				clos.add(arg.substring(0, 2));
				// no argument or separated by whitespace (already taken care of by split)
				if (arg.length() == 2)
					continue;
				// rest is argument potentially separated by '='
				if (arg.charAt(2) == '=')
					clos.add(arg.substring(3));
				else
					clos.add(arg.substring(2));
				continue;
			}
			// long option
			int eqls = arg.indexOf('=');
			if (eqls >= 0) {
				clos.add(arg.substring(0, eqls));
				clos.add(arg.substring(eqls + 1));
			} else
				clos.add(arg);
		}
		// parse options
		ListIterator<String> parse = clos.listIterator();
		initnext: while (parse.hasNext()) {
			String optionName = parse.next();
			for (CLOption option : options) {
				switch (option.processOption(optionName, parse)) {
					case 2: // short option found but parsing of concatenated argument failed
						logWarning("parsing argument '" + optionName + "' failed - option removed.");
						parse.remove();
						continue initnext;
					case 1: // option found but parsing of argument failed
						if (!parse.hasNext()) {
							logWarning("argument for " + optionName + " missing - option removed.");
							parse.previous();
							parse.remove();
							continue initnext;
						}
						String arg = parse.next();
						parse.previous();
						if (arg.startsWith("--")) {
							logWarning("argument for " + optionName + " missing - option removed.");
							parse.previous();
							parse.remove();
							continue initnext;
						}
						logWarning("parsing argument '" + arg + "' for " + optionName + " failed - option removed.");
						parse.remove();
						parse.previous();
						parse.remove();
						continue initnext;
					case 0: // success
					default:
						continue initnext;
					case -1: // no match
				}
			}
			logWarning("option " + optionName + " unknown - ignored.");
			success = false;
		}
		// apply all options (including default values)
		// note: options sorted alphabetically; not processed in order provided on
		// command line
		for (CLOption option : options) {
			try {
				if (!option.parse()) {
					// parsing failed - try again using default
					success = false;
					option.parseDefault();
				}
			} catch (Exception e) {
				logWarning("option --" + option.getName() + " failed to parse argument '" + option.getArg() + "'.");
				success = false;
			}
		}
		return success;
	}

	/**
	 * Get the current list of command line <code>options</code>.
	 * 
	 * @return list of command line <code>options</code>
	 */
	public List<CLOption> getCLOptions() {
		return options;
	}

	/**
	 * Return String containing all currently set command line options. Command line
	 * arguments that had been set using short options are converted to their long
	 * counterpart and all arguments are separated by ' '.
	 * <p>
	 * <strong>Note:</strong> using {@link String#split(String)} the returned string
	 * is ready to be parsed again {@link #parseCLO(String[])}.
	 * </p>
	 * 
	 * @return all current command line options formatted as String
	 */
	public String getCLO() {
		Iterator<CLOption> i = options.iterator();
		if (!i.hasNext())
			return "";
		StringBuilder cmd = new StringBuilder();
		while (i.hasNext()) {
			CLOption clo = i.next();
			if (!clo.isSet())
				continue;
			cmd.append(" --");
			cmd.append(clo.getName());
			switch (clo.getType()) {
				case NONE:
					continue;
				case OPTIONAL:
					if (!clo.isSet())
						break;
					cmd.append(" " + clo.getArg());
					continue;
				case REQUIRED:
					cmd.append(" " + clo.getArg());
					continue;

			}
		}
		return cmd.toString();
	}

	public void setOutput(PrintStream output) {
		this.output = output;
	}

	/**
	 * Reports the current setting of every command line option in the
	 * <code>options</code> list.
	 * 
	 * @see CLOption#report()
	 */
	public void dumpCLO() {
		// dump options
		for (CLOption clo : options)
			clo.report(output);
	}

	/**
	 * Returns a short description of every command line option in the
	 * <code>options</code> list, including its default value as well as the current
	 * setting (if different). This string typically serves as a quick help and
	 * reminder of the different command line options available.
	 * 
	 * @return help for command line options
	 * @see CLOption#getDescription()
	 */
	public String helpCLO() {
		StringBuilder help = new StringBuilder();
		for (CLOption option : options) {
			String descr = option.getDescription();
			if (descr == null)
				continue; // skip
			help.append(descr + "\n");
		}
		// drop terminating newline
		return help.deleteCharAt(help.length() - 1).toString();
	}

	/**
	 * Adds <code>option</code> to current list of command line
	 * <code>options</code>. If <code>option</code> has already been added the
	 * request is ignored and <code>options</code> remains unchanged. If an option
	 * of the same name already exists in <code>options</code> then
	 * <code>option</code> is <em>not</em> added and a warning is logged. Finally,
	 * if <code>option</code> does not exist, it is reset to its default values and
	 * added to the <code>options</code> list.
	 * 
	 * @param option to be added to the list of <code>options</code>
	 */
	public void addCLO(CLOption option) {
		// check if option already added
		String name = option.getName();
		for (CLOption opt : options) {
			if (opt == option)
				// option already in list
				return;
			if (opt.getName().equals(name)) {
				logWarning("option --" + option.getName() + " overridden in subclass\n" + "         using  "
						+ opt.getDescription() + "\n" + "         instead of " + option.getDescription());
				return;
			}
		}
		option.reset();
		options.add(option);
	}

	/**
	 * Remove <code>option</code> from current list of command line
	 * <code>options</code>.
	 * 
	 * @param option to be removed from list of <code>options</code>, if present
	 * @return <code>true</code> if <code>options</code> contained
	 *         <code>option</code>
	 */
	public boolean removeCLO(CLOption option) {
		return options.remove(option);
	}

	/**
	 * Remove option with <code>name</code> from current list of command line
	 * <code>options</code>. Does nothing if no option with <code>name</code>
	 * exists.
	 * 
	 * @param name of option to be removed from list of <code>options</code>
	 * @return <code>true</code> if <code>options</code> contained option with
	 *         <code>name</code>
	 */
	public boolean removeCLO(String name) {
		for (CLOption option : options) {
			if (option.getName().equals(name))
				return options.remove(option);
		}
		logDebug("option '" + name + "' not found.");
		return false;
	}

	/**
	 * Remove all options with names listed in <code>names</code>.
	 * 
	 * @param names of options to remove
	 * @return <code>true</code> if <code>options</code> contained all options
	 *         listed in <code>names</code>; <code>false</code> if at least one name
	 *         could not be found
	 */
	public boolean removeCLO(String[] names) {
		boolean removed = true;
		int len = names.length;
		for (int i = 0; i < len; i++)
			removed &= removeCLO(names[i]);
		return removed;
	}

	/**
	 * Return option with <code>name</code> from current list of command line
	 * <code>options</code>. Leaves <code>options</code> unchanged.
	 * 
	 * @param name of option to retrieve
	 * @return option if found; <code>null</code> otherwise
	 */
	public CLOption getCLO(String name) {
		for (CLOption option : options) {
			if (option.getName().equals(name))
				return option;
		}
		return null;
	}

	/**
	 * Check if current list of command line <code>options</code> includes option
	 * with <code>name</code>.
	 * 
	 * @param name of option to check
	 * @return <code>true</code> if included in <code>options</code>
	 */
	public boolean providesCLO(String name) {
		return getCLO(name) != null;
	}

	/**
	 * Sets the logger for reporting warnings while parsing options.
	 * 
	 * @param logger to use for warnings
	 * @see #parseCLO(String[])
	 * @see Logger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	private void logWarning(String msg) {
		if (logger != null)
			logger.warning(msg);
	}

	private void logDebug(String msg) {
		if (logger != null)
			logger.fine(msg);
	}

	// UTILITY methods - for parsing command line arguments

	public static final String SPECIES_DELIMITER = ":";
	public static final String MATRIX_DELIMITER = ";";
	public static final String VECTOR_DELIMITER = ",";

	/**
	 * Parse string <code>arg</code> as an integer. If <code>arg</code> contains
	 * <code>'x'</code> or <code>'X'</code> the number is assumed to refer to the
	 * side of a square. For example <code>"42x"</code> returns
	 * <code>42*42=1764</code>.
	 * <p>
	 * <strong>Note:</strong> any digits following <code>'x'</code> or
	 * <code>'X'</code> are currently ignored. For example <code>"42x37"</code> also
	 * returns <code>42*42=1764</code>.
	 * </p>
	 * 
	 * @param aDim string to convert to an <code>int</code>
	 * @return <code>int</code> representation of <code>aDim</code>
	 */
	public static int parseDim(String aDim) {
		int idx = (aDim.toLowerCase()).indexOf('x');
		if (idx >= 0) {
			int size = parseInteger(aDim.substring(0, idx));
			return size * size;
		}
		return parseInteger(aDim);
	}

	/**
	 * Parse string <code>anInteger</code> as an <code>int</code>.
	 * 
	 * @param anInteger string to convert to an <code>int</code>
	 * @return <code>int</code> representation of <code>anInteger</code> or
	 *         <code>0</code> if <code>anInteger</code> is <code>null</code> or an
	 *         empty string.
	 * @see Integer#parseInt(String)
	 */
	public static int parseInteger(String anInteger) {
		if (anInteger == null)
			return 0;
		return Integer.parseInt(anInteger);
	}

	/**
	 * Parse string <code>aLong</code> as a <code>long</code>.
	 * 
	 * @param aLong string to convert to a <code>long</code>
	 * @return <code>long</code> representation of <code>aLong</code> or
	 *         <code>0</code> if <code>aLong</code> is <code>null</code> or an empty
	 *         string.
	 * @see Long#parseLong(String)
	 */
	public static long parseLong(String aLong) {
		if (aLong == null)
			return 0;
		return Long.parseLong(aLong);
	}

	/**
	 * Parse string <code>aFloat</code> as a <code>float</code>.
	 * 
	 * @param aFloat string to convert to an <code>float</code>
	 * @return <code>float</code> representation of <code>aFloat</code> or
	 *         <code>0</code> if <code>aFloat</code> is <code>null</code> or an
	 *         empty string.
	 * @see Float#parseFloat(String)
	 */
	public static float parseFloat(String aFloat) {
		if (aFloat == null || aFloat.length() == 0)
			return 0f;
		return Float.parseFloat(aFloat);
	}

	/**
	 * Parse string <code>aDouble</code> as a <code>double</code>.
	 * 
	 * @param aDouble string to convert to an <code>double</code>
	 * @return <code>double</code> representation of <code>aDouble</code> or
	 *         <code>0</code> if <code>aDouble</code> is <code>null</code> or an
	 *         empty string.
	 * @see Double#parseDouble(String)
	 */
	public static double parseDouble(String aDouble) {
		if (aDouble == null || aDouble.length() == 0)
			return 0.0;
		return Double.parseDouble(aDouble);
	}

	/**
	 * Parse string <code>aVector</code> as a <code>boolean[]</code> array. Vector
	 * entries are separated by {@value #VECTOR_DELIMITER}.
	 * 
	 * @param aVector string to convert to <code>boolean[]</code>
	 * @return <code>boolean[]</code> representation of <code>aVector</code> or an
	 *         empty array <code>boolean[0]</code> if <code>aVector</code> is
	 *         <code>null</code> or an empty string
	 * @see Boolean#parseBoolean(String)
	 */
	public static boolean[] parseBoolVector(String aVector) {
		if (aVector == null)
			return new boolean[0];
		aVector = aVector.trim();
		if (aVector.length() == 0)
			return new boolean[0];

		String[] elem = aVector.split(VECTOR_DELIMITER);
		int len = elem.length;
		boolean[] result = new boolean[len];
		for (int i = 0; i < len; i++) {
			result[i] = Boolean.parseBoolean(elem[i]);
		}
		return result;
	}

	/**
	 * Parse string <code>aVector</code> as an <code>int[]</code> array. Vector
	 * entries are separated by {@value #VECTOR_DELIMITER}.
	 * 
	 * @param aVector string to convert to <code>int[]</code>
	 * @return <code>int[]</code> representation of <code>aVector</code> or an empty
	 *         array <code>int[0]</code> if <code>aVector</code> is
	 *         <code>null</code> or an empty string
	 * @see Integer#parseInt(String)
	 */
	public static int[] parseIntVector(String aVector) {
		if (aVector == null)
			return new int[0];
		aVector = aVector.trim();
		if (aVector.length() == 0)
			return new int[0];

		String[] entries = aVector.split(VECTOR_DELIMITER);
		int len = entries.length;
		int[] result = new int[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.parseInt(entries[i]);
		return result;
	}

	/**
	 * Parse string <code>aVector</code> as a <code>double[]</code> array. Vector
	 * entries are separated by {@value #VECTOR_DELIMITER}. Returns an array of zero
	 * length if <code>aVector</code> is <code>null</code> or an empty string.
	 * 
	 * @param aVector string to convert to <code>double[]</code>
	 * @return <code>double[]</code> representation of <code>aVector</code> or
	 *         <code>null</code> if any entry of <code>aVector</code> caused a
	 *         {@link NumberFormatException}.
	 * 
	 * @see #parseVector(String, String)
	 */
	public static double[] parseVector(String aVector) {
		return parseVector(aVector, VECTOR_DELIMITER);
	}

	/**
	 * Parse string <code>aVector</code> as a <code>double[]</code> array. Vector
	 * entries are separated by <code>separator</code>, which can be any valid
	 * regular expression. Returns an array of zero length if <code>aVector</code>
	 * is <code>null</code> or an empty string.
	 * 
	 * @param aVector string to convert to <code>double[]</code>
	 * @return <code>double[]</code> representation of <code>aVector</code> or
	 *         <code>null</code> if any entry of <code>aVector</code> caused a
	 *         {@link NumberFormatException}.
	 * 
	 * @see String#split(String)
	 * @see Double#parseDouble(String)
	 */
	public static double[] parseVector(String aVector, String separator) {
		if (aVector == null)
			return new double[0];
		aVector = aVector.trim();
		if (aVector.length() == 0)
			return new double[0];

		String[] entries = aVector.split(separator);
		int len = entries.length;
		double[] result = new double[len];
		// note: in GWT Double.parseDouble() does not throw NumberFormatException
		// without the try-catch-block; instead, execution is quietly aborted...
		try {
			for (int i = 0; i < len; i++)
				result[i] = Double.parseDouble(entries[i]);
		} catch (Exception e) {
			return null;
		}
		return result;
	}

	/**
	 * Parse string <code>aMatrix</code> as a <code>double[][]</code> matrix (two
	 * dimensional array). For each row entries are separated by
	 * {@value #VECTOR_DELIMITER} and rows are separated by
	 * {@value #MATRIX_DELIMITER}.
	 * 
	 * @param aMatrix string to convert to <code>double[][]</code>
	 * @return <code>double[][]</code> representation of <code>aMatrix</code> and
	 *         <code>null</code> if <code>aMatrix</code> is <code>null</code>, an
	 *         empty string or any entry caused a {@link NumberFormatException}.
	 * @see #parseVector(String)
	 */
	public static double[][] parseMatrix(String aMatrix) {
		if (aMatrix == null)
			return new double[0][0];
		aMatrix = aMatrix.trim();
		if (aMatrix.length() == 0)
			return new double[0][0];

		String[] entries = aMatrix.split(MATRIX_DELIMITER);
		int len = entries.length;
		double[][] result = new double[len][];
		for (int i = 0; i < len; i++) {
			double[] vec = parseVector(entries[i]);
			if (vec == null)
				return null;
			result[i] = vec;
		}
		return result;
	}

	/**
	 * Lookup table for predefined colors.
	 * 
	 * @see Color
	 */
	private static final Map<String, Color> COLOR_KEYS = new HashMap<String, Color>();
	static {
		// init slide keys
		COLOR_KEYS.put("black", Color.black);
		COLOR_KEYS.put("blue", Color.blue);
		COLOR_KEYS.put("cyan", Color.cyan);
		COLOR_KEYS.put("darkgray", Color.darkGray);
		COLOR_KEYS.put("gray", Color.gray);
		COLOR_KEYS.put("green", Color.green);
		COLOR_KEYS.put("lightgray", Color.lightGray);
		COLOR_KEYS.put("magenta", Color.magenta);
		COLOR_KEYS.put("orange", Color.orange);
		COLOR_KEYS.put("pink", Color.pink);
		COLOR_KEYS.put("red", Color.red);
		COLOR_KEYS.put("white", Color.white);
		COLOR_KEYS.put("yellow", Color.yellow);
	}

	/**
	 * Parse string <code>aColor</code> as a {@link Color}. The color string can
	 * have different formats:
	 * <ul>
	 * <li>named color</li>
	 * <li>a single number [0,255] specifying a gray scale color</li>
	 * <li>a triplet of numbers in [0,255] (separated by <code>,</code>) specifying
	 * the red, green and blue components of the color, respectively.</li>
	 * <li>a quadruple of numbers in [0,255] (separated by <code>,</code>)
	 * specifying the red, green, blue and alpha components of the (transparent)
	 * color, respectively.</li>
	 * </ul>
	 * 
	 * @param aColor string to convert to Color
	 * @return {@link Color} representation of <code>aColor</code> and
	 *         <code>null</code> if <code>aColor</code> is <code>null</code>, an
	 *         empty or a malformed string.
	 */
	public static Color parseColor(String aColor) {
		if (aColor == null)
			return null;
		aColor = aColor.trim().toLowerCase();
		int len = aColor.length();
		if (len == 0)
			return null;
		Color color = COLOR_KEYS.get(aColor);
		if (color != null)
			return color;
		if (aColor.charAt(0) != '(' || aColor.charAt(len - 1) != ')')
			return null;
		// try (r, g, b) or (r, g, b, a)
		String[] rgb = aColor.substring(1, len - 1).split(",");
		try {
			switch (rgb.length) {
				case 1: // (g) grayscale
					int grey = parseInteger(rgb[0]);
					color = new Color(grey, grey, grey);
					break;
				case 3: // (r, g, b)
					color = new Color(parseInteger(rgb[0]), parseInteger(rgb[1]), parseInteger(rgb[2]));
					break;
				case 4: // (r, g, b, a)
					color = new Color(parseInteger(rgb[0]), parseInteger(rgb[1]), parseInteger(rgb[2]),
							parseInteger(rgb[3]));
					break;
				default:
					return null;
			}
		} catch (Exception e) {
			return null;
		}
		return color;
	}
}
