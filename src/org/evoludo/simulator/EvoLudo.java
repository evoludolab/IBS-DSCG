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

package org.evoludo.simulator;

import java.awt.Color;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.evoludo.simulator.Model.Mode;
import org.evoludo.simulator.Model.Type;
//import org.evoludo.simulator.lab.AsymmetricTBT;
//import org.evoludo.simulator.lab.CDL;
//import org.evoludo.simulator.lab.CDLP;
//import org.evoludo.simulator.lab.CDLPQ;
//import org.evoludo.simulator.lab.CG;
//import org.evoludo.simulator.lab.CLabour;
import org.evoludo.simulator.lab.CSD;
//import org.evoludo.simulator.lab.DemesTBT;
//import org.evoludo.simulator.lab.Dialect;
//import org.evoludo.simulator.lab.EcoMoran;
//import org.evoludo.simulator.lab.EcoPGG;
//import org.evoludo.simulator.lab.EcoTBT;
//import org.evoludo.simulator.lab.EvoLudoTest;
//import org.evoludo.simulator.lab.Moran;
//import org.evoludo.simulator.lab.Motility;
//import org.evoludo.simulator.lab.Mutualism;
//import org.evoludo.simulator.lab.NetGames;
//import org.evoludo.simulator.lab.RSP;
//import org.evoludo.simulator.lab.TwoByTwo;
//import org.evoludo.simulator.models.ODEEuler.ODEDelegate;
//import org.evoludo.simulator.models.PDEReactionDiffusion.PDEDelegate;
//import org.evoludo.simulator.models.PDESupervisor;
//import org.evoludo.simulator.models.SDEEuler.SDEDelegate;
import org.evoludo.util.CLOParser;
import org.evoludo.util.CLOProvider;
import org.evoludo.util.CLOption;
import org.evoludo.util.MersenneTwister;
import org.evoludo.util.CLOption.CLODelegate;
import org.evoludo.util.Plist;
import org.evoludo.util.RNGDistribution;
import org.evoludo.util.XMLCoder;

/**
 * GWT/JRE neutral abstract implementation of interface between EvoLudo core and
 * GUI interface. JRE specific code relegated to
 * {@link org.evoludo.jre.simulator.EvoLudoJRE EvoLudoJRE} and GWT specific code
 * to {@link org.evoludo.gwt.simulator.EvoLudoGWT EvoLudoGWT}.
 * 
 * @author Christoph Hauert
 */
public abstract class EvoLudo implements ModelListener, CLOProvider, MersenneTwister.Chronometer {

	public interface Directive {
		public void execute();
	}

	/**
	 * Construct instance of the EvoLudo controller. EvoLudo manages different
	 * model/game implementations and the connection between simulations/numerical
	 * calculations and the GUI interface as well as the execution environment. This
	 * includes logging (verbosity and output channels), restoring previously saved
	 * engine states as well saving the current state of the engine, export its data
	 * or graphical snapshots.
	 */
	public EvoLudo() {
		logger = Logger.getLogger(EvoLudo.class.getName() + "-" + ID);
		addCLOProvider(this);
		addListener(this);
		// load all available games
//		new Moran(this);
//		new EcoMoran(this);
//		new Motility(this);
//		new TwoByTwo(this);
//		new DemesTBT(this);
//		new EcoTBT(this);
//		new RSP(this);
//		new CDL(this);
//		new CDLP(this);
//		new CDLPQ(this);
//		new EcoPGG(this);
		new CSD(this);
//		new CLabour(this);
//		new Dialect(this);
//		new NetGames(this);
//		new Mutualism(this);
//		new CG(this);
//		new AsymmetricTBT(this);
//		new EvoLudoTest(this);
	}

	/**
	 * <code>true</code> when running as GWT application; <code>false</code> for JRE
	 * applications.
	 */
	public static boolean isGWT = false;

	/**
	 * <code>true</code> if device/program supports touch
	 * <p>
	 * <strong>Note:</strong> cannot be <code>static</code> or <code>final</code> to
	 * allow disabling touch events for debugging (see
	 * {@link org.evoludo.gwt.simulator.EvoLudoGWT#cloGUIFeatures
	 * EvoLudoGWT.cloGUIFeatures}).
	 */
	public boolean hasTouch = false;

	/**
	 * Loggers of each EvoLudo lab instance need to have unique names to keep the
	 * logs separate. Use <code>IDcounter</code> to generate unique
	 * <code>ID</code>'s.
	 */
	private static int IDcounter = 0;

	/**
	 * Unique <code>ID</code> of EvoLudo instance.
	 */
	public final int ID = IDcounter++;

	/**
	 * Instance of java.util.logging.Logger to manage notifications
	 */
	protected Logger logger;

	/**
	 * <strong>Important:</strong> this is the only RNG that should be used for
	 * simulations, i.e. must be shared with other populations and other
	 * distributions (e.g. for mutations, migration...) to ensure reproducibility of
	 * simulations! for the same reason the RNG must NOT be shared with layout
	 * procedures (although it may be desirable to set a seed for the layout
	 * routines if one was set for the simulations, to ensure visual
	 * reproducibility).
	 */
	protected RNGDistribution rng = new RNGDistribution.Uniform();

	/**
	 * Get random number generator
	 * 
	 * @return
	 */
	public RNGDistribution getRNG() {
		return rng;
	}

	/**
	 * Lookup table for all games available.
	 */
	protected HashMap<String, Population> games = new HashMap<String, Population>();

	/**
	 * Instance of individual based simulation (IBS) model
	 */
	protected Model.IBS ibs;

	/**
	 * Instance of ordinary differential equations (ODE) model
	 */
//	protected Model.ODE ode;

	/**
	 * Instance of stochastic differential equations (SDE) model
	 */
//	protected Model.SDE sde;

	/**
	 * Instance of partial differential equations (PDE) model
	 */
//	protected Model.PDE pde;

	/**
	 * Active model
	 */
	protected Model activeModel;

	/**
	 * Active game (eventually this should replace 'population' and be distinct from 'ibs')
	 */
	protected Population game;

	/**
	 * Some parameter changes require that the population state is reset. For
	 * example, changing the population size (see {@link Population#nPopulation})
	 * requires a reset to (re)generate population geometries and initialize
	 * types/strategies.
	 */
	private boolean resetRequested = true;

	protected void requiresReset(boolean reset) {
		resetRequested |= reset;
	}

	/**
	 * <code>true</code> if sample for statistics has been read; active model may
	 * start working on next sample.
	 */
	protected boolean statisticsSampleRead = true;

	protected void readStatisticsSample() {
		// check if new sample completed
		if (!statisticsSampleRead) {
			activeModel.readStatisticsSample();
			statisticsSampleRead = true;
		}
	}

	protected void initStatisticsSample() {
		statisticsSampleRead = false;
		activeModel.initStatisticsSample();
	}

	/**
	 * List of engine listeners that get notified when the state of the population
	 * changed, for example after population reset or completed an update step (see
	 * {@link ModelListener} for details).
	 */
	protected List<ModelListener> engineListeners = new ArrayList<ModelListener>();

	/**
	 * @param newListener to add to list that gets notified when state of population
	 *                    changes.
	 */
	public void addListener(ModelListener newListener) {
		engineListeners.add(newListener);
	}

	/**
	 * @param obsoleteListener to remove from list of listeners.
	 */
	public void removeListener(ModelListener obsoleteListener) {
		engineListeners.remove(obsoleteListener);
	}

