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

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * Command line option and argument.
 *
 * @author Christoph Hauert
 */
public class CLOption implements Comparable<CLOption> {

	/**
	 * Interface to process command line arguments
	 */
	public interface CLODelegate {

		/**
		 * Parse string <code>arg</code> and set configurable parameters that correspond
		 * to this command line option.
		 * <p>
		 * <strong>Note:</strong> should only return <code>false</code> if a warning or
		 * other information was logged.
		 * </p>
		 * 
		 * @param arg for parsing by command line option
		 * @return <code>true</code> if parsing successful
		 */
		public boolean parse(String arg);

		/**
		 * Report settings of configurable parameters that correspond to this command
		 * line option (optional implementation).
		 * @param output TODO
		 */
		public default void report(PrintStream output) {
		}

		/**
		 * If settings for option are not known upon initialization, an up-to-date
		 * description is requested when needed (e.g. if help is requested, typically
		 * using <code>-h</code> or <code>--help</code> options).
		 * 
		 * @return description of command line option.
		 */
		public default String getDescription() {
			return null;
		};
	}

	/**
	 * Types of command line options:
	 * <dl>
	 * <dt>REQUIRED</dt>
	 * <dd>required argument. Must be separated from command line option (long or
	 * short version) by whitespace or <code>'='</code>.</dd>
	 * <dt>OPTIONAL</dt>
	 * <dd>optional argument. If present, must be separated from command line option
	 * (long or short version) by whitespace or <code>'='</code>.</dd>
	 * <dt>NONE</dt>
	 * <dd>no argument.</dd>
	 * </dl>
	 */
	public enum Argument {
		/**
		 * <code>REQUIRED</code>: required argument. Must be separated from command line
		 * option (long or short version) by whitespace or <code>'='</code>.
		 */
		REQUIRED,

		/**
		 * <code>OPTIONAL</code>: optional argument. If present, must be separated from
		 * command line option (long or short version) by whitespace or
		 * <code>'='</code>.
		 */
		OPTIONAL,

		/**
		 * <code>NONE</code>: no argument.
		 */
		NONE;
	}

	public static class Key {
		final String key;
		final String title;
		final String description;

		public Key(String key, String title, String description) {
			this.key = key;
			this.title = title;
			this.description = description;
		}

		public boolean equals(String anotherKey) {
			// anotherKey must start with key but may have additional arguments
			return anotherKey.startsWith(key);
		}

		public String getTitle() {
			return title;
		}

		@Override
		public String toString() {
			return key + ": " + (description==null?title:description);
		}
	}

	public interface KeyCollection {
		public KeyCollection[] getKeys();
		public String getKey();
		public String getTitle();
		@Override
		public String toString();
	}

	/**
	 * Counter to assign every option a unique identifier.
	 */
	private static int uniqueID = 0;

	/**
	 * Unique identifier of command line option (currently unused).
	 */
	final int ID;

	/**
	 * Long name of command line option (required).
	 */
	final String longName;

	/**
	 * Short name of command line option (optional).
	 */
	final int shortName;

	/**
	 * Type of command line option with no, optional, or required argument.
	 */
	final Argument type;

	/**
	 * Short description of command line option. May include newline's
	 * <code>'\n'</code> for basic formatting but no HTML or other formatting.
	 */
	String description = null;

	/**
	 * Argument provided on the command line (if any).
	 */
	String optionArg = null;

	/**
	 * Default argument for option (if applicable).
	 */
	String defaultArg = null;

	/**
	 * List of valid keys (if applicable).
	 */
	HashMap<String, Key> keys = null;

	/**
	 * Flag to indicate if keys were inherited from another option. If
	 * <code>true</code> the keys will not be printed as part of the description.
	 */
	boolean inheritedKeys = false;

	/**
	 * <code>true</code> if option was set on command line.
	 */
	boolean isSet = false;

	/**
	 * Delegate for parsing arguments, reporting settings and retrieving customized
	 * descriptions.
	 */
	private CLODelegate delegate;

	/**
	 * Creates command line option with the long name <code>longName</code> (no
	 * short version, no arguments) and short description <code>description</code>
	 * as well as the <code>delegate</code> to process the argument and retrieve
	 * description.
	 * <p>
	 * <strong>Note:</strong>
	 * <ul>
	 * <li>on the command line long options need to be preceded by <code>--</code>,
	 * e.g. <code>--help</code>.</li>
	 * <li><code>delegate</code> must implement {@link CLODelegate#getDescription()}
	 * to provide option description.</li>
	 * </ul>
	 * 
	 * @param longName long version of command line option
	 * @param delegate delegate for processing command line argument
	 */
	public CLOption(String longName, CLODelegate delegate) {
		this(longName, (char) -1, Argument.NONE, "no" + longName, null, delegate);
	}

