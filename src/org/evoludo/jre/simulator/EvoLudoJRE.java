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

package org.evoludo.jre.simulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.evoludo.simulator.EvoLudo;
import org.evoludo.simulator.Population;
//import org.evoludo.simulator.models.PDESupervisor;
import org.evoludo.util.CLOParser;
import org.evoludo.util.CLOption;
import org.evoludo.util.CLOption.CLODelegate;
import org.evoludo.util.Plist;
import org.evoludo.util.PlistParser;

/**
 * JRE specific implementation of EvoLudo controller.
 * 
 * @author Christoph Hauert
 */
public class EvoLudoJRE extends EvoLudo implements Runnable {

	/**
	 * <code>true</code> when running as JRE application.
	 */
	public boolean isApplication = true;

	/**
	 * Store time to measure execution times since instantiation.
	 */
	private final long startmsec = System.currentTimeMillis();

	@Override
	public int elapsedTimeMsec() {
		return (int) (System.currentTimeMillis() - startmsec);
	}

	Timer timer = null;

	Thread engineThread = null;		// engine thread
	Thread executeThread = null;	// command execution thread

	public EvoLudoJRE() {
		// allocate a coalescing timer for poking the engine in regular intervals
		// note: timer needs to be ready before parsing command line options
		timer = new Timer(0, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				poke();
			}
		});
		launchEngine();
	}

	@Override
	public void execute(Directive directive) {
		executeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				directive.execute();
			}
		}, "Execute");
		executeThread.start();		
	}

	protected void launchEngine() {
		if( engineThread!=null )
			return;
		engineThread = new Thread(this, "Engine");
		engineThread.start();		
	}