	/**
	 * Load frameworks for individual based simulations as well as for numerical
	 * integration of ODE/SDE/PDE models, if appropriate. Notifies all registered
	 * {@link ModelListener}'s.
	 * <p>
	 * <strong>Note:</strong> the type of model will be determined only after
	 * processing the command line options.
	 */
	public void modelLoad() {
		// clear any previously set model types
		cloModel.clearKeys();
//		if (game instanceof IBSDelegate) 
			cloModel.addKey(Type.IBS);
//		if (game instanceof ODEDelegate)
//			cloModel.addKey(Type.ODE);
//		if (game instanceof SDEDelegate)
//			cloModel.addKey(Type.SDE);
//		if (game instanceof PDEDelegate)
//			cloModel.addKey(Type.PDE);
		game.load();
		if (cloModel.isValidKey(Model.Type.IBS)) {
			if (ibs == null)
				ibs = game;
//				ibs = game.createIBS();
//			addCLOProvider(ibs);
		}
		else {
//			removeCLOProvider(ibs);
			ibs = null;
		}
//		if (cloModel.isValidKey(Model.Type.ODE) && game.cloGeometry.isValidKey(Geometry.MEANFIELD)) {
//			if (ode == null)
//				ode = game.createODE();
//			addCLOProvider(ode);
//		}
//		else {
//			removeCLOProvider(ode);
//			ode = null;
//		}
//		if (cloModel.isValidKey(Model.Type.SDE) && game.cloGeometry.isValidKey(Geometry.MEANFIELD)) {
//			if (sde == null)
//				sde = game.createSDE();
//			addCLOProvider(sde);
//		}
//		else {
//			removeCLOProvider(sde);
//			sde = null;
//		}
//		if (cloModel.isValidKey(Model.Type.PDE) && game.cloGeometry.isValidKey(Geometry.SQUARE)) {
//			if (pde == null)
//				pde = game.createPDE();
//			addCLOProvider(pde);
//		}
//		else {
//			removeCLOProvider(pde);
//			pde = null;
//		}
		// the trait count in each population is only known after load()
		ArrayList<Population> species = game.getSpecies();
		int offset = 0;
		for (Population pop : species) {
//XXX share DE models with populations - should not be needed; currently still needed for data
//	  retrieval methods (for example DPopulation.getMeanTrait)
//pop.ode = ode;
//pop.sde = sde;
//pop.pde = pde;
			pop.deOffset = offset;
			offset += pop.getNTraits();
		}
//		if (ode!=null)
//			ode.setSpecies(species);
//		if (sde!=null)
//			sde.setSpecies(species);
//		if (pde!=null)
//			pde.setSpecies(species);
		fireModelLoaded();
	}

	/**
	 * Unload model framework. Notifies all registered {@link ModelListener}'s.
	 */
	public void modelUnload() {
		game.unload();
		// unload models
		if( ibs!=null ) {
//			removeCLOProvider(ibs);
			ibs.dealloc();
			ibs = null;
		}
//		if( ode!=null ) {
//			removeCLOProvider(ode);
//			ode.dealloc();
//			ode = null;
//		}
//		if( sde!=null ) {
//			removeCLOProvider(sde);
//			sde.dealloc();
//			sde = null;
//		}
//		if( pde!=null ) {
//			removeCLOProvider(pde);
//			pde.dealloc();
//			pde = null;
//		}
		fireModelUnloaded();
	}

	/**
	 * Check consistency of parameters in all populations.
	 * <p>
	 * <strong>Note:</strong> in multi-species interactions optimizations seem sensible only
	 * if all populations involved approve of them.
	 * 
	 * @return <code>true</code> if reset is required
	 */
	public final boolean modelCheck() {
		boolean doReset = false;
//XXX IBS needs to be able to manage several populations on its own!
//		switch (activeModel.getModelType()) {
//			case ODE:
//			case SDE:
//			case PDE:
//XXX DemesTBT.check(Model) requires geometries, which get initialized in check - is it always appropriate
//	  to call delegate.check _after_ checking model?!
//				return model.check();
//				doReset |= activeModel.check();
//XXX temporary - delegate should always be set (otherwise model would not get initialized)
//				return doReset | activeModel.getDelegate().check(model);
//				if (activeModel.getDelegate() != null)
//					doReset |= activeModel.getDelegate().check(activeModel);
//				return doReset;
//			case IBS:
//			default:
				for (Population pop : game.getSpecies())
					doReset |= pop.check();
//		}
//XXX this optimization business needs to be dealt with by IBS
		boolean oHomo = true;
		boolean oMoran = true;
		for (Population pop : game.getSpecies()) {
			oHomo &= pop.optimizeHomo;
			oMoran &= pop.optimizeMoran;
		}
		if (oHomo && game.isMultispecies) {
			// requires: (1) uniform mutation rates and (2) uniform population update rates
			// across species
			double pMutation = game.getMutationProb();
			double rUpdate = game.getSpeciesUpdateRate();
			for (Population pop : game.getSpecies()) {
				if (pop == game)
					continue;
				if (Math.abs(pop.getMutationProb() - pMutation) > 1e-8) {
					oHomo = false;
					logger.warning(
							"optimizations to skip homogeneous populations disabled - uniform mutation probabilities across species required.");
					doReset = true;
					break;
				}
				if (Math.abs(pop.getSpeciesUpdateRate() - rUpdate) > 1e-8) {
					oHomo = false;
					logger.warning(
							"optimizations to skip homogeneous populations disabled - uniform population update rates across species required.");
					doReset = true;
					break;
				}
			}
		}
		// optimizations only make sense if all populations are on board
		for (Population pop : game.getSpecies()) {
			pop.optimizeHomo = oHomo;
			pop.optimizeMoran = oMoran;
		}
//XXX temporary - delegate should always be set (otherwise model would not get initialized)
//		return doReset | activeModel.getDelegate().check(model);
		if (activeModel.getDelegate() != null)
			doReset |= activeModel.getDelegate().check(activeModel);
		return doReset;
	}

	/**
	 * Reset all populations and notify all listeners.
	 */
	public final void modelReset() {
		// reset random number generator if seed was specified
		if (rng.isRNGSeedSet())
			rng.setRNGSeed();
		// check consistency of parameters in models
		modelCheck();
//		switch (activeModel.getModelType()) {
//			case ODE:
//			case SDE:
//			case PDE:
//				activeModel.reset();
//				break;
//			case IBS:
//			default:
				for (Population pop : game.getSpecies())
					pop.reset();
//		}
//XXX temporary - delegate should always be set (otherwise model would not get initialized)
//		model.getDelegate().reset(model);
		if (activeModel.getDelegate() != null)
			activeModel.getDelegate().reset(activeModel);
		resetRequested = false;
		modelInit();
		modelUpdate();
		// notify of reset
		fireModelReset();
	}

	/**
	 * Initialize all populations (includes strategies but not structures).
	 */
	public final void modelInit() {
		initStatisticsSample();
//XXX IBS needs to be able to manage several populations on its own!
//		switch (activeModel.getModelType()) {
//			case ODE:
//			case SDE:
//			case PDE:
//				activeModel.init();
//				break;
//			case IBS:
//			default:
				for (Population pop : game.getSpecies())
					pop.init();
//		}
//XXX temporary - delegate should always be set (otherwise model would not get initialized)
//		model.getDelegate().init(model);
		if (activeModel.getDelegate() != null)
			activeModel.getDelegate().init(activeModel);
	}

	/**
	 * Re-init all populations and notify all listeners.
	 */
	public final void modelReinit() {
		modelInit();
		modelUpdate();
		fireModelReinit();
	}

	/**
	 * Update all populations.
	 */
	public final void modelUpdate() {
		activeModel.update();
	}

	/**
	 * Advance simulation by one step (<code>reportFreq</code> updates) and notify
	 * all listeners.
	 * 
	 * @return <code>true</code> if not converged, i.e. if <code>modelNext()</code>
	 *         can be called again.
	 */
	public final boolean modelNext() {
		fireModelRunning();
		if( activeModel.isAsynchronous() ) {
			activeModel.next();
			return true;
		}
		return modelNextDone(activeModel.next());
	}