	/**
	 * Creates command line option with the long name <code>longName</code> (no
	 * short version, no arguments) and short description <code>description</code>
	 * as well as the delegate <code>delegate</code>.
	 * <p>
	 * <strong>Note:</strong> on the command line long options need to be preceded
	 * by <code>--</code>, e.g. <code>--help</code>.
	 * 
	 * @param longName    long version of command line option
	 * @param description short description of command line option
	 * @param delegate    delegate for processing command line argument
	 */
	public CLOption(String longName, String description, CLODelegate delegate) {
		this(longName, (char) -1, Argument.NONE, "no" + longName, description, delegate);
	}

	/**
	 * Creates command line option with the long name <code>longName</code> (no
	 * short version) that requires arguments as specified by <code>type</code>, has
	 * the default setting <code>defaultArg</code>, as well as the
	 * <code>delegate</code> to process the argument and retrieve description.
	 * <p>
	 * <strong>Note:</strong>
	 * <ul>
	 * <li>on the command line long options need to be preceded by <code>--</code>,
	 * e.g. <code>--help</code>.</li>
	 * <li><code>delegate</code> must implement {@link CLODelegate#getDescription()}
	 * to provide option description.</li>
	 * </ul>
	 * 
	 * @param longName   long version of command line option
	 * @param type       of command line option (whether argument required)
	 * @param defaultArg default argument if option is not specified on command line
	 * @param delegate   delegate for processing command line argument
	 */
	public CLOption(String longName, Argument type, String defaultArg, CLODelegate delegate) {
		this(longName, (char) -1, type, defaultArg, null, delegate);
	}

	/**
	 * Creates command line option with the long name <code>longName</code> (no
	 * short version) that requires arguments as specified by <code>type</code>, has
	 * the default setting <code>defaultArg</code>, a short description of
	 * <code>description</code> and the delegate <code>delegate</code>.
	 * <p>
	 * <strong>Note:</strong> on the command line long options need to be preceded
	 * by <code>--</code>, e.g. <code>--help</code>.
	 * 
	 * @param longName    long version of command line option
	 * @param type        of command line option (whether argument required)
	 * @param defaultArg  default argument if option is not specified on command
	 *                    line
	 * @param description short description of command line option
	 * @param delegate    delegate for processing command line argument
	 */
	public CLOption(String longName, Argument type, String defaultArg, String description, CLODelegate delegate) {
		this(longName, (char) -1, type, defaultArg, description, delegate);
	}

	/**
	 * Creates command line option with the long name <code>longName</code> (no
	 * short version) that requires arguments as specified by <code>type</code>, has
	 * the default setting <code>defaultArg</code>, as well as the
	 * <code>delegate</code> to process the argument and retrieve description.
	 * <p>
	 * <strong>Note:</strong>
	 * <ul>
	 * <li>on the command line long options need to be preceded by <code>--</code>,
	 * e.g. <code>--help</code>, and short options are single characters preceded by
	 * <code>-</code>, e.g. <code>-h</code>.</li>
	 * <li><code>delegate</code> must implement {@link CLODelegate#getDescription()}
	 * to provide option description.</li>
	 * </ul>
	 * 
	 * @param longName   long version of command line option
	 * @param shortName  short version of command line option
	 * @param type       of command line option (whether argument required)
	 * @param defaultArg default argument if option is not specified on command line
	 * @param delegate   delegate for processing command line argument
	 */
	public CLOption(String longName, char shortName, Argument type, String defaultArg, CLODelegate delegate) {
		this(longName, shortName, type, defaultArg, null, delegate);
	}