//	@Override
//	public PDESupervisor hirePDESupervisor() {
//		return new PDESupervisorJRE();
//	}

	@Override
	public void setDelay(int delay) {
		super.setDelay(delay);
		timer.setDelay(delay);
		// if delay is more that APP_MIN_DELAY keep timer running or restart it
		if( !timer.isRunning() && delay>1 && isRunning ) {
			timer.start();
			return;
		}
		// if not don't bother with timer - run, run, run as fast as you can
		if (timer.isRunning() && delay <= 1) {
			timer.stop();
			poke();
		}
	}

	/**
	 * poke waiting thread to resume execution.
	 */
	public void poke() {
		if (isWaiting) {
			synchronized (this) {
				isWaiting = false;
				notify();
			}
		}
	}

	/**
	 * The <code>run()</code> method is double booked: first to start running the
	 * EvoLudo model and second to implements the {@link Runnable} interface for
	 * starting the engine in a separate thread. The two tasks can be easily triaged
	 * because requests to start running are always issued from the Event Dispatch
	 * Thread (EDT) while the engine is running in in a thread named
	 * <code>Engine</code>.
	 */
	@Override
	public void run() {
		Thread me = Thread.currentThread();
		if (!me.getName().equals("Engine")) {
			isSuspended = false;
			if (isRunning)
				return;
			isRunning = true;
			// this is the EDT, check if engine thread alive and kicking
			if( engineThread==null ) {
				logger.severe("engine crashed. resetting and relaunching.");
				modelReset();
				launchEngine();
			}
			// if delay is more that APP_MIN_DELAY set timer
			// if not don't bother with timer - run, run, run as fast as you can
			if (delay > 1)
				timer.start();
			else
				poke();
			return;
		}
		// this is the engine thread, start waiting for tasks
		isWaiting = true;
		me.setPriority(Thread.MIN_PRIORITY);
		while (true) {
			// Possible optimization:
			// to reduce waiting times the next state should be calculated while waiting but
			// this makes synchronization with frontend much more difficult - engine would
			// probably have to keep a copy of the relevant data - currently the views keep
			// this copy... if the engine took care of that it would be enough to pass a
			// reference to the views!
			while (isWaiting) {
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}
			// woke up; calculate next step
			try {
				modelNext();
			} catch (Error e) {
				// we do not really catch this error but rather spread the word...
				// the error will be reported in the console anyways, therefore only check
				// we have a front-end and if so raise alert.
				logger.severe("Engine crashed: " + e.getMessage());
				throw e;
			}
			if (!isRunning) {
				timer.stop();
				isWaiting = true;
				continue;
			}
			// wait only if delay is more that APP_MIN_DELAY
			isWaiting = (delay > 1);
		}
	}

	@Override
	public void next() {
		poke();
	}

	@Override
	public void modelStopped() {
		timer.stop();
		isWaiting = true;
		super.modelStopped();
	}

	@Override
	public void modelDidReinit() {
		timer.stop();
		isWaiting = true;
		super.modelDidReinit();
	}

	@Override
	public void modelDidReset() {
		timer.stop();
		isWaiting = true;
		super.modelDidReset();
	}

	/**
	 * Flag indicating whether engine is idling.
	 */
	private boolean isWaiting = true;

	/**
	 * Name of file that stores current git version in jar archive.
	 */
	protected static final String GIT_REVISION_FILE = "/org/evoludo/simulator/git.version";

	/**
	 * {@inheritDoc} Retrieves git version from file in jar archive.
	 */
	@Override
	public String getVersion() {
		String revision = "unknown";
		// in order to prevent security exceptions applets must follow particular rules
		// when loading additional files
		try {
			String line;
			InputStream is = EvoLudo.class.getResourceAsStream(GIT_REVISION_FILE);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			if ((line = reader.readLine()) != null)
				revision = line.trim();
			reader.close();
		} catch (Exception e) {
			revision = "unresolved";
		}
		return "Engine: " + revision;
	}

	/**
	 * Attributes of MANIFEST are stored here.
	 */
	protected static Attributes attributes = null;

	/**
	 * Retrieve attribute for given key.
	 * 
	 * @param key of <code>attribute</code>
	 * @return attribute for <code>key</code>
	 */
	public static String getAttribute(String key) {
		readAttributes();
		if (attributes == null)
			return "failed"; // reading attributes failed
		return attributes.getValue(key);
	}

	/**
	 * Retrieve attribute for given key.
	 * 
	 * @param key of <code>attribute</code>
	 * @return attribute for <code>key</code>
	 */
	public static String getAttribute(Attributes.Name key) {
		readAttributes();
		if (attributes == null)
			return "failed"; // reading attributes failed
		return attributes.getValue(key);
	}

	/**
	 * Helper method to read {@link Attributes} from MANIFEST in jar archive.
	 * <p>
	 * <b>Note:</b> {@link java.io.UnsupportedEncodingException} should never be
	 * thrown.
	 */
	private static void readAttributes() {
		if (attributes != null)
			return;
		try {
			Class<Population> myClass = Population.class;
			String className = "/" + myClass.getName().replace('.', '/') + ".class";
			String myUrl = myClass.getResource(className).toString();
			int to = myUrl.indexOf("!/");
			String jarName;
			jarName = URLDecoder.decode(myUrl.substring(0, to + 1), "UTF-8");
			attributes = new Manifest(new URL(jarName + "/META-INF/MANIFEST.MF").openStream()).getMainAttributes();
		} catch (Exception e) {
			// not much we can do...
		}
	}

	/**
	 * All output should be printed to <code>output</code> (defaults to
	 * <code>stdout</code>). This is only relevant for JRE applications (mainly
	 * simulations) and ignored by GWT.
	 */
	protected PrintStream output = System.out;

	/**
	 * @return where output should be directed (JRE only)
	 */
	public PrintStream getOutput() {
		return output;
	}

	/**
	 * Set output for all reporting (JRE only). Used by {@link #cloOutput} and
	 * {@link #cloAppend} to redirect output to a file.
	 * 
	 * @param output stream to redirect output to.
	 */
	public void setOutput(PrintStream output) {
		this.output = (output == null?System.out:output);
		parser.setOutput(this.output);
	}

	@Override
	public void dumpParameters() {
		output.println(
				// print easily identifiable delimiter to simplify processing of data files
				// containing
				// multiple simulation runs - e.g. for graphical representations in MATLAB
				"! New Record" + "\n# " + game.getTitle() + "\n# " + game.getVersion()
						+ "\n# today:                " + (new Date().toString()));
		output.println("# arguments:            " + parser.getCLO());
		parser.dumpCLO();
		output.println("# data:");
		output.flush();
	}

	/**
	 * Name of file to export state of model at end of run.
	 */
	String exportname = null;

	/**
	 * Name of directory for exports.
	 */
	String exportdir = null;

	@Override
	public void dumpEnd() {
		int deltamilli = elapsedTimeMsec();
		int deltasec = deltamilli / 1000;
		int deltamin = deltasec / 60;
		deltasec %= 60;
		int deltahour = deltamin / 60;
		deltamin %= 60;
		DecimalFormat twodigits = new DecimalFormat("00");
		output.println("# runningtime:          " + deltahour + ":" + twodigits.format(deltamin) + ":"
				+ twodigits.format(deltasec) + "." + twodigits.format(deltamilli % 1000));
		if (exportname == null)
			return;
		exportState(exportname);
	}

	/**
	 * Name of file to restore state from.
	 */
	String plistname = null;
	Plist plist = null;

	/**
	 * {@inheritDoc}
	 * <ul>
	 * <li>if {@code -h} or {@code --help} is provided, ignore all other
	 * options.</li>
	 * <li>if {@code --restore <filename>} is provided, ignore all simulator options
	 * (but not GUI options).</li>
	 * </ul>
	 */
	@Override
	protected String[] preprocessCLO(String[] args) {
		// once game is loaded pre-processing of command line arguments can proceed
		args = super.preprocessCLO(args);
		// check if --help or --restore requested
		String helpName = "--" + cloHelp.getName();
		String restoreName = "--" + cloRestore.getName();
		int nArgs = args.length;
		for (int i = 0; i < nArgs; i++) {
			String arg = args[i];
			if (arg.startsWith(helpName) || arg.equals("-h")) {
				// discard/ignore all other options
				return new String[] { "-h" };
			}
			if (arg.startsWith(restoreName)) {
				if (i == nArgs - 1) {
					String[] sargs = new String[i];
					System.arraycopy(args, 0, sargs, 0, i);
					args = sargs;
					logger.warning("file name to restore state missing - ignored.");
					break;
				}
				// ignore if already restoring; strip restore option and argument
				plistname = args[i + 1];
				nArgs -= 2;
				String[] sargs = new String[nArgs];
				System.arraycopy(args, 0, sargs, 0, i);
				System.arraycopy(args, i + 2, sargs, i, nArgs - i);
				args = sargs;
				if (!doRestore) {
					plist = readPlist(plistname);
					if (plist==null)
						continue;
					String restoreOptions = (String) plist.get("CLO");
					if (restoreOptions == null) {
						logger.warning("state in '" + plistname + "' corrupt (CLO key missing) - ignored.");
						plist = null;
						continue;
					}
					String[] clos = restoreOptions.split("\\s+");
					String gameName = "--" + cloGame.getName();
					String gameKey = game.getKey();
					int rArgs = clos.length;
					for (int j = 0; j < rArgs; j++) {
						if (clos[j].startsWith(gameName)) {
							if (!clos[j + 1].equals(gameKey)) {
								logger.warning("state in '" + plistname + "' refers to game '" + clos[j + 1]
										+ "' but expected '" + gameKey + "' - ignored.");
								plist = null;
								break;
							}
							// merge options and remove --game from clos
							rArgs -= 2;
							sargs = new String[nArgs + rArgs];
							System.arraycopy(args, 0, sargs, 0, nArgs);
							System.arraycopy(clos, 0, sargs, nArgs, j);
							System.arraycopy(clos, j + 2, sargs, nArgs + j, rArgs - j);
							doRestore = true;
							// restart preprocessing with extended command line arguments
							// note: if the same option is listed multiple times the last one overwrites
							// the previous ones. thus, any options specified in the restore file take
							// precedence over those specified on the command line.
							return preprocessCLO(sargs);
						}
					}
				}
			}
		}
		return args;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Special treatment of <code>--restore</code> option.
	 * </p>
	 */
	@Override
	public boolean parseCLO(String[] cloarray) {
		boolean success = super.parseCLO(cloarray);
		if (!doRestore)
			return success;
		// parseCLO does not reset model - do it now to be ready for restore
		modelReset();
		// finish restoring
		if (!restoreState(plist)) {
			logger.warning("failed to restore state in '" + plistname + "'");
			return false;
		}
		return success;
	}

	@Override
	public void helpCLO() {
		output.println(game.getName() + "\nlist of command line options:\n" + parser.helpCLO());
	}

	/**
	 * Check if command line option <code>name</code> is available.
	 *
	 * @param name of command line option
	 * @return <code>true</code> if <code>name</code> is an option.
	 */
	public boolean providesCLO(String name) {
		return parser.providesCLO(name);
	}

	/**
	 * Command line option to redirect output to file (output overwrites potentially
	 * existing file).
	 */
	protected final CLOption cloOutput = new CLOption("output", CLOption.Argument.REQUIRED, "stdout",
			"--output <f>              redirect output to file", new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					// --append option takes precedence; ignore --output setting
					if (cloAppend.isSet())
						return true;
					if (!cloOutput.isSet()) {
						setOutput(null);
						return true;
					}
					File out = new File(arg);
					if (fileCheck(out, true)) {
						try {
							// open print stream for appending
							setOutput(new PrintStream(new FileOutputStream(out, true), true));
							return true;
						} catch (Exception e) {
							// ignore exception
						}
					}
					setOutput(null);
					logger.warning("failed to open '" + arg + "' - using stdout.");
					return false;
				}
			});

	/**
	 * Command line option to redirect output to file (appends output to potentially
	 * existing file).
	 */
	protected final CLOption cloAppend = new CLOption("append", CLOption.Argument.REQUIRED, "stdout",
			"--append <f>              append output to file", new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					// --append option takes precedence; ignore --output setting
					if (!cloAppend.isSet()) {
						if (cloOutput.isSet())
							return true;
						// if neither --append nor --output are set use default stdout
						setOutput(null);
						return true;
					}
					File out = new File(arg);
					if (fileCheck(out, false)) {
						try {
							// open print stream for appending
							setOutput(new PrintStream(new FileOutputStream(out, true), true));
							return true;
						} catch (Exception e) {
							// ignore exception
						}
					}
					setOutput(null);
					logger.warning("failed to append to '" + arg + "' - using stdout.");
					return false;
				}
			});

	/**
	 * Command line option to restore state from file. Typically states have been
	 * saved previously using the export options in the context menu of the GUI or
	 * when requesting to save the end state of a simulation run with
	 * {@code --export}, see {@link #cloExport}.
	 */
	protected final CLOption cloRestore = new CLOption("restore", CLOption.Argument.REQUIRED, "norestore",
			"--restore <filename>      restore saved state from file", new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					// option gets special treatment
					return true;
				}

				@Override
				public void report(PrintStream out) {
					if (cloRestore.isSet())
						out.println("# restored:             " + cloRestore.getArg());
				}
			});

	/**
	 * Command line option to export end state of simulation to file. Saved states
	 * can be read using {@code --restore} to restore the state and resume
	 * execution, see {@link #cloRestore}.
	 */
	protected final CLOption cloExport = new CLOption("export", CLOption.Argument.OPTIONAL, "evoludo-%d.plist",
			"--export [<filename>]    export final state of simulation (%d for generation)", new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					if (!cloExport.isSet())
						return true;
					exportname = arg;
					if (!cloExport.isDefault())
						return true;
					// arg is default; prefer --append or --output file name (with extension plist
					// added or substituted)
					if (cloAppend.isSet()) {
						exportname = cloAppend.getArg();
						return true;
					}
					if (cloOutput.isSet()) {
						exportname = cloOutput.getArg();
						return true;
					}
					// use default
					return true;
				}
			});

	/**
	 * Command line option to print help message for available command line options.
	 */
	protected final CLOption cloHelp = new CLOption("help", 'h', CLOption.Argument.NONE, "nohelp",
			"--help, -h,               print this help screen", new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					if (cloHelp.isSet()) {
						helpCLO();
						System.exit(0); // abort
					}
					return true;
				}
			});

	@Override
	public void collectCLO(CLOParser prsr) {
		// some options are only meaningful when running simulations
		if (!isApplication) {
			// simulation
			prsr.addCLO(cloOutput);
			prsr.addCLO(cloAppend);
			prsr.addCLO(cloExport);
		}
		prsr.addCLO(cloRestore);
		prsr.addCLO(cloHelp);
		super.collectCLO(prsr);
		// some options are not meaningful when running simulations
		if (!isApplication) {
			// --run does not make sense for simulations
			prsr.removeCLO(cloRun);
		}
	}

	public Plist readPlist(String name) {
		try {
			String content = new String(Files.readAllBytes(Paths.get(name)));
			return PlistParser.parse(content);
		} catch (Exception e) {
			logger.warning("failed to read state in '" + name + "'");
			// e.printStackTrace(); // for debugging
			return null;
		}
	}

	/**
	 * Helper method to generate a unique file name based on the
	 * <code>template</code>. If <code>template</code> sports and extension it is
	 * replaced by <code>extension</code> otherwise the <code>extension</code> is
	 * appended. If the <code>template</code> contains <code>%d</code> it is
	 * replaced by the current generation. Finally, if necessary, <code>"-n"</code>
	 * is appended to <code>template</code>, where <code>n</code> is a number, to
	 * make file name unique.
	 * 
	 * @param template  where the placeholder <code>%d</code> is replaced by current
	 *                  generation (if present).
	 * @param extension of file name
	 * @return unique file
	 */
	private File uniqueFile(String template, String extension) {
		String dir = exportdir==null?".":exportdir;
		if (!dir.endsWith(File.separator))
			dir += File.separator;
		template = dir+template;
		// replace/add extension
		if (!template.endsWith("." + extension)) {
			int ext = template.lastIndexOf('.');
			if (ext < 0)
				ext = template.length();
			template = template.substring(0, ext) + "." + extension;
		}
		if (template.contains("%d"))
			template = String.format(template, (int) activeModel.getTime());
		File unique = new File(template);
		int counter = 0;
		while (!fileCheck(unique, true) && counter < 1000) {
			unique = new File(template.substring(0, template.lastIndexOf('.')) + "-" + (++counter) + "." + extension);
		}
		// check if emergency brake was pulled
		if (counter >= 1000)
			return null;
		return unique;
	}

	/**
	 * Helper method to check whether <code>file</code> is writable.
	 * 
	 * @param file   name of file to check
	 * @param unique if <code>true</code> file must not exist
	 * @return <code>true</code> if all checks passed
	 */
	private boolean fileCheck(File file, boolean unique) {
		if (file.isDirectory()) {
			logger.warning("'" + file.getPath() + "' is a directory");
			return false;
		}
		if (unique || !file.exists()) {
			try {
				if (!file.createNewFile()) {
					logger.warning("file '" + file.getPath() + "' already exists");
					return false;
				}
			} catch (IOException io) {
				logger.warning("failed to create file '" + file.getPath() + "'");
				return false;
			}
		}
		if (!file.canWrite()) {
			logger.warning("file '" + file.getPath() + "' not writable");
			return false;
		}
		return true;
	}

	public void setExportDir(File dir) {
		exportdir = dir.getAbsolutePath();
	}

	@Override
	public void exportState() {
		exportState(null);
	}

	private void exportState(String filename) {
		File export;
		if (filename == null)
			export = openSnapshot("plist");
		else
			export = uniqueFile(filename, "plist");
		String state = encodeState();
		if (state == null) {
			logger.severe("failed to encode state.");
			return;
		}
		try {
			// if export==null this throws an exception
			PrintStream stream = new PrintStream(export);
			stream.println(state);
			stream.close();
			logger.info("state saved in '" + export.getName() + "'.");
		} catch (Exception e) {
			String msg = "";
			if (export != null)
				msg = "to '" + export.getPath() + "' ";
			else if (filename != null)
				msg = "to '" + filename + ".plist' ";
			logger.warning("failed to export state " + msg + "- using '"
					+ (cloAppend.isSet() ? cloAppend.getArg() : cloOutput.getArg()) + "'");
			output.println(state);
		}
	}

	/**
	 * Open file with name <code>evoludo-%d</code> (the placeholder <code>%d</code>
	 * is replaced by current generation) and extension <code>ext</code>. Used to
	 * export snapshots of the current state or other data. Ensures that file does
	 * not exist and, if necessary, appends <code>-n</code> to the file name where
	 * <code>n</code> is a number that makes the name unique.
	 * <p>
	 * <b>Note:</b> almost copy from MVAbstract, better organization could prevent
	 * the duplication...
	 * 
	 * @param ext file name extension
	 * @return new unique file
	 */
	protected File openSnapshot(String ext) {
		String dir = exportdir==null?".":exportdir;
		if (!dir.endsWith(File.separator))
			dir += File.separator;
		File snapfile = new File(dir+String.format("evoludo-%d." + ext, (int) activeModel.getTime()));
		int counter = 0;
		while (snapfile.exists() && counter < 1000)
			snapfile = new File(dir+String.format("evoludo-%d-%d." + ext, (int) activeModel.getTime(), ++counter));
		if (counter >= 1000)
			return null;
		return snapfile;
	}

	/**
	 * Generic main entry point for simulations. The constructor is determined by
	 * the "Engine-Class" entry in the MANIFEST file and instantiated through
	 * reflection.
	 * <p>
	 * <strong>Notes:</strong> (as of 20191223 likely only of historical
	 * interest)<br>
	 * At least on Mac OS X.6 (Snow Leopard) initializing static Color makes the JRE
	 * ignore the headless request. The behavior is inconsistent and hence most
	 * likely a cause of Apple's java implementation. In particular, if
	 * -Djava.awt.headless=true is specified on the command line, the request is
	 * honored but not by setting the system property. Fortunately an easy
	 * work-around exists in that the colors (and potentially other AWT classes)
	 * should only be instantiated in the constructor and not along with the
	 * variable declaration. probably the JRE switches to headless=false once it
	 * encounters a static allocation of AWT classes. Since this happens even before
	 * entering main(), subsequent requests to set headless may fail/be ignored.
	 * </p>
	 * 
	 * @param args command line arguments
	 */
	public static void main(final String[] args) {
		System.setProperty("java.awt.headless", "true");
		String main = "a ghost";
		// automatic instantiation of desired class!
		try {
			EvoLudoJRE engine = new EvoLudoJRE();
			engine.isApplication = false;
			main = EvoLudoJRE.getAttribute("Engine-Class");
			Population pop = (Population) Class.forName(main).getDeclaredConstructor(EvoLudo.class).newInstance(engine);
			// prepend --game option (any potential additional --game options are ignored) 
			String[] extargs = new String[args.length+2];
			System.arraycopy(args, 0, extargs, 2, args.length);
			extargs[0] = "--game";
			extargs[1] = pop.getKey();
			engine.parseCLO(extargs);
			pop.setOutput(engine.getOutput());
			// reset model to check and apply all parameters
			engine.modelReset();
			pop.exec();
			// close output stream if needed
			PrintStream out = engine.getOutput();
			if (!out.equals(System.out))
				out.close();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			// log directly to System.out - no other outlets available at this time
			Logger.getLogger(EvoLudo.class.getName()).severe("Failed to instantiate " + main);
			System.exit(1);
		}
	}
}