	/**
	 * Callback for asynchronous PDE calculations (GWT only).
	 * 
	 * @param cont <code>false</code> if converged or halting time reached
	 * @return <code>true</code> if not converged, i.e. if <code>modelNext()</code>
	 *         can be called again.
	 */
	public final boolean modelNextDone(boolean cont) {
		if (!cont) {
			fireModelStopped();
			return false;
		}
		fireModelChanged();
		return true;
	}

	/**
	 * Check if model has reached a steady state
	 * 
	 * @return true if model has converged
	 */
	public final boolean modelConverged() {
//XXX IBS needs to be able to manage several populations on its own!
//		switch (activeModel.getModelType()) {
//			case ODE:
//			case SDE:
//			case PDE:
//				return activeModel.hasConverged();
//			case IBS:
//			default:
				boolean converged = true;
				for (Population pop : game.getSpecies())
					converged &= pop.hasConverged();
				return converged;
//		}
	}

	/**
	 * Called after parameters have changed. Checks new settings and resets
	 * population(s) (and/or GUI) if necessary.
	 * 
	 * @return <code>true</code> if reset was necessary
	 */
	public boolean paramsDidChange() {
		if (resetRequested) {
			modelReset();
			return true;
		}
		if (modelCheck()) {
			modelReset();
			return true;
		}
		modelUpdate();
		return false;
	}

	/**
	 * @return <code>true</code> if model type changed
	 */
	public boolean setModelType(Type type) {
		Type old = null;
		if (activeModel!=null)
			old = activeModel.getModelType();
		boolean changed = (old!=type);
//XXX consider instantiating only active model
//		switch (type) {
//			case ODE:
//				activeModel = ode;
//				if (ibs != null)
//					ibs.dealloc();
//				if (sde != null)
//					sde.dealloc();
//				if (pde != null)
//					pde.dealloc();
//				break;
//			case SDE:
//				activeModel = sde;
//				if (ibs != null)
//					ibs.dealloc();
//				if (ode != null)
//					ode.dealloc();
//				if (pde != null)
//					pde.dealloc();
//				break;
//			case PDE:
//				activeModel = pde;
//				if (ibs != null)
//					ibs.dealloc();
//				if (ode != null)
//					ode.dealloc();
//				if (sde != null)
//					sde.dealloc();
//				break;
//			case IBS:
//			default:
				activeModel = ibs;
//				if (ode != null)
//					ode.dealloc();
//				if (sde != null)
//					sde.dealloc();
//				if (pde != null)
//					pde.dealloc();
//		}
		requiresReset(changed);
		return changed;
	}

	/**
	 * @return active model
	 */
	public Model getModel() {
		return activeModel;
	}

	/**
	 * @return current status of the model:
	 *         <ul>
	 *         <li>after launch or reset, display version information;</li>
	 *         <li>if status message has been explicitly set, e.g. when logging a
	 *         message with severity {@link Level#WARNING} or higher, the log
	 *         message is shown;</li>
	 *         <li>otherwise query population(s) for current status.</li>
	 *         </ul>
	 */
	public final String modelStatus() {
		return activeModel.getStatus();
	}

	public final String modelCounter() {
		return activeModel.getCounter();
	}

	/**
	 * Command line options (raw string provided in URL, HTML tag, TextArea or
	 * command line)
	 */
	protected String clo;

	public String getCLO() {
		return clo;
	}

	public void setCLO(String clo) {
		this.clo = clo;
	}

	/**
	 * Flag to indicate whether running of the model is suspended. For example while
	 * parameters are being applied. If the changes do not require a reset of the
	 * model the calculations are resumed after new parameters are applied. Also
	 * used when command line options are set to immediately start running after
	 * loading (see {@link #cloRun}).
	 */
	protected boolean isSuspended = false;

	/**
	 * @return <code>true</code> if model is suspended
	 */
	public boolean isSuspended() {
		return isSuspended;
	}

	/**
	 * Sets whether model is suspended. A suspended model resumes execution as soon
	 * as possible. For example after a new set parameters has been checked.
	 * 
	 * @param suspend <code>true</code> to indicate that model is suspended.
	 * @return <code>true</code> if suspension has changed
	 */
	public boolean setSuspended(boolean suspend) {
		if (isSuspended == suspend)
			return false;
		isSuspended = suspend;
		return true;
	}

	/**
	 * Command line option to restore state from file require special considerations
	 * (only applicable to JRE).
	 */
	protected boolean doRestore = false;

	/**
	 * Minimum delay between subsequent updates for speed slider
	 * {@link org.evoludo.gwt.ui.Slider}
	 */
	public static final double DELAY_MIN = 1.0;

	/**
	 * Maximum delay between subsequent updates for speed slider
	 * {@link org.evoludo.gwt.ui.Slider}
	 */
	public static final double DELAY_MAX = 10000.0;

	/**
	 * Initial delay between subsequent updates for speed slider
	 * {@link org.evoludo.gwt.ui.Slider}
	 */
	public static final double DELAY_INIT = 100.0;

	/**
	 * Delay decrement for speed slider {@link org.evoludo.gwt.ui.Slider}
	 */
	private static final double DELAY_INCR = 1.2;

	/**
	 * Delay between subsequent updates in milliseconds when model is running.
	 */
	protected int delay = (int) DELAY_INIT;

	/**
	 * Set delay between subsequent updates.
	 *
	 * @param delay in milliseconds
	 */
	public void setDelay(int delay) {
		this.delay = Math.min(Math.max(delay, (int) DELAY_MIN), (int) DELAY_MAX);
	}

	/**
	 * Get delay between subsequent updates.
	 *
	 * @return the delay in milliseconds
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * Increase delay between subsequent updates by fixed factor.
	 */
	public void increaseDelay() {
		int newdelay = delay;
		newdelay *= DELAY_INCR;
		if (newdelay == delay)
			newdelay++;
		setDelay(Math.min(newdelay, (int) DELAY_MAX));
	}

	/**
	 * Decrease delay between subsequent updates by fixed factor.
	 */
	public void decreaseDelay() {
		int newdelay = delay;
		newdelay /= DELAY_INCR;
		setDelay(Math.max(newdelay, (int) DELAY_MIN));
	}

	/**
	 * Load new model/game with key <code>newGameKey</code>. If necessary unload
	 * current model/game.
	 * 
	 * @param newGameKey the key of the game to load
	 */
	public void loadGame(String newGameKey) {
		Population newGame = games.get(newGameKey);
		if (newGame == null) {
			if (game != null) {
				logger.warning("game key '" + newGameKey + "' not found - keeping '" + game.getKey() + "'.");
				return; // leave as is
			}
			// simply pick random entry
			Collection<Population> mods = games.values();
			int nMods = mods.size();
			int idx = rng.random0n(nMods);
			for (Population mod: mods) {
				if (idx-- > 0)
					continue;
				newGame = games.get(mod.getKey());				
			}
			logger.warning("game key '" + newGameKey + "' not found - loading random module '"+newGame.getKey()+"'.");
		}
		if (game == newGame && game.isLoaded)
			return;
		if (game != newGame)
			unloadGame();
		game = newGame;
		modelLoad();
		addCLOProvider(game);
	}

	/**
	 * Unload current model/game to free up resources.
	 * <p>
	 * Note: Called from loadGame to first unload current game or triggered by
	 *		 GWT's onUnload, i.e. when unloading GWT application. In both cases
	 *		 the model has stopped running (either through PengingAction.APPLY or
	 *		 PendingAction.UNLOAD) and hence no need to issue further requests.
	 */
	public void unloadGame() {
		if (game == null)
			return;
		removeCLOProvider(game);
		modelUnload();
		game = null;
	}

	/**
	 * Add <code>newgame</code> to lookup table of games using the game's key. If a GUI
	 * is present, add GUI as a listener of <code>game</code> to get notified about
	 * state changes.
	 * 
	 * @param newgame to add to lookup table
	 */
	public void addGame(Population newgame) {
		games.put(newgame.getKey(), newgame);
		cloGame.addKey(newgame.getKey(), newgame.getTitle());
	}