	/**
	 * Creates command line option with the long name <code>longName</code>, short
	 * version <code>shortName</code> that requires arguments as specified by
	 * <code>type</code>, has the default setting <code>defaultArg</code>, a short
	 * description <code>description</code> and the delegate <code>delegate</code>.
	 * <p>
	 * <strong>Note:</strong> on the command line long options need to be preceded
	 * by <code>--</code>, e.g. <code>--help</code>, and short options are single
	 * characters preceded by <code>-</code>, e.g. <code>-h</code>.
	 *
	 * @param longName    long version of command line option
	 * @param shortName   short version of command line option
	 * @param type        of command line option (whether argument required)
	 * @param defaultArg  default argument if option is not specified on command
	 *                    line
	 * @param description short description of command line option
	 * @param delegate    delegate for processing command line argument
	 */
	public CLOption(String longName, char shortName, Argument type, String defaultArg, String description,
			CLODelegate delegate) {
		this.ID = uniqueID++;
		this.longName = longName;
		this.shortName = shortName;
		this.type = type;
		this.defaultArg = defaultArg;
		this.description = description;
		this.delegate = delegate;
	}

	/**
	 * Set the default argument. This argument is parsed by the delegate if option
	 * is not specified on command line or if the parsing of the provided argument
	 * failed. If the parsing of <code>defaultArg</code> fails, the option settings
	 * are undefined.
	 * 
	 * @param defaultArg default argument for command line option
	 */
	public void setDefault(String defaultArg) {
		this.defaultArg = defaultArg;
	}

	public void setDefault(KeyCollection key) {
		defaultArg = key.getKey();
	}

	/**
	 * Return the default argument for command line option.
	 * 
	 * @return default argument
	 */
	public String getDefault() {
		return defaultArg;
	}

	/**
	 * Process command line option and argument. If <code>option</code> matches the
	 * long or short name of this option then the option <code>isSet</code> and,
	 * depending on the option <code>type</code> the next entry in
	 * <code>options</code> is processed as the option argument, as appropriate.
	 * Returns <code>0</code> on successful processing of option and possible
	 * arguments. Returns <code>-1</code> if <code>option</code> matches neither
	 * long nor short name and <code>1</code> if processing of argument fails for
	 * other reasons.
	 * 
	 * @param option  name of option to parse
	 * @param options list of options and arguments provided on command line
	 * @return <code>-1</code> if no match, <code>0</code> on success,
	 *         <code>1</code> if parsing of argument failed, and <code>2</code> if
	 *         parsing of concatenated argument of short option failed
	 */
	public int processOption(String option, ListIterator<String> options) {
		// if an option has been removed in some preliminary screening option==null;
		// simply skip over
		if (option == null)
			return 0;
		String longArg = "--" + longName;
		// check if long option (must be perfect match because long options
		// have to be followed by either ' ' or '=' and the latter has already
		// been taken care of (see CLOParser.parseCLO)
		if (option.equals(longArg))
			return processOptionArg(options) ? 0 : 1;
		String shortArg = "-" + (char) shortName;
		if (shortName < 256 && option.startsWith(shortArg)) {
			// short option
			if (option.length() > shortArg.length()) {
				// check if option is followed by stuff
				if (type == Argument.NONE)
					return -1;
				return processOptionArg(option.substring(2)) ? 0 : 2;
			}
			return processOptionArg(options) ? 0 : 1;
		}
		return -1;
	}

	/**
	 * For options with {@link Argument#OPTIONAL} or {@link Argument#REQUIRED}
	 * checks next entry on command line and processes arguments as appropriate.
	 * <p>
	 * <strong>Note:</strong> legitimate arguments can start with <code>'-'</code>,
	 * e.g. negative numbers, vectors or matrices. If next entry starts with
	 * <code>'-'</code> but parses as a number or matrix, assume it is an argument.
	 * If, for example, <code>-1</code> is a valid short option then specifying
	 * <code>-1</code> after an option with optional argument processes
	 * <code>-1</code> as the optional argument and <em>not</em> as the next option.
	 * 
	 * @param options list of options and arguments provided on command line
	 * @return <code>true</code> on success
	 */
	private boolean processOptionArg(ListIterator<String> options) {
		optionArg = null;
		isSet = false;
		if (type == Argument.NONE) {
			isSet = true;
			return true;
		}
		if (!options.hasNext()) {
			// last option, no argument
			if (type == Argument.REQUIRED)
				return false;
			// last option had optional argument
			isSet = true;
			return true;
		}
		// read argument
		String arg = options.next();
		// argument should not start with '--', must be subsequent option
		if (arg.startsWith("--")) {
			options.previous();
			if (type == Argument.REQUIRED)
				return false;
			// optional argument
			isSet = true;
			return true;
		}
		if (type == Argument.REQUIRED) {
			// argument could still start with '-' indicating negative number [in array], be
			// greedy and assume this is the argument; check if first char of argument is
			// valid key (or options has no keys).
			if (!isValidKey(arg)) {
				options.previous();
				return false;
			}
			optionArg = arg;
			isSet = true;
			return true;
		}
		// argument is optional; could still be a short option (only start with '--' 
		// has been ruled out)
		if (arg.charAt(0)=='-') {
			// short option or negative number - try to parse as matrix
			try {
				if (CLOParser.parseMatrix(arg).length > 0) {
					// option is number, treat it as argument
					optionArg = arg;
				}
			} catch (Exception e) {
				// doesn't look like a number; rewind options
				options.previous();
				if (type == Argument.REQUIRED)
					return false;
				// argument optional
			}
			isSet = true;
			return true;
		}
		// in the most complex valid case it is a matrix
		isSet = true;
		optionArg = arg;
		return true;
	}

