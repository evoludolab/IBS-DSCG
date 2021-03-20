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
import java.util.Arrays;
import java.util.List;

//import org.evoludo.simulator.views.HasHistogram;
import org.evoludo.util.CLOParser;
import org.evoludo.util.CLOption;
import org.evoludo.util.CLOption.CLODelegate;
import org.evoludo.util.Formatter;
import org.evoludo.util.ArrayMath;
import org.evoludo.util.Plist;
import org.evoludo.util.RNGDistribution;

/**
 *
 * @author Christoph Hauert
 */
public abstract class Population extends Species implements Geometry.Delegate {

	// would seem to make sense to make VACANT final
	public int VACANT = -1;

	private static int idCounter = 0;

	protected String name;
	protected final int ID = createID();

	/**
	 *
	 * @return
	 */
	public static synchronized int createID() {
		return idCounter++;
	}

	/**
	 *
	 * @return
	 */
	public String getName() {
		if( name==null ) {
			name = "";
			if( species.size()>1 )
				name = "Species-"+ID;
		}
		return name;
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public boolean setName(String name) {
		if( this.name!=null ) return false;
		this.name = name;
		return true;
	}

	protected IBSDelegate delegate;

	@Override
	public IBSDelegate getDelegate() {
		return delegate;
	}

	// NOTE: immediately after instantiating Population() an engine needs to
	// be added using setEngine(EvoLudo engine) to avoid trouble
	/**
	 * @param engine
	 * @param key
	 */
	protected Population(EvoLudo engine, String key) {
		super(engine, key);
		engine.addGame(this);
//XXX temporary - delegate should be argument to IBS constructor (just as for xDE models) 
		if (this instanceof IBSDelegate)
			delegate = (IBSDelegate) this;
	}
	
	/**
	 * @param partner
	 */
	protected Population(Population partner) {
		super(partner);
//XXX temporary - delegate should be argument to IBS constructor (just as for xDE models) 
		if (this instanceof IBSDelegate)
			delegate = (IBSDelegate) this;
	}

	// engine must be set before calling load!
	@Override
	public void load() {
		super.load();
		// by default disable vacant sites
		VACANT = -1;
		addSpecies(this);

		// activate statistics
//		if (this instanceof HasHistogram.StatisticsProbability || this instanceof HasHistogram.StatisticsTime)
//			fixData = new FixationData();
//		else
			fixData = null;

//XXX alloc() or reset() would be a better place but parsing of options requires these
//	to set the number of interaction partners and references
		interactionGroup = new Group(rng);
		referenceGroup = new Group(rng);

		// initialize command line options
		Geometry.load(cloGeometry);
		cloGeometryInteraction.inheritKeysFrom(cloGeometry);
		cloGeometryReproduction.inheritKeysFrom(cloGeometry);
		cloPopulationUpdate.addKeys(PopulationUpdateType.values());
//ToDo: further updates to implement or make standard
		cloPopulationUpdate.removeKey(PopulationUpdateType.WRIGHT_FISHER);
		cloPopulationUpdate.removeKey(PopulationUpdateType.ECOLOGY);
		cloPlayerUpdate.addKeys(PlayerUpdateType.values());
		cloMigration.addKeys(MigrationType.values());
		cloFitnessMap.addKeys(Map2Fitness.values());

		// assign default colors
		defaultColor = new Color[] {
				Color.RED,
				Color.BLUE,
				Color.YELLOW,
				Color.GREEN,
				Color.MAGENTA,
				Color.ORANGE,
				Color.PINK,
				Color.CYAN
		};
	}

	@Override
	public void unload() {
		super.unload();
		// free resources
		interaction = null;
		reproduction = null;
		structure = null;
		interactionGroup = null;
		referenceGroup = null;
		cloPopulationUpdate.clearKeys();
		cloPlayerUpdate.clearKeys();
		cloMigration.clearKeys();
		cloGeometry.clearKeys();
		defaultColor = null;
		traitName = null;
		traitColor = null;
		traitActive = null;
		imitated = null;
		cProbs = null;
		groupScores = null;
		smallScores = null;
		fixData = null;
		dealloc();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note:</strong> make implementation optional.
	 */
	@Override
	public double[] getDerivatives(double t, double[] yt, double[] ft, double[] dyt, double[] scorescratch) {
		throw new Error("PDEDelegate: getDerivatives not implemented.");
	}

	/**
	 * <strong>Important:</strong> must be overridden and implemented in subclasses that define game 
	 * interactions.
	 * <p>
	 * Calculate and return the minimum payoff/score of an individual. This value is important for
	 * converting payoffs/scores into probabilities, for scaling graphical output and some 
	 * optimizations.
	 * </p>
	 * @return minimum payoff/score
	 */
	public abstract double	getMinGameScore();

	/**
	 * <strong>Important:</strong> must be overridden and implemented in subclasses that define game 
	 * interactions.
	 * <p>
	 * Calculate and return the maximum payoff/score of an individual. This value is important for
	 * converting payoffs/scores into probabilities, for scaling graphical output and some 
	 * optimizations.
	 * </p>
	 * @return maximum payoff/score
	 */
	public abstract double	getMaxGameScore();

	/**
	 * Calculate and return the minimum payoff/score of individuals in monomorphic populations.
	 * 
	 * @return minimum payoff/score in monomorphic population
	 */
	public abstract double	getMinMonoGameScore();

	/**
	 * Calculate and return the maximum payoff/score of individuals in monomorphic populations.
	 * 
	 * @return maximum payoff/score in monomorphic population
	 */
	public abstract double	getMaxMonoGameScore();

	/**
	 * Mutate strategy of individual at <code>index</code>. If <code>changed==true</code> then 
	 * check {@link DPopulation#strategiesScratch} (or {@link CXPopulation#strategiesScratch},
	 * respectively) for current strategy; otherwise check {@link DPopulation#strategies} (or
	 * {@link CXPopulation#strategies}, repsectively).
	 * 
	 * @param index of individual that mutates its strategy
	 * @param changed <code>true</code> if individual updated/changed strategy (prior to 
	 * 		mutating)
	 */
	protected abstract void	mutateStrategyAt(int index, boolean changed);

	/**
	 *
	 */
	protected abstract void	prepareStrategies();

	/**
	 *
	 */
	protected abstract void	commitStrategies();

	/**
	 *
	 * @param index
	 */
	protected abstract void	commitStrategyAt(int index);

	/**
	 * Check if individuals with index <code>a</code> and index <code>b</code> have the same strategies.
	 * 
	 * @param a index of first individual
	 * @param b index of second individual
	 * @return <code>true</code> if the two individuals have the same strategies
	 */
	protected abstract boolean	haveSameStrategy(int a, int b);

	/**
	 * Check if individual with index <code>a</code> has switched strategies.
	 * <p>
	 * <strong>Note:</strong> this test is only meaningful before strategy gets committed (see 
	 * {@link #commitStrategyAt(int)} and {@link #commitStrategies()}).
	 * </p>
	 * @param a index of individual
	 * @return <code>true</code> if strategy remained the same
	 */
	protected abstract boolean	isSameStrategy(int a);

	/**
	 * Swap strategies of individuals with index <code>a</code> and index <code>b</code>.
	 * 
	 * @param a index of first individual
	 * @param b index of second individual
	 */
	protected abstract void	swapStrategies(int a, int b);

	/**
	 *
	 * @param group
	 */
	protected abstract void	playGameAt(Group group);

	/**
	 * Counterpart of {@link #playGameAt(Group)}, {@link #playGameAt(int)} and/or {@link 
	 * #playGameSyncAt(int)}. Removes the payoffs of pairwise interactions (this is reflected 
	 * in the factor <code>2.0</code> - an optimization for undirected graphs).
	 * <p>
	 * <strong>Note:</strong> requires {@link Group#SAMPLING_ALL}. Must have been checked
	 * in the calling routine {@link #adjustGameScoresAt(int)}.
	 *</p>
	 * @param group interaction group
	 */
	protected abstract void yalpPairGameAt(Group group);

	/**
	 * Counterpart of {@link #playGameAt(Group)}, {@link #playGameAt(int)} and/or {@link 
	 * #playGameSyncAt(int)}. Removes the payoffs of group interactions
	 * see {@link #yalpPairGameAt(Group)} above.
	 *
	 * @param group interaction group
	 */
	protected abstract void yalpGroupGameAt(Group group);

	/**
	 *
	 * @param me index of individual to update
	 * @param group array with indices of reference group
	 * @param size size of reference group
	 * @return <code>true</code> if strategy changed (signaling score needs to be reset)
	 */
	protected abstract boolean	updatePlayerBestReply(int me, int[] group, int size);

	/**
	 *
	 * @param me
	 * @param best
	 * @param sample
	 * @return
	 */
	protected abstract boolean	preferredPlayerBest(int me, int best, int sample);

	/**
	 *
	 * @return
	 */
	public int getVacant() {
		return VACANT;
	}

	/**
	 * Check if site with index <code>index</code> is occupied by an individual or vacant.
	 * <p>
	 * <strong>Note:</strong> Assumes that strategies are committed
	 *</p>
	 * @param index of individual/site to check
	 * @return <code>true</code> if site <code>index</code> is vacant
	 */
	public boolean isVacantAt(int index) {
		return false;
	}

	/**
	 * assumes strategies are not yet committed
	 *
	 * @param index
	 * @return
	 */
	public boolean becomesVacantAt(int index) {
		return false;
	}

	protected boolean monoStop = false;

	/**
	 *
	 * @param monoStop
	 */
	public void setMonoStop(boolean monoStop) {
		this.monoStop = monoStop;
	}

	/**
	 *
	 * @return
	 */
	public boolean isMonomorphic() { return false; }

	/**
	 * checks whether population has reached an absorbing state. by default true if population is homogeneous and no (zero) mutations. 
	 * gives different implementations a chance to adjust/refine criteria for absorption.
	 * 
	 * @return true to abort calculations; false to continue
	 */
	@Override
	public boolean hasConverged() {
		return pMutation<=0.0;
	}

	/**
	 *
	 */
	protected int nTraits = -1;

	public void setNTraits(int count) {
		int oldNTraits = nTraits;
		nTraits = Math.max(1, count);
		// NOTE: no shortcuts if nTraits did not change! need to ensure that traitActive is up-to-date.
		if( traitActive==null || traitActive.length!=nTraits ) {
			boolean[] active = new boolean[nTraits];
			Arrays.fill(active, true);
			setActiveTraits(active);
		}
		engine.requiresReset(nTraits!=oldNTraits);
	}

	public int getNTraits() {
		return nTraits;
	}

	/**
	 *
	 */
	protected	String[] traitName;

	/**
	 *
	 * @param names
	 */
	protected void setTraitNames(String[] names) {
		traitName = names;
		setNTraits(traitName.length);
	}

	/**
	 *
	 * @return
	 */
	public String[] getTraitNames() {
		return traitName;
	}

	public String getTraitName(int idx) {
		if( traitName!=null ) {
			if( idx<traitName.length ) return traitName[idx];
			return "noname "+idx;
		}
		return "Trait "+(char)('A'+idx);
	}

	/**
	 * Color for traits (stored here for snapshots).
	 */
	protected Color[] traitColor;

	/**
	 * Color for trajectories (stored here for snapshots).
	 * <p>
	 * Default color for trajectories is black. Using transparent colors is useful
	 * for models with noise (simulations or SDE's) to identify regions of
	 * attraction (e.g. stochastic limit cycles) as darker shaded areas because the
	 * population spends much of its time there while less frequently visited areas
	 * of the phase space are lighter in color.
	 */
	protected Color trajColor;

	/**
	 * note: if defaultColor is set already here, headless mode for simulations is prevented.
	 *		 to avoid this, simply allocate and assign the colors in the constructor.
	 */
	protected static Color[] defaultColor;

	/**
	 *
	 * @param colors
	 */
	public void setTraitColors(Color[] colors) {
		int nColors = nTraits;
		if( isContinuous ) nColors *= 3;	// continuous strategies require colors for min, mean, max of each trait
		if( colors==null ) {
			// use default colors
			traitColor = new Color[nColors];
			if( isContinuous ) {
				for( int n=0; n<Math.min(nTraits, defaultColor.length); n++ ) {
					Color color = defaultColor[n];
					traitColor[3*n] = color;				// mean
					// NOTE: Color.brighter() does not work on pure colors.
					traitColor[3*n+1] = ColorMap.blendColors(color, Color.WHITE, 0.333);	// min
					traitColor[3*n+2] = traitColor[3*n+1];	// max
				}
				if( nTraits<=defaultColor.length ) return;
				// not enough default colors - add some random colors
				for( int n=defaultColor.length; n<nTraits; n++ ) {
					Color color = new Color(random0n(256), random0n(256), random0n(256));
					traitColor[3*n] = color;				// mean
					traitColor[3*n+1] = ColorMap.blendColors(color, Color.WHITE, 0.333);	// min
					traitColor[3*n+2] = traitColor[3*n+1];	// max
				}
				return;
			}
			// discrete strategies
			System.arraycopy(defaultColor, 0, traitColor, 0, Math.min(nColors, defaultColor.length));
			if( nColors<=defaultColor.length ) return;
			// not enough default colors - add some random colors
			for( int n=defaultColor.length; n<nColors; n++ )
				traitColor[n] = new Color(random0n(256), random0n(256), random0n(256));
			return;
		}
		int len = colors.length;
		if( len!=nColors ) {
			// use colors provided and derive lighter and darker shades for continuous games
			traitColor = new Color[nColors];
			if( isContinuous ) {
				for( int n=0; n<Math.min(nTraits, len); n++ ) {
					Color color = colors[n];
					traitColor[3*n] = color;				// mean
					traitColor[3*n+1] = ColorMap.blendColors(color, Color.WHITE, 0.333);	// min
					traitColor[3*n+2] = traitColor[3*n+1];	// max
				}
				if( nTraits<=len ) return;
				// not enough colors provided - add default colors
				int idx = len;
			nextcolor:		
				for( int n=0; n<defaultColor.length; n++ ) {
					Color color = defaultColor[n];
					// make sure default color has not already been used
					for( int i=0; i<len; i++ )
						if( traitColor[3*i].equals(color) ) 
							continue nextcolor;
					traitColor[3*idx] = color;					// mean
					traitColor[3*idx+1] = ColorMap.blendColors(color, Color.WHITE, 0.333);	// min
					traitColor[3*idx+2] = traitColor[3*idx+1];	// max
					if( ++idx==nTraits ) return;
				}
				// not enough default colors - add some random colors
				for( int n=idx; n<nTraits; n++ ) {
					Color color = new Color(random0n(256), random0n(256), random0n(256));
					traitColor[3*n] = color;				// mean
					traitColor[3*n+1] = ColorMap.blendColors(color, Color.WHITE, 0.333);	// min
					traitColor[3*n+2] = traitColor[3*n+1];	// max
				}
				return;
			}
			// discrete strategies
			System.arraycopy(colors, 0, traitColor, 0, Math.min(nColors, len));
			if( nColors<len ) return;
			System.arraycopy(defaultColor, 0, traitColor, len, Math.min(nColors-len, defaultColor.length));
			if( nColors<=len+defaultColor.length ) return;
			// not enough default colors - add some random colors
			for( int n=len+defaultColor.length; n<nColors; n++ )
				traitColor[n] = new Color(random0n(256), random0n(256), random0n(256));
			return;
		}
		traitColor = colors;
	}

	/**
	 *
	 * @return
	 */
	public Color[] getTraitColors() {
		if( traitColor==null )
			setTraitColors(null);
		return traitColor;
	}

	/**
	 *
	 * @param idx
	 * @param color
	 */
	public void setTraitColor(int idx, Color color) {
		if( traitColor==null )
			setTraitColors(null);
		if( idx<0 || idx>traitColor.length )
			return;
		traitColor[idx] = color;
	}

	/**
	 *
	 * @param idx
	 * @return
	 */
	public Color getTraitColor(int idx) {
		if( traitColor==null )
			setTraitColors(null);
		if( idx<0 || idx>traitColor.length )
			return Color.BLACK;
		return traitColor[idx];
	}

	/**
	 * @return
	 */
	public Color getTrajectoryColor() {
		if (trajColor==null)
			return Color.BLACK;
		return trajColor;
	}

	/**
	 *
	 */
	protected int	nActive = -1;

	/**
	 *
	 * @return
	 */
	public int getActiveCount() {
		return nActive;
	}

	/**
	 *
	 */
	protected boolean[] traitActive;

	/**
	 *
	 * @param active
	 */
	public void setActiveTraits(boolean[] active) {
		int len = active.length;
		int act = 0;
		if( traitActive==null || traitActive.length!=len ) {
//XXX strictly speaking this requires only a reinit - no need to re-build population structure
			engine.requiresReset(true);
			for( int i=0; i<len; i++ )
				if( active[i] )
					act++;
		}
		else {
			for( int i=0; i<len; i++ ) {
				if( active[i]!=traitActive[i] ) 
					engine.requiresReset(true);
				if( active[i] )
					act++;
			}
		}
		nActive = act;
		traitActive = active;
	}

	/**
	 *
	 * @return
	 */
	public boolean[] getActiveTraits() {
		int len = traitActive.length;
		boolean[] clone = new boolean[len];
		System.arraycopy(traitActive, 0, clone, 0, len);
		return clone;
	}

	/**
	 *
	 * @return
	 */
	public String[] getActiveTraitNames() {
		if( nActive==nTraits ) return getTraitNames();
		String[] activeNames = new String[nActive];
		int n = 0;
		for( int i=0; i<nTraits; i++ ) {
			if( !traitActive[i] ) continue;
			activeNames[n++] = getTraitName(i);
		}
		return activeNames;
	}

	/**
	 *
	 * @param index
	 * @return
	 */
	public abstract String	getTraitNameAt(int index);

	/**
	 *
	 */
	protected int dependentTrait = -1;

	/**
	 * For replicator dynamics the frequencies of all strategies must sum up to one. Hence, for <code>nTraits</code>
	 * strategies there are only <code>nTraits-1</code> degrees of freedom. <code>dependentTrait</code> marks the one that is derived
	 * from the others. 
	 * 
	 * @return index of dependent trait or -1 if there is none
	 */
	public int getDependentTrait() {
		return dependentTrait;
	}

	/**
	 *
	 * @param dependentTrait
	 */
	public void setDependentTrait(int dependentTrait) {
		this.dependentTrait = dependentTrait;
	}

	/**
	 * Geometry of population (interaction and reproduction graphs are the same)
	 */
	protected Geometry structure;

	/**
	 * Geometry of interaction graph
	 */
	protected Geometry interaction;

	/**
	 * Helper class to sample interaction group
	 */
	protected Group interactionGroup;

	/**
	 * Number of interactions
	 */
	protected int nInteractions = 1;

	/**
	 * Geometry of reproduction graph
	 */
	protected Geometry reproduction;

	/**
	 * Helper class to sample reference group
	 */
	protected Group referenceGroup;

	// 020301 
	// the treatment of the score should be no longer linked to the population update
	// async/imitate reset scores only upon strategy changes while async/replicate reset
	// scores always. besides a new option is added to consider accumulated payoffs.
	/**
	 *
	 */
	protected boolean playerScoreAveraged = true;

	/**
	 *
	 */
	protected boolean playerScoreResetAlways = true;

	/**
	 * baseline fitness of individuals - particularly important for Moran type updates (avoids negative payoffs)
	 */
	protected double	playerBaselineFitness = 0.0;

	/**
	 *
	 */
	protected double	playerSelection = 1.0;

	/* 
	 * migration 
	 */
	public static enum MigrationType implements CLOption.KeyCollection {
		NONE ("none", "no migration"),
		DIFFUSION ("D", "diffusive migration (exchange of neighbors)"),
		BIRTH_DEATH ("B", "birth-death migration (fit migrates, random death)"),
		DEATH_BIRTH ("d", "death-birth migration (random death, fit migrates)");

		String key;
		String title;

		MigrationType(String key, String title) {
			this.key = key;
			this.title = title;
		}

		static MigrationType parse(String arg) {
			int best = 0;
			MigrationType match = null;
			// pick best match (if any)
			for (MigrationType mt : values()) {
				int diff = CLOption.differAt(arg, mt.key);
				if (diff>best) {
					best = diff;
					match = mt;
				}
			}
			return match;
		}

		@Override
		public String toString() {
			return key+": "+title;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public CLOption.KeyCollection[] getKeys() {
			return values();
		}
	}

	/**
	 *
	 */
	protected MigrationType	migrationType = MigrationType.NONE;

	/**
	 *
	 */
	protected double	pMigration = 0.0;

	/**
	 *
	 */
	protected RNGDistribution.Geometric distrMigrants;

	/*
	 * initialization 
	 */
	private boolean[]	imitated;
	private double[]	cProbs;

	public static enum Map2Fitness implements CLOption.KeyCollection {
		NONE ("none", "no mapping"),					// scores/payoffs equal fitness
		STATIC ("static", "b+w*score"),					// static baseline fitness, b+w*score
		CONVEX ("convex", "b*(1-w)+w*score"),			// convex combination of baseline fitness and scores, b(1-w)+w*score
		EXPONENTIAL ("exponential", "b*exp(w*score)");	// exponential mapping, b*exp(w*score)

		String key;
		String title;

		Map2Fitness(String key, String title) {
			this.key = key;
			this.title = title;
		}

		static Map2Fitness parse(String arg) {
			int best = 0;
			Map2Fitness match = null;
			// pick best match (if any)
			for (Map2Fitness m2f : values()) {
				int diff = CLOption.differAt(arg, m2f.key);
				if (diff>best) {
					best = diff;
					match = m2f;
				}
			}
			return match;
		}

		@Override
		public String toString() {
			return key+": "+title;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public CLOption.KeyCollection[] getKeys() {
			return values();
		}
	}

	public Map2Fitness score2FitnessMap = Map2Fitness.NONE;

	/**
	 * stores the fitness of each individual - allows to change the score-to-fitness mapping
	 */
	protected double[]	fitness;

	/**
	 *
	 */
	protected double	sumFitness = -1.0;

	/**
	 * stores the game score of each individual
	 */
	protected double[]	scores;

	/**
	 *
	 */
	protected double[]	groupScores;

	/**
	 *
	 */
	protected double[]	smallScores;

	/**
	 *
	 */
	protected int[]		interactions;

	/**
	 *
	 */
	protected double[]	tags;

	/**
	 *
	 */
	protected int	nPopulation = 1000;

	/**
	 *
	 */
	protected int	nGroup = 2;

	/**
	 *
	 */
	protected boolean	pairwise = (nGroup==2);

	/**
	 *
	 */
	protected boolean pairwiseOnly = false;	// restrict to pairwise interactions?

	/**
	 *
	 * @return
	 */
	public boolean pairwiseOnly() { return pairwiseOnly; }

	/**
	 *
	 */
	protected boolean	isContinuous = false;

	/**
	 *
	 * @return
	 */
	public boolean isContinuous() { return isContinuous; }

	/**
	 *
	 */
	protected boolean	adjustScores;

	/**
	 *
	 */
	protected boolean	hasLookupTable;

	// reduce calls to getMxxScore()
	/**
	 *
	 */
	protected double minScore = Double.MAX_VALUE;

	/**
	 *
	 */
	protected double maxScore = -Double.MAX_VALUE;

	/**
	 * index of individual with the currently maximum score
	 */
	protected int maxEffScoreIdx = -1;

	/**
	 *
	 * @return
	 */
	public int doSyncMigration() {
		// number of migratory events
		int nMigrants = 0;
		switch( migrationType ) {
			case NONE:
				break;
			case BIRTH_DEATH:
				nMigrants = nextBinomial(1.0-pMigration, nPopulation);
				for( int n=0; n<nMigrants; n++ ) doBirthDeathMigration();
				break;
			case DEATH_BIRTH:
				nMigrants = nextBinomial(1.0-pMigration, nPopulation);
				for( int n=0; n<nMigrants; n++ ) doDeathBirthMigration();
				break;
			case DIFFUSION:
				nMigrants = nextBinomial(1.0-pMigration, nPopulation);
				for( int n=0; n<nMigrants; n++ ) doDiffusionMigration();
				break;
			default:		// should never get here
				throw new Error("Unknown migration type ("+migrationType+")");
		}
		return nMigrants;
	}

	/**
	 *
	 */
	public void doMigration() {
		switch( migrationType ) {
			case NONE:
				break;
			case BIRTH_DEATH:
				doBirthDeathMigration();
				break;
			case DEATH_BIRTH:
				doDeathBirthMigration();
				break;
			case DIFFUSION:
				doDiffusionMigration();
				break;
			default:		// should never get here
				throw new Error("Unknown migration type ("+migrationType+")");
		}
	}

	/**
	 *
	 */
	public void doDiffusionMigration() {
		int migrant = random0n(nPopulation);
		// migrant swaps places with random neighbor
		int[] myNeighs = interaction.out[migrant];
		int aNeigh = myNeighs[random0n(interaction.kout[migrant])];
		updatePlayerSwap(migrant, aNeigh);
	}

	/**
	 *
	 * @param a
	 * @param b
	 */
	public void updatePlayerSwap(int a, int b) {
		swapStrategies(a, b);	// strategy change still needs to be committed
		// fitness accounting:
		// synchronous: no need to worry about fitness - this is determined afterwards
		// asynchronous:
		// - if interactions are random (payoffs are accumulated), simply take the scores along
		// - if interactions are with all neighbors (payoffs are adjusted) we need to recalculate the payoffs
		if( isSynchronous ) {
//XXX this is not efficient because it deals unnecessarily with types and scores
			// enough to only copy from scratch to strategies.
			commitStrategyAt(a); 
			commitStrategyAt(b);
			return;
		}
		if( adjustScores ) {
			if( haveSameStrategy(a, b) ) return;	// nothing to do
			adjustGameScoresAt(a);
			adjustGameScoresAt(b);
			return;
		}
//XXX again, commitStrategy is overkill because the composition of the population has not changed
		commitStrategyAt(a); 
		commitStrategyAt(b);
		swapScoresAt(a, b);
	}

	// this is almost identical (victim and migrant always different) to moran (birth-death) updating in well-mixed population
	/**
	 *
	 */
	public void doBirthDeathMigration() {
		int migrant = pickFitFocalIndividual();
		int vacant = random0n(nPopulation-1);
		if( vacant>=migrant ) vacant++;
//XXX NOTE: updatePlayerMoran checks for mutations - this may not be what we want...
		updatePlayerMoran(migrant, vacant);
	}

	// this is almost identical (victim and migrant always different) to moran (birth-death) updating in well-mixed population
	/**
	 *
	 */
	public void doDeathBirthMigration() {
		int vacant = random0n(nPopulation);
		int migrant = pickFitFocalIndividual(vacant);
//XXX NOTE: updatePlayerMoran checks for mutations - this may not be what we want...
		updatePlayerMoran(migrant, vacant);
	}

	/* 
	 *
	 * methods focusing on population 
	 *
	 */

	/**
	 * update focal individual for debugging
	 * 
	 * NOTE: this needs to remain in sync with population updates in modelNext() in Species
	 *
	 * @param focal
	 */
	public void debugUpdatePopulationAt(int focal) {
		double wPopTot = 0.0, wScoreTot = 0.0;
		boolean doRealtime = true;
		// reset strategies (colors)
		for( Population pop : species ) {
			pop.resetStrategies();
			double rate = pop.getSpeciesUpdateRate();
			// determine generation time and real time increments
			// NOTE: generation time increments based on maximum population sizes and do not take
			//		 potentially fluctuating sizes into account (relevant for ecological settings)
			wPopTot += pop.getNPopulation()*rate;
			double sum = pop.sumFitness;
			if( doRealtime && sum<=1e-8 ) {
				doRealtime = false;
				continue;
			}
			wScoreTot += sum*rate;
		}
		double gincr = 1.0/wPopTot;
		double rincr = (!doRealtime?0.0:1.0/(wScoreTot*wScoreTot));
		generation += gincr;
		switch( populationUpdateType ) {
			case ASYNC:   // exclusively the current payoff matters
				realtime += rincr*sumFitness*getSpeciesUpdateRate();
				updatePlayerAsyncReplicateAt(focal);
				break;

			case MORAN_BIRTHDEATH:	 // moran process - birth-death
				realtime += rincr*sumFitness*getSpeciesUpdateRate();
				updatePlayerMoranBirthDeathAt(focal);
				break;

			case MORAN_DEATHBIRTH:	 // moran process - death-birth
				realtime += rincr*sumFitness*getSpeciesUpdateRate();
				updatePlayerMoranDeathBirthAt(focal);
				break;

			case ECOLOGY:	 // ecological updating - varying population sizes
//XXX updatePlayerEcologyAt returns time increment in real time units - how to scale with several populations?
				rincr = updatePlayerEcologyAt(focal);
				realtime += rincr/getSpeciesUpdateRate();
				break;

			case SYNC:	 // synchronous updating - gets here only in debugging mode
				realtime += rincr*sumFitness*getSpeciesUpdateRate();
				if( updatePlayerAt(focal) ) adjustGameScoresAt(focal);
				break;

			default:		// last resort - opportunity to implement custom updating schemes
				rincr = updatePlayerCustomAt(focal);
				realtime += rincr/getSpeciesUpdateRate();
		}
		engine.fireModelChanged();
	}

	/**
	 * returns the index of a random member of the population selected uniform at random
	 *
	 * @return index of random member of population
	 */
	protected int pickRandomFocalIndividual() {
		return random0n(nPopulation);
	}

	/**
	 * returns the index of a random member of the population selected uniform at random, excluding individual with
	 * index <code>excl</code>.
	 *
	 * @param excl
	 * @return index of random member of population
	 */
	protected int pickRandomFocalIndividual(int excl) {
		if( excl<0 || excl>nPopulation )
			return pickRandomFocalIndividual();
		int rand = random0n(nPopulation-1);
		if( rand>=excl ) return rand+1;
		return rand;
	}

	/**
	 * returns the index of a random member of the population selected proportional to fitness
	 * <p>
	 * <strong>Note:</strong> scores must be <code>&ge;0</code>
	 * </p>
	 * @return
	 */
	protected int pickFitFocalIndividual() {
		if( VACANT>=0 )
			throw new Error("Population.pickFitFocalIndividual() not (yet) ready to handle vacant sites.");

		// differences in scores too small, pick random individual
		if( sumFitness<1e-8 )
			return pickRandomFocalIndividual();

//TODO: perform more systematic analysis regarding the threshold population size for the two methods
//    sometimes 4*nPopulation trials are needed, which seems too much...
		if( nPopulation>=100 ) {
			// optimization of gillespie algorithm to prevent bookkeeping (at the expense of drawing more random numbers)
			// see e.g. http://arxiv.org/pdf/1109.3627.pdf
			double mScore;
			// note: for constant selection maxEffScoreIdx is never set
			// 		 using the effective current maximum score makes this optimization more efficient
			if( maxEffScoreIdx<0 ) mScore = mapToFitness(maxScore);
			else mScore = getFitnessAt(maxEffScoreIdx);
			int aRand = -1;
			do {
				aRand = random0n(nPopulation);
			}
			while( random01()*mScore>getFitnessAt(aRand) );	// note: if < holds aRand is ok
			return aRand;
		}
		double hit = random01()*sumFitness;
		for( int n=0; n<nPopulation; n++ ) {
			hit -= getFitnessAt(n);
			if( hit<0.0 ) return n;
		}
		if( hit<1e-6 && getFitnessAt(nPopulation-1)>1e-6 ) return nPopulation-1;
		debugScores(hit);
		throw new Error("Failed to pick parent...");
	}

	/**
	 * returns the index of a random member of the population selected proportional to fitness, excluding individual excl
	 * <p>
	 * <strong>Note:</strong> scores must be <code>&ge;0</code>
	 * </p>
	 * @param excl
	 * @return
	 */
	protected int pickFitFocalIndividual(int excl) {
		if( VACANT>=0 )
			throw new Error("Population.pickFitFocalIndividual(excl) not (yet) ready to handle vacant sites.");
		if( excl<0 || excl>nPopulation )
			return pickFitFocalIndividual();

		// differences in scores too small, pick random individual
		if( sumFitness<1e-8 )
			return pickRandomFocalIndividual(excl);

		// note: review threshold for optimizations (see pickFitFocalIndividual above)
		if( nPopulation>=100 ) {
			// optimization of gillespie algorithm to prevent bookkeeping (at the expense of drawing more random numbers)
			// see e.g. http://arxiv.org/pdf/1109.3627.pdf
			if( excl==maxEffScoreIdx ) {
				// excluding the maximum score can cause issues if it is much larger than the rest;
				// need to find the second largest fitness value (note using mapToFitness(maxScore)
				// may be even worse because most candidates are rejected 
				double mScore = mapToFitness(second(maxEffScoreIdx));
				int aRand = -1;
				do {
					aRand = random0n(nPopulation-1);
					if( aRand>=excl ) aRand++;
				}
				while( random01()*mScore>getFitnessAt(aRand) );	// note: if < holds aRand is ok
				return aRand;
			}

			double mScore;
			// note: for constant selection maxEffScoreIdx is never set
			// 		 using the effective current maximum score makes this optimization more efficient
			if( maxEffScoreIdx<0 ) mScore = mapToFitness(maxScore);
			else mScore = getFitnessAt(maxEffScoreIdx);
			int aRand = -1;
			do {
				aRand = random0n(nPopulation-1);
				if( aRand>=excl ) aRand++;
			}
			while( random01()*mScore>getFitnessAt(aRand) );	// note: if < holds aRand is ok
			return aRand;
		}
		double hit = random01()*(sumFitness-getFitnessAt(excl));
		for( int n=0; n<excl; n++ ) {
			hit -= getFitnessAt(n);
			if( hit<0.0 ) return n;
		}
		for( int n=excl+1; n<nPopulation; n++ ) {
			hit -= getFitnessAt(n);
			if( hit<0.0 ) return n;
		}
		// last resort...
		if( excl==nPopulation-1 )
			if( hit<1e-6 && getFitnessAt(nPopulation-2)>1e-6 )
				return nPopulation-2;
			else
				if( hit<1e-6 && getFitnessAt(nPopulation-1)>1e-6 )
					return nPopulation-1;
		debugScores(hit);
		throw new Error("Failed to pick parent...");
	}

	private double second(int excl) {
		double max = getScoreAt(maxEffScoreIdx);
		double second = -Double.MAX_VALUE;
		for( int n=0; n<excl; n++ ) {
			second = Math.max(second, getScoreAt(n));
			// if a second individual has maximum score, no need to look further
			if( Math.abs(second-max)<1e-8 )
				return second;
		}
		for( int n=excl+1; n<nPopulation; n++ ) {
			second = Math.max(second, getScoreAt(n));
			// if a second individual has maximum score, no need to look further
			if( Math.abs(second-max)<1e-8 )
				return second;
		}
		return second;
	}

	/**
	 *
	 * @param hit
	 */
	protected void debugScores(double hit) {
		logger.fine("aborted in generation: "+generation+"\nscore dump:");
		double sum = 0.0;
		for( int n=0; n<nPopulation; n++ ) {
			double fn = getFitnessAt(n);
			logger.fine("score["+n+"]="+Formatter.format(scores[n], 6)+" -> "+Formatter.format(fn, 6)+", interactions["+n+"]="+interactions[n]+
					", base="+playerBaselineFitness+", selection="+playerSelection);
			sum += fn;
		}
		logger.fine("Failed to pick parent... hit: "+hit+", sumScores: "+Formatter.format(sumFitness, 6)+" (should be "+Formatter.format(sum, 6)+")");
	}

	/**
	 *
	 * @param me
	 * @return
	 */
	protected int drawFitNeighborAt(int me) {
		if( VACANT>=0 ) throw new Error("Population.drawFitNeighborAt not (yet) ready to handle vacant sites.");

		// mean-field
		if( reproduction.geometry==Geometry.MEANFIELD ) {
			if( deathBirthIncludeSelf ) return pickFitFocalIndividual();
			return pickFitFocalIndividual(me);
		}

		// structured population
		int[] neighs = reproduction.in[me];
		int len = reproduction.kin[me];
		double totFitness = 0.0;
		if( deathBirthIncludeSelf ) {
			if( len==0 ) return me;
			totFitness = getFitnessAt(me);
		}
		else {
			switch( len ) {
				case 0: return -1;
				case 1:	return neighs[0];
				default:
			}
		}
		for( int n=0; n<len; n++ ) totFitness += getFitnessAt(neighs[n]);
		// should not be <0.0 - reset() took care of that
		// if roughly 0.0 choose neighbor with equal probabilities
		if( totFitness<=1e-8 ) return neighs[random0n(len)];

		double hit = random01()*totFitness;
		for( int n=0; n<len; n++ ) {
			hit -= getFitnessAt(neighs[n]);
			if( hit<0.0 ) return neighs[n];
		}
		// at this point we should be sure that deathBirthIncludeSelf is true and that 'me' is selected...
		// keep this for now as a check...
		if( deathBirthIncludeSelf ) {
			hit -= getFitnessAt(neighs[me]);
			if( hit<1e-6 ) return me;
		}
		else {
			if( hit<1e-6 && getFitnessAt(neighs[len-1])>1e-6 ) return neighs[len-1];
		}
		// dump scores:
		logger.fine("neighbor score dump:");
		for( int n=0; n<len; n++ )
			logger.fine("score["+n+"]="+scores[neighs[n]]+", fitness["+n+"]="+getFitnessAt(neighs[n]));
		throw new Error("Failed to pick neighbor... ("+hit+", sum: "+totFitness+")");
	}

	/**
	 * NOTE: in original Moran process offspring can replace parent
	 *
	 * @param me
	 * @return
	 */
	protected int drawNeighborAt(int me) {
		// mean-field
		if( reproduction.geometry==Geometry.MEANFIELD ) {
			// offspring can replace parent
			return random0n(nPopulation);
// exclude parent - same as complete graph
//			int aNeigh = random0n(nPopulation-1);
//			if( aNeigh >= me ) aNeigh++;
//			return aNeigh;
		}

		int[] neighs = reproduction.out[me];
		int len = reproduction.kout[me];
		switch( len ) {
			case 0: return -1;
			case 1: return neighs[0];
			default: return neighs[random0n(len)];
		}
	}

	/* 
	 *
	 * methods focusing on players 
	 *
	 */

	/**
	 * Keep track of tags
	 * <p>
	 * <b>Note:</b> method must be subclassed and call super to implement updating of different strategy types
	 * 
	 * @param me index of individual to update
	 * @param model index of individual to adopt strategy from
	 */
	protected void	updateFromModelAt(int me, int model) {
		tags[me] = tags[model];
		//<jf
		if (verbose) logger.fine("update: node "+me+" adopts strategy at node "+model);
		//jf>
	}

	/**
	 * after initialization and for synchronized population updating this method is
	 * invoked (rather than playGameAt()). this simply indicates that all players
	 * are going to be updated. for directed graphs, it follows that we do not have
	 * to treat incoming and outgoing links separately as every outgoing link
	 * corresponds to an incoming link of another node.
	 *
	 * @param me
	 */
	public void playGameSyncAt(int me) {
		// during initialization, pretend we are doing this synchronously - just in case someone's interested.
		boolean sync = isSynchronous;
		isSynchronous = true;
		for( int i=0; i<nInteractions; i++ ) {
			interactionGroup.pickAt(me, interaction, true);
			playGameAt(interactionGroup);
		}
		isSynchronous = sync;
	}

	/**
	 *
	 * @param me
	 */
	public void playGameAt(int me) {
		// any graph - interact with out-neighbors
		// same as earlier approach to undirected graphs
		if( adjustScores ) {
			throw new Error("ERROR: playGameAt(int idx) and adjustScores are incompatible!");
		}
		for( int i=0; i<nInteractions; i++ ) {
			interactionGroup.pickAt(me, interaction, true);
			playGameAt(interactionGroup);
		}
		// if undirected, we are done
		if( interaction.isUndirected ) return;

		// directed graph - additionally/separately interact with in-neighbors
		for( int i=0; i<nInteractions; i++ ) {
			interactionGroup.pickAt(me, interaction, false);
			playGameAt(interactionGroup);
		}
	}

	/**
	 * this optimized method is only applicable if Group.SAMPLING_ALL is true and 
	 * not Geometry.MEANFIELD, i.e. if the interaction group includes all neighbors.
	 * @param me
	 */
	protected void adjustGameScoresAt(int me) {
		// check first whether an actual strategy change has occurred
		if( isSameStrategy(me) ) {
			commitStrategyAt(me);
			return;
		}

		// any graph - interact with out-neighbors
		// same as earlier approach to undirected graphs
		if( interaction.isUndirected ) {
			// undirected graph - same as earlier approach
			if( pairwise ) {
				// remove old scores - removes the doubled score on undirected graphs
				interactionGroup.setGroupAt(me, interaction.out[me], interaction.kout[me]);
				yalpPairGameAt(interactionGroup);
				commitStrategyAt(me);
				// add new scores - we need to do this twice: once for the focal site and
				// once for the opponents site.
				playGameAt(interactionGroup);
				playGameAt(interactionGroup);
				return;
			}
			// remove old scores
			int[] neigh = interaction.out[me];
			int nNeigh = reproduction.kout[me];
			interactionGroup.setGroupAt(me, neigh, nNeigh);
			yalpGroupGameAt(interactionGroup);
			for( int i=0; i<nNeigh; i++ ) {
				int you = neigh[i];
				interactionGroup.setGroupAt(you, interaction.out[you], interaction.kout[you]);
				yalpGroupGameAt(interactionGroup);
			}
			commitStrategyAt(me);
			// add new scores
			interactionGroup.setGroupAt(me, neigh, nNeigh);
			playGameAt(interactionGroup);
			for( int i=0; i<nNeigh; i++ ) {
				int you = neigh[i];
				interactionGroup.setGroupAt(you, interaction.out[you], interaction.kout[you]);
				playGameAt(interactionGroup);
			}
			return;
		}

		// directed graph - separately interact with in- and out-neighbors
		if( pairwise ) {
			// remove old scores
			interactionGroup.setGroupAt(me, interaction.out[me], interaction.kout[me]);
			yalpPairGameAt(interactionGroup);
			interactionGroup.setGroupAt(me, interaction.in[me], interaction.kin[me]);
			yalpPairGameAt(interactionGroup);
			commitStrategyAt(me);
			interactionGroup.setGroupAt(me, interaction.out[me], interaction.kout[me]);
			playGameAt(interactionGroup);
			interactionGroup.setGroupAt(me, interaction.in[me], interaction.kin[me]);
			playGameAt(interactionGroup);
			return;
		}
		// remove old scores
		int[] neigh = interaction.out[me];
		int nNeigh = interaction.kout[me];
		interactionGroup.setGroupAt(me, neigh, nNeigh);
		yalpGroupGameAt(interactionGroup);
		for( int i=0; i<nNeigh; i++ ) {
			int you = neigh[i];
			interactionGroup.setGroupAt(you, interaction.out[you], interaction.kout[you]);
			yalpGroupGameAt(interactionGroup);
		}
		neigh = interaction.in[me];
		nNeigh = interaction.kin[me];
		interactionGroup.setGroupAt(me, neigh, nNeigh);
		yalpGroupGameAt(interactionGroup);
		for( int i=0; i<nNeigh; i++ ) {
			int you = neigh[i];
			interactionGroup.setGroupAt(you, interaction.in[you], interaction.kin[you]);
			yalpGroupGameAt(interactionGroup);
		}
		commitStrategyAt(me);
		// add new scores
		neigh = interaction.out[me];
		nNeigh = interaction.kout[me];
		interactionGroup.setGroupAt(me, neigh, nNeigh);
		playGameAt(interactionGroup);
		for( int i=0; i<nNeigh; i++ ) {
			int you = neigh[i];
			interactionGroup.setGroupAt(you, interaction.out[you], interaction.kout[you]);
			playGameAt(interactionGroup);
		}
		neigh = interaction.in[me];
		nNeigh = interaction.kin[me];
		interactionGroup.setGroupAt(me, neigh, nNeigh);
		playGameAt(interactionGroup);
		for( int i=0; i<nNeigh; i++ ) {
			int you = neigh[i];
			interactionGroup.setGroupAt(you, interaction.in[you], interaction.kin[you]);
			playGameAt(interactionGroup);
		}
	}

	/**
	 *
	 * @param index
	 * @param newscore
	 */
	protected void updateScoreAt(int index, double newscore) {
		updateScoreAt(index, newscore, 1);
	}

	/**
	 * Update the payoff/score of individual with index <code>index</code> by adding (or 
	 * removing) <code>newscore</code> as the result of <code>incr</code> interactions.
	 * <p>
	 * <strong>Important:</strong> Strategies are already committed when adding scores 
	 * (<code>incr&gt;0</code>) but not when removing scores (<code>incr&lt;0</code>). However, 
	 * this routine is never called for focal site (i.e. the one that has changed and 
	 * hence where it matters whether strategies are committed). Instead, 
	 * {@link #resetScoreAt(int)} has to deal with focal site.
	 * </p>
	 * @param index of individual to update payoff/score
	 * @param newscore score/payoff to add (<code>incr&gt;0</code>) or subtract 
	 * 		  (<code>incr&lt;0</code>)
	 * @param incr number of interactions
	 */
	protected void updateScoreAt(int index, double newscore, int incr) {
		double before = scores[index];
		if( incr==0 ) {
			// makes sense only for constant selection
			scores[index] = newscore;
		}
		else {
			if( incr<0 ) newscore = -newscore;
			if( playerScoreAveraged ) {
				int count = interactions[index];
				scores[index] = (before*count+newscore)/Math.max(1, count+incr);
			}
			else {
				scores[index] += newscore;
			}
			interactions[index] += incr;
		}
		updateEffScoreRange(index, before, scores[index]);
		updateFitnessAt(index);
	}

	/**
	 * utility function for scores-to-fitness mapping and to keep track of sumFitness
	 *
	 * @param idx index of site
	 */
	public void updateFitnessAt(int idx) {
		double after = mapToFitness(scores[idx]);
		double diff = after-(isVacantAt(idx)?0.0:fitness[idx]);
		fitness[idx] = after;
		sumFitness += diff;
		// whenever sumFitness decreases dramatically rounding errors become an issue
		// if update reduces sumFitness by half or more, recalculate from scratch
		if( -diff>sumFitness ) sumFitness = ArrayMath.norm(fitness);
	}

	/**
	 * utility function to set scores and fitness and adjust sumFitness; assumes that resetScores
	 * was called earlier (at least for those sites that setScoreAt is used for updating their score)
	 *
	 * @param index index of site
	 * @param newscore new score to set
	 * @param inter number of interactions
	 */
	protected void setScoreAt(int index, double newscore, int inter) {
		interactions[index] = inter;
		scores[index] = (playerScoreAveraged?newscore:newscore*inter);
		double fit = mapToFitness(scores[index]);
		fitness[index] = fit;
		sumFitness += fit;
	}

	/**
	 *
	 * @param index
	 * @param nilscore
	 */
	protected void removeScoreAt(int index, double nilscore) {
		updateScoreAt(index, nilscore, -1);
	}

	protected void removeScoreAt(int index, double nilscore, int incr) {
		updateScoreAt(index, nilscore, -incr);
	}

	/**
	 * Reset score of individual at index <code>index</code>.
	 * <p>
	 * <strong>Important:</strong> Strategies not committed at this point.
	 * </p>
	 * @param index of individual
	 */
	public void resetScoreAt(int index) {
//XXX revise entire strategy updating business; it's inefficient to first reset scores then update strategies then update score...
		double before = scores[index];
		scores[index] = 0.0;
		interactions[index] = 0;
		updateEffScoreRange(index, before, 0.0);
		sumFitness -= fitness[index];
		fitness[index] = 0.0;
	}

	/**
	 *
	 * @param idxa
	 * @param idxb
	 */
	public void swapScoresAt(int idxa, int idxb) {
		double myScore = scores[idxa];
		scores[idxa] = scores[idxb];
		scores[idxb] = myScore;
		int myInteractions = interactions[idxa];
		interactions[idxa] = interactions[idxb];
		interactions[idxb] = myInteractions;
		if( maxEffScoreIdx==idxa ) maxEffScoreIdx = idxb;
		else if( maxEffScoreIdx==idxb ) maxEffScoreIdx = idxa;
	}

	/**
	 * reset all scores and fitness to zero
	 */
	public void resetScores() {
		// well-mixed populations use lookup table for scores and fitness
		if( scores!=null )
			Arrays.fill(scores, 0.0);
		if( fitness!=null )
			Arrays.fill(fitness, 0.0);
		Arrays.fill(interactions, 0);
		sumFitness = 0.0;
		if( VACANT<0 ) {
			// no vacancies
			maxEffScoreIdx = 0;
			return;
		}
// NOTE: setting maxEffScoreIdx<0 disables tracking of maximum - useful for frequency independent cases
//		// getPopulationSize() accounts for potential vacancies
//		int nPopEff = getPopulationSize();
//		if( nPopEff==0 ) {
//			// no population
//			maxEffScoreIdx = -1;
//			return;
//		}
		int start = -1;
		// find first non-vacant site
//XXX what if population is empty?
		while( isVacantAt(++start) );
		maxEffScoreIdx = start;
	}

	/**
	 * Reset all scores to <code>homoScore</code>.
	 * 
	 * @param homoScore
	 */
	public void resetScores(double homoScore) {
		double fit = 0.0;
		double score = 0.0;
		int inter = 0;
		int mIdx = -1;
		if( !isVacantAt(0) ) {
			// homogeneous population is not vacant
			score = playerScoreAveraged?homoScore:nInteractions*homoScore;
			fit = mapToFitness(score);
			inter = nInteractions;
			mIdx = 0;
		}
		Arrays.fill(scores, score);
		Arrays.fill(fitness, fit);
		Arrays.fill(interactions, inter);
		// getPopulationSize() accounts for potential vacancies
		sumFitness = getPopulationSize()*fit;
		maxEffScoreIdx = mIdx;
	}

	/**
	 * Update the scores of every individual in the population.
	 */
	public void updateScores() {
		for( int n=0; n<nPopulation; n++ ) {
			// since we update everybody here, we do not want to treat in- and out-going links differently -
			// after all, each outgoing link is an incoming link for another node...
			playGameSyncAt(n);
			updateFitnessAt(n);
		}
		setMaxEffScoreIdx();
	}

	/**
	 * utility function to retrieve the payoff at site idx
	 *
	 * @param idx index of site
	 * @return effective score
	 */
	public double getScoreAt(int idx) {
		return scores[idx];
	}

	/**
	 * utility function to retrieve the fitness at site idx
	 *
	 * @param idx index of site
	 * @return effective score
	 */
	public double getFitnessAt(int idx) {
		return fitness[idx];
	}

	/**
	 * Keep track of highest score through a reference to the corresponding individual.
	 * <p>
	 * <strong>Note:</strong> similarly keeping track of the lowest performing individual is very 
	 *		 costly because the poor performers are much more likely to get updated. in contrast,
	 *		 the maximum performer is least likely to get replaced.
	 *		 For example, in <code>cSD</code> with a well-mixed population of <code>10'000</code> 
	 *		 spends <code>&gt;80%</code> of CPU time hunting down the least performing individual!
	 *		 Besides, the minimum score is never used. The maximum score is only used for
	 *		 global, fitness based selection such as the Birth-death process. (room for optimization?)
	 * </p>
	 * @param index
	 * @param before
	 * @param after
	 * @return <code>true</code> if effective range of payoffs has changed (or, more precisely, if the reference individuals 
	 * 		   have changed); <code>false</code> otherwise.
	 */
	protected boolean updateEffScoreRange(int index, double before, double after) {
		// note: for synchronous updates this is not only wasteful but problematic as later
		//		 updated sites will experience different conditions
		//		 for constant fitness (maxEffScoreIdx<0) tracking the maximum is not needed
		if( isSynchronous || maxEffScoreIdx<0 ) return false;

		if( after>before ) {
			// score increased
			if( index==maxEffScoreIdx ) return false;
			if( after<=scores[maxEffScoreIdx] ) return false;
			maxEffScoreIdx = index;
			return true;
		}
		// score decreased
		if( after<before ) {
			if( index==maxEffScoreIdx ) {
				// maximum score decreased - find new maximum
				setMaxEffScoreIdx();
				return true;
			}
			return false;
		}
//XXX this code is almost dead - applies only if after==before... suspicious!
		// site became VACANT
		if( isVacantAt(index) ) {
			if( index==maxEffScoreIdx ) {
				setMaxEffScoreIdx();
				return true;
			}
			return false;
		}
		// score unchanged
		return false;
	}

	/**
	 *
	 */
	protected void setMaxEffScoreIdx() {
		if( maxEffScoreIdx<0 )
			return;
		maxEffScoreIdx = ArrayMath.maxIndex(scores);
	}

	/**
	 * @return real time increment
	 */
	public double step() {
//TODO: review migration - should be an independent event, independent of population update
		double rincr;
		if (pMigration > 0.0 && random01() < pMigration) {
			// migration event
			rincr = 1.0/(sumFitness*speciesUpdateRate);
			doMigration();
			return rincr;
		}
		// real time increment based on current fitness
		switch (populationUpdateType) {
			case ASYNC: // exclusively the current payoff matters
				rincr = 1.0/(sumFitness*speciesUpdateRate);
				updatePlayerAsyncReplicate();
				return rincr;

			case MORAN_BIRTHDEATH: // moran process - birth-death
				rincr = 1.0/(sumFitness*speciesUpdateRate);
				updatePlayerMoranBirthDeath();
				return rincr;

			case MORAN_DEATHBIRTH: // moran process - death-birth
				rincr = 1.0/(sumFitness*speciesUpdateRate);
				updatePlayerMoranDeathBirth();
				return rincr;

			case ECOLOGY: // ecological updating - varying population sizes
//XXX updatePlayerEcologyAt returns time increment in real time units - how to scale with several populations?
//				rincr = updatePlayerEcology();
//				realtime += rincr / getPopulationUpdateRate();
//				break;
				return updatePlayerEcology()/speciesUpdateRate;

			default: // last resort - opportunity to implement custom updating schemes
//				// updatePlayerCustom must return time increment in realtime units
//				rincr = updatePlayerCustom();
//				realtime += rincr / getPopulationUpdateRate();
				return updatePlayerCustom()/speciesUpdateRate;
		}
	}

	/**
	 *
	 */
	protected void updatePlayerAsyncReplicate() {
		updatePlayerAsyncReplicateAt(pickRandomFocalIndividual());
	}

	/**
	 *
	 * @param me
	 */
	protected void updatePlayerAsyncReplicateAt(int me) {
		/* in the case of Group.SAMPLING_ALL, and not Geometry.MEANFIELD,
		 * it is possible to adjust the payoffs of the relevant players after a strategy change
		 * occurred. this nice approach is not possible if interact with randomly chosen
		 * neighbors or random individuals from the population. in the latter cases, the payoffs
		 * of all players in the reference neighborhood are determined by playing a single
		 * game. if reference and interaction neighborhoods overlap, the final payoff may
		 * be derived from several interactions and is averaged accordingly.
		 */
		if( adjustScores ) {
			if( !updatePlayerAt(me) ) return;
			/* player switched strategy - adjust scores, commit strategy */
			adjustGameScoresAt(me);
			return;
		}

		/* alternative approach - update random player and play one game */
		if( updatePlayerAt(me) ) {
			resetScoreAt(me);
			commitStrategyAt(me);
		}
		playGameAt(me);
	}

	// allow subclasses to provide optimized implementations
	/**
	 *
	 */
	protected void updatePlayerMoranBirthDeath() {
		updatePlayerMoranBirthDeathAt(pickFitFocalIndividual());
	}

	/**
	 * Player 'parent' reproduces and its offspring replaces a randomly selected neighbor
	 * <p>
	 * <strong>Note:</strong> in original Moran process offspring can replace parent. 
	 * However, do not return prematurely if <code>vacant==parent</code> because this 
	 * would omit mutations and fail to reset score
	 * </p>
	 * @param parent
	 */
	protected void updatePlayerMoranBirthDeathAt(int parent) {
		// note: choose random neighbor among out-neighbors (those are downstream to get replaced; this is the opposite for imitation scenarios) 
		referenceGroup.pickAt(parent, reproduction, true);
		int vacant = referenceGroup.group[0];
		if( vacant<0 ) return;	// parent has no outgoing-neighbors (sink)
		updatePlayerMoran(parent, vacant);
	}

	// allow subclasses to provide optimized implementations
	/**
	 *
	 */
	protected void updatePlayerMoranDeathBirth() {
		updatePlayerMoranDeathBirthAt(pickRandomFocalIndividual());
	}

	/**
	 * player 'vacant' has left a vacant site that will be populated by one of its neighbors
	 * with a probability proportional to their fitness
	 *
	 * @param vacant
	 */
	protected void updatePlayerMoranDeathBirthAt(int vacant) {
		int parent = drawFitNeighborAt(vacant);
		if( parent<0 ) return;	// vacant has no incoming-neighbors (source)
		updatePlayerMoran(parent, vacant);
	}

	/**
	 * moran optimization for discrete strategies requires access to this method (DPopulation).
	 *
	 * @param source
	 * @param dest
	 */
	protected void updatePlayerMoran(int source, int dest) {
		if( adjustScores ) {
			// allow for mutations
			if( pMutation>=1.0 || (pMutation>0.0 && random01()<pMutation) ) {
				updateFromModelAt(dest, source);
				mutateStrategyAt(dest, true);
				adjustGameScoresAt(dest);
				return;
			}
			if( haveSameStrategy(source, dest) ) return;
			updateFromModelAt(dest, source);
			adjustGameScoresAt(dest);
			return;
		}

		// allow for mutations
		if( pMutation>=1.0 || (pMutation>0.0 && random01()<pMutation) ) {
			updateFromModelAt(dest, source);
			mutateStrategyAt(dest, true);
			resetScoreAt(dest);
			commitStrategyAt(dest);
			playGameAt(dest);
			return;
		}
		if( !haveSameStrategy(source, dest) ) {
			// replace 'vacant'
			updateFromModelAt(dest, source);
			resetScoreAt(dest);
			commitStrategyAt(dest);
			playGameAt(dest);
			return;
		}
		// no actual strategy change occurred - reset score always (default) or only on actual change?
		if( playerScoreResetAlways )
			resetScoreAt(dest);
		playGameAt(dest);
	}

	/**
	 * currently, this needs to be implemented by game class
	 *
	 * @return real-time increment
	 */
	protected double updatePlayerEcology() {
		return updatePlayerEcologyAt(pickRandomFocalIndividual());
	}

	/**
	 * ecological update at site index
	 *
	 * @param index
	 * @return real-time increment
	 */
	protected double updatePlayerEcologyAt(int index) {
		throw new Error("updatePlayerEcologyAt not implemented.");
	}

	/**
	 * currently, this needs to be implemented by game class
	 *
	 * @return real-time increment
	 */
	protected double updatePlayerCustom() {
		return updatePlayerCustomAt(pickRandomFocalIndividual());
	}

	/**
	 * custom update at site index
	 *
	 * @param index
	 * @return real-time increment
	 */
	protected double updatePlayerCustomAt(int index) {
		throw new Error("updatePlayerCustomAt not implemented.");
	}

	/**
	 * returns true if strategy changed (score needs to be reset)
	 * 
	 * @param me
	 * @return
	 */
	protected boolean updatePlayerAt(int me) {
		// note: choose random neighbor among in-neighbors (those are upstream to serve as models; this is the opposite for birth-death scenarios)
		referenceGroup.pickAt(me, reproduction, false);
		return updatePlayerAt(me, referenceGroup.group, referenceGroup.size);
	}

	/* returns true if strategy changed -> score will be reset */
	private boolean updatePlayerAt(int me, int[] refGroup, int refGroupSize) {
		if( refGroupSize <= 0 ) return false;

		boolean switched;
		switch( playerUpdateType ) {
			case BEST_REPLY: // best-reply update
				// this makes little sense for continuous strategies - should not happen...
				// takes entire population (mean-field) or entire neighborhood into account.
				// for details check updatePlayerBestReply() in DPopulation.java
				switched = updatePlayerBestReply(me, refGroup, refGroupSize);
				break;

			case BEST: // best update
				switched = updatePlayerBest(me, refGroup, refGroupSize);
				break;

			case BEST_RANDOM: // best update - equal payoffs 50% chance to switch
				switched = updatePlayerBestHalf(me, refGroup, refGroupSize);
				break;

			case PROPORTIONAL: // proportional update
				switched = updateProportionalAbs(me, refGroup, refGroupSize);
				break;

			case IMITATE_BETTER: // imitation update
				switched = updateReplicatorPlus(me, refGroup, refGroupSize);
				break;

			case IMITATE: // imitation update
				switched = updateReplicatorHalf(me, refGroup, refGroupSize);
				break;

			case THERMAL:
				switched = updateThermal(me, refGroup, refGroupSize);
				break;

			default:
				throw new Error("Unknown update method for players ("+playerUpdateType+")");
		}
		if( pMutation>=1.0 || (pMutation>0.0 && random01()<pMutation) ) {
			mutateStrategyAt(me, switched);
			return true;	// if mutated always indicate change
		}
		if( playerScoreResetAlways )
			return switched;
		// signal change only if actual change of strategy occurred
		return !isSameStrategy(me);
	}

	/**
	 * returns true if strategy changed (score needs to be reset)
	 * <p>
	 * <strong>Note:</strong> for the best update it does not matter whether scores are averaged or accumulated
	 * </p>
	 * @param me
	 * @param refGroup
	 * @param rGroupSize
	 * @return
	 */
	protected boolean updatePlayerBest(int me, int[] refGroup, int rGroupSize) {

		// neutral case: no one is better -> nothing happens
		if( Math.abs(maxScore-minScore)<1e-8 ) return false;

		int bestPlayer = me;
		double bestScore = getFitnessAt(me);
		boolean switched = false;

		for( int i=0; i<rGroupSize; i++ ) {
			int aPlayer = refGroup[i];
			double aScore = getFitnessAt(aPlayer);
			double bScore = aScore;
			if( Math.abs(bestScore-bScore)<1e-8 ) {
				// we need some expert advice on which strategy is the preferred one
				// does 'me' prefer 'aPlayer' over 'bestPlayer'?
				if( preferredPlayerBest(me, bestPlayer, aPlayer) ) bScore += 1e-8;
				else bScore -= 1e-8;
			}
			if( bestScore>bScore ) continue;
			bestScore = aScore;
			bestPlayer = aPlayer;
			switched = true;
		}
		if( !switched ) return false;
		updateFromModelAt(me, bestPlayer);
		return true;
	}

	/**
	 * returns true if strategy changed (score needs to be reset)
	 * <p>
	 * <strong>Note:</strong> for the best update it does not matter whether scores are averaged or accumulated
	 * </p>
	 * @param me
	 * @param refGroup
	 * @param rGroupSize
	 * @return
	 */
	protected boolean updatePlayerBestHalf(int me, int[] refGroup, int rGroupSize) {
		int bestPlayer = me;
		double bestScore = getFitnessAt(me);
		boolean switched = false;

		for( int i=0; i<rGroupSize; i++ ) {
			int aPlayer = refGroup[i];
			double aScore = getFitnessAt(aPlayer);
			if( aScore>bestScore ) {
				bestScore = aScore;
				bestPlayer = aPlayer;
				switched = true;
				continue;
			}
			if( Math.abs(aScore-bestScore)<1e-8 ) {
				// equal scores - switch with probability 50%
				if( random01()<0.5 ) {
					bestPlayer = aPlayer;
					switched = true;
				}
			}
		}
		if( !switched ) return false;
		updateFromModelAt(me, bestPlayer);
		return true;
	}

	/**
	 * returns true if strategy changed (score needs to be reset)
	 * 
	 * @param me
	 * @param refGroup
	 * @param rGroupSize
	 * @return
	 */
	protected boolean updateProportionalAbs(int me, int[] refGroup, int rGroupSize) {

		// neutral case: choose random neighbor or individual itself
		if( Math.abs(maxScore-minScore)<1e-8 ) {
			int hit = random0n(rGroupSize+1);
			if( hit == rGroupSize ) return false;
			updateFromModelAt(me, refGroup[hit]);
			return true;
		}

		double myScore, totScore, aScore;
		double minFit = mapToFitness(minScore);
		myScore = getFitnessAt(me)-minFit;
		totScore = myScore;
		for( int i=0; i<rGroupSize; i++ ) {
			aScore = getFitnessAt(refGroup[i])-minFit;
			groupScores[i] = aScore;
			totScore += aScore;
		}

		if( totScore<=0.0 ) {	// everybody has the minimal score - pick at random
			int hit = random0n(rGroupSize+1);
			if( hit==rGroupSize ) return false;
			updateFromModelAt(me, refGroup[hit]);
			return true;
		}

		double choice = random01()*totScore;
		double bin = myScore;
		if( choice<=bin ) return false;		// individual keeps its place

		choice -= bin;
		for( int i=0; i<rGroupSize; i++ ) {
			bin = groupScores[i];
			if( choice<=bin ) {
				updateFromModelAt(me, refGroup[i]);
				return true;
			}
			choice -= bin;
		}
		// should not get here!
		throw new Error("Problem in updateProportionalAbs()...");
	}

	/**
	 * returns true if strategy changed (score needs to be reset)
	 * <p>
	 * replicator dynamics, imitate only better performing strategies 
	 * <code>(P<sub>i</sub>-P<sub>j</sub>)<sub>+</sub>/a</code>, <code>a</code> normalization
	 * </p>
	 * @param me
	 * @param refGroup
	 * @param rGroupSize
	 * @return
	 */
	protected boolean updateReplicatorPlus(int me, int[] refGroup, int rGroupSize) {
		return updateReplicator(me, refGroup, rGroupSize, true);
	}

	/**
	 * returns true if strategy changed (score needs to be reset)
	 * <p>
	 * replicator dynamics, linear preference of strategies 
	 * <code>1/2+(P<sub>i</sub>-P<sub>j</sub>)/a</code>, <code>a</code> normalization
	 * </p>
	 * @param me
	 * @param refGroup
	 * @param rGroupSize
	 * @return
	 */
	protected boolean updateReplicatorHalf(int me, int[] refGroup, int rGroupSize) {
		return updateReplicator(me, refGroup, rGroupSize, false);
	}

	private boolean updateReplicator(int me, int[] refGroup, int rGroupSize, boolean betterOnly) {
		// neutral case
		if( Math.abs(maxScore-minScore)<1e-8 ) {
			// return if betterOnly because no one is better
			if( betterOnly ) return false;
			// choose random neighbor or individual itself
			int hit = random0n(rGroupSize+1);
			if( hit == rGroupSize ) return false;
			updateFromModelAt(me, refGroup[hit]);
			return true;
		}

		double myScore = getFitnessAt(me);
		double aProb, nProb, norm;
		double equalProb = betterOnly?playerError:0.5;

		if( invThermalNoise<=0.0 ) {
			// zero noise
			// generalize update to competition among arbitrary numbers of players
			double aDiff = getFitnessAt(refGroup[0])-myScore;
			if( aDiff>0.0 ) aProb = 1.0-playerError;
			else aProb = (aDiff<0.0?playerError:equalProb);
			norm = aProb;
			nProb = 1.0-aProb;
			if( rGroupSize>1 ) {
				cProbs[0] = aProb;
				for( int i=1; i<rGroupSize; i++ ) {
					aDiff = getFitnessAt(refGroup[i])-myScore;
					if( aDiff>0.0 ) aProb = 1.0-playerError;
					else aProb = (aDiff<0.0?playerError:equalProb);
					cProbs[i] = cProbs[i-1]+aProb;
					nProb *= 1.0-aProb;
					norm += aProb;
				}
			}
		}
		else {
			double inoise = invThermalNoise;
			double shift = 0.0;
			if( !betterOnly ) {
				inoise = 0.5*invThermalNoise;
				shift = 0.5;
			}
			if( playerScoreAveraged ) {
				double scale = inoise/(mapToFitness(maxScore)-mapToFitness(minScore));

				// generalize update to competition among arbitrary numbers of players
				aProb = Math.min(1.0-playerError, Math.max(playerError, (getFitnessAt(refGroup[0])-myScore)*scale+shift));
				norm = aProb;
				nProb = 1.0-aProb;
				if( rGroupSize>1 ) {
					cProbs[0] = aProb;
					for( int i=1; i<rGroupSize; i++ ) {
						aProb = Math.min(1.0-playerError, Math.max(playerError, (getFitnessAt(refGroup[i])-myScore)*scale+shift));
						cProbs[i] = cProbs[i-1]+aProb;
						nProb *= 1.0-aProb;
						norm += aProb;
					}
				}
			}
			else {
				// take number of interactions into account for normalization; scores denote averages
				// note: because the number of interactions may differ the baseline fitness requires more careful treatment
				int inter = interactions[me];
				double myMinScore = mapToFitness(minScore*inter);
				// generalize update to competition among arbitrary numbers of players
				int you = refGroup[0];
				inter = interactions[you];
				aProb = Math.min(1.0-playerError, Math.max(playerError, (getFitnessAt(you)-myScore)/(mapToFitness(maxScore*inter)-myMinScore)*inoise+shift));
				norm = aProb;
				nProb = 1.0-aProb;
				if( rGroupSize>1 ) {
					cProbs[0] = aProb;
					for( int i=1; i<rGroupSize; i++ ) {
						you = refGroup[i];
						inter = interactions[you];
						aProb = Math.min(1.0-playerError, Math.max(playerError, (getFitnessAt(you)-myScore)/(mapToFitness(maxScore*inter)-myMinScore)*inoise+shift));
						cProbs[i] = cProbs[i-1]+aProb;
						nProb *= 1.0-aProb;
						norm += aProb;
					}
				}
			}
		}
		if( norm<=0.0 ) return false;

		double choice = random01();
		if( choice >= 1.0-nProb ) return false;

		// optimization
		if( rGroupSize==1 ) {
			updateFromModelAt(me, refGroup[0]);
			return true;
		}

		norm = (1.0-nProb)/norm;
		for( int i=0; i<rGroupSize; i++ ) cProbs[i] *= norm;

		for( int i=0; i<rGroupSize; i++ ) {
			if( choice < cProbs[i] ) {
				updateFromModelAt(me, refGroup[i]);
				return true;
			}
		}
		/* should not get here! */
		throw new Error("Problem in "+(betterOnly?"updateReplicatorPlus()...":"updateReplicatorHalf()..."));
	}

	/**
	 * returns true if strategy changed (score needs to be reset)
	 * 
	 * @param me
	 * @param refGroup
	 * @param rGroupSize
	 * @return
	 */
	protected boolean updateThermal(int me, int[] refGroup, int rGroupSize) {

		// neutral case: choose random neighbor or individual itself
		if( Math.abs(maxScore-minScore)<1e-8 ) {
			int hit = random0n(rGroupSize+1);
			if( hit == rGroupSize ) return false;
			updateFromModelAt(me, refGroup[hit]);
			return true;
		}

		double myScore = getFitnessAt(me);
		double norm, nProb;

		// generalize update to competition among arbitrary numbers of players
		// treat case of zero noise separately
		if( invThermalNoise<=0.0 ) {	// zero noise
			double aProb;
			double aDiff = getFitnessAt(refGroup[0])-myScore;
			if( aDiff>0.0 ) aProb = 1.0-playerError;
			else aProb = (aDiff<0.0?playerError:0.5);
			norm = aProb;
			nProb = 1.0-aProb;
			if( rGroupSize>1 ) {
				cProbs[0] = aProb;
				for( int i=1; i<rGroupSize; i++ ) {
					aDiff = getFitnessAt(refGroup[i])-myScore;
					if( aDiff>0 ) aProb = 1.0-playerError;
					else aProb = (aDiff<0.0?playerError:0.5);
					cProbs[i] = cProbs[i-1]+aProb;
					nProb *= 1.0-aProb;
					norm += aProb;
				}
			}
		}
		else {
			// some noise
			double aProb = Math.min(1.0-playerError, Math.max(playerError, 1.0/(2.0+Math.expm1(-(getFitnessAt(refGroup[0])-myScore)*invThermalNoise))));
			norm = aProb;
			nProb = 1.0-aProb;
			if( rGroupSize>1 ) {
				cProbs[0] = aProb;
				for( int i=1; i<rGroupSize; i++ ) {
					aProb = Math.min(1.0-playerError, Math.max(playerError, 1.0/(2.0+Math.expm1(-(getFitnessAt(refGroup[i])-myScore)*invThermalNoise))));
					cProbs[i] = cProbs[i-1]+aProb;
					nProb *= 1.0-aProb;
					norm += aProb;
				}
			}
		}
		if( norm<=0.0 ) return false;
		double choice = random01();
		if( choice >= 1.0-nProb ) return false;

		// optimization
		if( rGroupSize==1 ) {
			updateFromModelAt(me, refGroup[0]);
			return true;
		}

		norm = (1.0-nProb)/norm;
		for( int i=0; i<rGroupSize; i++ )
			cProbs[i] *= norm;

		for( int i=0; i<rGroupSize; i++ ) {
			if( choice < cProbs[i] ) {
				updateFromModelAt(me, refGroup[i]);
				return true;
			}
		}
		// should not get here!
		String msg = "Report: myScore="+myScore+", nProb="+nProb+", norm="+norm+", choice="+choice+"\nCumulative probabilities: ";
		for( int i=0; i<rGroupSize; i++ ) msg += cProbs[i]+"\t";
		logger.fine(msg);
		throw new Error("Problem in updateThermal()...");
	}

	/**
	 *
	 * @return
	 */
	public double getMinScore() {
		return processMinMaxScore(getMinGameScore(), true);
	}

	/**
	 *
	 * @return
	 */
	public double getMaxScore() {
		return processMinMaxScore(getMaxGameScore(), false);
	}

	/**
	 *
	 * @return
	 */
	public double getMinMonoScore() {
		return processMinMaxScore(getMinMonoGameScore(), true);
	}

	/**
	 *
	 * @return
	 */
	public double getMaxMonoScore() {
		return processMinMaxScore(getMaxMonoGameScore(), false);
	}

	/**
	 *
	 * @return
	 */
	public double getMinFitness() {
		return mapToFitness(getMinScore());
	}

	private double processMinMaxScore(double minmax, boolean isMin) {
		// assumes averaged or adjustable payoffs
		if( playerScoreAveraged )
			return minmax;
		if( adjustScores ) {
			// for accumulated scores minimum may depend on geometry both in terms of game payoffs as well as
			// interaction count. however, getMinGameScore must deal with structure and games
			if( pairwise ) {
				if( interaction.minOut+interaction.maxOut>0 ) {
					// geometry initialized and not well-mixed
					if( isMin ) return (minmax<0?interaction.maxOut:interaction.minOut)*(2*nInteractions)*minmax;
					return (minmax>0?interaction.maxOut:interaction.minOut)*(2*nInteractions)*minmax;
				}
				// well-mixed, with vacancies, at most nPopulation-1 interactions
				return (2*nInteractions)*(nPopulation-1)*minmax;
			}
			// each individual participates in at most nGroup interactions
			return nInteractions*nGroup*minmax;
		}
		// not ready for unbounded accumulated payoffs... checkParams should catch this
		throw new Error("cannot handle accumulated scores with random interactions (unbounded payoffs, in principle)");
	}

	/* 
	 *
	 * administrative methods 
	 *
	 */
	public void updateMinMaxScores() {
		minScore = getMinScore();
		maxScore = getMaxScore();
	}

	@Override
	public boolean check() {
//XXX temporary - delegate should always be set (otherwise IBS model would not get initialized)
//		boolean doReset = delegate.checkIBS(this);
		boolean doReset = false;
//		if (delegate != null)
//			doReset = delegate.checkIBS(this);
		// check population geometry - for this we need to know the model (see reset)
		if( nPopulation<1 ) {
			logger.warning("population size "+nPopulation+" is not admissible - set to 100!");
			setNPopulation(100);
			doReset = true;
		}

		// check geometries: --geometry set structure, --geominter set interaction and --geomrepro set reproduction.
		// now it is time to amalgamate. the more specific options --geominter, --geomrepro take precedence. structure
		// is always available from parsing the default of cloGeometry. hence first check --geominter, --geomrepro.
		if( interaction!=null ) {
			if( reproduction!=null ) {
				// NOTE: this is hackish because every population has its own cloGeometry parsers but only one will actually do the parsing...
				Population master = engine.getGame();
				if( structure!=null ) {
					// --geometry was provided on command line
					if( !interaction.equals(structure) && !master.cloGeometryInteraction.isSet() ) {
						interaction = structure;
						doReset = true;
					}
					if( !reproduction.equals(structure) && !master.cloGeometryReproduction.isSet() ) {
						reproduction = structure;
						doReset = true;
					}
					interaction.interReproSame = reproduction.interReproSame = interaction.equals(reproduction);
				}
				// both geometries set on command line OR parameters manually changed and applied - check if can be collapsed
				// NOTE: assumes that same arguments imply same geometries. this precludes that interaction and reproduction are both random
				//		 structures with otherwise identical parameters (e.g. random regular graphs of same degree but different realizations)
				if( !interaction.isValid || !reproduction.isValid ) {
					interaction.interReproSame = reproduction.interReproSame = 
							master.cloGeometryInteraction.getArg().equals(master.cloGeometryReproduction.getArg());
				}
			}
			else {
				// reproduction not set - use --geometry (or its default for reproduction)
				interaction.interReproSame = cloGeometryInteraction.getArg().equals(cloGeometry.getArg());
				if( !interaction.interReproSame ) {
					reproduction = structure;
					reproduction.interReproSame = false;
				}
			}
		}
		else {
			// interaction not set
			if( reproduction!=null ) {
				// reproduction set - use --geometry (or its default for interaction)
				// NOTE: this is slightly different from above because reproduction knows nothing about potentially different opponents in
				//		 inter-species interactions.
				interaction = structure;
				interaction.interReproSame = cloGeometryReproduction.getArg().equals(cloGeometry.getArg());
			}
			else {
				// neither geometry is set - e.g. initial launch with --geometry specified (or no geometry specifications at all)
				interaction = structure;
				interaction.interReproSame = true;
			}
		}
		String prefix = getName();
		if( interaction.interReproSame ) {
			if (prefix.isEmpty())
				interaction.name = "Structure";
			else
				interaction.name = prefix+": Structure";
			interaction.size = nPopulation;
			doReset |= interaction.check();
			// population structure may require special population sizes
			setNPopulation(interaction.size);
		}
		else {
			if( Geometry.displayUniqueGeometry(interaction, reproduction) ) {
				if (prefix.isEmpty())
					interaction.name = reproduction.name = "Structure";
				else
					interaction.name = reproduction.name = prefix+": Structure";
			}
			else {
				if (prefix.isEmpty()) {
					interaction.name = "Interaction";
					reproduction.name = "Reproduction";
				}
				else {
					interaction.name = prefix+": Interaction";
					reproduction.name = prefix+": Reproduction";
				}
			}
			interaction.size = nPopulation;
			doReset |= interaction.check();
			// population size may have been adjusted to accommodate interaction geometry
			reproduction.size = interaction.size;
			doReset |= reproduction.check();
			// population structures may require special population sizes
			// Warning: there is a small chance that the interaction and reproduction geometries require different population
			//			sizes, which does not make sense and would most likely result in a never ending initialization loop.
			setNPopulation(interaction.size);
		}
		// Note: now that interaction and reproduction are set, we still cannot set structure to null because of subsequent CLO parsing

		// check sampling in special geometries
		if( interaction.geometry==Geometry.SQUARE && interaction.isRegular && interaction.connectivity>8 &&
				getInteractionType()==Group.SAMPLING_ALL && nGroup>2 && nGroup<9 ) {
			// if count > 8 then the interaction pattern Group.SAMPLING_ALL with a group size between 2 and 8
			// (excluding boundaries is not allowed because this pattern requires a particular (internal) 
			// arrangement of the neighbors.
			setInteractionType(Group.SAMPLING_COUNT);
			logger.warning("square "+name+" geometry has incompatible interaction pattern and neighborhood size"+
					" - using random sampling of interaction partners!");
		}
		if( interaction.geometry==Geometry.CUBE && getInteractionType()==Group.SAMPLING_ALL && 
				nGroup>2 && nGroup<=interaction.connectivity ) {
			// Group.SAMPLING_ALL only works with pairwise interactions or all neighbors; restrictions do not apply for PDE's
			setInteractionType(Group.SAMPLING_COUNT);
			logger.warning("cubic "+name+" geometry has incompatible interaction pattern and neighborhood size"+
					" - using random sampling of interaction partners!");
		}

		// check reproduction geometry (reproduction may still be undefined at this point)
		int reprogeom = (reproduction!=null?reproduction.geometry:interaction.geometry);
		if( !isMoranType() ) {
			// Moran type updates ignore playerUpdateType
			if( reprogeom==Geometry.MEANFIELD && referenceGroup.samplingType==Group.SAMPLING_ALL ) {
				// 010320 using everyone as a reference in mean-field simulations is not feasible - except for best-reply
				if( playerUpdateType!=PlayerUpdateType.BEST_REPLY ) {
					logger.warning("reference type ("+getReferenceType()+") unfeasible in well-mixed populations!");
					setReferenceType(Group.SAMPLING_COUNT);
				}
			}
			// best-reply in well-mixed populations should skip sampling of references
			if( reprogeom==Geometry.MEANFIELD && playerUpdateType==PlayerUpdateType.BEST_REPLY ) {
				setReferenceType(Group.SAMPLING_NONE);
			}
		}

		isMultispecies = species.size() > 1;
		// currently: if pop has interaction structure different from MEANFIELD its
		// opponent population needs to be of the same size
		Population opponent = getOpponent();
		if (getNPopulation() != opponent.getNPopulation() 
				&& opponent.getInteractionGeometry()!=null	// opponent geometry may not yet be initialized
															// check will be repeated for opponent
				&& (getInteractionGeometry().geometry != Geometry.MEANFIELD
				|| opponent.getInteractionGeometry().geometry != Geometry.MEANFIELD)) {
			// at least for now, both populations need to be of the same size - except for
			// well-mixed populations
			logger.warning(
					"inter-species interactions with populations of different size limited to well-mixed structures"
							+ " - well-mixed structure forced!");
			getInteractionGeometry().geometry = Geometry.MEANFIELD;
			opponent.getInteractionGeometry().geometry = Geometry.MEANFIELD;
			doReset = true;
		}
		// combinations of unstructured and structured populations in inter-species interactions
		// require more attention. exclude for now.
		if( getInteractionGeometry().isInterspecies() && opponent.getInteractionGeometry()!=null ) {
			// opponent not yet ready; check will be repeated for opponent
			if( (getInteractionGeometry().geometry!=opponent.getInteractionGeometry().geometry) && 
					(getInteractionGeometry().geometry==Geometry.MEANFIELD || 
						opponent.getInteractionGeometry().geometry==Geometry.MEANFIELD) ) {
				logger.warning("interspecies interactions combining well-mixed and structured populations not (yet) tested"
						+ " - well-mixed structure forced!");
				getInteractionGeometry().geometry = Geometry.MEANFIELD;
				opponent.getInteractionGeometry().geometry = Geometry.MEANFIELD;
				doReset = true;
			}
		}

		if( pMigration<1e-10 ) 
			setMigrationType(MigrationType.NONE);
		if( migrationType!=MigrationType.NONE && pMigration>0.0 ) {
			if( !interaction.isUndirected ) {
				logger.warning("no migration on directed graphs!");
				setMigrationType(MigrationType.NONE);
			}
			else if( !interaction.interReproSame ) {
				logger.warning("no migration on graphs with different interaction and reproduction neighborhoods!");
				setMigrationType(MigrationType.NONE);
			}
			else if( interaction.geometry == Geometry.MEANFIELD ) {
				logger.warning("no migration in well-mixed populations!");
				setMigrationType(MigrationType.NONE);
			}
		}
		if( migrationType==MigrationType.NONE )
			setMigrationProb(0.0);
		else {
			// need to get new instance to make sure potential changes in pMigration are reflected
			distrMigrants = new RNGDistribution.Geometric(rng.getRNG(), 1.0-pMigration);
		}

		// check if adjustScores can be used - subclasses may have different opinions
		adjustScores = doAdjustScores();

		// accumulated scores and random sampling of interaction partners has, in principle, unbounded payoffs...
		// avoid this can of worms...
		if( !adjustScores && !playerScoreAveraged ) {
			setPlayerScoreAveraged(true);
			logger.warning("random sampling of interaction partners is incompatible with accumulated payoffs\n"+
					"because of unbounded fitness range and challenges to convert to probabilities.\n"+
					"switching to averaged scores.");
			adjustScores = doAdjustScores();	// should now be true
		}
		hasLookupTable |= (adjustScores && interaction.geometry==Geometry.MEANFIELD);

		// min and max scores potentially unbounded for accumulated scores with random interactions (no adjustable)
		minScore = getMinScore();
		maxScore = getMaxScore();

		// check for specific population update types
		switch( populationUpdateType ) {
			case MORAN_BIRTHDEATH:	 // moran process - birth-death
			case MORAN_DEATHBIRTH:	 // moran process - death-birth
				// avoid negative fitness for Moran type updates
				if( mapToFitness(minScore)<0.0 ) {
					logger.warning("Moran updates require fitness>=0 (score range ["+Formatter.format(minScore, 6)+", "+
							Formatter.format(maxScore, 6)+"]; "+
							"fitness range ["+Formatter.format(mapToFitness(minScore), 6)+", "+Formatter.format(mapToFitness(maxScore), 6)+"]).\n"+
							"Changed baseline fitness to "+getBaselineFitness()+(getMapToFitness()!=Map2Fitness.STATIC?" with static payoff-to-fitness map":""));
					// just change to something meaningful
					setMapToFitness(Map2Fitness.STATIC);
					setBaselineFitness(-getSelection()*minScore);
					minScore = getMinScore();
					maxScore = getMaxScore();
					if( mapToFitness(minScore)<0.0 ) {
						throw new Error("Adjustment of selection failed... (minimal fitness: "+Formatter.format(minScore, 6)+" should be positive)");
					}
				}
				// use referenceGroup for picking random neighbour in Moran process (birth-death)
				// reason: referenceGroup properly deals with hierarchies
				// future: pick parent to populate vacated site (death-birth, fitness dependent)
				setReferenceType(Group.SAMPLING_COUNT);
				setRGroupSize(1);
				break;
			default:	// all other update rules can handle this
		}
		return doReset;
	}

	/**
	 * adjust scores requires that individuals interact with all neighbours and that geometry is not well-mixed 
	 * (some implementation may improve on this, e.g. DPopulation can handle well-mixed populations)
	 * <dl>
	 * <dt>Group.SAMPLING_ALL</dt><dd>individuals need to be interacting with all their neighbours (not just a 
	 * randomly selected subset).</dd>
	 * <dt>Geometry.MEANFIELD</dt><dd>interactions with everyone are not feasible (impossible to model efficiently), 
	 * in general, for unstructured populations (subclasses can do better, e.g. for discrete strategies it is 
	 * possible, see {@link DPopulation#doAdjustScores()}).</dd>
	 * <dt>playerScoreResetAlways</dt><dd>if scores are reset whenever an individual adopts the strategy of another
	 * (regardless of whether an actual strategy change occurred) then the expected number of interactions of each
	 * individual remains constant over time (but may be different between individuals on heterogeneous structures).
	 * </dd>
	 * </dl>
	 * 
	 * @return <code>true</code> if adjusting scores is feasible
	 */
	protected boolean doAdjustScores() {
		return !(interaction.geometry==Geometry.MEANFIELD || 
				(interaction.geometry==Geometry.HIERARCHY && interaction.subgeometry==Geometry.MEANFIELD) ||
				interactionGroup.samplingType!=Group.SAMPLING_ALL ||
				!playerScoreResetAlways);
	}

	// at this point all parameters must be set
	@Override
	public synchronized void reset() {
		super.reset();
		interaction.init();
		interaction.rewire();
		interaction.evaluate();
		// for accumulated scores the final minimum and maximum scores may be known only now because it depends on min/max connectivity of nodes
		double mScore = getMinScore();
		if( Math.abs(minScore-mScore)>1e-8 ) {
			// minimum score has changed... now what?
			logger.info("minimum score has changed to "+Formatter.format(mScore, 6)+", was "+Formatter.format(minScore, 6));
			minScore = mScore;
		}
		mScore = getMaxScore();
		if( Math.abs(maxScore-mScore)>1e-8 ) {
			// minimum score has changed... now what?
			logger.info("maximum score has changed to "+Formatter.format(mScore, 6)+", was "+Formatter.format(maxScore, 6));
			maxScore = mScore;
		}
		if( interaction.interReproSame ) {
			reproduction = interaction.deriveReproductionGeometry();
		}
		else {
			reproduction.init();
			reproduction.rewire();
			reproduction.evaluate();
		}
		alloc();
	}

	/**
	 * implementation notes: 
	 *		1) memory allocation for groups (groupScores, smallScores, interactionGroup, referenceGroup, cProbs, changed)
	 *		requires initGeometry() so that maxGroup can be determined based on the current geometry).
	 *		2) 
	 *
	 * @return <code>true</code> to signal troubles in allocating memory.
	 */
	@Override
	public boolean alloc() {
		// otherwise this destroys geometry - undesired for reinit()
		if( scores==null || scores.length!=nPopulation )
			scores = new double[nPopulation];
		if( fitness==null || fitness.length!=nPopulation )
			fitness = new double[nPopulation];
		if( tags==null || tags.length!=nPopulation )
			tags = new double[nPopulation];
		if( interactions==null || interactions.length!=nPopulation )
			interactions = new int[nPopulation];
//		if( interactionGroup==null )
//			interactionGroup = new Group(rng);
//		if( referenceGroup==null )
//			referenceGroup = new Group(rng);
		// determine maximum reasonable group size
		int maxGroup = Math.max(Math.max(interaction.maxIn, interaction.maxOut), Math.max(reproduction.maxIn, reproduction.maxOut));
		maxGroup = Math.max(maxGroup, nGroup)+1;	// add 1 if focal should be part of group
		if( groupScores==null || groupScores.length!=maxGroup )
			groupScores = new double[maxGroup];	// can hold scores for any group size!
		if( smallScores==null || smallScores.length!=maxGroup )
			smallScores = new double[maxGroup];	// can hold scores for any group size!
		interactionGroup.alloc(maxGroup);
		referenceGroup.alloc(maxGroup);
		if( cProbs==null || cProbs.length!=maxGroup )
			cProbs = new double[maxGroup];					// can hold groups of any size!
		if( imitated==null || imitated.length!=maxGroup )
			imitated = new boolean[maxGroup];				// can hold groups of any size!
		return false;
	}

	@Override
	public void dealloc() {
		scores = null;
		fitness = null;
		tags = null;
		interactions = null;
	}

	@Override
	public void init() {
		super.init();
		// initialize tags (may not be available, e.g. in DE's)
		if( tags!=null )
			for( int n=0; n<nPopulation; n++ ) tags[n] = n;
	}

	/**
	 * provide a hook for subclasses - see DPopulation.java
	 */
	public void resetStrategies() { }

	/**
	 *
	 * @param map score-to-fitness map type
	 */
	public void setMapToFitness(Map2Fitness map) {
		score2FitnessMap = map;
	}

	/**
	 *
	 * @return score-to-fitness map type
	 */
	public Map2Fitness getMapToFitness() {
		return score2FitnessMap;
	}

	/**
	 * utility function for scores-to-fitness mapping and to keep track of sumScores
	 *
	 * @param score
	 * @return fitness
	 */
	public double mapToFitness(double score) {
		switch( score2FitnessMap ) {
			case STATIC:
				return playerBaselineFitness+playerSelection*score;
			case CONVEX:
				return playerBaselineFitness+playerSelection*(score-playerBaselineFitness);
			case EXPONENTIAL:
				return playerBaselineFitness*Math.exp(playerSelection*score);
			case NONE:
			default:
				return score;
		}
	}

	/**
	 *
	 * @param colors
	 * @param colorMap
	 */
	public <T> void getFitnessData(T[] colors, ColorMap<T> colorMap) {
		if( VACANT<0 ) {
			colorMap.translate(fitness, colors);
			return;
		}
		// note: cannot use colors[0] as template because colors may be an array of null
		T vacant = engine.convertColor(Color.WHITE, colorMap.getColorTemplate());
		for( int n=0; n<nPopulation; n++ ) {
			if( isVacantAt(n) ) {
				// how to color vacant sites?
				colors[n] = vacant;
				continue;
			}
			colors[n] = colorMap.translate(getFitnessAt(n));
		}
	}

	/**
	 *
	 * @param avgscores
	 * @return
	 */
	public abstract boolean	getMeanFitness(double[] mean);

	/**
	 *
	 * @param colors
	 * @param colorMap
	 */
	public abstract <T> void getTraitData(T[] colors, ColorMap<T> colorMap);

	/**
	 *
	 * @param avgscores
	 * @return
	 */
	public abstract boolean	getMeanTrait(double[] mean);

	/**
	 *
	 * @param idx
	 * @return
	 */
	public String getScoreNameAt(int idx) {
		if( isVacantAt(idx) ) return "-";
		return Formatter.format(getScoreAt(idx), 4);
	}

	/**
	 *
	 * @param idx
	 * @param pretty
	 * @return
	 */
	public String getFitnessNameAt(int idx, boolean pretty) {
		if( isVacantAt(idx) ) return "-";
		// for strong selection fitness can be huge - use scientific notation if >10^7
		double fiti = getFitnessAt(idx);
		return fiti>1e7?(pretty?(Formatter.formatSci(fiti, 4).replace("E", "⋅10<sup>")+"</sup>"):
								 Formatter.formatSci(fiti, 4)):
								 Formatter.format(fiti, 4);
	}

	/**
	 *
	 * @param idx
	 * @return
	 */
	public String getFitnessNameAt(int idx) {
		return getFitnessNameAt(idx, false);
	}

	/**
	 *
	 * @param phase2d
	 * @return
	 */
	public boolean getPhase2D(double[] phase2d) {
		throw new Error("getPhase2D must be implemented in subclasses");
	}

	/**
	 *
	 * @param phase2d
	 * @return
	 */
	public boolean setPhase2D(double[] phase2d) {
		return false;
	}

	/**
	 *
	 * @param state
	 * @return
	 */
	public String getTooltipPhase2D(double[] state) { 
		return null;
	}

	/**
	 * by default we simply produce a histogram of the scores
	 * DPopulation does this for each strategy - for this reason we have a double[][] as an argument
	 * 
	 * @param bins
	 */
	public void getFitHistogramData(double[][] bins) {
		// clear bins
		Arrays.fill(bins[0], 0.0);
		int nBins = bins[0].length;
		// for neutral selection maxScore==minScore! in that case assume range [score-1, score+1]
		// needs to be synchronized with GUI (e.g. MVFitness, MVFitHistogram, ...)
		double map, min = minScore;
		if( maxScore-minScore<1e-8 ) {
			map = nBins*0.5;
			min--;
		}
		else
			map = nBins/(maxScore-minScore);

		// fill bins
		int max = nBins-1;
		for( int n=0; n<nPopulation; n++ ) {
			if( isVacantAt(n) ) continue;
			int bin = (int)((scores[n]-min)*map);
			bin = Math.max(0, Math.min(max, bin));
			bins[0][bin]++;
		}
		ArrayMath.multiply(bins[0], 1.0/nPopulation);
	}

	/**
	 *
	 * @param colors
	 * @param colorMap
	 */
	public <T> void getTagData(T[] colors, ColorMap<T> colorMap) {
		colorMap.translate(tags, colors);
	}

	/**
	 *
	 * @param idx
	 * @return
	 */
	public String getTagNameAt(int idx) {
		return Formatter.format(tags[idx], 4);
	}

	/**
	 *
	 * @param idx
	 * @return
	 */
	public int getInteractionCountAt(int idx) {
		return interactions[idx];
	}

	/**
	 *
	 * @param mem
	 * @return
	 */
	public double[] getTags(double[] mem) {
		System.arraycopy(tags, 0, mem, 0, nPopulation);
		return mem;
	}

	/**
	 *
	 * @param idx
	 * @return
	 */
	public double getTagAt(int idx) {
		return tags[idx];
	}

	/**
	 *
	 * @param idx
	 * @param tag
	 */
	public void setTagAt(int idx, double tag) {
		tags[idx] = tag;
	}

	/**
	 *
	 * @param size
	 */
	public void setNPopulation(int size) {
		int oldNPopulation = nPopulation;
		nPopulation = Math.max(1, size);
		engine.requiresReset(nPopulation!=oldNPopulation);
	}

	/**
	 * return nPopulation: corresponds to fixed population size for most models and 
	 * maximum population size for ecological models with variable population sizes
	 * 
	 * @return
	 */
	public int getNPopulation() {
		return nPopulation;
	}

	/**
	 * Return current population size. For most models with fixed population sizes this 
	 * simply returns nPopulation. Ecological models with variable population sizes must
	 * override this method to return the actual population size.
	 * 
	 * @return current population size
	 */
	public int getPopulationSize() {
		return nPopulation;
	}

	/**
	 *
	 * @param inter
	 */
	public void setInteractionType(int inter) {
		interactionGroup.setSampling(inter);
	}

	/**
	 *
	 * @return
	 */
	public int getInteractionType() {
		return interactionGroup.samplingType;
	}

	/**
	 *
	 * @param size
	 */
	public void setNGroup(int size) {
		int oldNGroup = nGroup;
		nGroup = Math.max(2, size);
		pairwise = (nGroup==2);
		engine.requiresReset(nGroup!=oldNGroup);
	}

	/**
	 *
	 * @return
	 */
	public int getNGroup() {
		return nGroup;
	}

	/**
	 *
	 * @param ninter
	 */
	public void setNInteractions(int ninter) {
		if( ninter!=nInteractions ) {
			nInteractions = Math.max(1, ninter);
		}
	}

	/**
	 *
	 * @return
	 */
	public int getNInteractions() {
		return nInteractions;
	}

	/**
	 *
	 * @param ref
	 */
	public void setReferenceType(int ref) {
		referenceGroup.setSampling(ref);
	}

	/**
	 *
	 * @return
	 */
	public int getReferenceType() {
		return referenceGroup.samplingType;
	}

	/**
	 *
	 * @param size
	 */
	public synchronized void setRGroupSize(int size) {
		referenceGroup.setSize(Math.max(1, size));
	}

	/**
	 *
	 * @return
	 */
	public int getRGroupSize() {
		return referenceGroup.size;
	}

	/**
	 *
	 */
	protected double speciesUpdateRate = 1.0;

	/**
	 *
	 * @param rate
	 */
	public void setSpeciesUpdateRate(double rate) {
		if( rate<=0.0 ) {
			logger.warning("population update rate must be positive - ignored, using 1.");
			speciesUpdateRate = 1.0;
			return;
		}
		speciesUpdateRate = rate;
	}

	/**
	 *
	 * @return
	 */
	public double getSpeciesUpdateRate() {
		return speciesUpdateRate;
	}

	public static enum PopulationUpdateType implements CLOption.KeyCollection {
		SYNC ("synchronous", "synchronized population updates"),
		WRIGHT_FISHER ("Wright-Fisher", "Wright-Fisher process (synchronous)"),
		ASYNC ("asynchronous", "asynchronized population updates"),
		MORAN_BIRTHDEATH ("Bd", "Moran process (birth-death, asynchronous)"),
		MORAN_DEATHBIRTH ("dB", "Moran process (death-birth, asynchronous)"),
		ECOLOGY ("ecology", "asynchronized updates (non-constant population size)"),
		CUSTOM ("custom", "custom population updates");

		String key;
		String title;

		PopulationUpdateType(String key, String title) {
			this.key = key;
			this.title = title;
		}

		public boolean isSynchronous() {
			return (equals(SYNC) || equals(WRIGHT_FISHER));
		}

		public boolean isMoran() {
			return (equals(MORAN_BIRTHDEATH) || equals(MORAN_DEATHBIRTH));
		}

		static PopulationUpdateType parse(String arg) {
			int best = 0;
			PopulationUpdateType match = null;
			// pick best match (if any)
			for (PopulationUpdateType put : values()) {
				int diff = CLOption.differAt(arg, put.key);
				if (diff>best) {
					best = diff;
					match = put;
				}
			}
			return match;
		}

		@Override
		public String toString() {
			return key+": "+title;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public CLOption.KeyCollection[] getKeys() {
			return values();
		}
	}

	/**
	 *
	 */
	protected PopulationUpdateType	populationUpdateType = PopulationUpdateType.ASYNC;

	/**
	 *
	 * @param type
	 */
	public void setPopulationUpdateType(PopulationUpdateType type) {
		if( type==populationUpdateType )
			return;
		if( cloPopulationUpdate.isValidKey(type) ) {
			populationUpdateType = type;
			isSynchronous = populationUpdateType.isSynchronous();
			return;
		}
		// type not found... ignore
		logger.warning("population update type '"+type+"' is invalid - ignored, using '"+populationUpdateType+"'.");
	}

	/**
	 *
	 * @return
	 */
	public PopulationUpdateType getPopulationUpdateType() {
		return populationUpdateType;
	}

	public static enum PlayerUpdateType implements CLOption.KeyCollection {
		BEST ("best", "best wins (equal - stay)"),
		BEST_RANDOM ("best-random", "best wins (equal - random)"),
		BEST_REPLY ("best-reply", "best-reply"),
		IMITATE ("imitate", "imitate/replicate (linear)"),
		IMITATE_BETTER ("imitate-better", "imitate/replicate (better only)"),
		PROPORTIONAL ("proportional", "proportional to payoff"),
		THERMAL ("thermal", "Fermi/thermal update");

		String key;
		String title;

		PlayerUpdateType(String key, String title) {
			this.key = key;
			this.title = title;
		}

		static PlayerUpdateType parse(String arg) {
			int best = 0;
			PlayerUpdateType match = null;
			// pick best match (if any)
			for (PlayerUpdateType put : values()) {
				int diff = CLOption.differAt(arg, put.key);
				if (diff>best) {
					best = diff;
					match = put;
				}
			}
			return match;
		}

		@Override
		public String toString() {
			return key+": "+title;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public CLOption.KeyCollection[] getKeys() {
			return values();
		}
	}

	/**
	 *
	 */
	protected PlayerUpdateType	playerUpdateType = PlayerUpdateType.IMITATE;

	/**
	 *
	 */
	protected double playerError = -1.0;

	/**
	 *
	 */
	protected double invThermalNoise = 10.0;

	/**
	 *
	 * @param type
	 */
	public void setPlayerUpdateType(PlayerUpdateType type) {
		if( type==playerUpdateType )
			return;
		if( cloPlayerUpdate.isValidKey(type) ) {
			playerUpdateType = type;
			return;
		}
		// type not found... ignore
		logger.warning("player update type '"+type+"' is invalid - ignored, using '"+playerUpdateType+"'.");
	}

	/**
	 *
	 * @return
	 */
	public PlayerUpdateType getPlayerUpdateType() {
		return playerUpdateType;
	}

	/**
	 *
	 * @return
	 */
	public double getPlayerError() {
		return playerError;
	}

	/**
	 *
	 * @param aValue
	 */
	public void setPlayerError(double aValue) {
		playerError = aValue;
	}

	/**
	 *
	 * @param type
	 */
	public void setMigrationType(MigrationType type) {
		if( type==migrationType )
			return;
		if( cloMigration.isValidKey(String.valueOf(type)) ) {
			migrationType = type;
			return;
		}
		// type not found... ignore
		logger.warning("migration type '"+type+"' is invalid - ignored, using '"+migrationType+"'.");
	}

	/**
	 *
	 * @return
	 */
	public MigrationType getMigrationType() {
		return migrationType;
	}

	/**
	 *
	 * @param aValue
	 */
	public void setMigrationProb(double aValue) {
		pMigration = aValue;
	}

	/**
	 *
	 * @return
	 */
	public double getMigrationProb() {
		return pMigration;
	}

	/**
	 * Return opponent of population. By default, for intra-species interactions, this simply returns this population.
	 * 
	 * @return opponent of population
	 */
	public Population getOpponent() {
		return this;
	}

	/**
	 *
	 * @return
	 */
	public Geometry getGeometry() {
		return structure;
	}

	public Geometry createGeometry() {
		if( structure==null ) structure = new Geometry(this, getOpponent());
		return structure;
	}

	/**
	 *
	 * @return
	 */
	public Geometry getInteractionGeometry() {
		return interaction;
	}

	public Geometry createInteractionGeometry() {
		if( interaction==null ) interaction = new Geometry(this, getOpponent());
		return interaction;
	}

	/**
	 *
	 * @return
	 */
	public Geometry getReproductionGeometry() {
		return reproduction;
	}

	public Geometry createReproductionGeometry() {
		if( reproduction==null ) reproduction = new Geometry(this);
		return reproduction;
	}

	/**
	 *
	 * @param temp
	 */
	public void setThermalNoise(double temp) {
		if( Math.abs(temp)<1e-8 )
			invThermalNoise = -1.0;
		else
			invThermalNoise = 1.0/Math.abs(temp);
	}

	/**
	 *
	 * @return
	 */
	public double getThermalNoise() {
		if( invThermalNoise<=0.0 ) return 0.0;
		return 1.0/invThermalNoise;
	}

	/**
	 *
	 * @param fit
	 */
	public void setBaselineFitness(double fit) {
		if( Math.abs(playerBaselineFitness-fit)>1e-8 ) {
			playerBaselineFitness = fit;
		}
	}

	/**
	 *
	 * @return
	 */
	public double getBaselineFitness() {
		return playerBaselineFitness;
	}

	/**
	 *
	 * @param sel
	 */
	public void setSelection(double sel) {
		// selection strength must be non-negative
		sel = Math.max(sel, 0.0);
		if( Math.abs(playerSelection-sel)>1e-8 ) {
			playerSelection = sel;
		}
	}

	/**
	 *
	 * @return
	 */
	public double getSelection() {
		return playerSelection;
	}

	/**
	 *
	 * @param reset
	 */
	public void setPlayerScoreResetAlways(boolean reset) {
		playerScoreResetAlways = reset;
	}

	/**
	 *
	 * @return
	 */
	public boolean getPlayerScoreResetAlways() {
		return playerScoreResetAlways;
	}

	/**
	 *
	 * @param aver
	 */
	public void setPlayerScoreAveraged(boolean aver) {
		playerScoreAveraged = aver;
	}

	/**
	 *
	 * @return
	 */
	public boolean getPlayerScoreAveraged() {
		return playerScoreAveraged;
	}

	/* 
	 *
	 * initialize population and generate structure 
	 *
	 */	
	/**
	 * enable subclasses to introduce new geometries
	 */
	@Override
	public boolean parseGeometry(Geometry geom, String arg) {
		return false;
	}

	/**
	 * enable subclasses to implement sanity checks for new geometries
	 */
	@Override
	public boolean checkGeometry(Geometry geom) {
		return false;
	}

	/**
	 * enable subclasses to introduce new geometries
	 */
	@Override
	public boolean generateGeometry(Geometry geom) {
		return false;
	}

//	private long linkCount(Geometry geom) {
//		long outcount = 0, incount = 0;
//		for( int n=0; n<nPopulation; n++ ) {
//			outcount += geom.kout[n];
//			incount += geom.kin[n];
//		}
//		if( outcount != incount ) {
//			logger.severe("ALARM: some links point to nirvana!? ("+incount+", "+outcount+")");
//		}
//		return outcount;
//	}

	/* 
	 *
	 * mouse stuff 
	 *
	 */
	/**
	 * return false if no actions taken
	 * 
	 * @param hit
	 * @return
	 */
	public boolean mouseHitNode(int hit) {
		return false;
	}

	/**
	 *
	 * @param hit
	 * @param ref
	 * @return
	 */
	public boolean mouseHitNode(int hit, int ref) {
		return false;
	}

	/* 
	 *
	 * parse command line options 
	 *
	 */

	/*
	 * command line parsing stuff
	 */
	// NOTE: there is something weird about these options... many, like --geometry, accept vectors for inter-species interactions
	//		 but every population has its own cloGeometry, which is unnecessary and causes confusion with defaults...
	protected final CLOption cloNPopulation = new CLOption("popsize", 'N', CLOption.Argument.REQUIRED, "100", 
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parsePopulationSize(arg);
		}
		@Override
		public void report(PrintStream output) {
			for( Population pop : species )
				output.println("# populationsize:       "+pop.getNPopulation()+(isMultispecies?" ("+pop.getName()+")":""));
		}
		@Override
		public String getDescription() {
			String descr = "";
			int nSpecies = species.size();
			switch( nSpecies ) {
				case 1:
					return "--popsize, -N <n>|<nxn>  population size n (or nxn, nXn)";
				case 2:
					descr = "--popsize, -N <n0,n1>  size ni of population i, with\n";
					break;
				case 3:
					descr = "--popsize, -N <n0,n1,n2>  size ni of population i, with\n";
					break;
				default:
					descr = "--popsize, -N <n0,...,n"+nSpecies+">  size ni of population i, with\n";
			}
			for( int i=0; i<nSpecies; i++ )
				descr += "            n"+i+": "+species.get(i).getName()+"\n";
			descr += "(or nixni, niXni e.g. for lattices)";
			return descr;
		}    	
	});
	protected final CLOption cloNGroup = new CLOption("groupsize", 'n', CLOption.Argument.REQUIRED, "2",
			"--groupsize, -n <n>  size of interaction groups",
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			setNGroup(CLOParser.parseInteger(arg));
			return true;
		}
		@Override
		public void report(PrintStream output) {
			for( Population pop : species )
				output.println("# groupsize:            "+pop.getNGroup()+(isMultispecies?" ("+pop.getName()+")":""));
		}
	});

	/**
	 * Command line option to set the method for updating the population(s).
	 */
	public final CLOption cloPopulationUpdate = new CLOption("popupdate", 'U', CLOption.Argument.REQUIRED, PopulationUpdateType.ASYNC.getKey(),
			"--popupdate, -U<u>   population update\n" + "      u: update type", new CLODelegate() {
				@Override
				public boolean parse(String arg) {
// Multispecies: consider different population updates for each species?
					return parsePopulationUpdate(arg);
				}

				@Override
				public void report(PrintStream output) {
//XXX Species does not know about optimizations... how to delegate this report to DPopulation?
//					output.println("# populationupdate:     "+validPopulationUpdates.getEntry(getPopulationUpdateType()).getName()+
//							 (((isMoranType() && optimizeMoran) || optimizeHomo)?" (optimized)":""));
					for (Population pop : species) {
						output.println("# populationupdate:     "
								+ pop.getPopulationUpdateType() + (isMultispecies?" ("
								+ pop.getName() + ")":""));
					}
				}
			});
	public final CLOption cloPlayerUpdate = new CLOption("playerupdate", 'u', CLOption.Argument.REQUIRED, PlayerUpdateType.IMITATE.getKey(),
			"--playerupdate, -u<u>  player update\n"+
			"      u: update type (ignored by Moran updates)",
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parsePlayerUpdates(arg);
		}
		@Override
		public void report(PrintStream output) {
			for( Population pop : species ) {
				if (pop.isMoranType()) {
					output.println("# playerupdate:         ignored");
					continue;
				}
				output.println("# playerupdate:         "+pop.getPlayerUpdateType()+(isMultispecies?" ("+pop.getName()+")":""));
				switch( pop.getPlayerUpdateType() ) {
					case THERMAL:
					case IMITATE:
						output.println("# playerupdatenoise:    "+Formatter.formatSci(pop.getThermalNoise(), 6));
						break;
					default:
//XXX check if no other updates implement player errors
						break;
				}
			}
		}
	});
	protected final CLOption cloPlayerError = new CLOption("playererror", CLOption.Argument.REQUIRED, "0",
			"--playererror <e>    error rate for adopting strategies",
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			String[] playererror = arg.split(CLOParser.SPECIES_DELIMITER);
			int n = 0;
			for( Population pop : getSpecies() ) {
				pop.setPlayerError(CLOParser.parseDouble(playererror[n]));
				n = (n+1)%playererror.length;
			}
			return true;
		}
		@Override
		public void report(PrintStream output) {
			for( Population pop : species ) {
				switch( pop.getPlayerUpdateType() ) {
					case THERMAL:
					case IMITATE:
					case IMITATE_BETTER:
						if( cloPlayerError.isSet() )
							output.println("# playerupdateerror:    "+Formatter.formatSci(pop.getPlayerError(), 6));
						break;
					default:
//XXX check if no other updates implement player errors
						break;
				}
			}
		}
	});
	protected final CLOption cloAccumulatedScores = new CLOption("accuscores", CLOption.Argument.NONE, "noaccu",
			"--accuscores,        accumulate scores (instead of averaging)",
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			// default is to average scores
			for (Population pop : species)
				pop.setPlayerScoreAveraged(!cloAccumulatedScores.isSet());
			return true;
		}
		@Override
		public void report(PrintStream output) {
			for (Population pop : species)
				output.println("# scoring:              "+(pop.getPlayerScoreAveraged()?"averaged":"accumulated")+
					(pop.getPlayerScoreResetAlways()?" (reset on change only)":"")+(isMultispecies?" ("
							+ pop.getName() + ")":""));
		}
	});
	protected final CLOption cloResetScoresOnChange = new CLOption("resetonchange", CLOption.Argument.NONE, "always",
			"--resetonchange      reset scores only on actual strategy change\n"+
					"            (instead of on every strategy update)",
					new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			// default is to reset score always (not only on actual strategy change)
			for (Population pop : species)
				pop.setPlayerScoreResetAlways(!cloResetScoresOnChange.isSet());
			return true;
		}
		// cloAccumulatedScores takes care of report (see above)
	});
	protected final CLOption cloInteractionType = new CLOption("intertype", 'O', CLOption.Argument.REQUIRED, "a",
			"--intertype, -O<o> interaction/opponents\n"+
					"      o: interaction neighbourhood\n"+
					"         a: all neighbors\n"+
					"         r: random sample of neighbors",
					new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseInteractionType(arg);
		}
		@Override
		public void report(PrintStream output) {
			String[] interactionname = new String[] {"all", "random"};
			for (Population pop : species)
				output.println("# interactions:         "+interactionname[pop.getInteractionType()]+(isMultispecies?" ("
						+ pop.getName() + ")":""));
		}
	});
	protected final CLOption cloInteractionCount = new CLOption("numinter", CLOption.Argument.REQUIRED, "1",
			"--numinter <n>       number of interactions is n",
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			String[] interactioncount = arg.split(CLOParser.SPECIES_DELIMITER);
			int n = 0;
			for( Population pop : getSpecies() ) {
				pop.setNInteractions(Integer.parseInt(interactioncount[n]));
				n = (n+1)%interactioncount.length;
			}
			return true;
		}
		@Override
		public void report(PrintStream output) {
			for( Population pop : getSpecies() ) 
				output.println("# interactioncount:     "+pop.getNInteractions()+(isMultispecies?" ("
						+ pop.getName() + ")":""));
		}
	});
	protected final CLOption cloReferenceType = new CLOption("references", 'M', CLOption.Argument.REQUIRED, "a",
			"--references, -M<m>[<s>] references (ignored by Moran updates)\n"+
					"      m: reference neighbourhood\n"+
					"         a: all neighbors\n"+
					"         r<s>: random neighbors, sample size s",
					new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseReferenceType(arg);
		}
		@Override
		public void report(PrintStream output) {
			String[] referencename = new String[] {"default", "count"};
			for( Population pop : getSpecies() ) {
				boolean isMoran = pop.isMoranType();
				output.println("# references:           "+(isMoran?"ignored":referencename[pop.getReferenceType()])+(isMultispecies?" ("
						+ pop.getName() + ")":"")
						+ "\n# referencesize:        "+(isMoran?"ignored":(""+pop.getRGroupSize())));
			}
		}
	});
	protected final CLOption cloBaselineFitness = new CLOption("basefit", 'W', CLOption.Argument.REQUIRED, "1",
			"--basefit, -W <b>    baseline fitness",
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseBaselineFitness(arg);
		}
		@Override
		public void report(PrintStream output) {
			for (Population pop : species)
				output.println("# baselinefitness:      "+Formatter.format(pop.getBaselineFitness(), 4) + (isMultispecies?" ("
						+ pop.getName() + ")":""));
		}
	});
	protected final CLOption cloSelection = new CLOption("selection", 'w', CLOption.Argument.REQUIRED, "1",
			"--selection, -w <s>  selection pressure",
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseSelection(arg);
		}
		@Override
		public void report(PrintStream output) {
			for (Population pop : species)
				output.println("# selection:            "+Formatter.format(pop.getSelection(), 4) + (isMultispecies?" ("
						+ pop.getName() + ")":""));
		}
	});
	protected final CLOption cloFitnessMap = new CLOption("fitnessmap", CLOption.Argument.REQUIRED, "none",
			"--fitnessmap <m>     mapping of scores to fitness\n"+
			"      m: type of map (b baseline fitness, w selection strength)",
					new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseMap2Fitness(arg);
		}
		@Override
		public void report(PrintStream output) {
			for (Population pop : species)
				output.println("# fitnessmap:           "+pop.getMapToFitness().getTitle() + (isMultispecies?" ("
						+ pop.getName() + ")":""));
		}
	});
	public final CLOption cloMigration = new CLOption("migration", CLOption.Argument.REQUIRED, "none",
			"--migration <tm>     migration\n" +
			"      t: migration type\n" +
			"      m: migration probability",
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseMigration(arg);
		}
		@Override
		public void report(PrintStream output) {
			for (Population pop : species) {
				MigrationType mig = pop.getMigrationType();
				output.println("# migration:            "+ mig + (isMultispecies?" ("
						+ pop.getName() + ")":""));
				if( mig!=MigrationType.NONE )
					output.println("# migrationrate:        "+Formatter.formatSci(getMigrationProb(), 8));
			}
		}
	});
	protected final CLOption cloTraitColors = new CLOption("colors", CLOption.Argument.REQUIRED, "default", null,
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			// default colors are set in load()
			if( !cloTraitColors.isSet() )
				return true;
			return parseTraitColors(arg);
		}
		@Override
		public String getDescription() {
			String descr;
			switch( nTraits ) {
				case 1:
					return "--colors <c;n>  trait colors (c: regular, n: new)\n"+
					"      c, n: color name or (r,g,b) triplet (in 0-255)";
				case 2:
					descr = "--colors <c1;c2;n1;n2>  trait colors (c: regular, n: new)";
					break;
				default:
					descr = "--colors <c1;...;c"+nTraits+";n1;...;n"+nTraits+">  trait colors (c: regular, n: new)";
			}
			descr += "\n      ci, ni: color name or (r,g,b) triplet (in 0-255), with i:";
			for (Population pop : species) {
				for( int n=0; n<pop.nTraits; n++ )
					descr += "\n      "+n+": "+(isMultispecies?pop.getName()+".":"")+pop.getTraitName(n);
			}
			return descr;
		}
	});
	protected final CLOption cloTraitNames = new CLOption("traitnames", CLOption.Argument.REQUIRED, "default",
					new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			// default trait names are set in load()
			if( !cloTraitNames.isSet() ) return true;
			return parseTraitNames(arg);
		}
		// note: trait names are reported in DPopulation or CX/CPopulation
		@Override
		public String getDescription() {
			switch( getNSpecies() ) {
				case 1:
					switch( nTraits ) {
						case 1:
							return "--traitnames <n>  override trait name";
						case 2:
							return "--traitnames <n1,n2>  override trait names";
						default:
							return "--traitnames <n1,...,n"+nTraits+">  override trait names";
					}
				case 2:
				default:
//NOTE: this gets tricky as every species may have a different number of traits...
					return "--traitnames <n1[,n2[...][;m1[,m2[,...]]]> override trait names\n"+
					"            <ni> in first species, <mi> in second etc.";
			}
		}
	});
	/**
	 * Command line option to set the color for trajectories. For example, this
	 * affects the display in {@link org.evoludo.gwt.simulator.MVS3} (and {@link org.evoludo.gwt.graphics.S3Graph}) or
	 * {@link org.evoludo.gwt.simulator.MVPhase2D} (and {@link org.evoludo.gwt.graphics.ParaGraph}).
	 */
	// note: cannot import org.evoludo.gwt-classes (otherwise compilation of java port fails).
	protected final CLOption cloTrajectoryColor = new CLOption("trajcolor", CLOption.Argument.REQUIRED, "black",
			"--trajcolor <c>	  color for trajectories\n"
		  + "            <c>: color name or '(r,g,b[,a])' with r,g,b,a in 0-255)",
			new CLODelegate() {
				@Override
				public boolean parse(String arg) {
					Color color = CLOParser.parseColor(arg);
					if (color == null)
						return false;
					trajColor = color;
					return true;
				}
			});
	protected final CLOption cloMonoStop = new CLOption("monostop", CLOption.Argument.NONE, "nostop",
			"--monostop           stop once population become monomorphic",
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			// default is to reset score always (not only on actual strategy change)
			setMonoStop(cloMonoStop.isSet());
			return true;
		}
	});

	@Override
	public void collectCLO(CLOParser parser) {
		super.collectCLO(parser);

		// (re)set defaults prior to parsing CLOs
		verbose = false;

		// prepare command line options
		parser.addCLO(cloNPopulation);
		if( !pairwiseOnly )
			parser.addCLO(cloNGroup);
		parser.addCLO(cloPopulationUpdate);
		parser.addCLO(cloPlayerUpdate);
		parser.addCLO(cloPlayerError);
		parser.addCLO(cloAccumulatedScores);
		parser.addCLO(cloResetScoresOnChange);
		parser.addCLO(cloInteractionType);
		parser.addCLO(cloInteractionCount);
		parser.addCLO(cloReferenceType);
		parser.addCLO(cloBaselineFitness);
		parser.addCLO(cloSelection);
		parser.addCLO(cloFitnessMap);
		parser.addCLO(cloMigration);
		parser.addCLO(cloMonoStop);
		parser.addCLO(cloTrajectoryColor);
		if( nTraits>0 ) {
			parser.addCLO(cloTraitColors);
			parser.addCLO(cloTraitNames);
		}
	}

	/**
	 *
	 * @param arg
	 * @return <code>true</code> if parsing successful
	 */
	public boolean parseTraitNames(String arg) {
		String[] namespecies = arg.split(CLOParser.SPECIES_DELIMITER);
		int n = 0;
		for( Population pop : getSpecies() ) {
			String[] names = namespecies[n].split(CLOParser.VECTOR_DELIMITER);
			if( pop.getNTraits()!=names.length ) {
				logger.warning("incorrect number of trait names specified ("+names.length+" instead of "+pop.getNTraits()+") - ignored.");
				continue;
			}
			pop.setTraitNames(names);
			n = (n+1)%namespecies.length;
		}
		return true;
	}

	/**
	 *
	 * @param arg
	 * @return <code>true</code> if parsing successful
	 */
	public boolean parseTraitColors(String arg) {
		String[] colorsets = arg.split(CLOParser.SPECIES_DELIMITER);
		if( colorsets==null ) {
			logger.warning("color specification '"+arg+"' not recognized - default colors used!");
			for( Population pop : getSpecies() )
				pop.setTraitColors(null);
			return false;
		}
		boolean success = true;
		int n = 0;
		for( Population pop : getSpecies() ) {
			String[] colors = colorsets[n].split(CLOParser.MATRIX_DELIMITER);
			n = (n+1)%colorsets.length;
			Color[] myColors = new Color[colors.length];
			for( int i=0; i<colors.length; i++ ) {
				Color newColor = CLOParser.parseColor(colors[i]);
				// if color was not recognized, choose random color
				if( newColor==null ) {
					logger.warning("color specification '"+colors[i]+"' not recognized - replaced by random color!");
					newColor = new Color(random0n(256), random0n(256), random0n(256));
					success = false;
				}
				myColors[i] = newColor;
			}
			// setTraitColor deals with missing colors and adding shades
			pop.setTraitColors(myColors);
		}
		return success;
	}

	/**
	 * parse string cli as an integer and check if population size contains an 'x' or 'X'
	 * if so, use as lattice dim and square the number
	 * 
	 * @param cli
	 * @return <code>true</code> if parsing successful
	 */
	public boolean parsePopulationSize(String cli) {
		String[] sizes = cli.split(CLOParser.SPECIES_DELIMITER);
		int n = 0;
		for( Population pop : getSpecies() ) {
			int size = CLOParser.parseDim(sizes[n]);
			if( size < 1 ) continue;
			pop.setNPopulation(size);
			n = (n+1)%sizes.length;
		}
		return true;
	}

	/**
	 *
	 * @param cli
	 * @return <code>true</code> if parsing successful
	 */
	public boolean parseBaselineFitness(String cli) {
		String[] basefitspecies = cli.split(CLOParser.SPECIES_DELIMITER);
		int n = 0;
		for( Population pop : getSpecies() ) {
			pop.setBaselineFitness(CLOParser.parseDouble(basefitspecies[n]));
			n = (n+1)%basefitspecies.length;
		}
		return true;
	}

	/**
	 *
	 * @param cli
	 * @return <code>true</code> if parsing successful
	 */
	public boolean parseSelection(String cli) {
		String[] selectionspecies = cli.split(CLOParser.SPECIES_DELIMITER);
		int n = 0;
		for( Population pop : getSpecies() ) {
			pop.setSelection(CLOParser.parseDouble(selectionspecies[n]));
			n = (n+1)%selectionspecies.length;
		}
		return true;
	}

	/**
	 *
	 * @param cli
	 * @return <code>true</code> if parsing successful
	 */
	public boolean parseMap2Fitness(String cli) {
		boolean success = true;
		String[] map2fitnessspecies = cli.split(CLOParser.SPECIES_DELIMITER);
		int n = 0;
		for( Population pop : getSpecies() ) {
			Map2Fitness map = Map2Fitness.parse(map2fitnessspecies[n]);
			if( map==null ) {
				logger.warning(
						(isMultispecies?getName()+": ":"")+
						"fitness map '"+map2fitnessspecies[n]+"' unknown - using '"+cloFitnessMap.getDefault()+"'");
				map = Map2Fitness.parse(cloFitnessMap.getDefault());
				success = false;
			}
			pop.setMapToFitness(map);
			n = (n+1)%map2fitnessspecies.length;
		}
		return success;
	}

	protected boolean isMoranType() {
		return getPopulationUpdateType().isMoran();
//		int poptyp = getPopulationUpdateType();
//		return (poptyp == POPULATION_UPDATE_MORAN_BIRTHDEATH || 
//				poptyp == POPULATION_UPDATE_MORAN_DEATHBIRTH);
	}

	/**
	 *
	 * @param arg
	 * @param species
	 */
	public boolean parsePopulationUpdate(String arg) {
		String[] popupdates = arg.split(CLOParser.SPECIES_DELIMITER);
		int n = 0;
		boolean success = true;
		for( Population pop : getSpecies() ) {
			PopulationUpdateType put = PopulationUpdateType.parse(popupdates[n]);
			n = (n+1)%popupdates.length;
			if (put==null && success) {
				logger.warning("population update '"+put+"' not recognized - using '"+pop.getPopulationUpdateType()+"'");
				success = false;
				continue;				
			}
			pop.setPopulationUpdateType(put);
		}
		return success;
	}

	public String stripKey(CLOption.KeyCollection keyopt, String arg) {
		return arg.substring(CLOption.differAt(keyopt.getKey(), arg));
	}

	/**
	 *
	 * @param arg
	 * @param population
	 * @return <code>true</code> if parsing successful
	 */
	public boolean parsePlayerUpdates(String arg) {
		boolean success = true;
		String[] playerupdates = arg.split(CLOParser.SPECIES_DELIMITER);
		int n = 0;
		for( Population pop : getSpecies() ) {
			PlayerUpdateType put = PlayerUpdateType.parse(playerupdates[n]);
			n = (n+1)%playerupdates.length;
			if (put==null && success) {
				logger.warning("player update '"+put+"' not recognized - using '"+pop.getPlayerUpdateType()+"'");
				success = false;
				continue;				
			}
			pop.setPlayerUpdateType(put);
			String keyarg = stripKey(put, arg);
			if( keyarg.length()>0 )
				pop.setThermalNoise(CLOParser.parseDouble(keyarg));
		}
		return success;
	}

	/**
	 *
	 * @param arg
	 * @param pop
	 * @return
	 */
	public boolean parseInteractionType(String arg) {
		boolean success = true;
		String[] interactiontypes = arg.split(CLOParser.SPECIES_DELIMITER);
		int n = 0;
		for( Population pop : getSpecies() ) {
			int type = interactiontypes[n].charAt(0);
			n = (n+1)%interactiontypes.length;
			if( type!='a' && type!='r' ) {
				logger.warning("interaction type '"+(char)type+"' not recognized - using '"+(char)pop.getInteractionType()+"'");
				success = false;
				continue;
			}
			pop.setInteractionType(type=='a'?Group.SAMPLING_ALL:Group.SAMPLING_COUNT);
		}
		return success;
	}

	/**
	 *
	 * @param arg
	 * @param pop
	 * @return
	 */
	public boolean parseReferenceType(String arg) {
		boolean success = true;
		String[] referencetypes = arg.split(CLOParser.SPECIES_DELIMITER);
		int n = 0;
		for( Population pop : getSpecies() ) {
			int type = referencetypes[n].charAt(0);
			n = (n+1)%referencetypes.length;
			if( type!='a' && type!='r' ) {
				logger.warning("reference type '"+(char)type+"' not recognized - using '"+(char)pop.getReferenceType()+"'");
				success = false;
				continue;
			}
			if(type=='a') {
				pop.setReferenceType(Group.SAMPLING_ALL);
				pop.setRGroupSize(-1);
				continue;
			}
			pop.setReferenceType(Group.SAMPLING_COUNT);
			if( referencetypes[n].length()<=1 ) {
				pop.setRGroupSize(1);
				continue;
			}
			pop.setRGroupSize(CLOParser.parseInteger(referencetypes[n].substring(1)));
		}
		return success;
	}

	/**
	 *
	 * @param arg
	 * @param pop
	 * @return
	 */
	public boolean parseMigration(String arg) {
		boolean success = true;
		String[] migrationtypes = arg.split(CLOParser.SPECIES_DELIMITER);
		int n = 0;
		for( Population pop : getSpecies() ) {
			MigrationType mt = MigrationType.parse(migrationtypes[n]);
			if( mt==null ) {
				logger.warning(
						(isMultispecies?getName()+": ":"")+
						"migration type '"+migrationtypes[n]+"' unknown - using '"+cloMigration.getDefault()+"'");
				mt = MigrationType.parse(cloMigration.getDefault());
				success = false;
			}
			pop.setMigrationType(mt);
			n = (n+1)%migrationtypes.length;
			if (mt==MigrationType.NONE) {
				pop.setMigrationProb(0.0);
				continue;
			}
			if( migrationtypes[n].length()>1 ) {
				pop.setMigrationProb(CLOParser.parseDouble(migrationtypes[n].substring(1)));
				double mig = pop.getMigrationProb(); 
				if( mig<1e-8) {
					logger.warning("migration rate (essentially) zero ("+Formatter.formatSci(mig, 4)+") - reverting to no migration");
					success = false;
					pop.setMigrationType(MigrationType.NONE);
					pop.setMigrationProb(0.0);
				}
				continue;
			}
			logger.warning("no migration rate specified - reverting to no migration");
			success = false;			
			pop.setMigrationType(MigrationType.NONE);
			pop.setMigrationProb(0.0);
		}
		return success;
	}

	@Override
	public void encodeFitness(StringBuilder plist) {
		if (!hasLookupTable)
			plist.append(EvoLudo.encodeKey("Fitness", scores));
	}

	@Override
	public boolean restoreFitness(Plist plist) {
		if (!hasLookupTable) {
			@SuppressWarnings("unchecked")
			List<Double> fit = (List<Double>)plist.get("Fitness");
			if( fit==null || fit.size()!=nPopulation )
				return false;
			sumFitness = 0.0;
			for( int n=0; n<nPopulation; n++ ) {
				double nscore = fit.get(n);
				scores[n] = nscore;
				double nfit = mapToFitness(nscore);
				fitness[n] = nfit;
				sumFitness += nfit;
			}
			setMaxEffScoreIdx();
		}
		return true;
	}

	public void encodeInteractions(StringBuilder plist) {
		plist.append(EvoLudo.encodeKey("Interactions", interactions));
	}

	@Override
	public boolean restoreInteractions(Plist plist) {
		@SuppressWarnings("unchecked")
		List<Integer> inter = (List<Integer>)plist.get("Interactions");
		if( inter==null || inter.size()!=nPopulation )
			return false;
		for( int n=0; n<nPopulation; n++ )
			interactions[n] = inter.get(n);
		return true;
	}

	@Override
	public void encodeStrategies(StringBuilder plist) {
		throw new Error("Must be implemented in subclasses");
	}

	@Override
	public boolean restoreStrategies(Plist plist) {
		throw new Error("Must be implemented in subclasses");
	}

	@Override
	public void encodeGeometry(StringBuilder plist) {
		plist.append(
				"<key>"+interaction.name+"</key>\n"+
				"<dict>\n");
		plist.append(interaction.encodeGeometry());
		plist.append("</dict>\n");
		if( interaction.interReproSame )
			return;
		plist.append(
				"<key>"+reproduction.name+"</key>\n"+
				"<dict>\n");
		plist.append(reproduction.encodeGeometry());
		plist.append("</dict>\n");
	}

	@Override
	public boolean restoreGeometry(Plist plist) {
		Plist igeo = (Plist)plist.get(interaction.name);
		if( igeo==null )
			return false;
		interaction.decodeGeometry(igeo);
		if( interaction.interReproSame ) {
			reproduction = interaction.deriveReproductionGeometry();
			return true;
		}
		Plist rgeo = (Plist)plist.get(reproduction.name);
		if( rgeo==null )
			return false;
		reproduction.decodeGeometry(rgeo);
		return true;
	}
}