	/**
	 * @return currently active game/model
	 */
	public Population getGame() {
		return game;
	}

	/**
	 * Execute <code>directive</code> in JRE or GWT environments.
	 * 
	 * @param directive the directive to execute in appropriate GWT or JRE manners
	 */
	public abstract void execute(Directive directive);

	/**
	 * <code>true</code> if engine is currently running
	 */
	protected boolean isRunning = false;

	/**
	 * @return <code>true</code> if model is running
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Notification from GUI that layout process has finished. Opportunity for
	 * taking snapshots.
	 */
	public void layoutComplete() {
	}

	public void stop() {
		if (isRunning)
			requestAction(PendingAction.STOP);
	}

	public void toggle() {
		if (isRunning)
			stop();
		else
			run();
	}

	/**
	 * Start the EvoLudo model and calculate the dynamics one step at a time.
	 */
	public abstract void run();

	/**
	 * Advances the EvoLudo model by a single step. Called when pressing the 'Next'
	 * button, the 'N' or 'right-arrow' key.
	 */
	public abstract void next();

	@Override
	public void modelStopped() {
		isRunning = false;
	}

	@Override
	public void modelDidReinit() {
		isRunning = false;
	}

	@Override
	public void modelDidReset() {
		isRunning = false;
	}

	/**
	 * 
	 */
	protected PendingAction pendingAction = PendingAction.NONE;

	/**
	 * @param action
	 */
	public synchronized void requestAction(PendingAction action) {
		pendingAction = action;
		// if model is not running process request immediately
		if (!isRunning) {
			_fireModelChanged();
		}
	}

	/**
	 * Called whenever a new model has finished loading. Notifies all registered
	 * {@link ModelListener}s.
	 */
	public synchronized void fireModelLoaded() {
		runFired = false;
		pendingAction = PendingAction.NONE;
		for (ModelListener i : engineListeners)
			i.modelLoaded();
		logger.info("Game '" + game.getTitle() + "' loaded\n" + game.getInfo() + "\nVersion: " + getVersion());
	}

	/**
	 * Called whenever the current model has finished unloading. Notifies all
	 * registered {@link ModelListener}s.
	 */
	public synchronized void fireModelUnloaded() {
		runFired = false;
		for (ModelListener i : engineListeners)
			i.modelUnloaded();
		logger.info("Game '" + game.getTitle() + "' unloaded");
	}

	private boolean runFired = false;

	/**
	 * Called whenever the model starts its calculations. Fires only when starting
	 * to run. Notifies all registered {@link ModelListener}s.
	 */
	public synchronized void fireModelRunning() {
		if (runFired)
			return;
		for (ModelListener i : engineListeners)
			i.modelRunning();
		runFired = isRunning();
	}

	/**
	 * Called whenever the state of the population has changed. For example to
	 * trigger the update of the state displayed in the GUI. Notifies all registered
	 * {@link ModelListener}s.
	 */
	public synchronized void fireModelChanged() {
		// any specific request causes model to stop (and potentially resume later)
		if (pendingAction != PendingAction.NONE)
			runFired = false;
		if (activeModel.isMode(Mode.DYNAMICS)) {
			_fireModelChanged();
		}
	}

	private void _fireModelChanged() {
		switch (pendingAction) {
			case UNLOAD:
				unloadGame();
				pendingAction = PendingAction.NONE;
				return;
			case INIT:
				modelReinit();
				break;
			case RESET:
				modelReset();
				break;
			case STOP:
				// stop requested (as opposed to simulations that stopped)
				runFired = false;
				for (ModelListener i : engineListeners)
					i.modelStopped();
				break;
			default:
				for (ModelListener i : engineListeners)
					i.modelChanged(pendingAction);
		}
		pendingAction = PendingAction.NONE;
	}

	/**
	 * Called after the state of the population has been restored either through
	 * drag'n'drop with the GWT GUI or through the <code>--restore</code> command
	 * line argument. Notifies all registered {@link ModelListener}s.
	 * 
	 * @see org.evoludo.gwt.simulator.EvoLudoWeb#restoreFromFile(String, String)
	 *      EvoLudoWeb.restoreFromFile(String, String)
	 * @see EvoLudo#restoreState(Plist)
	 */
	public synchronized void fireModelRestored() {
		runFired = false;
		for (ModelListener i : engineListeners)
			i.modelRestored();
		logger.info("Engine restored.");
	}

	/**
	 * Called after the population is re-initialized. Notifies all registered
	 * {@link ModelListener}s.
	 */
	public synchronized void fireModelReinit() {
		if (activeModel.isMode(Mode.DYNAMICS)) {
			runFired = false;
			for (ModelListener i : engineListeners)
				i.modelDidReinit();
			logger.info("Engine init.");
		}
	}

	/**
	 * Called after the population is reset. Notifies all registered
	 * {@link ModelListener}s.
	 */
	public synchronized void fireModelReset() {
		runFired = false;
		for (ModelListener i : engineListeners)
			i.modelDidReset();
		logger.info("Engine reset.");
	}

	/**
	 * Called after the population has reached an absorbing state (or has converged
	 * to an equilibrium state). Notifies all registered {@link ModelListener}s.
	 */
	public synchronized void fireModelStopped() {
		// check if new sample completed
		readStatisticsSample();
		if (activeModel.isMode(Mode.DYNAMICS)) {
			// MODE_DYNAMICS
			runFired = false;
			for (ModelListener i : engineListeners)
				i.modelStopped();
		} else {
			// MODE_STATISTICS
			// note: calling fireModelChanged doesn't work because MODE_STATISTICS
			// prevents firing
			if (pendingAction == PendingAction.NONE)
				pendingAction = PendingAction.STATISTIC;
			_fireModelChanged();
		}
	}

	@Override
	public void modelChanged(PendingAction action) {
		switch (action) {
			case APPLY:
				if (isRunning) {
					isSuspended = true;
					isRunning = false;
				}
				break;
			case SNAPSHOT:
				isRunning = false;
				break;
			default:
		}
	}

	/**
	 * <strong>Note:</strong> Instead of sharing logging system, EvoLudo could
	 * implement helper routines for logging notifications. However, when logging
	 * notifications with a severity of {@link Level#WARNING} or higher the default
	 * logging message includes the name of the calling routine and hence would
	 * always refer to the (unhelpful) helper routines.
	 * 
	 * @return logger of this EvoLudo controller
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * GWT/JRE require different instances of <code>PDESupervisor</code>'s - use
	 * EvoLudo controller as factory. GWT uses asynchronous execution to prevent the
	 * GUI from stalling, while JRE implementations take advantage of multiple
	 * threads for significantly faster execution due to parallelization.
	 * 
	 * @return supervisor for coordinating PDE calculations
	 */
//	public abstract PDESupervisor hirePDESupervisor();

	/**
	 * Hide GWT/JRE differences in measuring execution time.
	 */
	@Override
	public abstract int elapsedTimeMsec();

	/**
	 * Copyright string
	 */
	public static final String COPYRIGHT = "\u00a9 Christoph Hauert"; // \u00a9 UTF-8 character code for ©

	/**
	 * Return version string of current model. Version must include reference to git
	 * commit to ensure reproducibility of results.
	 * 
	 * @return version string
	 */
	public abstract String getVersion();

	/**
	 * Report all parameter settings to <code>output</code> (JRE only).
	 */
	public void dumpParameters() {
	}

	/**
	 * Concluding words for report (JRE only).
	 */
	public void dumpEnd() {
	}

	/**
	 * Export the current state of the engine using the appropriate means available
	 * in the current environment (GWT/JRE).
	 */
	public abstract void exportState();