	/**
	 * @param arg concatenated argument of short option
	 * @return <code>true</code> on success
	 */
	private boolean processOptionArg(String arg) {
		optionArg = null;
		isSet = false;
		if (type == Argument.REQUIRED) {
			// argument could still start with '-' indicating negative number [in array], be
			// greedy and assume this is the argument; check if first char of argument is
			// valid key (or options has no keys).
			if (!isValidKey(arg)) {
				return false;
			}
			optionArg = arg;
			isSet = true;
			return true;
		}
		// argument is optional; in the most complex valid case it is a matrix
		try {
			if (CLOParser.parseMatrix(arg).length > 0) {
				// option is number, treat it as argument
				optionArg = arg;
				isSet = true;
				return true;
			}
		} catch (Exception e) {
			// doesn't look like a number; rewind options
			if (type == Argument.REQUIRED)
				return false;
			// ARGUMENT_OPTIONAL
			isSet = true;
			return true;
		}
		// should not get here... rewriting code above seems dangerous because compiler
		// might be tempted to strip the parsing of 'option' if nothing is done with
		// the result
		throw new Error(
				"this is unreachable code! (parsing of option/argument failed: " + getName() + ", arg=" + arg + ").");
	}

	/**
	 * Parses the option and its argument, if applicable, through the delegate. If
	 * this option was not specified on command line, the default argument is passed
	 * to the delegate.
	 * 
	 * @return <code>true</code> on successful parsing of argument
	 */
	public boolean parse() {
		if (delegate == null)
			return false;
		return delegate.parse(getArg());
	}

	/**
	 * Parses the default argument for this option. Typically called if
	 * {@link #parse()} failed.
	 * 
	 * @return <code>true</code> on successful parsing of default argument
	 */
	public boolean parseDefault() {
		if (delegate == null)
			return false;
		return delegate.parse(getDefault());
	}

	public static final int NUMBERED_KEY_OFFSET = 256;

	public void addKeys(KeyCollection[] chain) {
		for (KeyCollection key : chain)
			addKey(key.getKey(), key.getTitle());
	}

	public void addKey(KeyCollection key) {
		addKey(key.getKey(), key.getTitle());
	}

	public Key addKey(int key, String title) {
		return addKey(key, title, null);
	}

	public Key addKey(String key, String title) {
		return addKey(key, title, null);
	}

	private String key2String(int key) {
		// treat key as number if outside of range of char's
		if (key >= NUMBERED_KEY_OFFSET)
			return String.valueOf(key - NUMBERED_KEY_OFFSET);
		return String.valueOf((char) key);
	}

	public Key addKey(int key, String title, String descr) {
		return addKey(new Key(key2String(key), title, descr));
	}

	public Key addKey(String key, String title, String descr) {
		return addKey(new Key(key, title, descr));
	}

	public Key addKey(Key key) {
		if (keys == null)
			keys = new HashMap<String, Key>();
		return keys.put(key.key, key);
	}

	public Key getKey(KeyCollection key) {
		return getKey(key.getKey());
	}

	public Key getKey(int aKey) {
		return getKey(key2String(aKey));
	}

	public Key getKey(String aKey) {
		if (keys == null)
			return null;
		return keys.get(aKey);
	}

	public Key removeKey(KeyCollection key) {
		return removeKey(key.getKey());
	}

	public Key removeKey(int aKey) {
		return removeKey(key2String(aKey));
	}

	public Key removeKey(String aKey) {
		if (keys == null)
			return null;
		return keys.remove(aKey);
	}

