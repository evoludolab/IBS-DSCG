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

import java.util.ArrayList;
import java.util.logging.Level;

import org.evoludo.util.CLOProvider;
import org.evoludo.util.CLOption;
import org.evoludo.util.Formatter;
import org.evoludo.util.Plist;

/**
 *
 * @author Christoph Hauert
 */
public interface Model extends CLOProvider {

	public interface ModelDelegate {
		/**
		 * Opportunity to check model specific parameters.
		 * 
		 * @param model the current model
		 * @return <code>true</code> if reset required
		 */
		public default boolean check(Model model) { 
			return false;
		}

		/**
		 * Opportunity for model specific initializations.
		 */
		public default void init(Model model) { }

		/**
		 * Opportunity for model specific initializations upon reset.
		 */
		public default void reset(Model model) { }
	}

	public interface DE extends Model {
		/**
		 * @param dt discretization for time increments in continuous time models.
		 * @return <code>false</code>
		 */
		public boolean setDt(double dt);

		/**
		 *
		 * @return discretization for time increments in continuous time models.
		 */
		public double getDt();

		/**
		 * 
		 * @param accuracy
		 */
		public void setAccuracy(double accuracy);

		/**
		 * 
		 * @return
		 */
		public double getAccuracy();

		public boolean isConnected();

	}

	public interface ODE extends DE {

		@Override
		public default Type getModelType() {
			return Type.ODE;
		}

		/**
		 * caller MUSTN'T change state!
		 *
		 * @return
		 */
		public double[] getState();

		/**
		 * caller MUSTN'T change value!
		 *
		 * @return
		 */
		public double[] getValue();

		/**
		 * {@inheritDoc}
		 * <p>
		 * ODE and SDE models return <code>true</code> by default.
		 * 
		 * @return <code>true</code> if time reversal permissible.
		 */
		@Override
		public default boolean permitsTimeReversal() {
			return true;
		}
	}

	public interface SDE extends ODE {

		@Override
		public default Type getModelType() {
			return Type.SDE;
		}

		/**
		 * @param noise
		 */
		public void setNoise(double noise);

		/**
		 * @return
		 */
		public double getNoise();
	}

	public interface PDE extends DE {

		@Override
		public default Type getModelType() {
			return Type.PDE;
		}

		@Override
		public default boolean isAsynchronous() {
			return true;
		}

		public default boolean isSymmetric() {
			return false;
		}

		default boolean setSymmetric(boolean symmetric) {
			return false;
		}

		boolean setInitType(int type);

		int getInitType();

		boolean[] getAutoScale();

		public double[] getMinScale();

		public double[] getMaxScale();

		Geometry getGeometry();

		int getDiscretization();

		double[][] getStates();

		double[] getStateAt(int idx);

		double[] getMeanState();

		double[] getMinState();

		double[] getMaxState();

		double[][] getValues();

		double[] getValueAt(int idx);

		double[] getMeanValue();

		double[] getMinValue();

		double[] getMaxValue();

		void encodeGeometry(StringBuilder plist);

		boolean restoreGeometry(Plist plist);
	}

	public interface IBS extends Model {

		@Override
		public default Type getModelType() {
			return Type.IBS;
		}

		void encodeGeometry(StringBuilder plist);

		boolean restoreGeometry(Plist plist);

		boolean restoreInteractions(Plist plist);
	}
	
	public ModelDelegate getDelegate();

	/**
	 *
	 */
	public static enum Type implements CLOption.KeyCollection {
		IBS			("IBS", "individual based simulations"),
		ODE			("ODE", "ordinary differential equations"),
		SDE			("SDE", "stochastic differential equations"),
		PDE			("PDE", "partial differential equations");

		String key;
		String title;

		Type(String key, String title) {
			this.key = key;
			this.title = title;
		}