	/**
	 * Encode current state of EvoLudo model as XML string (plist format).
	 * 
	 * @return encoded state
	 */
	public String encodeState() {
		StringBuilder plist = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n"
				+ "<plist version=\"1.0\">\n" + "<dict>\n");
		plist.append(encodeKey("Export date", new Date().toString()));
		plist.append(encodeKey("Title", game.getTitle()));
		plist.append(encodeKey("Version", game.getVersion()));
		plist.append(encodeKey("CLO", "--game " + game.getKey() + " " + parser.getCLO()));
		plist.append(encodeKey("Generation", activeModel.getTime()));
		plist.append(encodeKey("Realtime", game.getRealtime()));
		plist.append(encodeKey("Model", activeModel.getModelType().toString()));
//		switch (activeModel.getModelType()) {
//			case PDE:
//				pde.encodeGeometry(plist);
//				//$FALL-THROUGH$
//			case ODE:
//			case SDE:
//				activeModel.encodeStrategies(plist);
//				activeModel.encodeFitness(plist);
//				break;
//			case IBS:
				boolean isMultiSpecies = (game.getNSpecies() > 1);
				for (Population pop : game.getSpecies()) {
					if (isMultiSpecies)
						plist.append("<key>" + pop.getName() + "</key>\n" + "<dict>\n");
					pop.encodeGeometry(plist);
					pop.encodeStrategies(plist);
					pop.encodeFitness(plist);
					pop.encodeInteractions(plist);
					if (isMultiSpecies)
						plist.append("</dict>");
				}
//				break;
//			default:
//				logger.severe(
//						"unknown model type '" + activeModel.getModelType() + "' - encoding state failed.");
//				return null;
//		}
		// the mersenne twister state is pretty long (and uninteresting) keep at end
		plist.append(
				"<key>RNG state</key>\n" + "<dict>\n" + (rng.getRNG().encodeState()) + "</dict>\n");
		plist.append("</dict>\n" + "</plist>");
		return plist.toString();
	}

	/**
	 * Restore state of EvoLudo model from pre-processed plist, which encodes engine
	 * state (see {@link #encodeState()}).
	 * <p>
	 * <strong>Note:</strong> the appropriate model must already have been loaded
	 * and the command line arguments specified with the key <code>CLO</code> in the
	 * <code>plist</code> must also have been processed already.
	 * </p>
	 * <p>
	 * In JRE the options in <code>plist</code> are merged with any other command
	 * line arguments (albeit the ones in <code>plist</code> have priority to
	 * minimize the chance of complications). In GWT <code>restoreState(Plist)</code>
	 * is overridden to first deal with the command line arguments.
	 * </p>
	 * 
	 * @param plist lookup table with key value pairs
	 * @return <code>true</code> on successful restoration of state
	 */
	public boolean restoreState(Plist plist) {
		if (plist == null) {
			logger.severe("restore state failed (state empty).");
			return false;
		}
		// retrieve version
		String version = (String) plist.get("Version");
		if (version == null) {
			logger.severe("restore state failed (version missing).");
			return false;
		}
		// version check
		String restoreGit = version.substring(version.lastIndexOf(' '), version.lastIndexOf(')'));
		String myVersion = game.getVersion();
		String myGit = myVersion.substring(myVersion.lastIndexOf(' '), myVersion.lastIndexOf(')'));
		if (!myGit.equals(restoreGit))
			// versions differ - may or may not be a problem...
			logger.warning(
					"state generated by version " + restoreGit + " but this is " + myGit + " - proceed with caution.");

		// restore RNG generator, population structure and state
		double restoreGeneration = (Double) plist.get("Generation");
		double restoreRealtime = (Double) plist.get("Realtime");
		Plist rngstate = (Plist) plist.get("RNG state");
		boolean success = true;
		if (!rng.getRNG().restoreState(rngstate)) {
			logger.warning("restore RNG failed.");
			success = false;
		}
		boolean isMultiSpecies = (game.getNSpecies() > 1);
		if (isMultiSpecies) {
			for (Population pop : game.getSpecies()) {
				Plist pplist = (Plist) plist.get(pop.getName());
				success &= restorePop(pop, pplist, restoreGeneration, restoreRealtime);
			}
		} else {
			success &= restorePop(game, plist, restoreGeneration, restoreRealtime);
		}
		if (success) {
			logger.info("Restore succeeded.");
			fireModelRestored();
		} else {
			logger.warning("restore failed - resetting model.");
			modelReset();
		}
		doRestore = false;
		return success;
	}

	/**
	 * Helper method to restore state of model/population.
	 * 
	 * @param pop          model/population to restore
	 * @param plist        lookup table with state of model/population
	 * @param myGeneration generation time of restored state
	 * @param myTime       real time of restored state
	 * @return <code>true</code> on successful restoration of model/population
	 */
	private boolean restorePop(Population pop, Plist plist, double myGeneration, double myTime) {
		boolean success = true;
		Model.Type mt = Model.Type.parse((String) plist.get("Model"));
//		switch (mt) {
//			case ODE:
//				if (!ode.restoreStrategies(plist)) {
//					logger.warning("restore strategies in " + activeModel.getModelType() + "-model failed"
//							+ (pop.getNSpecies() > 1 ? " (" + pop.getName() + ")." : "."));
//					success = false;
//				}
//				if (!ode.restoreFitness(plist)) {
//					logger.warning("restore fitness in " + activeModel.getModelType() + "-model failed"
//							+ (pop.getNSpecies() > 1 ? " (" + pop.getName() + ")." : "."));
//					success = false;
//				}
//				break;
//			case SDE:
//				if (!sde.restoreStrategies(plist)) {
//					logger.warning("restore strategies in " + activeModel.getModelType() + "-model failed"
//							+ (pop.getNSpecies() > 1 ? " (" + pop.getName() + ")." : "."));
//					success = false;
//				}
//				if (!sde.restoreFitness(plist)) {
//					logger.warning("restore fitness in " + activeModel.getModelType() + "-model failed"
//							+ (pop.getNSpecies() > 1 ? " (" + pop.getName() + ")." : "."));
//					success = false;
//				}
//				break;
//			case PDE:
//				if (!pde.restoreGeometry(plist)) {
//					logger.warning("restore geometry in " + activeModel.getModelType() + "-model failed"
//							+ (pop.getNSpecies() > 1 ? " (" + pop.getName() + ")." : "."));
//					success = false;
//				}
//				if (!pde.restoreStrategies(plist)) {
//					logger.warning("restore strategies in " + activeModel.getModelType() + "-model failed"
//							+ (pop.getNSpecies() > 1 ? " (" + pop.getName() + ")." : "."));
//					success = false;
//				}
//				if (!pde.restoreFitness(plist)) {
//					logger.warning("restore fitness in " + activeModel.getModelType() + "-model failed"
//							+ (pop.getNSpecies() > 1 ? " (" + pop.getName() + ")." : "."));
//					success = false;
//				}
//				break;
//			case IBS:
				if (!pop.restoreGeometry(plist)) {
					logger.warning("restore geometry failed (" + pop.getName() + ").");
					success = false;
				}
				if (!pop.restoreInteractions(plist)) {
					logger.warning("restore interactions in " + activeModel.getModelType() + "-model failed"
							+ (pop.getNSpecies() > 1 ? " (" + pop.getName() + ")." : "."));
					success = false;
				}
				if (!pop.restoreStrategies(plist)) {
					logger.warning("restore strategies in " + activeModel.getModelType() + "-model failed"
							+ (pop.getNSpecies() > 1 ? " (" + pop.getName() + ")." : "."));
					success = false;
				}
				if (!pop.restoreFitness(plist)) {
					logger.warning("restore fitness in " + activeModel.getModelType() + "-model failed"
							+ (pop.getNSpecies() > 1 ? " (" + pop.getName() + ")." : "."));
					success = false;
				}
//				break;
//			default:
//				logger.severe("unknown model type '" + activeModel.getModelType() + "' - restoring state failed.");
//				success = false;
//		}
		if (!pop.restoreTime(myGeneration, myTime)) {
			logger.warning("restore time failed" + (pop.getNSpecies() > 1 ? " (" + pop.getName() + ")." : "."));
			success = false;
		}
		return success;
	}

	/**
	 * Parser for command line options.
	 */
	protected CLOParser parser;

	/**
	 * Register <code>clo</code> as a provider of command line options. Initialize
	 * command line parser if necessary.
	 * 
	 * @param provider option provider to add
	 */
	public void addCLOProvider(CLOProvider provider) {
		if (provider == null)
			return;
		if (parser == null) {
			parser = new CLOParser(provider);
			return;
		}
		parser.addCLOProvider(provider);
	}

	/**
	 * Unregister <code>clo</code> as a provider of command line options.
	 * 
	 * @param provider option provider to remove
	 */
	public void removeCLOProvider(CLOProvider provider) {
		if (parser == null || provider == null)
			return;
		parser.removeCLOProvider(provider);
	}

	/**
	 * @return processed command line options
	 */
	public String getProcessedCLO() {
		return "--game " + game.getKey() + parser.getCLO();
	}

	/**
	 * Pre-process array of command line arguments. Some arguments need priority
	 * treatment. Examples include the options <code>--game</code>,
	 * <code>--verbosity</code> or <code>--restore</code>.
	 * <ul>
	 * <li>process {@code --game} option, load game/model and remove option.</li>
	 * <li>process {@code --verbose} option and set verbosity level accordingly such
	 * that the desired verbosity level already applies to the parsing of command
	 * line arguments.</li>
	 * </ul>
	 * 
	 * @param cloarray array of command line arguments
	 * @return pre-processed array of command line options
	 * @see org.evoludo.jre.simulator.EvoLudoJRE#preprocessCLO(String[])
	 *      EvoLudoJRE#preprocessCLO(String[])
	 * @see org.evoludo.gwt.simulator.EvoLudoGWT#preprocessCLO(String[])
	 *      EvoLudoGWT#preprocessCLO(String[])
	 */
	protected String[] preprocessCLO(String[] cloarray) {
		// first deal with --game option
		String gameName = "--" + cloGame.getName();
		String newGameKey = null;
		int nArgs = cloarray.length;
		for (int i = 0; i < nArgs; i++) {
			String arg = cloarray[i];
			if (arg.startsWith(gameName)) {
				if (i + 1 == nArgs) {
					logger.warning("game key missing - use default.");
					// remove game option
					String[] args = new String[nArgs - 1];
					System.arraycopy(cloarray, 0, args, 0, nArgs - 1);
					cloarray = args;
					nArgs--;
					// must have been the last entry
					break;
				}
				newGameKey = cloarray[i + 1];
				// remove game option
				String[] args = new String[nArgs - 2];
				System.arraycopy(cloarray, 0, args, 0, i);
				System.arraycopy(cloarray, i + 2, args, i, nArgs - 2 - i);
				cloarray = args;
				nArgs -= 2;
				break;
			}
		}
		loadGame(newGameKey);
		// check if cloOptions contain --verbose
		CLOption verbose = game.cloVerbose;
		String verboseName = "--" + verbose.getName();
		for (int i = 0; i < nArgs; i++) {
			String arg = cloarray[i];
			if (arg.startsWith(verboseName)) {
				if (i + 1 == nArgs) {
					logger.warning("verbose level missing - ignored.");
					// remove verbose option
					String[] args = new String[nArgs - 1];
					System.arraycopy(cloarray, 0, args, 0, nArgs - 1);
					cloarray = args;
					// must have been the last entry
					break;
				}
				// parse --verbose first in order to set logging level already for processing of
				// command line arguments; gets processed again with all others but there is no
				// harm in it
				verbose.processOption(arg, Arrays.asList(new String[] { cloarray[i + 1] }).listIterator());
				verbose.parse();
				break;
			}
		}
		return cloarray;
	}

	/**
	 * Parse command line options.
	 *
	 * @return <code>true</code> if parsing successful and <code>false</code> if
	 *         problems occurred
	 * @see #parseCLO(String[])
	 */
	public boolean parseCLO() {
		return parseCLO(clo.split("\\s+"));
	}

	/**
	 * Pre-process and parse array of command line arguments.
	 *
	 * @param cloarray string array of command line arguments
	 * @return <code>true</code> if parsing successful and <code>false</code> if
	 *         problems occurred
	 * @see #preprocessCLO(String[])
	 * @see CLOParser#parseCLO(String[])
	 */
	public boolean parseCLO(String[] cloarray) {
		cloarray = preprocessCLO(cloarray);
		parser.setLogger(logger);
		parser.initCLO();
		return parser.parseCLO(cloarray);
	}

	/**
	 * Format, encode and output help on command line options.
	 */
	public abstract void helpCLO();

	/**
	 * Command line option to set game/model (GWT only, at present).
	 */
	protected final CLOption cloGame = new CLOption("game", CLOption.Argument.REQUIRED, "2x2",
			"--game <g>  select game from:", new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					// option gets special treatment by GWT and is ignored by JRE
					return true;
				}
			});

	/**
	 * Command line option to set the type of model
	 * (see {@link Model.Type}).
	 */
	public final CLOption cloModel = new CLOption("model", CLOption.Argument.REQUIRED,
			Model.Type.IBS.getKey(),
			"--model <m>  model type",
			new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					setModelType(Model.Type.parse(arg));
					return true;
				}

				@Override
				public void report(PrintStream output) {
					output.println("# model:                " + activeModel.getModelType());
				}
			});

	/**
	 * Command line option to set seed of random number generator.
	 */
	protected final CLOption cloSeed = new CLOption("seed", CLOption.Argument.OPTIONAL, "0",
			"--seed[=s]     random seed (0)", new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					if (!cloSeed.isSet()) {
						// set default
						rng.clearRNGSeed();
						return true;
					}
					rng.setRNGSeed(Long.parseLong(arg));
					return true;
				}

				@Override
				public void report(PrintStream output) {
					output.println("# fixedseed:            " + rng.isRNGSeedSet()
							+ (rng.isRNGSeedSet() ? " (" + rng.getRNGSeed() + ")" : ""));
				}
			});

	/**
	 * Command line option to request that the EvoLudo model immediately starts
	 * running after loading.
	 */
	protected final CLOption cloRun = new CLOption("run", CLOption.Argument.NONE, "norun",
			"--run                simulations run after launch", new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					// by default do not interfere - i.e. leave simulations running if possible
					if (!cloRun.isSet())
						return true;
					isSuspended = true;
					return true;
				}
			});

	/**
	 * Command line option to set the delay between subsequent updates.
	 */
	protected final CLOption cloDelay = new CLOption("delay", CLOption.Argument.REQUIRED, "" + DELAY_INIT, // DELAY_INIT
			"--delay <d>          delay between updates (d: delay in msec)", new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					// by default do not interfere - i.e. leave delay as is
					if (!cloDelay.isSet())
						return true;
					setDelay(Integer.parseInt(arg));
					return true;
				}
			});

	/**
	 * Command line option to set color scheme for coloring continuous traits:
	 * <dl>
	 * <dt>traits</dt>
	 * <dd>each trait has a color (see {@link Population#cloTraitColors}). The
	 * brightness of the color indicates the continuous trait. This is the
	 * default.</dd>
	 * <dt>distance</dt>
	 * <dd>color trait combinations as the distance from the origin (heat map
	 * ranging from black and grey to yellow and red).</dd>
	 * </dl>
	 * <strong>Note:</strong> currently only used by
	 * {@link org.evoludo.simulator.lab.Dialect}.
	 */
	protected final CLOption cloTraitColorScheme = new CLOption("traitcolorscheme", CLOption.Argument.REQUIRED,
			"traits",
			"--traitcolorscheme <m>  color scheme for traits\n" + "            <m>: traits (default); distance",
			new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					COLOR_MODEL_TYPE aColorModelType = COLOR_MODEL_TYPE.parse(arg);
					if (aColorModelType != colorModelType) {
						setColorModelType(aColorModelType);
					}
					return true;
				}
			});

	/**
	 * Command line option to perform test of random number generator on launch.
	 * This takes approximately 10-20 seconds. The test reports (1) whether the
	 * generated sequence of random numbers is consistent with the reference
	 * implementation of {@link MersenneTwister} and (2) the performance of
	 * MersenneTwister compared to {@link java.util.Random}.
	 */
	protected final CLOption cloRNG = new CLOption("testRNG", CLOption.Argument.NONE, "skip",
			"--testRNG   test random number generator", new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					if (!cloRNG.isSet())
						return true;
					// test of RNG requested
					logger.info("Testing MersenneTwister...");
					int start = elapsedTimeMsec();
					MersenneTwister.testCorrectness(logger);
					MersenneTwister.testSpeed(logger, EvoLudo.this);
					int lap = elapsedTimeMsec();
					logger.info("MersenneTwister tests done: " + ((lap - start) / 1000.0) + " sec.");
					MersenneTwister mt = rng.getRNG();
					RNGDistribution.Uniform.test(mt, logger, EvoLudo.this);
					RNGDistribution.Exponential.test(mt, logger, EvoLudo.this);
					RNGDistribution.Normal.test(mt, logger, EvoLudo.this);
					RNGDistribution.Geometric.test(mt, logger, EvoLudo.this);
					RNGDistribution.Binomial.test(mt, logger, EvoLudo.this);
					return true;
				}
			});

	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note:</strong> In contrast to other providers of command line
	 * options, the EvoLudo class maintains a reference to the parser
	 * (<code>prsr</code> and <code>parser</code> must be identical).
	 */
	@Override
	public void collectCLO(CLOParser prsr) {
		parser.addCLO(cloGame);
		parser.addCLO(cloModel);
		parser.addCLO(cloSeed);
		parser.addCLO(cloRun);
		parser.addCLO(cloDelay);
		parser.addCLO(cloRNG);
		parser.addCLO(cloTraitColorScheme);
	}

	/**
	 * Storage for coloring method type.
	 * <p>
	 * <strong>Note:</strong> currently only used by
	 * {@link org.evoludo.simulator.lab.Dialect}.
	 */
	protected COLOR_MODEL_TYPE colorModelType = COLOR_MODEL_TYPE.DEFAULT;

	/**
	 * Coloring method types for continuous traits (see
	 * {@link #cloTraitColorScheme}).
	 * <p>
	 * <strong>Note:</strong> currently only used by
	 * {@link org.evoludo.simulator.lab.Dialect}.
	 */
	public static enum COLOR_MODEL_TYPE {
		DEFAULT("default"), TRAITS("traits"), DISTANCE("distance");

		private final String name;

		COLOR_MODEL_TYPE(String name) {
			this.name = name;
		}

		boolean equals(String type) {
			return name.equalsIgnoreCase(type.trim());
		}

		static COLOR_MODEL_TYPE parse(String name) {
			if (name == null || name.length() == 0)
				return COLOR_MODEL_TYPE.DEFAULT;
			for (COLOR_MODEL_TYPE t : COLOR_MODEL_TYPE.values()) {
				if (t.equals(name))
					return t;
			}
			return COLOR_MODEL_TYPE.DEFAULT;
		}
	}

	/**
	 * @return Coloring method type for continuous traits (see
	 *         {@link #cloTraitColorScheme}).
	 */
	public COLOR_MODEL_TYPE getColorModelType() {
		return colorModelType;
	}

	/**
	 * @param colorModelType Coloring method type for continuous traits (see
	 *                       {@link #cloTraitColorScheme}).
	 */
	public void setColorModelType(COLOR_MODEL_TYPE colorModelType) {
		this.colorModelType = colorModelType;
	}

	/**
	 * Convert <code>color</code> to a color representation in terms of the
	 * <code>template</code>.
	 * 
	 * @param <T>      the class of the color template
	 * @param color    the color sample
	 * @param template for encoding the color sample.
	 * @return color representation (same class as <code>template</code>)
	 * 
	 * @see #convertColor(Color, Color, Object)
	 */
	public <T> T convertColor(Color color, T template) {
		return convertColor(color, color, template);
	}

	/**
	 * Convert <code>color</code> to a color representation in terms of the
	 * <code>template</code>. Colors in JRE and GWT are incompatible and require
	 * special care. For JRE, colors in 2D graphics are specified as
	 * {@link java.awt.Color} objects (3D graphics no longer exist). For GWT, colors
	 * in 2D graphics are {@link String} objects and
	 * {@link thothbot.parallax.core.shared.materials.Material} for 3D)
	 * <p>
	 * <strong>Note:</strong> this default implementation ignores 3D samples. Only
	 * applies to GWT implementations, see
	 * {@link org.evoludo.gwt.simulator.EvoLudoGWT#convertColor(Color, Color, Object)
	 * EvoLudoGWT.convertColor(Color, Color, Object)}
	 * 
	 * @param <T>      the class of the color template
	 * @param color2D  the color sample for 2D representations
	 * @param color3D  the color sample for 3D representations
	 * @param template the template for encoding the color sample.
	 * @return color representation (same class as <code>template</code>)
	 */
	@SuppressWarnings("unchecked")
	public <T> T convertColor(Color color2D, Color color3D, T template) {
		if (template instanceof Color)
			return (T) color2D;
		throw new IllegalArgumentException("unknown class of template: " + template.getClass().getSimpleName());
	}

	/**
	 * Helper method to create custom color gradients in a JRE/GWT agnostic manner.
	 * 
	 * @param <T>      the class of the color template
	 * @param colors   the equally spaced reference colors of the gradient
	 * @param template the template for encoding the color sample.
	 * @param steps    the number of intermediate, gradient colors
	 * @return color gradient spanning all the colors in the array
	 *         <code>colors</code>.
	 * 
	 * @see #convertColor(Color, Color, Object)
	 * @see ColorMap.Gradient1D#Gradient1D(Color[], int) Gradient1D(Color[], int)
	 */
	public <T> ColorMap<T> createColorGradient1D(Color[] colors, T template, int steps) {
		if (template instanceof Color)
			return new ColorMap.Gradient1D<T>(colors, 100);
		throw new IllegalArgumentException("unknown class of template: " + template.getClass().getSimpleName());
	}

	/**
	 * Utility method to encode <code>boolean</code> with tag <code>key</code>.
	 * 
	 * @param key  tag name
	 * @param bool <code>boolean</code> value
	 * @return encoded String
	 */
	public static String encodeKey(String key, boolean bool) {
		return "<key>" + key + "</key>\n<" + (bool ? "true" : "false") + "/>\n";
	}

	/**
	 * Utility method to encode <code>int</code> with tag <code>key</code>.
	 * 
	 * @param key     tag name
	 * @param integer <code>int</code> value
	 * @return encoded String
	 */
	public static String encodeKey(String key, int integer) {
		return "<key>" + key + "</key>\n<integer>" + integer + "</integer>\n";
	}

	/**
	 * Utility method to encode <code>double</code> with tag <code>key</code>.
	 * <p>
	 * <strong>Note:</strong> floating point values are saved as bit strings to
	 * avoid rounding errors when saving/restoring the state of the model.
	 * 
	 * @param key  tag name
	 * @param real <code>double</code> value
	 * @return encoded String
	 */
	public static String encodeKey(String key, double real) {
		return "<key>" + key + "</key>\n<real>0x" + (real > 0.0 ? "" : "-")
				+ Long.toHexString(Double.doubleToLongBits(Math.abs(real))) + "</real>\n";
	}

	/**
	 * Utility method to encode <code>String</code> with tag <code>key</code>.
	 * 
	 * @param key    tag name
	 * @param string <code>String</code> value
	 * @return encoded String
	 */
	public static String encodeKey(String key, String string) {
		return "<key>" + key + "</key>\n<string>" + XMLCoder.encode(string) + "</string>\n";
	}

	/**
	 * Utility method to encode <code>int</code> array with tag <code>key</code>.
	 * 
	 * @param key   tag name
	 * @param array <code>int[]</code> value
	 * @return encoded String
	 */
	public static String encodeKey(String key, int[] array) {
		return "<key>" + key + "</key>\n" + encodeArray(array);
	}

	/**
	 * Utility method to encode first <code>len</code> entries of <code>int</code>
	 * array with tag <code>key</code>.
	 * 
	 * @param key   tag name
	 * @param array <code>int[]</code> value
	 * @param len   number elements to encode
	 * @return encoded String
	 */
	public static String encodeKey(String key, int[] array, int len) {
		return "<key>" + key + "</key>\n" + encodeArray(array, len);
	}

	/**
	 * Utility method to encode <code>double</code> array with tag <code>key</code>.
	 * <p>
	 * <strong>Note:</strong> floating point values are saved as bit strings to
	 * avoid rounding errors when saving/restoring the state of the model.
	 * 
	 * @param key   tag name
	 * @param array <code>double[]</code> value
	 * @return encoded String
	 */
	public static String encodeKey(String key, double[] array) {
		return "<key>" + key + "</key>\n" + encodeArray(array);
	}

	/**
	 * Utility method to encode first <code>len</code> entries of
	 * <code>double</code> array with tag <code>key</code>.
	 * <p>
	 * <strong>Note:</strong> floating point values are saved as bit strings to
	 * avoid rounding errors when saving/restoring the state of the model.
	 * 
	 * @param key   tag name
	 * @param array <code>double[]</code> value
	 * @param len   number elements to encode
	 * @return encoded String
	 */
	public static String encodeKey(String key, double[] array, int len) {
		return "<key>" + key + "</key>\n" + encodeArray(array, len);
	}

	/**
	 * Utility method to encode <code>double</code> matrix with tag
	 * <code>key</code>.
	 * <p>
	 * <strong>Note:</strong> floating point values are saved as bit strings to
	 * avoid rounding errors when saving/restoring the state of the model.
	 * 
	 * @param key    tag name
	 * @param matrix <code>double[][]</code> value
	 * @return encoded String
	 */
	public static String encodeKey(String key, double[][] matrix) {
		return "<key>" + key + "</key>\n" + encodeArray(matrix);
	}

	/**
	 * Utility method to encode <code>String</code> array with tag <code>key</code>.
	 * 
	 * @param key   tag name
	 * @param array <code>String[]</code> value
	 * @return encoded String
	 */
	public static String encodeKey(String key, String[] array) {
		return "<key>" + key + "</key>\n" + encodeArray(array);
	}

	/**
	 * Utility method to encode first <code>len</code> entries of
	 * <code>String</code> array with tag <code>key</code>.
	 * 
	 * @param key   tag name
	 * @param array <code>String[]</code> value
	 * @param len   number elements to encode
	 * @return encoded String
	 */
	public static String encodeKey(String key, String[] array, int len) {
		return "<key>" + key + "</key>\n" + encodeArray(array, len);
	}

	/**
	 * Helper method to encode <code>int</code> array
	 * 
	 * @param array <code>int[]</code> value
	 * @return encoded String
	 */
	private static String encodeArray(int[] array) {
		StringBuilder plist = new StringBuilder("<array>\n");
		for (int a : array)
			plist.append("<integer>" + a + "</integer>\n");
		return plist.append("</array>\n").toString();
	}

	/**
	 * Helper method to encode first <code>len</code> elements of <code>int</code>
	 * array
	 * 
	 * @param array <code>int[]</code> value
	 * @param len   number elements to encode
	 * @return encoded String
	 */
	private static String encodeArray(int[] array, int len) {
		StringBuilder plist = new StringBuilder("<array>\n");
		for (int n = 0; n < len; n++)
			plist.append("<integer>" + array[n] + "</integer>\n");
		return plist.append("</array>\n").toString();
	}

	/**
	 * Helper method to encode <code>double</code> array
	 * <p>
	 * <strong>Note:</strong> floating point values are saved as bit strings to
	 * avoid rounding errors when saving/restoring the state of the model.
	 * 
	 * @param array <code>double[]</code> value
	 * @return encoded String
	 */
	private static String encodeArray(double[] array) {
		StringBuilder plist = new StringBuilder("<array>\n");
		for (double a : array)
			plist.append("<real>0x" + (a > 0.0 ? "" : "-") + Long.toHexString(Double.doubleToLongBits(Math.abs(a)))
					+ "</real>\n");
		return plist.append("</array>\n").toString();
	}

	/**
	 * Helper method to encode first <code>len</code> elements of
	 * <code>double</code> array
	 * <p>
	 * <strong>Note:</strong> floating point values are saved as bit strings to
	 * avoid rounding errors when saving/restoring the state of the model.
	 * 
	 * @param array <code>double[]</code> value
	 * @param len   number elements to encode
	 * @return encoded String
	 */
	private static String encodeArray(double[] array, int len) {
		StringBuilder plist = new StringBuilder("<array>\n");
		for (int n = 0; n < len; n++)
			plist.append("<real>0x" + (array[n] > 0.0 ? "" : "-")
					+ Long.toHexString(Double.doubleToLongBits(Math.abs(array[n]))) + "</real>\n");
		return plist.append("</array>\n").toString();
	}

	/**
	 * Helper method to encode <code>double</code> matrix
	 * 
	 * @param array <code>double[][]</code> value
	 * @return encoded String
	 */
	private static String encodeArray(double[][] array) {
		StringBuilder plist = new StringBuilder("<array>\n");
		for (double[] a : array)
			plist.append(encodeArray(a));
		return plist.append("</array>\n").toString();
	}

	/**
	 * Helper method to encode <code>String</code> array
	 * 
	 * @param array <code>String[]</code> value
	 * @return encoded String
	 */
	private static String encodeArray(String[] array) {
		StringBuilder plist = new StringBuilder("<array>\n");
		for (String a : array)
			plist.append("<string>" + XMLCoder.encode(a) + "</string>\n");
		return plist.append("</array>\n").toString();
	}

	/**
	 * Helper method to encode first <code>len</code> elements of
	 * <code>String</code> array
	 * 
	 * @param array <code>String[]</code> value
	 * @param len   number elements to encode
	 * @return encoded String
	 */
	private static String encodeArray(String[] array, int len) {
		StringBuilder plist = new StringBuilder("<array>\n");
		for (int n = 0; n < len; n++)
			plist.append("<string>" + XMLCoder.encode(array[n]) + "</string>\n");
		return plist.append("</array>\n").toString();
	}

	/**
	 * Utility method to convert a list of <code>Integer</code>'s to an array of
	 * <code>int</code>'s.
	 * 
	 * @param array {@code List<Integer>} value
	 * @return <code>int[]</code> array
	 */
	public static int[] list2int(List<Integer> array) {
		int[] iarray = new int[array.size()];
		int idx = 0;
		for (Iterator<Integer> i = array.iterator(); i.hasNext();)
			iarray[idx++] = i.next();
		return iarray;
	}

	/**
	 * Utility method to convert a list of <code>Double</code>'s to an array of
	 * <code>double</code>'s.
	 * 
	 * @param array {@code List<Double>} value
	 * @return <code>double[]</code> array
	 */
	public static double[] list2double(List<Double> array) {
		double[] darray = new double[array.size()];
		int idx = 0;
		for (Iterator<Double> i = array.iterator(); i.hasNext();)
			darray[idx++] = i.next();
		return darray;
	}
}