	public void clearKeys() {
		if (keys == null)
			return;
		keys.clear();
	}

	public boolean isValidKey(KeyCollection key) {
		return isValidKey(key.getKey());
	}

	public boolean isValidKey(int aKey) {
		return isValidKey(key2String(aKey));
	}

//	public boolean isValidKey(String aKey) {
//		if (keys == null)
//			return true;
//		// a match is found if:
//		// 1) aKey starts with key (additional arguments specific to the key may follow, see e.g. see Geometry), or
//		// 2) aKey abbreviates key
//		// more specifically, the check is whether the longer specification starts with the shorter one.
//		int len = aKey.length();
//		for (String key : keys.keySet()) {
//			if( key.length()>len ) {
//				if (key.startsWith(aKey))
//					return true;
//			}
//			else {
//				if (aKey.startsWith(key))
//					return true;
//			}
//		}
//		return false;
//	}

	public boolean isValidKey(String aKey) {
		if (keys == null)
			return true;
		// in order to allow abbreviating keys as well as appending options, this test is very lenient
		// and passes if aKey and one of the keys start at least with one identical character
		for (String key : keys.keySet()) {
			if (differAt(key, aKey)>0)
				return true;
		}
		return false;
	}

	public static int differAt(String a, String b) {
		int max = Math.min(a.length(), b.length());
		int idx = 0;
		while (a.charAt(idx) == b.charAt(idx)) {
			if (++idx == max)
				return max;
		}
		return idx;
	}

	public Collection<Key> getKeys() {
		return keys.values();
	}

	public void inheritKeysFrom(CLOption option) {
		inheritedKeys = true;
		keys = option.keys;
	}

	public String getDescriptionKey(int aKey) {
		return getDescriptionKey(key2String(aKey));
	}

	public String getDescriptionKey(String aKey) {
		Key key = getKey(aKey);
		if (key == null)
			return null;
		return key.toString();
	}

	public String getDescriptionKey() {
		if (keys == null || inheritedKeys )
			return "";
		String keydescr = "";
		for (Key key : keys.values())
			keydescr += "\n         " + key.toString();
		return keydescr;
	}

	/**
	 * Print report for this option through delegate.
	 */
	public void report(PrintStream output) {
		if (delegate == null || output == null)
			return;
		delegate.report(output);
	}

	/**
	 * Reset option. Clear argument, if applicable, and mark as not
	 * <code>isSet</code>.
	 */
	public void reset() {
		optionArg = null;
		isSet = false;
		// custom description has not yet been retrieved
		if (description == null)
			return;
		// check if custom description provided
		String descr = delegate.getDescription();
		if (descr != null)
			description = null;
	}

	/**
	 * @return long name of option
	 */
	public String getName() {
		return longName;
	}

	/**
	 * @return type of option
	 */
	public Argument getType() {
		return type;
	}

	/**
	 *
	 * @return argument of option or default if argument not set.
	 */
	public String getArg() {
		if (optionArg == null)
			return defaultArg;
		return optionArg;
	}

	/**
	 * @return <code>true</code> if no argument set.
	 */
	public boolean isDefault() {
		return (optionArg == null);
	}

	/**
	 * @return <code>true</code> if option set on command line (regardless of
	 *         whether an argument was provided).
	 */
	public boolean isSet() {
		return isSet;
	}

	/**
	 * Retrieve short description of option and include the default as well as the
	 * current arguments. If no description was provided at initialization, the
	 * delegate is queried for an up-to-date description.
	 * 
	 * @return description of option and arguments.
	 */
	public String getDescription() {
		String myDescr;
		if (description == null) {
			// description is delegate's responsibility - including keys (if applicable)
			myDescr = delegate.getDescription();
		} else {
			myDescr = description + getDescriptionKey();
		}
		if (type == Argument.NONE)
			return myDescr + "\n      (current: " + (isSet() ? "" : "not ") + "set)";
		String arg = getArg();
		if (!isSet() || isDefault() || arg.equals(defaultArg))
			return myDescr + "\n      (default: " + defaultArg + ")";
		return myDescr + "\n      (current: " + arg + ", default: " + defaultArg + ")";
	}

	/**
	 * Set short description of option.
	 * 
	 * @param descr description of option
	 */
	public void setDescription(String descr) {
		description = descr;
	}

	@Override
	public int compareTo(CLOption opt) {
		return longName.compareTo(opt.getName());
	}
}