		public static Type parse(String arg) {
			int best = 0;
			Type match = null;
			for (Type t : values()) {
				int diff = CLOption.differAt(arg, t.key);
				if (diff>best) {
					best = diff;
					match = t;
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
	 * @return type of model
	 */
	public Type getModelType();

	/**
	 * @return <code>true</code> if model is of type <code>type</code>
	 */
	public default boolean isModelType(Type type) {
		return type==getModelType();
	}

	public static enum Mode {
		DYNAMICS	("dynamics"),
		STATISTICS	("statistics");

		String id;

		Mode(String id) {
			this.id = id;
		}
	}

	public static enum Data {
		UNDEFINED	("undefined"),
		STRATEGY	("Strategy"),
		FITNESS		("Fitness"),
		DEGREE		("Degree"),
		STATISTICS_FIXATION_PROBABILITY	("Fixation probability"),
		STATISTICS_FIXATION_TIME		("Fixation time");

		String id;

		Data(String id) {
			this.id = id;
		}

		public static boolean isStatistics(Data type) {
			return (type==STATISTICS_FIXATION_PROBABILITY || type==STATISTICS_FIXATION_TIME);
		}

		public static Mode getModeFor(Data type) {
			if( isStatistics(type) ) return Mode.STATISTICS;
			return Mode.DYNAMICS;
		}
	}

	/**
	 * @return <code>true</code> if model calculations are asynchronous
	 */
	public default boolean isAsynchronous() {
		return false;
	}

	/**
	 *
	 * @return
	 */
	public boolean alloc();

	/**
	 * @param species
	 */
	public void setSpecies(ArrayList<Population> species);

	/**
	 * NOTE: never deallocate memory associated with getter and setter methods
	 */
	public void dealloc();

	/**
	 * Initialize this model
	 */
	public void init();

	/**
	 * Reset this model
	 */
	public void reset();

	/**
	 * Update this model
	 */
	public void update();

	/**
	 * Check consistency of parameters; adjust if necessary (and possible); report
	 * all issues and modifications through <code>logger</code>.
	 * 
	 * @return <code>true</code> if reset required
	 */
	public boolean check();

	/**
	 *
	 * @return
	 */
	public boolean next();

	/**
	 * Returns status message from model. Typically this is a string summarizing the
	 * current state of the simulation. For example, models with discrete strategy
	 * sets (such as 2x2 games, see {@link org.evoludo.simulator.lab.TwoByTwo})
	 * return the average frequencies of each strategy type in the population(s),
	 * see {@link DPopulation}. Similarly, models with continuous strategies (such
	 * as continuous snowdrift games, see {@link org.evoludo.simulator.lab.CSD})
	 * return the mean, minimum and maximum trait value(s) in the population(s), see
	 * {@link CXPopulation}. The status message is displayed along the bottom of the
	 * GUI.
	 * <p>
	 * <strong>Note:</strong> if the model runs into difficulties, problems should be reported
	 * through the logging mechanism. Messages with severity {@link Level#WARNING}
	 * or higher are displayed in the status of the GUI and override status messages
	 * returned here.
	 * 
	 * @return status of active model
	 */
	public abstract String getStatus();

	/**
	 * @return formatted string summarizing elapsed time
	 */
	public default String getCounter() {
		return "time: " + Formatter.format(getTime(), 2);
	}

	/**
	 * @return elapsed time
	 */
	public double getTime();

	/**
	 *
	 * @return
	 */
	public boolean hasConverged();

	/**
	 * Check if time reversal is permitted. by default returns <code>false</code>.
	 * 
	 * @return <code>true</code> if time reversal permissible.
	 */
	public default boolean permitsTimeReversal() {
		return false;
	}

	/**
	 * @return <code>true</code> if time is reversed
	 */
	public default boolean isTimeReversed() {
		return false;
	}

	/**
	 * @param reversed <code>true</code> if time is reversed.
	 *                 <p>
	 *                 <strong>Note:</strong>only possible for ODE and SDE models and even
	 *                 there it may not be feasible due to details of the dynamics
	 *                 (e.g. in {@link org.evoludo.simulator.lab.CG} the ecological
	 *                 dynamics of the patch quality prevents time reversal, i.e.
	 *                 results are numerically unstable due to exponential
	 *                 amplification of deviations in dissipative systems).
	 */
	public default void setTimeReversed(boolean reversed) {
	}

	/**
	 * Check if current model implements mode <code>test</code>; by default only
	 * {@link Mode#DYNAMICS} is permitted.
	 * 
	 * @param test the mode to test
	 * @return <code>true</code> if <code>test</code> is available in current model
	 */
	public default boolean permitsMode(Mode test) {
		return (test == Mode.DYNAMICS);
	}

	/**
	 * @return mode of model
	 */
	public default boolean isMode(Mode test) {
		return (test == getMode());
	}

	/**
	 * Sets the mode of model/simulator. Must check whether current model
	 * permits <code>mode</code>.
	 * 
	 * @param mode change mode of model to <code>mode</code>
	 * @return <code>true</code> if mode changed
	 * 
	 * @see #permitsMode(Mode)
	 */
	public default boolean setMode(Mode mode) {
		return false;
	}

	/**
	 * @return mode of model
	 */
	public default Mode getMode() {
		return Mode.DYNAMICS;
	}

	/**
	 * 
	 */
	public default void readStatisticsSample() {
	}

	/**
	 * 
	 */
	public default void initStatisticsSample() {
	}

	/**
	 *
	 * @param plist
	 */
	public void encodeStrategies(StringBuilder plist);

	/**
	 *
	 * @param map
	 * @return
	 */
	public boolean restoreStrategies(Plist map);

	/**
	 *
	 * @param plist
	 */
	public void encodeFitness(StringBuilder plist);

	/**
	 *
	 * @param map
	 * @return
	 */
	public boolean restoreFitness(Plist map);
}
