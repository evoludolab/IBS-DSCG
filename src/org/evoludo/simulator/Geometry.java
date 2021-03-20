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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.evoludo.util.CLOParser;
import org.evoludo.util.CLOption;
import org.evoludo.util.Formatter;
import org.evoludo.util.ArrayMath;
import org.evoludo.util.Combinatorics;
import org.evoludo.util.Plist;
import org.evoludo.util.RNGDistribution;

/**
 * Instances of <code>Geometry</code> represent the interaction and/or
 * reproduction structure of the population. Currently, the available geometries
 * are:
 * <dl>
 * <dt>MEANFIELD</dt>
 * <dd></dd>
 * <dt>COMPLETE</dt>
 * <dd></dd>
 * <dt>HIERARCHY</dt>
 * <dd></dd>
 * <dt>STAR</dt>
 * <dd></dd>
 * <dt>WHEEL</dt>
 * <dd></dd>
 * <dt>SUPER_STAR</dt>
 * <dd></dd>
 * <dt>STRONG_AMPLIFIER</dt>
 * <dd></dd>
 * <dt>STRONG_SUPPRESSOR</dt>
 * <dd></dd>
 * <dt>LINEAR</dt>
 * <dd></dd>
 * <dt>SQUARE_NEUMANN</dt>
 * <dd></dd>
 * <dt>SQUARE_MOORE</dt>
 * <dd></dd>
 * <dt>SQUARE</dt>
 * <dd></dd>
 * <dt>CUBE</dt>
 * <dd></dd>
 * <dt>HONEYCOMB</dt>
 * <dd></dd>
 * <dt>TRIANGULAR</dt>
 * <dd></dd>
 * <dt>FRUCHT</dt>
 * <dd></dd>
 * <dt>TIETZE</dt>
 * <dd></dd>
 * <dt>FRANKLIN</dt>
 * <dd></dd>
 * <dt>HEAWOOD</dt>
 * <dd></dd>
 * <dt>ICOSAHEDRON</dt>
 * <dd></dd>
 * <dt>DODEKAHEDRON</dt>
 * <dd></dd>
 * <dt>DESARGUES</dt>
 * <dd></dd>
 * <dt>RANDOM_GRAPH</dt>
 * <dd></dd>
 * <dt>RANDOM_GRAPH_DIRECTED</dt>
 * <dd></dd>
 * <dt>RANDOM_REGULAR_GRAPH</dt>
 * <dd></dd>
 * <dt>SCALEFREE</dt>
 * <dd></dd>
 * <dt>SCALEFREE_BA</dt>
 * <dd></dd>
 * <dt>SCALEFREE_KLEMM</dt>
 * <dd></dd>
 * </dl>
 * 
 * @author Christoph Hauert
 */
public class Geometry {

	/**
	 * Interface to enable games to supply their own, customized population
	 * structures. This is mainly useful for evaluating and testing new structures.
	 */
	public interface Delegate {

		/**
		 *
		 * @param geometry
		 * @param arg
		 * @return
		 */
		public boolean parseGeometry(Geometry geometry, String arg);

		/**
		 * 
		 * @param geometry
		 * @return
		 */
		public boolean checkGeometry(Geometry geometry);

		/**
		 *
		 * @param geometry
		 * @return
		 */
		public boolean generateGeometry(Geometry geometry);
	}

	Population population, opponent;
	Logger logger;

	/**
	 * @param population
	 */
	public Geometry(Population population) {
		this(population, population);
	}

	/**
	 * @param population
	 * @param opponent
	 */
	public Geometry(Population population, Population opponent) {
		this.population = population;
		this.opponent = opponent;
		logger = population.getLogger();
	}

	/**
	 * @return
	 */
	public Population getPopulation() {
		return population;
	}

	/**
	 * @return
	 */
	public Population getOpponent() {
		return opponent;
	}

	/**
	 * @return
	 */
	public String getName() {
		// name == null is possible for pseudo-geometries that are used to
		// display e.g. trait distributions in MVDistribution
		if (name == null || name.equals("Structure"))
			return "";
		if (name.endsWith(": Structure"))
			return name.substring(0, name.length()-": Structure".length());
		return name;
	}

	// provide storage to share networks between views
//	private Network2D network2D = null;

//	private Network3D network3D = null;

	/**
	 * @param net
	 */
//	public void setNetwork2D(Network2D net) {
//		network2D = net;
//	}

	/**
	 * @return
	 */
//	public Network2D getNetwork2D() {
//		return network2D;
//	}

	/**
	 * @param net
	 */
//	public void setNetwork3D(Network3D net) {
//		network3D = net;
//	}

	/**
	 * @return
	 */
//	public Network3D getNetwork3D() {
//		return network3D;
//	}

	/**
	 * well-mixed population
	 */
	public static final int MEANFIELD = 'M';

	/**
	 * complete graph
	 */
	public static final int COMPLETE = 'c';

	/**
	 * deme-structured populations; hierarchical structures; meta-populations
	 */
	public static final int HIERARCHY = 'H';

	// regular geometries
	/**
	 *
	 */
	public static final int LINEAR = 'l';

	/**
	 * Square lattice with neighbourhood size <code>n</code> (<code>n</code> must be
	 * one of <code>{1, 4, 8, 9=3&times;3, 25=5&times;5, 49=7&times;7, ...}</code>).
	 */
	public static final int SQUARE = 'N';

	/**
	 * Square lattice with von Neumann neighbourhood (4 nearest neighbours, same as
	 * 'N4').
	 */
	public static final int SQUARE_NEUMANN = 'n';

	/**
	 * Square lattice with Moore neighbourhood (8 nearest neighbours, same as 'N8').
	 */
	public static final int SQUARE_MOORE = 'm';

	/**
	 *
	 */
	public static final int CUBE = 'C';

	/**
	 *
	 */
	public static final int HONEYCOMB = 'h';

	/**
	 *
	 */
	public static final int TRIANGULAR = 't';

	/**
	 * regular (cubic), no symmetries, N=12
	 */
	public static final int FRUCHT = CLOption.NUMBERED_KEY_OFFSET + 0;

	/**
	 * regular (cubic), some symmetries, N=12
	 */
	public static final int TIETZE = CLOption.NUMBERED_KEY_OFFSET + 1;

	/**
	 * regular (cubic), vertex-transitive, N=12
	 */
	public static final int FRANKLIN = CLOption.NUMBERED_KEY_OFFSET + 2;

	/**
	 * regular (cubic), symmetric, N=14
	 */
	public static final int HEAWOOD = CLOption.NUMBERED_KEY_OFFSET + 3;

	/**
	 * regular (quintic), symmetric, N=12
	 */
	public static final int ICOSAHEDRON = CLOption.NUMBERED_KEY_OFFSET + 4;

	/**
	 * regular (cubic), symmetric, N=20
	 */
	public static final int DODEKAHEDRON = CLOption.NUMBERED_KEY_OFFSET + 5;

	/**
	 * regular (cubic), symmetric, N=20
	 */
	public static final int DESARGUES = CLOption.NUMBERED_KEY_OFFSET + 6;

	// special geometries
	/**
	 *
	 */
	public static final int STAR = 's';

	/**
	 *
	 */
	public static final int SUPER_STAR = 'S';

	/**
	 *
	 */
	public static final int STRONG_AMPLIFIER = '+';

	/**
	 *
	 */
	public static final int STRONG_SUPPRESSOR = '-';

	/**
	 *
	 */
	public static final int WHEEL = 'w';

	// random geometries
	/**
	 *
	 */
	public static final int RANDOM_REGULAR_GRAPH = 'r';

	/**
	 *
	 */
	public static final int RANDOM_REGULAR_GRAPH_DIRECTED = 'd';

	/**
	 *
	 */
	public static final int RANDOM_GRAPH = 'R';

	/**
	 *
	 */
	public static final int RANDOM_GRAPH_DIRECTED = 'D';

	/**
	 *
	 */
	public static final int SCALEFREE = 'p';

	/**
	 *
	 */
	public static final int SCALEFREE_KLEMM = 'F';

	/**
	 *
	 */
	public static final int SCALEFREE_BA = 'f';

//	/**
//	 *
//	 */
//	public static final int SCALEFREE_DIRECTED = '-';	// not yet implemented

	// generic geometries
	/**
	 *
	 */
	public static final int VOID = -1;

	/**
	 *
	 */
	public static final int GENERIC = -2;

	/**
	 *
	 */
	public static final int DYNAMIC = '*'; // used to be -3

	/**
	 *
	 */
	public static final int INVALID = -4;

	/**
	 *
	 */
	public int geometry = MEANFIELD;

	/**
	 *
	 */
	public int subgeometry = MEANFIELD;

	/**
	 *
	 */
	String name = null;

	/**
	 *
	 */
	public int[][] in = null;

	/**
	 *
	 */
	public int[][] out = null;

	/**
	 *
	 */
	public int[] kin = null;

	/**
	 *
	 */
	public int[] kout = null;

	/**
	 *
	 */
	public int size = -1;

	/**
	 * @param size
	 * @return
	 */
	public boolean setSize(int size) {
		if (this.size == size)
			return false; // no change
		this.size = size;
		if (population != null)
			// signal population size change of if it had been explicitly set
			return population.cloNPopulation.isSet();
		return true;
	}

	/**
	 *
	 */
	public boolean fixedBoundary = false;

	/*
	 * public Boundary boundary = Boundary.PERIODIC;
	 * 
	 * public enum Boundary { PERIODIC, FIXED, ABSORBING }
	 */

	/**
	 *
	 */
	protected int[] rawhierarchy;
	public int[] hierarchy;
	public double hierarchyweight;

	/**
	 *
	 */
	public int minIn = -1;

	/**
	 *
	 */
	public int maxIn = -1;

	/**
	 *
	 */
	public double avgIn = -1.0;

	/**
	 *
	 */
	public int minOut = -1;

	/**
	 *
	 */
	public int maxOut = -1;

	/**
	 *
	 */
	public double avgOut = -1.0;

	/**
	 *
	 */
	public int minTot = -1;

	/**
	 *
	 */
	public int maxTot = -1;

	/**
	 *
	 */
	public double avgTot = -1.0;

	/**
	 *
	 */
	public int petalscount = -1;

	/**
	 *
	 */
	public int petalsamplification = -1;

	/**
	 *
	 */
	public double sfExponent = -2.0;

	/**
	 *
	 */
	public int linearAsymmetry = 0;

	/**
	 *
	 */
	public double connectivity = -1.0;

	/**
	 *
	 */
	public double pUndirLinks = -1.0;

	/**
	 *
	 */
	public double pDirLinks = -1.0;

	/**
	 *
	 */
	public boolean addUndirLinks = false; // default is to rewire links

	/**
	 *
	 */
	public boolean addDirLinks = false; // default is to rewire links

	/**
	 *
	 */
	public boolean isUndirected = true;

	/**
	 *
	 */
	public boolean isRewired = true;

	/**
	 * @return
	 */
	public boolean isInterspecies() {
		return (population != opponent);
	}

	/**
	 *
	 */
	public boolean interReproSame = true;

	/**
	 * helper method to determine whether population pop requires two distinct
	 * graphical representations for its interaction and reproduction graph. for
	 * random structures the creation routines set interReproSame to false if two
	 * distinct instances of a network are used. however, if lattices with different
	 * neighbourhoods are used, report as unique geometry. tooltips need to be
	 * careful to report the different neighborhoods properly.
	 * 
	 * @param pop
	 * @return
	 */
	public static boolean displayUniqueGeometry(Geometry inter, Geometry repro) {
		int geometry = inter.geometry;
		if (geometry == LINEAR || geometry == SQUARE || geometry == CUBE || geometry == HONEYCOMB
				|| geometry == TRIANGULAR) {
			// lattice interaction geometry - return true if reproduction geometry is the
			// same (regardless of connectivity)
			if (repro == null)
				return inter.interReproSame;
			return (repro.geometry == geometry);
		}
		return inter.interReproSame;
	}

	public static boolean displayUniqueGeometry(Population pop) {
		return displayUniqueGeometry(pop.getInteractionGeometry(), pop.getReproductionGeometry());
	}
	/**
	 *
	 */
	public boolean isDynamic = false;

	/**
	 *
	 */
	public boolean isRegular = false;

	/**
	 *
	 */
	public boolean isLattice = false;

	/**
	 *
	 */
	public boolean isValid = false;

	private static final int[] nulllink = new int[0];

	/**
	 *
	 */
	public void reset() {
//		network2D = null;
//		network3D = null;
		name = null;
		in = null;
		out = null;
		kin = null;
		kout = null;
		size = -1;
		geometry = MEANFIELD;
		fixedBoundary = false;
		minIn = -1;
		maxIn = -1;
		avgIn = -1.0;
		minOut = -1;
		maxOut = -1;
		avgOut = -1.0;
		minTot = -1;
		maxTot = -1;
		avgTot = -1.0;
		petalscount = -1;
		petalsamplification = -1;
		rawhierarchy = null;
		hierarchy = null;
		sfExponent = -2.0;
		connectivity = -1.0;
		pUndirLinks = -1.0;
		pDirLinks = -1.0;
		addUndirLinks = false; // default is to rewire links
		addDirLinks = false; // default is to rewire links
		isUndirected = true;
		isRewired = true;
		interReproSame = true;
		isDynamic = false;
		isRegular = false;
		isLattice = false;
		isValid = false;
	}

	/**
	 *
	 */
	public void alloc() {
		// allocate memory to hold links - avoid null values
		if (in == null || in.length != size) {
			in = new int[size][];
			kin = new int[size];
		}
		if (out == null || out.length != size) {
			out = new int[size][];
			kout = new int[size];
		}
		if (isUndirected && isRegular) {
			int k = (int) (connectivity + 0.5);
			for (int i = 0; i < size; i++) {
				in[i] = new int[k];
				out[i] = new int[k];
				// DEBUG - triggers exceptions to capture some bookkeeping issues
//				Arrays.fill(in[i], -1);
//				Arrays.fill(out[i], -1);
			}
		} else {
			Arrays.fill(in, nulllink);
			Arrays.fill(out, nulllink);
		}
		Arrays.fill(kin, 0);
		Arrays.fill(kout, 0);
	}

	/**
	 * @return
	 */
	public boolean check() {
		boolean doReset = false;
		int side, side2; // helper variables for lattice structures

		switch (geometry) {
			case COMPLETE:
				connectivity = (size - 1);
				petalsamplification = 1;
				break;
			case HIERARCHY:
				int nHierarchy = rawhierarchy.length;
				// NOTE: - structured populations with a single hierarchy can be meaningful
				// (neighbours vs everyone on a lattice).
				// not meaningful in well-mixed populations, though.
				// - hierarchies with only one unit can be collapsed
				for (int i = nHierarchy - 1; i >= 0; i--) {
					if (rawhierarchy[i] <= 1) {
						if (i < nHierarchy - 1)
							System.arraycopy(rawhierarchy, i + 1, rawhierarchy, i, nHierarchy - i - 1);
						nHierarchy--;
					}
				}
				if (nHierarchy == 0) {
					// no hierarchies remain
					// if subgeometry complete, well-mixed or structured with hierarchyweight==0
					// fall back on subgeometry
					if (subgeometry == MEANFIELD || subgeometry == COMPLETE || hierarchyweight <= 0.0) {
						geometry = subgeometry;
						logger.warning("hierarchies must encompass ≥2 levels - collapsed to geometry '"
								+ (char) geometry + "'!");
						return check();
					}
					// maintain single hierarchy
					rawhierarchy[0] = 1;
					nHierarchy = 1;
				}
				if (nHierarchy != rawhierarchy.length) {
					// one or more hierarchies collapsed
					logger.warning("hierarchy levels must include >1 units - hierarchies collapsed to "
							+ (nHierarchy + 1) + " levels!");
				}
				if (hierarchy == null || hierarchy.length != nHierarchy + 1)
					hierarchy = new int[nHierarchy + 1];
				System.arraycopy(rawhierarchy, 0, hierarchy, 0, nHierarchy);
				int prod = 1;
				int nIndiv;
				// NOTE: if subgeometry is 'm' or 'n' it will be set to SQUARE and hence
				// connectivity will not be set again upon subsequent
				// calls to check(); square subgeometries with larger neighbourhoods are not
				// (yet) supported; any digit after 'N' is
				// interpreted as the number of units in the first hierarchy.
				// note; need to reset connectivity otherwise we cannot properly deal with the
				// FALL_THROUGH below (this makes extensions
				// to larger neighbourhood sizes more difficult but we'll cross that bridge
				// if/when we get there)
				connectivity = 0;
				switch (subgeometry) {
					case SQUARE_MOORE:
						connectivity = 8;
						//$FALL-THROUGH$
					case SQUARE_NEUMANN:
						connectivity = Math.max(connectivity, 4); // keep 8 if we fell through
						subgeometry = SQUARE;
						//$FALL-THROUGH$
					case SQUARE:
						if (nHierarchy != 1 || hierarchy[0] != 1) {
							// all levels of hierarchy must be square integers (exception is structured
							// population with hierarchyweight==0)
							for (int i = 0; i < nHierarchy; i++) {
								int sqrt = (int) Math.sqrt(hierarchy[i]);
								// every hierarchy level needs to be at least a 2x2 grid
								hierarchy[i] = Math.max(4, sqrt * sqrt);
								prod *= hierarchy[i];
							}
						}
						nIndiv = size / prod;
						int subside = (int) Math.sqrt(nIndiv);
						nIndiv = Math.max(9, subside * subside); // at least 3x3 grid of individuals per deme
						break;
//XXX other geometries should follow
//					case STAR:
//						break;
//					case TRIANGULAR:
//						// nIndiv must be square even integer
//						subside = (int)Math.sqrt(nIndiv);
//						subside -= subside%2;	// ensure subside is even
//						nIndiv = Math.max(16, subside*subside);	// 4x4 grid (requires even integer square)
//						connectivity = 3;
//						break;
//					case HONEYCOMB:
//						// nIndiv must be square even integer
//						subside = (int)Math.sqrt(nIndiv);
//						subside -= subside%2;	// ensure subside is even
//						nIndiv = Math.max(16, subside*subside);	// 4x4 grid (requires even integer square)
//						connectivity = 6;
//						break;
					default:
						logger.warning("subgeometry '" + (char) subgeometry
								+ "' not supported - well-mixed structure forced!");
						doReset = true;
						//$FALL-THROUGH$
					case COMPLETE:
						// avoid distinctions between MEANFIELD and COMPLETE graphs
						subgeometry = MEANFIELD;
						//$FALL-THROUGH$
					case MEANFIELD:
						for (int i = 0; i < nHierarchy; i++)
							prod *= hierarchy[i];
						nIndiv = Math.max(2, size / prod); // at least two individuals per deme
						connectivity = (nIndiv - 1);
						break;
				}
				hierarchy[nHierarchy] = nIndiv;
				if (setSize(prod * nIndiv)) {
					logger.warning("hierarchical " + name + " geometry with levels " + Formatter.format(hierarchy)
							+ " requires population size of " + size + "!");
					doReset = true;
				}
				break;
			case LINEAR:
				// check connectivity
				connectivity = Math.max(1, Math.rint(connectivity));
				if ((Math.abs(1.0 - connectivity) < 1e-8 && !isInterspecies()) || ((int) connectivity) % 2 == 1
						|| connectivity >= size) {
					connectivity = Math.min(Math.max(2, connectivity + 1), size - 1 - (size - 1) % 2);
					logger.warning("linear " + name + " geometry requires even integer number of neighbors - using "
							+ connectivity + "!");
					doReset = true;
				}
				break;
			case STAR:
				connectivity = 2.0 * (size - 1) / size;
				petalsamplification = 2;
				break;
			case WHEEL:
				connectivity = 4.0 * (size - 1) / size;
				break;
			case SUPER_STAR:
				if (petalsamplification < 3) {
					petalsamplification = 3;
					logger.warning("super-star " + name + " geometry requires amplification of >=3 - using "
							+ petalsamplification + "!");
				}
				// check population size
				int pnodes = petalscount * (petalsamplification - 2);
				int nReservoir = (size - 1 - pnodes) / petalscount;
				if (setSize(nReservoir * petalscount + pnodes + 1)) {
					logger.warning("super-star " + name + " geometry requires special size - using " + size + "!");
					doReset = true;
				}
				connectivity = (double) (2 * nReservoir * petalscount + pnodes) / (double) size;
				break;
			case STRONG_SUPPRESSOR:
				int unit = (int) Math.floor(Math.pow(size, 0.25));
				if (setSize(unit * unit * (1 + unit * (1 + unit)))) {
					logger.warning(
							"strong suppressor " + name + " geometry requires special size - using " + size + "!");
					doReset = true;
				}
				break;
			case STRONG_AMPLIFIER:
				// note unit^(1/3)>=5 must hold to ensure that epsilon<1
				int unit13 = Math.max(5, (int) Math.pow(size / 4, 1.0 / 3.0));
				int unit23 = unit13 * unit13;
				unit = unit23 * unit13;
				double lnunit = 3.0 * Math.log(unit13);
				double epsilon = lnunit / unit13;
				double alpha = 3.0 * lnunit / Math.log(1.0 + epsilon);
				if (setSize((int) (unit + (1 + alpha) * unit23 + 0.5))) {
					logger.warning(
							"strong amplifier " + name + " geometry requires special size - using " + size + "!");
					doReset = true;
				}
				break;
			case SQUARE_MOORE: // moore
				connectivity = 8;
				//$FALL-THROUGH$
			case SQUARE_NEUMANN: // von neumann
				connectivity = Math.max(connectivity, 4); // keep 8 if we fell through
				geometry = SQUARE; // no longer distinguish variants of square lattices
				//$FALL-THROUGH$
			case SQUARE:
				// check population size
				side = (int) Math.floor(Math.sqrt(size) + 0.5);
				side2 = side * side;
				if (setSize(side2)) {
					logger.warning("square " + name + " geometry requires integer square size - using " + size + "!");
					doReset = true;
				}
				// check connectivity - must be 1, 4 or 3x3, 5x5, 7x7 etc.
				int range = Math.min(side / 2, Math.max(1, (int) (Math.sqrt(connectivity + 1.5) / 2.0)));
				int count = (2 * range + 1) * (2 * range + 1) - 1;
				if ((Math.abs(count - connectivity) > 1e-8 && Math.abs(4.0 - connectivity) > 1e-8
						&& Math.abs(1.0 - connectivity) > 1e-8)
						|| (Math.abs(1.0 - connectivity) < 1e-8 && !isInterspecies())) {
					connectivity = count;
					if (connectivity >= size)
						connectivity = 4; // simply reset to von Neumann
					logger.warning(
							"square " + name + " geometry has invalid connectivity - using " + connectivity + "!");
					doReset = true;
				}
				break;
			case CUBE:
				if (size != 25000) {
					// check population size
					side = Math.max((int) Math.floor(Math.pow(size, 1.0 / 3.0) + 0.5), 2); // minimum side length is 2
					int side3 = side * side * side;
					if (setSize(side3)) {
						logger.warning("cubic " + name + " geometry requires integer cube size - using " + size + "!");
						doReset = true;
					}
					// check connectivity - must be 6 or 3x3x3, 5x5x5, 7x7x6 etc.
					range = Math.min(side / 2, Math.max(1, (int) (Math.pow(connectivity + 1.5, 1.0 / 3.0) / 2.0)));
				} else
					range = Math.min(4, Math.max(1, (int) (Math.pow(connectivity + 1.5, 1.0 / 3.0) / 2.0)));
				count = (2 * range + 1) * (2 * range + 1) * (2 * range + 1) - 1;
				if ((Math.abs(count - connectivity) > 1e-8 && Math.abs(6.0 - connectivity) > 1e-8
						&& Math.abs(1.0 - connectivity) > 1e-8)
						|| (Math.abs(1.0 - connectivity) < 1e-8 && !isInterspecies())) {
					connectivity = count;
					if (connectivity >= size)
						connectivity = 6; // simply reset to minimum
					logger.warning(
							"cubic " + name + " geometry has invalid connectivity - using " + connectivity + "!");
					doReset = true;
				}
				break;
			case HONEYCOMB:
				// check population size and set connectivity
				side = (int) Math.floor(Math.sqrt(size) + 0.5);
				side2 = side * side;
				if (size != side2 || (side % 2) == 1) {
					side += side % 2;
					side2 = side * side;
					setSize(side2);
					logger.warning(
							"hexagonal " + name + " geometry requires even integer square size - using " + size + "!");
					doReset = true;
				}
				if ((Math.abs(connectivity - 6) > 1e-8 && Math.abs(1.0 - connectivity) > 1e-8)
						|| (Math.abs(1.0 - connectivity) < 1e-8 && !isInterspecies())) {
					connectivity = 6;
					logger.warning("hexagonal " + name + " geometry requires connectivity 6!");
				}
				break;
			case TRIANGULAR:
				// check population size and set connectivity
				side = (int) Math.floor(Math.sqrt(size) + 0.5);
				side2 = side * side;
				if (size != side2 || (side % 2) == 1) {
					side += side % 2;
					side2 = side * side;
					setSize(side2);
					logger.warning(
							"triangular " + name + " geometry requires even integer square size - using " + size + "!");
					doReset = true;
				}
				if ((Math.abs(connectivity - 3) > 1e-8 && Math.abs(1.0 - connectivity) > 1e-8)
						|| (Math.abs(1.0 - connectivity) < 1e-8 && !isInterspecies())) {
					connectivity = 3;
					logger.warning("triangular " + name + " geometry requires connectivity 3!");
				}
				break;
			case RANDOM_REGULAR_GRAPH:
				// check that number of links is even (since bidirectional); round connectivity
				// to integer
				connectivity = Math.min(Math.floor(connectivity), size - 1);
				int nConn = (int) connectivity;
				if ((size * nConn) % 2 == 1) {
					setSize(size + 1);
					logger.warning("RRG " + name + " geometry requires even (directed) link count - set size to " + size
							+ "!");
					doReset = true;
				}
				break;
			case FRUCHT:
			case TIETZE:
			case FRANKLIN:
				if (setSize(12)) {
					logger.warning((geometry == FRUCHT ? "Frucht" : (geometry == TIETZE ? "Tietze" : "Franklin"))
							+ " graph " + name + " geometry requires size 12!");
					doReset = true;
				}
				connectivity = 3.0;
				break;
			case HEAWOOD:
				if (setSize(14)) {
					logger.warning("Heawood graph " + name + " geometry requires size 14!");
					doReset = true;
				}
				connectivity = 3.0;
				break;
			case ICOSAHEDRON:
				if (setSize(12)) {
					logger.warning("Icosahedron graph " + name + " geometry requires size 12!");
					doReset = true;
				}
				connectivity = 5.0;
				break;
			case DODEKAHEDRON:
				if (setSize(20)) {
					logger.warning("Dodekahedron graph " + name + " geometry requires size 20!");
					doReset = true;
				}
				connectivity = 3.0;
				break;
			case DESARGUES:
				if (setSize(20)) {
					logger.warning("Desargues graph " + name + " geometry requires size 20!");
					doReset = true;
				}
				connectivity = 3.0;
				break;
			case MEANFIELD:
				petalsamplification = 1;
				//$FALL-THROUGH$
			case RANDOM_GRAPH:
			case RANDOM_GRAPH_DIRECTED:
			case SCALEFREE: // bi-directional scale-free network
			case SCALEFREE_BA: // bi-directional scale-free network (barabasi-albert model)
			case SCALEFREE_KLEMM: // bi-directional scale-free network (klemm-eguiluz model)
				break;

			case GENERIC:
			case DYNAMIC:
				break;

			default:
				// last resort: try engine - maybe new implementations provide new geometries
				if (!population.checkGeometry(this))
					throw new Error("Unknown geometry");
		}
		return doReset;
	}

	/**
	 * 
	 */
	public void init() {
		switch (geometry) {
			case MEANFIELD:
				initGeometryMeanField();
				break;
			case COMPLETE:
				initGeometryComplete();
				break;
			case HIERARCHY:
				initGeometryHierarchical();
				break;
			case LINEAR:
				initGeometryLinear();
				break;
			case STAR:
				initGeometryStar();
				break;
			case WHEEL:
				initGeometryWheel();
				break;
			case SUPER_STAR:
				initGeometrySuperstar();
				break;
			case STRONG_AMPLIFIER:
				initGeometryAmplifier();
				break;
			case STRONG_SUPPRESSOR:
				initGeometrySuppressor();
				break;
			case SQUARE:
				initGeometrySquare();
				break;
			case CUBE:
				initGeometryCube();
				break;
			case HONEYCOMB:
				initGeometryHoneycomb();
				break;
			case TRIANGULAR:
				initGeometryTriangular();
				break;
			case FRUCHT:
				initGeometryFruchtGraph();
				break;
			case TIETZE:
				initGeometryTietzeGraph();
				break;
			case FRANKLIN:
				initGeometryFranklinGraph();
				break;
			case HEAWOOD:
				initGeometryHeawoodGraph();
				break;
			case ICOSAHEDRON:
				initGeometryIcosahedronGraph();
				break;
			case DODEKAHEDRON:
				initGeometryDodekahedronGraph();
				break;
			case DESARGUES:
				initGeometryDesarguesGraph();
				break;
			case RANDOM_GRAPH:
				initGeometryRandomGraph();
				break;
			case RANDOM_GRAPH_DIRECTED:
				initGeometryRandomGraphDirected();
				break;
			case RANDOM_REGULAR_GRAPH:
				initGeometryRandomRegularGraph();
				break;
			case SCALEFREE: // bi-directional scale-free network
				initGeometryScaleFree();
				break;
			case SCALEFREE_BA: // bi-directional scale-free network (barabasi-albert model)
				initGeometryScaleFreeBA();
				break;
			case SCALEFREE_KLEMM: // bi-directional scale-free network (klemm-eguiluz model)
				initGeometryScaleFreeKlemm();
				break;
			default:
				// last resort: try engine - maybe new implementations provide new geometries
				if (!population.generateGeometry(this))
					throw new Error("Unknown geometry");
		}
		isValid = true;
		evaluated = false;
	}

	/**
	 * report relevant parameters
	 * 
	 * @param output
	 */
	public void printParams(PrintStream output) {
		switch (geometry) {
			case SUPER_STAR:
				output.println("# connectivity:         " + Formatter.format(connectivity, 4));
				output.println("# petalscount:          " + Formatter.format(petalscount, 0));
				output.println("# amplification:        " + Formatter.format(petalsamplification, 4));
				break;
			case LINEAR:
				if (linearAsymmetry != 0) {
					output.println("# connectivity:         " + Formatter.format(connectivity, 4) + " (left: "
							+ ((connectivity + linearAsymmetry) / 2) + ", right: "
							+ ((connectivity - linearAsymmetry) / 2) + ")");
					break;
				}
				//$FALL-THROUGH$
			case MEANFIELD:
			case COMPLETE:
			case STAR:
			case WHEEL:
			case SQUARE:
			case CUBE:
			case HONEYCOMB:
			case TRIANGULAR:
			case RANDOM_GRAPH:
			case RANDOM_GRAPH_DIRECTED:
			case RANDOM_REGULAR_GRAPH:
			case SCALEFREE:
			case SCALEFREE_BA:
			case SCALEFREE_KLEMM:
			default:
				output.println("# connectivity:         " + Formatter.format(connectivity, 4));
		}
	}

	/**
	 * initialize well-mixed (mean-field, unstructured) population
	 */
	public void initGeometryMeanField() {
		isRewired = false;
		isUndirected = true;
		isRegular = false;
		isLattice = false;
		// allocate minimal memory - just in case
		alloc();
	}

	/**
	 * initialize complete network (every individual is connected to every other
	 * member of the population. this is very similar to well-mixed (unstructured)
	 * populations. the only difference is the potential treatment of the focal
	 * individual. for example, in the Moran process offspring can replace their
	 * parent in the original formulation for well-mixed populations (birth-death
	 * updating) but this does not occur in complete networks where offspring
	 * replaces one _neighbour_.
	 */
	public void initGeometryComplete() {
		int size1 = size - 1;

		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = true;
		connectivity = size1;
		alloc();

		for (int n = 0; n < size; n++) {
			// setting in- and outlinks equal saves some memory
			int[] links = new int[size1];
			in[n] = links;
			kin[n] = size1;
			out[n] = links;
			kout[n] = size1;
			for (int i = 0; i < size1; i++) {
				if (i >= n)
					links[i] = i + 1;
				else
					links[i] = i;
			}
		}
	}

	/**
	 * initialize hierarchical population structure
	 */
	public void initGeometryHierarchical() {
		// initialize interaction network
		isRewired = false;
		isRegular = false;
		isUndirected = true;
		alloc();
		// hierarchical structures call for recursive initialization/generation
		initGeometryHierarchy(0, 0);
	}

	private void initGeometryHierarchy(int level, int start) {
		if (level == hierarchy.length - 1) {
			// bottom level reached
			int nIndiv = hierarchy[level];
			int end = start + nIndiv;
			switch (subgeometry) {
				case SQUARE:
					initGeometryHierarchySquare((int) Math.rint(connectivity), start, end);
					return;
				case MEANFIELD:
				case COMPLETE:
				default:
					initGeometryHierarchyMeanfield(start, end);
					return;
			}
		}
		// recursion
		switch (subgeometry) {
			case SQUARE:
				int side = (int) Math.sqrt(size);
				int hskip = 1;
				for (int dd = level + 1; dd < hierarchy.length; dd++)
					hskip *= (int) Math.sqrt(hierarchy[dd]);
				int hside = (int) Math.sqrt(hierarchy[level]);
				for (int i = 0; i < hside; i++) {
					for (int j = 0; j < hside; j++) {
						initGeometryHierarchy(level + 1, start + (i + j * side) * hskip);
					}
				}
				break;
			case MEANFIELD:
			case COMPLETE:
			default:
				hskip = 1;
				for (int dd = level + 1; dd < hierarchy.length; dd++)
					hskip *= hierarchy[dd];
				int skip = start;
				for (int d = 0; d < hierarchy[level]; d++) {
					initGeometryHierarchy(level + 1, skip);
					skip += hskip;
				}
		}
	}

	private int initGeometryHierarchyMeanfield(int start, int end) {
		int nIndiv = end - start;
		int nIndiv1 = nIndiv - 1;
		for (int n = start; n < end; n++) {
			// setting in- and outlinks equal saves some memory
			int[] links = new int[nIndiv1];
			for (int i = 0; i < nIndiv1; i++) {
				if (start + i >= n)
					links[i] = start + i + 1;
				else
					links[i] = start + i;
			}
			in[n] = links;
			out[n] = links;
			kin[n] = nIndiv1;
			kout[n] = nIndiv1;
		}
		return nIndiv;
	}

	private int initGeometryHierarchySquare(int degree, int start, int end) {
		int nIndiv = end - start;
		int dside = (int) Math.sqrt(nIndiv);
		int side = (int) Math.sqrt(size);
		switch (degree) {
			case 4: // von Neumann
				initGeometrySquareVonNeumann(dside, side, start);
				break;
			case 8:
				initGeometrySquareMoore(dside, side, start);
				break;
			default:
				initGeometrySquare(dside, side, start);
				break;
		}
		return nIndiv;
	}

	/**
	 * initializes a linear population structure (1D lattice).
	 * 
	 * requirements/notes: - even connectivity (same number of neighbours to the
	 * left and right). - adds one more connection pointing to the focal site for
	 * inter-species interactions.
	 */
	public void initGeometryLinear() {
		isRewired = false;
		isRegular = !fixedBoundary; // is regular if boundaries periodic
		isLattice = true;
		alloc();
		boolean isInterspecies = isInterspecies();

		int left = ((int) (connectivity + 0.5) + linearAsymmetry) / 2;
		int right = ((int) (connectivity + 0.5) - linearAsymmetry) / 2;
		isUndirected = (left == right);
		for (int i = 0; i < size; i++) {
			for (int j = -left; j <= right; j++) {
				if ((j == 0 && !isInterspecies) || (fixedBoundary && (i + j >= size || i + j < 0)))
					continue;
				addLinkAt(i, (i + j + size) % size);
			}
		}
	}

	/**
	 * initializes the star population structure. the simplest evolutionary
	 * amplifier for Moran Birth-death updating. the individual/node/vertex with
	 * index 0 is the hub.
	 */
	public void initGeometryStar() {
		isRewired = false;
		isUndirected = true;
		isRegular = false;
		isLattice = false;
		alloc();

		// hub is node 0
		for (int i = 1; i < size; i++) {
			addLinkAt(0, i);
			addLinkAt(i, 0);
		}
	}

	/**
	 * initializes the wheel population structure. this corresponds to a ring
	 * (periodic 1D lattice) with a hub in the middle that is connected to all nodes
	 * of the ring. the individual/node with index 0 is the hub.
	 */
	public void initGeometryWheel() {
		int size1 = size - 1;

		isRewired = false;
		isUndirected = true;
		isRegular = false;
		isLattice = false;
		alloc();

		// hub is node 0
		for (int i = 0; i < size1; i++) {
			addLinkAt(i + 1, (i - 1 + size1) % size1 + 1);
			addLinkAt(i + 1, (i + 1 + size1) % size1 + 1);
			addLinkAt(0, i + 1);
			addLinkAt(i + 1, 0);
		}
	}

	/**
	 * initialize the superstar geometry. the superstar is consists of a hub
	 * surrounded by p petals that are each equipped with a reservoir of size r and
	 * a linear chain of length k with the last node being the hub. the hub connects
	 * to all reservoir nodes (in all petals) and all reservoir nodes in each petal
	 * connect to the first node in the linear chain which links back to the hub.
	 * the superstar represents the best studied evolutionary amplifier of arbitrary
	 * strength (in the limit <code>N -&gt; oo</code> as well as
	 * <code>p, r, k -&gt; oo</code>).
	 * <p>
	 * superstars require particular population sizes satisfying:
	 * <code>N=(r+k-1)*p+1</code>. the individual/node with index <code>0</code> is
	 * the hub.
	 * </p>
	 */
	public void initGeometrySuperstar() {
		isRewired = false;
		isUndirected = false;
		isRegular = false;
		isLattice = false;
		alloc();

		// hub is node 0, outermost petals are nodes 1 - p
		// inner petal nodes are p+1 - 2p, 2p+1 - 3p etc.
		// petal nodes (kernel-2)p+1 - (kernel-2)p+p=(kernel-1)p are connected to hub
		int pnodes = petalscount * (petalsamplification - 2);

		// connect hub
		for (int i = pnodes + 1; i < size; i++) {
			addLinkAt(0, i);
			addLinkAt(i, (i - pnodes - 1) % petalscount + 1);
		}

		// chain petals - outer petal nodes to inner petal nodes
		for (int i = 1; i <= (pnodes - petalscount); i++)
			addLinkAt(i, i + petalscount);

		// connect petals - inner petal nodes to hub
		for (int i = 1; i <= petalscount; i++)
			addLinkAt(pnodes - petalscount + i, 0);
	}

	/**
	 * initialize strong undirected amplifier proposed by George Giakkoupis (2016)
	 * arXiv 1611.01585
	 */
	public void initGeometryAmplifier() {
		isRewired = false;
		isUndirected = true;
		isRegular = false;
		isLattice = false;
		alloc();

		RNGDistribution rng = population.rng;
		int unit13 = Math.max(5, (int) Math.pow(size / 4, 1.0 / 3.0));
		int unit23 = unit13 * unit13;
		int unit = unit23 * unit13;
		int nU = unit, nV = unit23, nW = size - nU - nV;
		// recall: size = unit^3+(1+a)x^2 for suitable a
		// three types of nodes: unit^3 in U, unit^2 in V and rest in W
		// arrangement: W (regular graph core), V, U
		int w0 = 0, wn = nW, v0 = wn, vn = v0 + nV, u0 = vn;// , un = size;
		// step 1: create a (approximate) random regular graph of degree unit^2 as a
		// core
		initRRGCore(rng, w0, wn, unit23);
		// each node in U is a leaf, connected to a single node in V, and each node in V
		// is connected to unit^2 nodes in W
		int idxU = u0;
		for (int v = v0; v < vn; v++) {
			for (int n = 0; n < unit13; n++)
				addEdgeAt(v, idxU++);
			int l = unit23;
			while (l > 0) {
				int idx = rng.random0n(nW);
				if (isNeighborOf(v, idx))
					continue;
				addEdgeAt(v, idx);
				l--;
			}
		}
	}

	/**
	 * utility routine to generate a random (almost) regular graph
	 *
	 * @param rng
	 * @param start
	 * @param end
	 * @param degree
	 * @return
	 */
	private void initRRGCore(RNGDistribution rng, int start, int end, int degree) {
		int nTodo = end - start;
		int nLinks = nTodo * degree;
		int[] todo = new int[nTodo];
		for (int n = 0; n < nTodo; n++)
			todo[n] = n;

		// ensure connectedness for static graphs
		int[] active = new int[nTodo];
		int idxa = rng.random0n(nTodo);
		active[0] = todo[idxa];
		nTodo--;
		if (idxa != nTodo)
			System.arraycopy(todo, idxa + 1, todo, idxa, nTodo - idxa);
		int nActive = 1;
		while (nTodo > 0) {
			idxa = rng.random0n(nActive);
			int nodea = active[idxa];
			int idxb = rng.random0n(nTodo);
			int nodeb = todo[idxb];
			addEdgeAt(nodea, nodeb);
			if (kout[nodea] == degree) {
				nActive--;
				if (idxa != nActive)
					System.arraycopy(active, idxa + 1, active, idxa, nActive - idxa);
			}
			// degree of nodeb not yet reached - add to active list
			if (kout[nodeb] < degree)
				active[nActive++] = nodeb;
			// remove nodeb from core of unconnected nodes
			nTodo--;
			if (idxb != nTodo)
				System.arraycopy(todo, idxb + 1, todo, idxb, nTodo - idxb);
		}
		// now we have a connected graph
		todo = active;
		nTodo = nActive;
		nLinks -= 2 * (end - start - 1);

		// ideally we should go from nTodo=2 to zero but a single node with a different
		// degree is acceptable
		while (nTodo > 1) {
			int a = rng.random0n(nLinks);
			int b = rng.random0n(nLinks - 1);
			if (b >= a)
				b++;

			// identify nodes
			idxa = 0;
			int nodea = todo[idxa];
			a -= degree - kout[nodea];
			while (a >= 0) {
				nodea = todo[++idxa];
				a -= degree - kout[nodea];
			}
			int idxb = 0, nodeb = todo[idxb];
			b -= degree - kout[nodeb];
			while (b >= 0) {
				nodeb = todo[++idxb];
				b -= degree - kout[nodeb];
			}

			if (nodea == nodeb || isNeighborOf(nodea, nodeb))
				continue;
			addEdgeAt(nodea, nodeb);
			nLinks -= 2;
			if (kout[nodea] == degree) {
				nTodo--;
				if (idxa != nTodo)
					System.arraycopy(todo, idxa + 1, todo, idxa, nTodo - idxa);
				if (idxb > idxa)
					idxb--;
			}
			if (kout[nodeb] == degree) {
				nTodo--;
				if (idxb != nTodo)
					System.arraycopy(todo, idxb + 1, todo, idxb, nTodo - idxb);
			}
		}
	}

	/**
	 * initialize strong undirected suppressor proposed by George Giakkoupis (2016)
	 * arXiv 1611.01585
	 */
	public void initGeometrySuppressor() {
		isRewired = false;
		isUndirected = true;
		isRegular = false;
		isLattice = false;
		alloc();

		int unit = (int) Math.floor(Math.pow(size, 0.25));
		// recall: size = unit^2(1+unit(1+unit)) = unit^2+unit^3+unit^4
		// three types of nodes: unit^2 in W, unit^4 in V and unit^3 in U
		// nodes: V, W, U
		int v0 = 0, vn = Combinatorics.pow(unit, 4), w0 = vn, wn = vn + unit * unit, u0 = wn; // , un = size;
		// each node in V is connected to one node in U and to all nodes in W
		for (int v = v0; v < vn; v++) {
			int u = u0 + (v - v0) / unit;
			addEdgeAt(v, u);
			for (int w = w0; w < wn; w++)
				addEdgeAt(v, w);
		}
	}

	/**
	 * initialize square lattice geometry
	 * 
	 * requirements/notes: - integer square population size. - appropriate
	 * connectivity (4 or 3x3-1, 5x5-1, 7x7-1 etc.) - for inter-species interactions
	 * includes interactions with site itself (i.e. connectivity of 1 is acceptable
	 * as well).
	 */
	public void initGeometrySquare() {
		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = true;
		alloc();

		int side = (int) Math.floor(Math.sqrt(size) + 0.5);
		switch ((int) Math.rint(connectivity)) {
			case 1: // self - makes sense only for inter-species interactions
				initGeometrySquareSelf(side, side, 0);
				break;

			case 4: // von Neumann
				initGeometrySquareVonNeumann(side, side, 0);
				break;

			// moore neighborhood is treated separately because the interaction pattern
			// Group.SAMPLING_ALL with
			// a group size between 2 and 8 (excluding boundaries) relies on a particular
			// arrangement of the
			// neighbors. Population.java must make sure that this interaction pattern is
			// not selected for
			// larger neighborhood sizes.
			case 8: // Moore 3x3
				initGeometrySquareMoore(side, side, 0);
				break;

			default: // XxX neighborhood - validity of range was checked in Population.java
				initGeometrySquare(side, side, 0);
		}
	}

	/*
	 * Note: check() ensures that a neighborhood size of 1 is only acceptable for
	 * inter-species interactions.
	 */
	private void initGeometrySquareSelf(int side, int fullside, int offset) {
		int aPlayer;
		for (int i = 0; i < side; i++) {
			int x = offset + i * fullside;
			for (int j = 0; j < side; j++) {
				aPlayer = x + j;
				addLinkAt(aPlayer, aPlayer);
			}
		}
	}

	private void initGeometrySquareVonNeumann(int side, int fullside, int offset) {
		int aPlayer;
		boolean isInterspecies = isInterspecies();

		for (int i = 0; i < side; i++) {
			int x = i * fullside;
			int u = ((i - 1 + side) % side) * fullside;
			int d = ((i + 1) % side) * fullside;
			for (int j = 0; j < side; j++) {
				int r = (j + 1) % side;
				int l = (j - 1 + side) % side;
				aPlayer = offset + x + j;
				if (isInterspecies)
					addLinkAt(aPlayer, aPlayer);
				addLinkAt(aPlayer, offset + u + j);
				addLinkAt(aPlayer, offset + x + r);
				addLinkAt(aPlayer, offset + d + j);
				addLinkAt(aPlayer, offset + x + l);
			}
		}
		if (fixedBoundary) {
			// corners
			aPlayer = offset;
			clearLinksFrom(aPlayer); // upper-left corner
			if (isInterspecies)
				addLinkAt(aPlayer, aPlayer);
			addLinkAt(aPlayer, aPlayer + 1); // right
			addLinkAt(aPlayer, aPlayer + fullside); // down
			aPlayer = offset + side - 1;
			clearLinksFrom(aPlayer); // upper-right corner
			if (isInterspecies)
				addLinkAt(aPlayer, aPlayer);
			addLinkAt(aPlayer, aPlayer - 1); // left
			addLinkAt(aPlayer, aPlayer + fullside); // down
			aPlayer = offset + (side - 1) * fullside;
			clearLinksFrom(aPlayer); // lower-left corner
			if (isInterspecies)
				addLinkAt(aPlayer, aPlayer);
			addLinkAt(aPlayer, aPlayer + 1); // right
			addLinkAt(aPlayer, aPlayer - fullside); // up
			aPlayer = offset + (side - 1) * (fullside + 1);
			clearLinksFrom(aPlayer); // lower-right corner
			if (isInterspecies)
				addLinkAt(aPlayer, aPlayer);
			addLinkAt(aPlayer, aPlayer - 1); // left
			addLinkAt(aPlayer, aPlayer - fullside); // up
			// edges
			for (int i = 1; i < (side - 1); i++) {
				// top
				aPlayer = offset + i;
				clearLinksFrom(aPlayer);
				if (isInterspecies)
					addLinkAt(aPlayer, aPlayer);
				addLinkAt(aPlayer, aPlayer - 1);
				addLinkAt(aPlayer, aPlayer + 1);
				addLinkAt(aPlayer, aPlayer + fullside);
				// bottom
				aPlayer = offset + (side - 1) * fullside + i;
				clearLinksFrom(aPlayer);
				if (isInterspecies)
					addLinkAt(aPlayer, aPlayer);
				addLinkAt(aPlayer, aPlayer - 1);
				addLinkAt(aPlayer, aPlayer + 1);
				addLinkAt(aPlayer, aPlayer - fullside);
				// left
				aPlayer = offset + fullside * i;
				clearLinksFrom(aPlayer);
				if (isInterspecies)
					addLinkAt(aPlayer, aPlayer);
				addLinkAt(aPlayer, aPlayer + 1);
				addLinkAt(aPlayer, aPlayer - fullside);
				addLinkAt(aPlayer, aPlayer + fullside);
				// right
				aPlayer = offset + fullside * i + side - 1;
				clearLinksFrom(aPlayer);
				if (isInterspecies)
					addLinkAt(aPlayer, aPlayer);
				addLinkAt(aPlayer, aPlayer - 1);
				addLinkAt(aPlayer, aPlayer - fullside);
				addLinkAt(aPlayer, aPlayer + fullside);
			}
			isRegular = false;
		}
	}

	private void initGeometrySquareMoore(int side, int fullside, int offset) {
		int aPlayer;
		boolean isInterspecies = isInterspecies();

		for (int i = 0; i < side; i++) {
			int x = i * fullside;
			int u = ((i - 1 + side) % side) * fullside;
			int d = ((i + 1) % side) * fullside;
			for (int j = 0; j < side; j++) {
				int r = (j + 1) % side;
				int l = (j - 1 + side) % side;
				aPlayer = offset + x + j;
				if (isInterspecies)
					addLinkAt(aPlayer, aPlayer);
				addLinkAt(aPlayer, offset + u + j);
				addLinkAt(aPlayer, offset + u + r);
				addLinkAt(aPlayer, offset + x + r);
				addLinkAt(aPlayer, offset + d + r);
				addLinkAt(aPlayer, offset + d + j);
				addLinkAt(aPlayer, offset + d + l);
				addLinkAt(aPlayer, offset + x + l);
				addLinkAt(aPlayer, offset + u + l);
			}
		}
		if (fixedBoundary) {
			// corners\
			aPlayer = offset;
			clearLinksFrom(aPlayer);
			if (isInterspecies)
				addLinkAt(aPlayer, aPlayer);
			addLinkAt(aPlayer, aPlayer + 1);
			addLinkAt(aPlayer, aPlayer + fullside);
			addLinkAt(aPlayer, aPlayer + fullside + 1);
			aPlayer = offset + side - 1;
			clearLinksFrom(aPlayer);
			if (isInterspecies)
				addLinkAt(aPlayer, aPlayer);
			addLinkAt(aPlayer, aPlayer - 1);
			addLinkAt(aPlayer, aPlayer + fullside);
			addLinkAt(aPlayer, aPlayer + fullside - 1);
			aPlayer = offset + (side - 1) * fullside;
			clearLinksFrom(aPlayer);
			if (isInterspecies)
				addLinkAt(aPlayer, aPlayer);
			addLinkAt(aPlayer, aPlayer + 1);
			addLinkAt(aPlayer, aPlayer - fullside);
			addLinkAt(aPlayer, aPlayer - fullside + 1);
			aPlayer = offset + (side - 1) * (fullside + 1);
			clearLinksFrom(aPlayer);
			if (isInterspecies)
				addLinkAt(aPlayer, aPlayer);
			addLinkAt(aPlayer, aPlayer - 1);
			addLinkAt(aPlayer, aPlayer - fullside);
			addLinkAt(aPlayer, aPlayer - fullside - 1);
			// edges
			for (int i = 1; i < (side - 1); i++) {
				// top
				aPlayer = offset + i;
				clearLinksFrom(aPlayer);
				if (isInterspecies)
					addLinkAt(aPlayer, aPlayer);
				addLinkAt(aPlayer, aPlayer - 1);
				addLinkAt(aPlayer, aPlayer + 1);
				addLinkAt(aPlayer, aPlayer + fullside);
				addLinkAt(aPlayer, aPlayer + fullside - 1);
				addLinkAt(aPlayer, aPlayer + fullside + 1);
				// bottom
				aPlayer = offset + (side - 1) * fullside + i;
				clearLinksFrom(aPlayer);
				if (isInterspecies)
					addLinkAt(aPlayer, aPlayer);
				addLinkAt(aPlayer, aPlayer - 1);
				addLinkAt(aPlayer, aPlayer + 1);
				addLinkAt(aPlayer, aPlayer - fullside);
				addLinkAt(aPlayer, aPlayer - fullside - 1);
				addLinkAt(aPlayer, aPlayer - fullside + 1);
				// left
				aPlayer = offset + fullside * i;
				clearLinksFrom(aPlayer);
				if (isInterspecies)
					addLinkAt(aPlayer, aPlayer);
				addLinkAt(aPlayer, aPlayer + 1);
				addLinkAt(aPlayer, aPlayer - fullside);
				addLinkAt(aPlayer, aPlayer + fullside);
				addLinkAt(aPlayer, aPlayer - fullside + 1);
				addLinkAt(aPlayer, aPlayer + fullside + 1);
				// right
				aPlayer = offset + fullside * i + side - 1;
				clearLinksFrom(aPlayer);
				if (isInterspecies)
					addLinkAt(aPlayer, aPlayer);
				addLinkAt(aPlayer, aPlayer - 1);
				addLinkAt(aPlayer, aPlayer - fullside);
				addLinkAt(aPlayer, aPlayer + fullside);
				addLinkAt(aPlayer, aPlayer - fullside - 1);
				addLinkAt(aPlayer, aPlayer + fullside - 1);
			}
			isRegular = false;
		}
	}

	private void initGeometrySquare(int side, int fullside, int offset) {
		int aPlayer, bPlayer;
		boolean isInterspecies = isInterspecies();
		int range = Math.min(side / 2, Math.max(1, (int) (Math.sqrt(connectivity + 1.5) / 2.0)));

		for (int i = 0; i < side; i++) {
			int x = i * fullside, y;
			for (int j = 0; j < side; j++) {
				aPlayer = offset + x + j;
				for (int u = i - range; u <= i + range; u++) {
					y = offset + ((u + side) % side) * fullside;
					for (int v = j - range; v <= j + range; v++) {
						bPlayer = y + (v + side) % side;
						// avoid self-interactions
						if (aPlayer == bPlayer && !isInterspecies)
							continue;
						addLinkAt(aPlayer, bPlayer);
					}
				}
			}
		}
		if (fixedBoundary) {
			// corners - top left
			aPlayer = offset;
			clearLinksFrom(aPlayer);
			for (int u = 0; u <= range; u++) {
				int r = aPlayer + u * fullside;
				for (int v = 0; v <= range; v++) {
					bPlayer = r + v;
					// avoid self-interactions
					if (aPlayer == bPlayer && !isInterspecies)
						continue;
					addLinkAt(aPlayer, bPlayer);
				}
			}
			// corners - top right
			aPlayer = offset + side - 1;
			clearLinksFrom(aPlayer);
			for (int u = 0; u <= range; u++) {
				int r = aPlayer + u * fullside;
				for (int v = -range; v <= 0; v++) {
					bPlayer = r + v;
					// avoid self-interactions
					if (aPlayer == bPlayer && !isInterspecies)
						continue;
					addLinkAt(aPlayer, bPlayer);
				}
			}
			// corners - bottom left
			aPlayer = offset + (side - 1) * fullside;
			clearLinksFrom(aPlayer);
			for (int u = -range; u <= 0; u++) {
				int r = aPlayer + u * fullside;
				for (int v = 0; v <= range; v++) {
					bPlayer = r + v;
					// avoid self-interactions
					if (aPlayer == bPlayer && !isInterspecies)
						continue;
					addLinkAt(aPlayer, bPlayer);
				}
			}
			// corners - bottom right
			aPlayer = offset + (side - 1) * (fullside + 1);
			clearLinksFrom(aPlayer);
			for (int u = -range; u <= 0; u++) {
				int r = aPlayer + u * fullside;
				for (int v = -range; v <= 0; v++) {
					bPlayer = r + v;
					// avoid self-interactions
					if (aPlayer == bPlayer && !isInterspecies)
						continue;
					addLinkAt(aPlayer, bPlayer);
				}
			}
			// edges
			for (int i = 1; i < (side - 1); i++) {
				// top
				int row = 0;
				int col = i;
				aPlayer = offset + row * fullside + col;
				clearLinksFrom(aPlayer);
				for (int u = row; u <= row + range; u++) {
					int r = offset + u * fullside;
					for (int v = col - range; v <= col + range; v++) {
						bPlayer = r + (v + side) % side;
						// avoid self-interactions
						if (aPlayer == bPlayer && !isInterspecies)
							continue;
						addLinkAt(aPlayer, bPlayer);
					}
				}
				// bottom
				row = side - 1;
				col = i;
				aPlayer = offset + row * fullside + col;
				clearLinksFrom(aPlayer);
				for (int u = row - range; u <= row; u++) {
					int r = offset + u * fullside;
					for (int v = col - range; v <= col + range; v++) {
						bPlayer = r + (v + side) % side;
						// avoid self-interactions
						if (aPlayer == bPlayer && !isInterspecies)
							continue;
						addLinkAt(aPlayer, bPlayer);
					}
				}
				// left
				row = i;
				col = 0;
				aPlayer = offset + row * fullside + col;
				clearLinksFrom(aPlayer);
				for (int u = row - range; u <= row + range; u++) {
					int r = offset + ((u + side) % side) * fullside;
					for (int v = col; v <= col + range; v++) {
						bPlayer = r + v;
						// avoid self-interactions
						if (aPlayer == bPlayer && !isInterspecies)
							continue;
						addLinkAt(aPlayer, bPlayer);
					}
				}
				// right
				row = i;
				col = side - 1;
				aPlayer = offset + row * fullside + col;
				clearLinksFrom(aPlayer);
				for (int u = row - range; u <= row + range; u++) {
					int r = offset + ((u + side) % side) * fullside;
					for (int v = col - range; v <= col; v++) {
						bPlayer = r + v;
						// avoid self-interactions
						if (aPlayer == bPlayer && !isInterspecies)
							continue;
						addLinkAt(aPlayer, bPlayer);
					}
				}
			}
			isRegular = false;
		}
	}

	/**
	 * initialize cubic (3D) lattice
	 * 
	 * requirements/notes: - integer cube population size. - appropriate
	 * connectivity (6 or 3x3x3-1=26, 5x5x5-1, 7x7x7-1 etc.) - does not (yet?) take
	 * inter-species interactions into account.
	 */
	public void initGeometryCube() {
		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = true;
		boolean isInterspecies = isInterspecies();
		alloc();

		int l = (int) Math.floor(Math.pow(size, 1.0 / 3.0) + 0.5);
		int lz = l;
		if (size == 25000) {
			l = 50;
			lz = 10;
		}
		int l2 = l * l;
		switch ((int) Math.rint(connectivity)) {
			case 1: // self - meaningful only for inter-species interactions
				for (int k = 0; k < lz; k++) {
					int z = k * l2;
					for (int i = 0; i < l; i++) {
						int x = i * l;
						for (int j = 0; j < l; j++) {
							int aPlayer = z + x + j;
							addLinkAt(aPlayer, aPlayer);
						}
					}
				}
				break;

			case 6: // north, east, south, west, top, bottom
				if (fixedBoundary) {
					// fixed boundary
					for (int k = 0; k < lz; k++) {
						int z = k * l2;
						int u = (k + 1 >= lz ? -1 : (k + 1) * l2);
						int d = (k - 1) * l2;
						for (int i = 0; i < l; i++) {
							int x = i * l;
							int n = (i - 1) * l;
							int s = (i + 1 >= l ? -1 : (i + 1) * l);
							for (int j = 0; j < l; j++) {
								int e = (j + 1 >= l ? -1 : j + 1);
								int w = j - 1;
								int aPlayer = z + x + j;
								if (isInterspecies)
									addLinkAt(aPlayer, aPlayer);
								if (n >= 0)
									addLinkAt(aPlayer, z + n + j);
								if (e >= 0)
									addLinkAt(aPlayer, z + x + e);
								if (s >= 0)
									addLinkAt(aPlayer, z + s + j);
								if (w >= 0)
									addLinkAt(aPlayer, z + x + w);
								if (u >= 0)
									addLinkAt(aPlayer, u + x + j);
								if (d >= 0)
									addLinkAt(aPlayer, d + x + j);
							}
						}
					}
					break;
				}
				// periodic boundary
				for (int k = 0; k < lz; k++) {
					int z = k * l2;
					int u = ((k + 1) % lz) * l2;
					int d = ((k - 1 + lz) % lz) * l2;
					for (int i = 0; i < l; i++) {
						int x = i * l;
						int n = ((i - 1 + l) % l) * l;
						int s = ((i + 1) % l) * l;
						for (int j = 0; j < l; j++) {
							int e = (j + 1) % l;
							int w = (j - 1 + l) % l;
							int aPlayer = z + x + j;
							if (isInterspecies)
								addLinkAt(aPlayer, aPlayer);
							addLinkAt(aPlayer, z + n + j);
							addLinkAt(aPlayer, z + x + e);
							addLinkAt(aPlayer, z + s + j);
							addLinkAt(aPlayer, z + x + w);
							addLinkAt(aPlayer, u + x + j);
							addLinkAt(aPlayer, d + x + j);
						}
					}
				}
				break;

			default: // XxXxX neighborhood - validity of range was checked in Population.java
				int range = Math.min(l / 2, Math.max(1, (int) (Math.pow(connectivity + 1.5, 1.0 / 3.0) / 2.0)));
				if (fixedBoundary) {
					for (int k = 0; k < lz; k++) {
						int z = k * l2;
						for (int i = 0; i < l; i++) {
							int y = i * l;
							for (int j = 0; j < l; j++) {
								int aPlayer = z + y + j;

								for (int kr = Math.max(0, k - range); kr <= Math.min(lz - 1, k + range); kr++) {
									int zr = ((kr + lz) % lz) * l2;
									for (int ir = Math.max(0, i - range); ir <= Math.min(l - 1, i + range); ir++) {
										int yr = ((ir + l) % l) * l;
										for (int jr = Math.max(0, j - range); jr <= Math.min(l - 1, j + range); jr++) {
											int bPlayer = zr + yr + ((jr + l) % l);
											// avoid self-interactions
											if (aPlayer == bPlayer && !isInterspecies)
												continue;
											addLinkAt(aPlayer, bPlayer);
										}
									}
								}
							}
						}
					}
					break;
				}
				// periodic boundary
				for (int k = 0; k < lz; k++) {
					int z = k * l2;
					for (int i = 0; i < l; i++) {
						int y = i * l;
						for (int j = 0; j < l; j++) {
							int aPlayer = z + y + j;

							for (int kr = k - range; kr <= k + range; kr++) {
								int zr = ((kr + lz) % lz) * l2;
								for (int ir = i - range; ir <= i + range; ir++) {
									int yr = ((ir + l) % l) * l;
									for (int jr = j - range; jr <= j + range; jr++) {
										int bPlayer = zr + yr + ((jr + l) % l);
										// avoid self-interactions
										if (aPlayer == bPlayer && !isInterspecies)
											continue;
										addLinkAt(aPlayer, bPlayer);
									}
								}
							}
						}
					}
				}
		}
	}

	/**
	 * initialize hexagonal lattice (connectivity 6)
	 * 
	 * requirements/notes: - even integer square population size. - connectivity 6 -
	 * does not (yet?) take inter-species interactions into account.
	 * 
	 * todo: - implement higher connectivities
	 */
	public void initGeometryHoneycomb() {
		int aPlayer;

		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = true;
		boolean isInterspecies = isInterspecies();
		alloc();

		int side = (int) Math.floor(Math.sqrt(size) + 0.5);
		switch ((int) Math.rint(connectivity)) {
			case 1:
				for (int i = 0; i < side; i++) {
					int x = i * side;
					for (int j = 0; j < side; j++) {
						aPlayer = x + j;
						addLinkAt(aPlayer, aPlayer);
					}
				}
				break;

			default:
				for (int i = 0; i < side; i += 2) {
					int x = i * side;
					int u = ((i - 1 + side) % side) * side;
					boolean uNowrap = (i > 0);
					int d = ((i + 1) % side) * side; // d cannot wrap because the increment is 2
					int r, l;
					for (int j = 0; j < side; j++) {
						aPlayer = x + j;
						if (isInterspecies)
							addLinkAt(aPlayer, aPlayer);
						if (!fixedBoundary || uNowrap)
							addLinkAt(aPlayer, u + j);
						r = (j + 1) % side;
						if (!fixedBoundary || r > 0)
							addLinkAt(aPlayer, x + r);
						addLinkAt(aPlayer, d + j);
						l = (j - 1 + side) % side;
						if (!fixedBoundary || l < side - 1) {
							addLinkAt(aPlayer, d + l);
							addLinkAt(aPlayer, x + l);
						}
						if (!fixedBoundary || (uNowrap && l < side - 1))
							addLinkAt(aPlayer, u + l);
					}
					x = ((i + 1) % side) * side; // x cannot wrap because the increment is 2
					u = i * side;
					d = ((i + 2) % side) * side;
					boolean dNowrap = (i < side - 2);
					for (int j = 0; j < side; j++) {
						aPlayer = x + j;
						if (isInterspecies)
							addLinkAt(aPlayer, aPlayer);
						addLinkAt(aPlayer, u + j);
						r = (j + 1) % side;
						if (!fixedBoundary || r > 0) {
							addLinkAt(aPlayer, u + r);
							addLinkAt(aPlayer, x + r);
						}
						if (!fixedBoundary || (dNowrap && r > 0))
							addLinkAt(aPlayer, d + r);
						if (!fixedBoundary || dNowrap)
							addLinkAt(aPlayer, d + j);
						l = (j - 1 + side) % side;
						if (!fixedBoundary || l < side - 1)
							addLinkAt(aPlayer, x + l);
					}
				}
		}
	}

	/**
	 * initialize triangular lattice (connectivity 3)
	 * 
	 * requirements/notes: - even integer square population size. - connectivity 3 -
	 * does not (yet?) take inter-species interactions into account.
	 * 
	 * todo: - implement higher connectivities
	 */
	public void initGeometryTriangular() {
		int aPlayer;

		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = true;
		boolean isInterspecies = isInterspecies();
		alloc();

		int side = (int) Math.floor(Math.sqrt(size) + 0.5);
		switch ((int) Math.rint(connectivity)) {
			case 1:
				for (int i = 0; i < side; i++) {
					int x = i * side;
					for (int j = 0; j < side; j++) {
						aPlayer = x + j;
						addLinkAt(aPlayer, aPlayer);
					}
				}
				break;

			default:
				for (int i = 0; i < side; i += 2) {
					int x = i * side;
					int u = ((i - 1 + side) % side) * side;
					boolean uNowrap = (i > 0);
					int d = ((i + 1) % side) * side; // d cannot wrap because the i increment is 2
					int r, l;
					for (int j = 0; j < side; j += 2) {
						aPlayer = x + j;
						if (isInterspecies)
							addLinkAt(aPlayer, aPlayer);
						r = j + 1; // r cannot wrap because the j increment is 2
						addLinkAt(aPlayer, x + r);
						l = (j - 1 + side) % side;
						if (!fixedBoundary || l < side - 1)
							addLinkAt(aPlayer, x + l);
						addLinkAt(aPlayer, d + j);
						aPlayer = x + j + 1;
						if (isInterspecies)
							addLinkAt(aPlayer, aPlayer);
						r = (r + 1) % side; // now r can wrap and will be zero if it did
						if (!fixedBoundary || r > 0)
							addLinkAt(aPlayer, x + r);
						l = j;
						addLinkAt(aPlayer, x + l);
						if (!fixedBoundary || uNowrap)
							addLinkAt(aPlayer, u + j + 1);
					}
					x = d;
					u = i * side;
					d = ((i + 2) % side) * side;
					boolean dNowrap = (i < side - 2);
					for (int j = 0; j < side; j += 2) {
						aPlayer = x + j;
						if (isInterspecies)
							addLinkAt(aPlayer, aPlayer);
						r = j + 1; // r cannot wrap because the j increment is 2
						addLinkAt(aPlayer, x + r);
						l = (j - 1 + side) % side;
						if (!fixedBoundary || l < side - 1)
							addLinkAt(aPlayer, x + l);
						addLinkAt(aPlayer, u + j);
						aPlayer = x + j + 1;
						if (isInterspecies)
							addLinkAt(aPlayer, aPlayer);
						r = (r + 1) % side; // now r can wrap and will be zero if it did
						if (!fixedBoundary || r > 0)
							addLinkAt(aPlayer, x + r);
						l = j;
						addLinkAt(aPlayer, x + l);
						if (!fixedBoundary || dNowrap)
							addLinkAt(aPlayer, d + j + 1);
					}
				}
		}
	}

	/**
	 * initialize Frucht graph. the Frucht graph is the smallest regular graph
	 * without any further symmetries. a cubic graph with 12 vertices and no
	 * automorphisms apart from the identity map.
	 * 
	 * Frucht, R. (1939), "Herstellung von Graphen mit vorgegebener abstrakter
	 * Gruppe.", Compositio Mathematica, 6: 239–250
	 * 
	 * requirements/notes: - connectivity of 3 - population size of 12 - does not
	 * (yet?) take inter-species interactions into account.
	 */
	public void initGeometryFruchtGraph() {
		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = false;
		alloc();

//		// link nodes - create ring first
//		for( int i=0; i<size; i++ ) {
//			addLinkAt(i, (i-1+size)%size);
//			addLinkAt(i, (i+1)%size);
//		}
//		addLinkAt(0, 7);
//		addLinkAt(1, 11);
//		addLinkAt(2, 10);
//		addLinkAt(3, 5);
//		addLinkAt(4, 9);
//		addLinkAt(5, 3);
//		addLinkAt(6, 8);
//		addLinkAt(7, 0);
//		addLinkAt(8, 6);
//		addLinkAt(9, 4);
//		addLinkAt(10, 2);
//		addLinkAt(11, 1);
// use same labels as in McAvoy & Hauert 2015 J. R. Soc. Interface
		addEdgeAt(0, 1);
		addEdgeAt(1, 2);
		addEdgeAt(2, 3);
		addEdgeAt(3, 4);
		addEdgeAt(4, 5);
		addEdgeAt(5, 6);
		addEdgeAt(6, 0);
		addEdgeAt(0, 7);
		addEdgeAt(1, 7);
		addEdgeAt(2, 8);
		addEdgeAt(3, 8);
		addEdgeAt(4, 9);
		addEdgeAt(5, 9);
		addEdgeAt(6, 10);
		addEdgeAt(7, 10);
		addEdgeAt(8, 11);
		addEdgeAt(9, 11);
		addEdgeAt(10, 11);
	}

	/**
	 * initialize Tietze graph. Tietze’s graph is a (regular) cubic graph with
	 * twelve vertices but with a higher degree of symmetry than the Frucht graph.
	 * Tietze’s graph has twelve automorphisms but is not vertex-transitive.
	 * 
	 * see Tietze, Heinrich (1910), "Einige Bemerkungen zum Problem des
	 * Kartenfärbens auf einseitigen Flächen", DMV Annual Report, 19: 155–159
	 * 
	 * requirements/notes: - connectivity of 3 - population size of 12 - does not
	 * (yet?) take inter-species interactions into account.
	 */
	public void initGeometryTietzeGraph() {
		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = false;
		alloc();

		// link nodes - create line first
		for (int i = 1; i < size; i++)
			addEdgeAt(i, i - 1);

		addEdgeAt(0, 4);
		addEdgeAt(0, 8);
		addEdgeAt(1, 6);
		addEdgeAt(2, 10);
		addEdgeAt(3, 7);
		addEdgeAt(5, 11);
		addEdgeAt(9, 11);
	}

	/**
	 * initialize Franklin graph. The Franklin graph is another a cubic graph with
	 * twelve vertices but is also vertex-transitive. Intuitively speaking, vertex
	 * transitivity means that the graph looks the same from the perspective of
	 * every node.
	 * 
	 * see Franklin, P. "A Six Color Problem." J. Math. Phys. 13, 363-379, 1934.
	 * 
	 * requirements/notes: - connectivity of 3 - population size of 12 - does not
	 * (yet?) take inter-species interactions into account.
	 */
	public void initGeometryFranklinGraph() {
		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = false;
		alloc();

		// link nodes - create ring first
		for (int i = 1; i < size; i++)
			addEdgeAt(i, i - 1);
		addEdgeAt(0, size - 1);

		addEdgeAt(0, 7);
		addEdgeAt(1, 6);
		addEdgeAt(2, 9);
		addEdgeAt(3, 8);
		addEdgeAt(4, 11);
		addEdgeAt(5, 10);
	}

	/**
	 * initialize Heawood graph. no cubic symmetric graph with 12 vertices exists.
	 * closest comparable graph is the Heawood graph with fourteen vertices
	 * 
	 * symmetric graphs satisfy the stronger structural symmetry requirements of
	 * arc-transitivity. intuitively speaking, arc-transitivity means that not only
	 * does the graph look the same from the perspective of every vertex but also
	 * that if a pair of neighbouring individuals is randomly relocated to some
	 * other neighbouring pair of vertices, the two individuals are not able to tell
	 * whether and where they have been moved even when both are aware of the
	 * overall graph structure and share their conclusions.
	 * 
	 * see Heawood, P. J. (1890). "Map colouring theorems". Quarterly J. Math.
	 * Oxford Ser. 24: 322–339.
	 * 
	 * requirements/notes: - connectivity of 3 - population size of 14 - does not
	 * (yet?) take inter-species interactions into account.
	 */
	public void initGeometryHeawoodGraph() {
		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = false;
		alloc();

		// link nodes - create ring first
		for (int i = 1; i < size; i++)
			addEdgeAt(i, i - 1);
		addEdgeAt(0, size - 1);

		addEdgeAt(0, 5);
		addEdgeAt(2, 7);
		addEdgeAt(4, 9);
		addEdgeAt(6, 11);
		addEdgeAt(8, 13);
		addEdgeAt(10, 1);
		addEdgeAt(12, 3);
	}

	/**
	 * initialize Icosahedron graph. another example of a symmetric graph with
	 * twelve vertices but degree five.
	 * 
	 * requirements/notes: - connectivity of 5 - population size of 12 - does not
	 * (yet?) take inter-species interactions into account.
	 */
	public void initGeometryIcosahedronGraph() {
		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = false;
		alloc();

		// link nodes - create line first
		for (int i = 1; i < size; i++)
			addEdgeAt(i, i - 1);

		addEdgeAt(0, 4);
		addEdgeAt(0, 5);
		addEdgeAt(0, 6);
		addEdgeAt(1, 6);
		addEdgeAt(1, 7);
		addEdgeAt(1, 8);
		addEdgeAt(2, 0);
		addEdgeAt(2, 4);
		addEdgeAt(2, 8);
		addEdgeAt(3, 8);
		addEdgeAt(3, 9);
		addEdgeAt(3, 10);
		addEdgeAt(4, 10);
		addEdgeAt(5, 10);
		addEdgeAt(5, 11);
		addEdgeAt(6, 11);
		addEdgeAt(7, 9);
		addEdgeAt(7, 11);
		addEdgeAt(9, 11);
	}

	/**
	 * initialize dodecahedron graph. cubic, symmetric graph with 20 vertices
	 * 
	 * requirements/notes: - connectivity of 3 - population size of 20 - does not
	 * (yet?) take inter-species interactions into account.
	 */
	public void initGeometryDodekahedronGraph() {
		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = false;
		alloc();

		// link nodes:
		// - create ring over even numbered vertices
		// - link even to previous odd numbered vertices
		for (int i = 0; i < size; i += 2) {
			addEdgeAt(i, (size + i - 2) % size);
			addEdgeAt(i, i + 1);
		}

		addEdgeAt(1, 5);
		addEdgeAt(3, 7);
		addEdgeAt(5, 9);
		addEdgeAt(7, 11);
		addEdgeAt(9, 13);
		addEdgeAt(11, 15);
		addEdgeAt(13, 17);
		addEdgeAt(15, 19);
		addEdgeAt(17, 1);
		addEdgeAt(19, 3);
	}

	/**
	 * initialize Desargues graph, named Girard Desargues (1591–1661). another
	 * cubic, symmetric graph with 20 vertices (same as icosahedron graph). the two
	 * graphs have the same diameter, mean shortest path length, and clustering
	 * coefficient. nevertheless the highly susceptible fixation times differ for
	 * the two graphs.
	 * 
	 * see Kagno, I. N. (1947), "Desargues' and Pappus' graphs and their groups",
	 * American Journal of Mathematics, 69 (4): 859–863, doi:10.2307/2371806.
	 * 
	 * requirements/notes: - connectivity of 3 - population size of 20 - does not
	 * (yet?) take inter-species interactions into account.
	 */
	public void initGeometryDesarguesGraph() {
		isRewired = false;
		isUndirected = true;
		isRegular = true;
		isLattice = false;
		alloc();

		// link nodes - create ring first
		for (int i = 0; i < size; i++)
			addEdgeAt(i, (size + i - 1) % size);

		addEdgeAt(0, 9);
		addEdgeAt(1, 12);
		addEdgeAt(2, 7);
		addEdgeAt(3, 18);
		addEdgeAt(4, 13);
		addEdgeAt(5, 16);
		addEdgeAt(6, 11);
		addEdgeAt(8, 17);
		addEdgeAt(10, 15);
		addEdgeAt(14, 19);
	}

	/**
	 * initialize (connected, undirected) random graph.
	 * 
	 * requirements/notes: - rounds link count to even number - does not (yet?) take
	 * inter-species interactions into account.
	 * 
	 * todo: - check if connectedness makes minimal assumptions, i.e. if general
	 * enough
	 */
	public void initGeometryRandomGraph() {
		int parent, parentIdx, child, childIdx;
		int todo, done;
		RNGDistribution rng = population.rng;

		isRewired = false;
		isUndirected = true;
		isRegular = false;
		isLattice = false;
		alloc();

		// ensure connectedness
		int nLinks = (int) Math.floor(connectivity * size + 0.5);
		nLinks = (nLinks - nLinks % 2) / 2; // nLinks must be even
		int[] isolated = new int[size];
		int[] connected = new int[size];
		for (int i = 0; i < size; i++)
			isolated[i] = i;
		todo = size;
		done = 0;
		parent = rng.random0n(todo);
		parentIdx = isolated[parent];
		todo--;
		if (parent != todo)
			System.arraycopy(isolated, parent + 1, isolated, parent, todo - parent);
		connected[done] = parentIdx;
		done++;
		child = rng.random0n(todo);
		childIdx = isolated[child];
		todo--;
		System.arraycopy(isolated, child + 1, isolated, child, todo - child);
		connected[done] = childIdx;
		done++;
		addLinkAt(parentIdx, childIdx);
		addLinkAt(childIdx, parentIdx);
		nLinks--;
		while (todo > 0) {
			parent = rng.random0n(done);
			parentIdx = connected[parent];
			child = rng.random0n(todo);
			childIdx = isolated[child];
			todo--;
			System.arraycopy(isolated, child + 1, isolated, child, todo - child);
			connected[done] = childIdx;
			done++;
			addLinkAt(parentIdx, childIdx);
			addLinkAt(childIdx, parentIdx);
			nLinks--;
		}

		// now we have a connected graph
		randomlinking: while (nLinks > 0) {
			parentIdx = rng.random0n(size);
			childIdx = rng.random0n(size - 1);
			if (childIdx >= parentIdx)
				childIdx++;
			int[] pNeigh = out[parentIdx];
			int len = kout[parentIdx];
			for (int i = 0; i < len; i++) {
				if (pNeigh[i] == childIdx)
					continue randomlinking;
			}
			addLinkAt(parentIdx, childIdx);
			addLinkAt(childIdx, parentIdx);
			nLinks--;
		}
	}

	/**
	 * initialize (connected, directed) random graph.
	 * 
	 * requirements/notes: - does not (yet?) take inter-species interactions into
	 * account.
	 * 
	 * todo: - check if connectedness makes minimal assumptions, i.e. if general
	 * enough
	 */
	public void initGeometryRandomGraphDirected() {
		int parent, parentIdx, child, childIdx;
		int todo, done;
		RNGDistribution rng = population.rng;

		isRewired = false;
		isUndirected = false;
		isRegular = false;
		isLattice = false;
		alloc();

		// ensure connectedness
//XXX this is flawed... the first node does not necessarily get an incoming link - besides,
//    the underlying ring is too restrictive (could be several connected rings)!
		int nLinks = (int) Math.floor(connectivity * size + 0.5);
		int[] isolated = new int[size];
		int[] connected = new int[size];
		for (int i = 0; i < size; i++)
			isolated[i] = i;
		todo = size;
		done = 0;
		parent = rng.random0n(todo);
		parentIdx = isolated[parent];
		todo--;
		if (parent != todo)
			System.arraycopy(isolated, parent + 1, isolated, parent, todo - parent);
		connected[done] = parentIdx;
		done++;
		child = rng.random0n(todo);
		childIdx = isolated[child];
		todo--;
		System.arraycopy(isolated, child + 1, isolated, child, todo - child);
		connected[done] = childIdx;
		done++;
		addLinkAt(parentIdx, childIdx);
		nLinks--;
		while (todo > 0) {
			parent = rng.random0n(done);
			parentIdx = connected[parent];
			child = rng.random0n(todo);
			childIdx = isolated[child];
			todo--;
			System.arraycopy(isolated, child + 1, isolated, child, todo - child);
			connected[done] = childIdx;
			done++;
			addLinkAt(parentIdx, childIdx);
			nLinks--;
		}

		// now we have a connected graph
		// however it is generally not possible to connect every vertex pair
		// the network has most likely numerous sources and sinks, i.e. each vertex has
		// at least one outgoing OR one incoming link.
		randomlinking: while (nLinks > 0) {
			parentIdx = rng.random0n(size);
			childIdx = rng.random0n(size - 1);
			if (childIdx >= parentIdx)
				childIdx++;
			int[] pNeigh = out[parentIdx];
			int len = kout[parentIdx];
			for (int i = 0; i < len; i++) {
				if (pNeigh[i] == childIdx)
					continue randomlinking;
			}
			addLinkAt(parentIdx, childIdx);
			nLinks--;
		}
	}

	protected static final int MAX_TRIALS = 10;

	/**
	 * initialize (connected, undirected) random regular graph.
	 * 
	 * requirements/notes: - does not (yet?) take inter-species interactions into
	 * account. - graph generation may fail; after MAX_TRIALS failures resort to
	 * well-mixed population
	 * 
	 * todo: - try to minimize risk of failure
	 */
	public void initGeometryRandomRegularGraph() {
		isRegular = true;
		int[] degrees = new int[size];
		Arrays.fill(degrees, (int) connectivity);
		int trials = 0;
		while (!initGeometryDegreeDistr(degrees) && ++trials < MAX_TRIALS);
		if (trials >= 10) {
			// reset sets size=-1
			int mysize = size;
			reset();
			size = mysize;
			check();
			init();
			logger.severe("initGeometryRandomRegularGraph failed - giving up (revert to well-mixed).");
			return;
		}
	}

	/**
	 * initialize (connected, undirected) scale-free network. generates power-law
	 * degree distribution first and then generates random network that satisfies
	 * those connectivities.
	 * 
	 * requirements/notes: - does not (yet?) take inter-species interactions into
	 * account.
	 * 
	 * todo: - try to minimize risk of failure
	 */
	public void initGeometryScaleFree() {
		double[] distr = new double[size]; // connectivities up to N-1
		RNGDistribution rng = population.rng;

		// generate power law distribution
		if (Math.abs(sfExponent) > 1e-8)
			for (int n = 0; n < size; n++)
				distr[n] = Math.pow((double) n / (double) size, sfExponent);
		else {
			// uniform distribution
			int max = (int) (2.0 * connectivity + 0.5);
			for (int n = 0; n <= max; n++)
				distr[n] = 1.0;
			for (int n = max + 1; n < size; n++)
				distr[n] = 0.0;
		}
		// calculate norm and average connectivity
		double norm = 0.0, conn = 0.0;
		for (int n = 1; n < size; n++) {
			norm += distr[n];
			conn += n * distr[n];
		}
		conn /= norm;
		// normalize distribution - makes life easier
		for (int n = 0; n < size; n++)
			distr[n] /= norm;

		// adjust distribution to match desired connectivity
		if (conn < connectivity) {
			// increase number of links
			// check feasibility
			double max = size / 2.0;
			if (connectivity > max) {
				// uniform distribution is the best we can do
				// System.out.println("connectivity too high - resort to uniform distribution");
				double p = 1.0 / (size - 1);
				for (int n = 1; n < size; n++)
					distr[n] = p;
//XXX must NOT change connectivity here!!! check in check()!
				connectivity = max;
			} else {
				// lift distribution
				double x = 1.0 - (connectivity - conn) / (max - conn);
				double lift = (1.0 - x) / (size - 1);
				for (int n = 1; n < size; n++)
					distr[n] = x * distr[n] + lift;
				/*
				 * { double checknorm=0.0, checkconn=0.0; for( int n=1; n<size; n++ ) {
				 * checknorm += distr[n]; checkconn += (double)n*distr[n]; } if(
				 * Math.abs(1.0-checknorm)>1e-10 )
				 * System.out.println("norm violated!!! - norm="+checknorm+" should be "+1+"!");
				 * if( Math.abs(connectivity-checkconn)>1e-8 )
				 * System.out.println("connectivity violated!!! - conn="+checkconn+" should be "
				 * +connectivity+"!"); }
				 */
				// System.out.println("distribution:");
				// for( int n=0; n<size; n++ ) System.out.println(n+": "+distr[n]);
			}
		} else {
			// decrease number of links - requires cutoff/maximum degree
			double km = 0.0, pm = 0.0;
			int m = 1;
			double sump = distr[1], sumpi = sump;
			while (km < connectivity && m < size - 1) {
				m++;
				pm = distr[m];
				sump += pm;
				sumpi += pm * m;
				km = (sumpi - pm * ((m * (m + 1)) / 2)) / (sump - m * pm);
			}
			for (int n = m; n < size; n++)
				distr[n] = 0.0;
			// System.out.println("cutoff:"+m+" -> km="+km);
			double decr = distr[m - 1];
			double newnorm = sump - pm - (m - 1) * decr;
			conn = 0.0;
			for (int n = 1; n < m; n++) {
				distr[n] = (distr[n] - decr) / newnorm;
				conn += distr[n] * n;
			}
			double x = 1.0 - (connectivity - conn) / (m / 2.0 - conn);
			double lift = (1.0 - x) / (m - 1);
			for (int n = 1; n < m; n++)
				distr[n] = x * distr[n] + lift;
			/*
			 * { double checknorm=0.0, checkconn=0.0; for( int n=1; n<size; n++ ) {
			 * checknorm += distr[n]; checkconn += (double)n*distr[n]; } if(
			 * Math.abs(1.0-checknorm)>1e-10 )
			 * System.out.println("norm violated!!! - norm="+checknorm+" should be "+1+"!");
			 * if( Math.abs(connectivity-checkconn)>1e-8 )
			 * System.out.println("connectivity violated!!! - conn="+checkconn+" should be "
			 * +connectivity+"!"); }
			 */
			// System.out.println("distribution:");
			// for( int n=0; n<size; n++ ) System.out.println(n+": "+distr[n]);
		}

		// allocate degree distribution
		int[] degrees = new int[size];
		int links;

		do {
			// choose degrees
			links = 0;
			int leaflinks = 0;
			int nonleaflinks = 0;
			for (int n = 0; n < size; n++) {
				double hit = rng.random01();
				for (int i = 1; i < size - 1; i++) {
					hit -= distr[i];
					if (hit <= 0.0) {
						degrees[n] = i;
						links += i;
						if (i > 1)
							nonleaflinks += i;
						else
							leaflinks++;
						break;
					}
				}
			}
			// check connectivity
			int adj = 0;
			if (connectivity > 0.0) {
				if (connectivity * size > links)
					adj = (int) Math.floor(connectivity * size + 0.5) - links;
				if (Math.max(2.0, connectivity) * size < links)
					adj = (int) Math.floor(Math.max(2.0, connectivity) * size + 0.5) - links;
				if ((links + adj) % 2 == 1)
					adj++; // ensure even number of docks
			}
			logger.warning("adjusting link count: " + links + " by " + adj + " to achieve "
					+ (int) Math.floor(connectivity * size + 0.5));

			// ensure right number of links
			// while( adj!=0 || (nonleaflinks < 2*(size-1)-leaflinks) || (links % 2 == 1) )
			// {
			while (adj != 0) {
				int node = rng.random0n(size);
				// draw new degree for random node
				int odegree = degrees[node], ndegree = -1;
				double hit = rng.random01();
				for (int i = 1; i < size - 1; i++) {
					hit -= distr[i];
					if (hit <= 0.0) {
						ndegree = i;
						break;
					}
				}
				int dd = ndegree - odegree;
				if (Math.abs(adj) <= Math.abs(adj - dd))
					continue;
				degrees[node] = ndegree;
				if (odegree == 1 && ndegree != 1) {
					leaflinks--;
					nonleaflinks += ndegree;
				}
				if (odegree != 1 && ndegree == 1) {
					leaflinks -= odegree;
					nonleaflinks--;
				}
				links += dd;
				adj -= dd;
				logger.warning(
						"links: " + links + ", goal: " + (int) Math.floor(Math.max(2.0, connectivity) * size + 0.5)
								+ ", change: " + (ndegree - odegree) + ", remaining: " + adj);
			}
			// do some basic checks on feasibility of distribution
			// 1) avoid uneven number of links
			// 2) number of non-leaf links must be above a certain threshold
			while ((nonleaflinks < 2 * (size - 1) - leaflinks) || (links % 2 == 1)) {
				int node = rng.random0n(size);
				// add link to random node
				if (degrees[node]++ == 1) {
					leaflinks--;
					nonleaflinks++;
				}
				nonleaflinks++;
				links++;
			}

			// sort degrees
			Arrays.sort(degrees); // sorts ascending
			for (int n = 0; n < size / 2; n++)
				swap(degrees, n, size - n - 1);

			/*
			 * System.out.println("distribution:"); for( int n=0; n<size-1; n++ )
			 * System.out.println(n+": "+distr[n]); System.out.println("degrees:"); for( int
			 * n=0; n<size; n++ ) System.out.println(n+": "+degrees[n]);
			 */
			for (int n = 0; n < 10; n++)
				if (initGeometryDegreeDistr(degrees))
					return;
		}
		/// while( !initGeometryDegreeDistr(geom, rng, degrees) );
		while (true);
		// printConnections(geom);
	}

	private static void swap(int x[], int a, int b) {
		int t = x[a];
		x[a] = x[b];
		x[b] = t;
	}

	/**
	 * utility method to generate network with a given degree distribution
	 * 
	 * requirements/notes: - degree distribution (sorted with descending degrees)
	 * 
	 * todo: - try to minimize risk of failure
	 */
	private boolean initGeometryDegreeDistr(int[] distr) {
		RNGDistribution rng = population.rng;

		// initialize
		isRewired = false;
		isUndirected = true;
		isLattice = false;
		alloc();

		int[] core = new int[size];
		int[] full = new int[size];
		// DEBUG - triggers exceptions to capture some bookkeeping issues
//		Arrays.fill(core, -1);
//		Arrays.fill(full, -1);
		int[] degree = new int[size];
		System.arraycopy(distr, 0, degree, 0, size);
		int todo = size;
		for (int n = 0; n < todo; n++)
			core[n] = n;

		if (!isDynamic) {
			// ensure connectedness for static graphs; exclude leaves for this stage
			// recall degree's are sorted in descending order
			int leafIdx = -1;
			for (int i=size-1; i>=0; i--) {
				if (degree[i]<=1)
					continue;
				leafIdx = i+1;
				break;
			}
			todo = leafIdx;
			int done = 0;
			int[] active = new int[size];
			int idxa = rng.random0n(todo);
			active[0] = core[idxa];
			core[idxa] = core[--todo];
			// DEBUG core[todo] = -1;
			int nActive = 1;
			while (todo > 0) {
				idxa = rng.random0n(nActive);
				int nodea = active[idxa];
				int idxb = rng.random0n(todo);
				int nodeb = core[idxb];
				addEdgeAt(nodea, nodeb);
				// if A reached degree add to full and remove from active
				if (kout[nodea] == degree[nodea]) {
					full[done++] = nodea;
					active[idxa] = active[--nActive];
					// DEBUG - triggers exceptions to capture some bookkeeping issues
//					active[nActive] = -1;
				}
				// if B reached degree add to full otherwise add to active
				if (kout[nodeb] == degree[nodeb])
					full[done++] = nodeb;
				else
					active[nActive++] = nodeb;
				// remove nodeb from core of unconnected nodes
				core[idxb] = core[--todo];
				// DEBUG - triggers exceptions to capture some bookkeeping issues
//				core[todo] = -1;
			}
			// now we have a connected core graph; add leaves to active nodes
			if (leafIdx<size)
				// all leaves are in tail of core
				System.arraycopy(core, leafIdx, active, nActive, size-leafIdx);
			core = active;
			todo = nActive;
		}
		// core: list of todo indices of nodes that require further connections
		// full: list of done indices of nodes with requested connectivity
		int escape = 0;
		while (todo > 1) {
			int idxa = rng.random0n(todo);
			int nodea = core[idxa];
			int idxb = rng.random0n(todo - 1);
			if (idxb >= idxa)
				idxb++;
			int nodeb = core[idxb];
			if (isNeighborOf(nodea, nodeb)) {
				// make sure there is at least one node in connected set
				if (todo == size)
					continue;
				// do not yet give up - pick third node at random from connected set plus one of its neighbours
				int idxc = rng.random0n(size-todo);
				int nodec = full[idxc];
				// note: D may or may not be member of full; must not be A or B
				int noded = out[nodec][rng.random0n(kout[nodec])];
				// A-B as well as C-D are connected
				if (noded != nodeb && (!isNeighborOf(nodea, nodec) || !isNeighborOf(nodeb, noded))) {
					// break C-D edge, connect A-C and B-D
					// leaves connectivity of C and D unchanged
					removeEdgeAt(nodec, noded);
					addEdgeAt(nodea, nodec);
					addEdgeAt(nodeb, noded);
				}
				else if (noded != nodea && (!isNeighborOf(nodea, noded) || !isNeighborOf(nodeb, nodec))) {
					// break C-D edge, connect A-D and B-C
					// leaves connectivity of C and D unchanged
					removeEdgeAt(nodec, noded);
					addEdgeAt(nodea, noded);
					addEdgeAt(nodeb, nodec);
				}
				else {
					if (++escape > 10) {
						logger.info("initGeometryDegreeDistr appears stuck - retry");
						return false;
					}
					continue;
				}
			}
			else {
				addEdgeAt(nodea, nodeb);
			}
			escape = 0;
			if (kout[nodea] == degree[nodea]) {
				full[size-todo] = nodea;
				core[idxa] = core[--todo];
				// DEBUG - triggers exceptions to capture some bookkeeping issues
//				core[todo] = -1;
				if (idxb == todo)
					idxb = idxa;
			}
			if (kout[nodeb] == degree[nodeb]) {
				full[size-todo] = nodeb;
				core[idxb] = core[--todo];
				// DEBUG - triggers exceptions to capture some bookkeeping issues
//				core[todo] = -1;
			}
		}
		if (todo == 1) {
			// let's try to fix this
			int nodea = core[0];
logger.info("todo=1: nodea="+nodea+", k="+kout[nodea]+" ("+degree[nodea]+")");
			int idxc = rng.random0n(size-1);
			int nodec = full[idxc];
			int noded = out[nodec][rng.random0n(kout[nodec])];
			// A is single C-D are connected
			if (noded != nodea && (!isNeighborOf(nodea, nodec) || !isNeighborOf(nodea, noded))) {
				// break C-D edge, connect A-C and A-D
				// leaves connectivity of C and D unchanged
				removeEdgeAt(nodec, noded);
				addEdgeAt(nodea, nodec);
				addEdgeAt(nodea, noded);
			}
//			// we are screwed... try again
//			logger.info("generation of degree distribution failed - retry");
//			return false;
		}
//check structure
//evaluateGeometry();
//checkConnections();
//checkConnections(degree);
//end check
		return true;
	}

	/**
	 * initialize (connected, undirected) scale-free network.
	 * 
	 * see Barabási, Albert-László; Albert, Réka (1999) "Emergence of scaling in
	 * random networks", Science. 286 (5439): 509–512.
	 * 
	 * requirements/notes: - does not (yet?) take inter-species interactions into
	 * account.
	 */
	public void initGeometryScaleFreeBA() {
		int nStart, nLinks;
		RNGDistribution rng = population.rng;

		isRewired = false;
		isUndirected = true;
		isRegular = false;
		isLattice = false;
		alloc();

		// create fully connected core graph
		int myLinks = Math.min((int) (connectivity / 2.0 + 0.5), size - 1);
		nStart = Math.max(myLinks, 2);
		for (int i = 1; i < nStart; i++) {
			for (int j = 0; j < i; j++) {
				addLinkAt(i, j);
				addLinkAt(j, i);
			}
		}
		nLinks = nStart * (nStart - 1);

		for (int n = nStart; n < size; n++) {
			for (int i = 0; i < myLinks; i++) {
				// count links of my neighbors
				int[] myNeigh = out[n];
				int nl = 0;
				for (int j = 0; j < i; j++)
					nl += kout[myNeigh[j]];

				int ndice = rng.random0n(nLinks - nl);
				int randnode = -1;
				for (int j = 0; j < n; j++) {
					if (isNeighborOf(n, j))
						continue;
					ndice -= kout[j];
					if (ndice < 0) {
						randnode = j;
						break;
					}
				}
				addLinkAt(n, randnode);
				addLinkAt(randnode, n);
				nLinks++; // add only new link of randnode
			}
			nLinks += myLinks; // only now add links of new node
		}
	}

	/**
	 * initialize (connected, undirected) scale-free network with small-worl
	 * properties.
	 * 
	 * see Konstantin Klemm and Víctor M. Eguíluz (2001) Growing scale-free networks
	 * with small-world behavior, Phys. Rev. E 65, 057102. doi:
	 * 10.1103/PhysRevE.65.057102
	 * 
	 * requirements/notes: - effective connectivity is k*(N-1)/N where k is the
	 * desired average connectivity and N is the population size - add bookkeeping
	 * to optimize creation time
	 */
	public void initGeometryScaleFreeKlemm() {
		int nStart;
		RNGDistribution rng = population.rng;

		isRewired = false;
		isUndirected = true;
		isRegular = false;
		isLattice = false;
		alloc();

		// create fully connected graph to start with
		int nActive = Math.min((int) (connectivity / 2.0 + 0.5), size);
		int[] active = new int[nActive];
		for (int i = 0; i < nActive; i++)
			active[i] = i;

		nStart = Math.max(nActive, 2);
		for (int i = 1; i < nStart; i++) {
			for (int j = 0; j < i; j++) {
				addLinkAt(i, j);
				addLinkAt(j, i);
			}
		}

		nextnode: for (int n = nStart; n < size; n++) {

			if (pUndirLinks < 1e-8) {
				// connect to active node
				for (int i = 0; i < nActive; i++) {
					addLinkAt(n, active[i]);
					addLinkAt(active[i], n);
				}
			} else {
				for (int i = 0; i < nActive; i++) {
					if (pUndirLinks > (1.0 - 1e-8) || rng.random01() < pUndirLinks) {
						// linear preferential attachment - count links
						int randnode;
						int links = 0;
//XXX some clever bookkeeping should spare us this loop!!!
						for (int j = 0; j < n; j++)
							links += kout[j];
						// avoid double connections
						do {
							int ndice = rng.random0n(links);
							randnode = -1;
							for (int j = 0; j < n; j++) {
								ndice -= kout[j];
								if (ndice < 0) {
									randnode = j;
									break;
								}
							}
						}
						// test produces an error if randnode is still -1
						// test relies on bi-directionality of graph
						// new node n may not have a neighbor yet - treat carefully!
						while (isNeighborOf(n, randnode));

						addLinkAt(n, randnode);
						addLinkAt(randnode, n);
					} else { // connect to active node
						addLinkAt(n, active[i]);
						addLinkAt(active[i], n);
					}
				}
			}
			// norm must be calculated separately because the random linking stuff
			// could change the number of links of the active nodes
//XXX some clever bookkeeping should spare us this loop!!!
			double norm = 0.0;
			for (int i = 0; i < nActive; i++)
				norm += 1.0 / kout[active[i]];
			// treat new node as active
			double hitNew = 1.0 / kout[n];
			// deactivate one node and replace by new node
			double dice = rng.random01() * (norm + hitNew) - hitNew;
			// new node removed?
			if (dice < 0.0)
				continue nextnode; // this leaves active node list unchanged
			for (int i = 0; i < nActive; i++) {
				dice -= 1.0 / kout[active[i]];
				if (dice < 0.0) {
					active[i] = n;
					continue nextnode;
				}
			}
			// we should not arrive here - scream!
			throw new Error("Emergency in scale-free network creation...");
		}
	}

	/**
	 * add/rewire random links
	 */
	public void rewire() {
		if (geometry == MEANFIELD)
			return;
		if (pUndirLinks > 0.0 && geometry != SCALEFREE_KLEMM) {
			if (addUndirLinks) {
				addUndirected();
				isRegular = false;
				isLattice = false;
			} else {
				if (!rewireUndirected()) {
					logger.warning("undirected rewiring failed...");
				}
			}
			isRewired = true;
		}

		if (pDirLinks > 0.0) {
			if (addDirLinks) {
				addDirected();
				isRegular = false;
				isLattice = false;
			} else {
				if (!rewireDirected()) {
					logger.warning("directed rewiring failed...");
				}
			}
			isRewired = true;
			isUndirected = false;
		}
	}

	/**
	 * rewire undirected links
	 * 
	 * requirements/notes: - requires an initially undirected graph - rewiring
	 * preserves connectivity of all nodes - resulting graph obviously remains
	 * undirected
	 * 
	 * @return false if rewiring failed
	 */
	public boolean rewireUndirected() {
		// is rewiring possible?
		if (!isUndirected)
			return false;

		switch ((int) (connectivity + 1e-6)) {
			case 1: // k=1-2 don't even try
				logger.severe("rewiring needs higher connectivity (should be >3 instead of "
						+ Formatter.format(connectivity, 2) + ")");
				return false;
			case 2: // k=2-3 challenging
				logger.warning("consider higher connectivity for rewiring (should be >3 instead of "
						+ Formatter.format(connectivity, 2) + ")");
				break;
		}
		if (connectivity > size / 2) {
			logger.warning("consider lower connectivity for rewiring (" + Formatter.format(connectivity, 2) + ")");
			return false;
		}
		if (connectivity > size - 2) {
			logger.severe("complete graph, rewiring impossible");
			return false;
		}

		RNGDistribution rng = population.rng;

		// rewire at most the number of links present in the system (corresponds to a
		// fraction of 1-1/e (~63%) of links rewired)
		long nLinks = (long) Math
				.floor((int) (avgOut * size + 0.5) / 2.0 * Math.min(1.0, -Math.log(1.0 - pUndirLinks)) + 0.5);
		long done = 0;
		int first, firstneigh, second, secondneigh, len;
		while (done < nLinks) {
			// draw first node - avoid sources (nodes without inlinks), leaves and fully
			// connected nodes
			do {
				first = rng.random0n(size);
				len = kin[first];
			} while (len <= 1 || len == size - 1);
			// choose random neighbor
			firstneigh = in[first][rng.random0n(len)];

			// draw second node - avoid source, leaves and fully connected nodes
			// in addition, firstneigh and second must not be neighbors
			do {
				second = rng.random0n(size - 1);
				if (second >= first)
					second++;
				len = kin[second];
			} while (len <= 1 || len == size - 1);
			// choose random neighbor
			secondneigh = in[second][rng.random0n(len)];

			if (!swapEdges(first, firstneigh, second, secondneigh))
				continue;
			if (!isGraphConnected()) {
				swapEdges(first, firstneigh, second, secondneigh);
				swapEdges(first, secondneigh, second, firstneigh);
			}
			done += 2;
		}
		return true;
	}

	private boolean evaluated = false;

	/**
	 * evaluate geometry; set min/max/average of incoming/outgoing/total link counts
	 */
	public void evaluate() {
		if (evaluated && !isDynamic)
			return;

		// determine minimum, maximum and average connectivities
		if (geometry == MEANFIELD || kout == null || kin == null) {
			maxOut = 0;
			maxIn = 0;
			maxTot = 0;
			minOut = 0;
			minIn = 0;
			minTot = 0;
			avgOut = 0.0;
			avgIn = 0.0;
			avgTot = 0.0;
			evaluated = true;
			return;
		}
		maxOut = -1;
		maxIn = -1;
		maxTot = -1;
		minOut = Integer.MAX_VALUE;
		minIn = Integer.MAX_VALUE;
		minTot = Integer.MAX_VALUE;
		long sumin = 0, sumout = 0, sumtot = 0;

//XXX could be improved for undirected geometries - worth the effort and additional maintenance? probably not...
		for (int n = 0; n < size; n++) {
			int lout = kout[n];
			maxOut = Math.max(maxOut, lout);
			minOut = Math.min(minOut, lout);
			sumout += lout;
			int lin = kin[n];
			// use ChHMath.max(geom.maxIn)?
			maxIn = Math.max(maxIn, lin);
			minIn = Math.min(minIn, lin);
			sumin += lin;
			int ltot = lout + lin;
			maxTot = Math.max(maxTot, ltot);
			minTot = Math.min(minTot, ltot);
			sumtot += ltot;
		}
		avgOut = (double) sumout / (double) size;
		avgIn = (double) sumin / (double) size;
		avgTot = (double) sumtot / (double) size;
		evaluated = true;
	}

	/**
	 * Utility method to determine whether a given geometry type is a lattice.
	 * 
	 * @param geometry the type of geometry to check whether it is a lattice
	 * @return <code>true</code> if <code>geometry</code> is lattice,
	 *         <code>false</code> otherwise
	 */
	static public boolean isLattice(int geometry) {
		switch (geometry) {
			case LINEAR:
			case SQUARE:
			case SQUARE_MOORE:
			case SQUARE_NEUMANN:
			case HONEYCOMB:
			case TRIANGULAR:
			case CUBE:
			case COMPLETE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * check if graph is connected.
	 * 
	 * requirements/notes: - only works for undirected graphs
	 * 
	 * @return true if graph is connected
	 */
	public boolean isGraphConnected() {
		boolean[] check = new boolean[size];
		Arrays.fill(check, false);
		isGraphConnected(0, check);

		for (int n = 0; n < size; n++)
			if (!check[n])
				return false;
		return true;
	}

	/**
	 * check if any other node can be reached from <code>node</code>.
	 * 
	 * requirements/notes: - only works for undirected graphs
	 * 
	 * @param node
	 * @param check
	 */
	public void isGraphConnected(int node, boolean[] check) {
		check[node] = true;
		int[] neighs = out[node];
		int len = kout[node];
		for (int i = 0; i < len; i++) {
			int nn = neighs[i];
			if (!check[nn])
				isGraphConnected(nn, check);
		}
	}

	/**
	 * utility method to swap edges (undirected links). change link a-an to a-bn and
	 * b-bn to b-an
	 * 
	 * requirements/notes: - does the same as rewireEdgeAt(a, bn, an);
	 * rewireEdgeAt(b, an, bn); without allocating and freeing memory
	 * 
	 * @param a
	 * @param an
	 * @param b
	 * @param bn
	 * @return false if swap failed (impossible)
	 */
	private boolean swapEdges(int a, int an, int b, int bn) {
		if (a == bn || b == an || an == bn)
			return false;
		if (isNeighborOf(a, bn) || isNeighborOf(b, an))
			return false;

		int[] aout = out[a];
		int ai = -1;
		while (aout[++ai] != an)
			;
		aout[ai] = bn;
		int[] bout = out[b];
		int bi = -1;
		while (bout[++bi] != bn)
			;
		bout[bi] = an;

		int[] ain = in[a];
		ai = -1;
		while (ain[++ai] != an)
			;
		ain[ai] = bn;
		int[] bin = in[b];
		bi = -1;
		while (bin[++bi] != bn)
			;
		bin[bi] = an;

		aout = out[an];
		ai = -1;
		while (aout[++ai] != a)
			;
		aout[ai] = b;
		bout = out[bn];
		bi = -1;
		while (bout[++bi] != b)
			;
		bout[bi] = a;

		ain = in[an];
		ai = -1;
		while (ain[++ai] != a)
			;
		ain[ai] = b;
		bin = in[bn];
		bi = -1;
		while (bin[++bi] != b)
			;
		bin[bi] = a;

		return true;
	}

//	private void printConnections() {
//		for( int n=0; n<size; n++ ) {
//            String msg = n+": ";
//			int k = kout[n];
//			for( int i=0; i<k; i++ )
//				msg += out[n][i]+" ";
//			EvoLudo.logDebug(msg);
//		}
//	}

//	private boolean checkConnections(int[] degdist) {
//		boolean ok = true;
//
//		logger.fine("Checking degree distribution... ");
//		for( int i=0; i<size; i++ ) {
//			//connectivity - degree distribution
//			if( degdist[i] != kout[i] ) {
//				ok = false;
//				logger.fine("Node "+i+" has "+kout[i]+" out-neighbors... ("+degdist[i]+")");
//			}
//			if( degdist[i] != kin[i] ) {
//				ok = false;
//				logger.fine("Node "+i+" has "+kin[i]+" in-neighbors... ("+degdist[i]+")");
//			}
//		}
//		logger.fine("Degree distribution check: "+(ok?"success!":"failed!"));
//		return (ok & checkConnections());
//	}

	/**
	 * check consistency of links.
	 * 
	 * notes: 1) self connections are unacceptable. 2) double links between nodes
	 * are unacceptable. 3) in undirected networks every outgoing link must
	 * correspond to an incoming link.
	 * 
	 * todo: - "self-connections" are acceptable for inter-species interactions
	 * 
	 * @return false if check failed.
	 */
	public boolean checkConnections() {
		boolean ok = true, allOk = true;

		logger.fine("Checking multiple out-connections... ");
		for (int i = 0; i < size; i++) {
			// double connections 'out'
			int nout = kout[i];
			for (int j = 0; j < nout; j++) {
				int idx = out[i][j];
				for (int k = j + 1; k < nout; k++)
					if (out[i][k] == idx) {
						ok = false;
						logger.fine("Node " + i + " has double out-connection with node " + idx);
					}
			}
		}
		logger.fine("Multiple out-connections check: " + (ok ? "success!" : "failed!"));
		allOk &= ok;
		ok = true;
		logger.fine("Checking multiple in-connections... ");
		for (int i = 0; i < size; i++) {
			// double connections 'in'
			int nin = kin[i];
			for (int j = 0; j < nin; j++) {
				int idx = in[i][j];
				for (int k = j + 1; k < nin; k++)
					if (in[i][k] == idx) {
						ok = false;
						logger.fine("Node " + i + " has double in-connection with node " + idx);
					}
			}
		}
		logger.fine("Multiple in-connections check: " + (ok ? "success!" : "failed!"));
		allOk &= ok;
		ok = true;
		logger.fine("Checking consistency of in-, out-connections... ");
		for (int i = 0; i < size; i++) {
			// each 'out' connection must be balanced by an 'in' connection
			int[] outi = out[i];
			int nout = kout[i];
			nextlink: for (int j = 0; j < nout; j++) {
				int[] ini = in[outi[j]];
				int nin = kin[outi[j]];
				for (int k = 0; k < nin; k++)
					if (ini[k] == i)
						continue nextlink;
				ok = false;
				logger.fine("Node " + i + " has 'out'-link to node " + outi[j]
						+ ", but there is no corresponding 'in'-link");
			}
		}
		logger.fine("Consistency of in-, out-connections check: " + (ok ? "success!" : "failed!"));
		allOk &= ok;
		ok = true;
		logger.fine("Checking for loops (self-connections) in in-, out-connections... ");
		for (int i = 0; i < size; i++) {
			// report loops
			int[] outi = out[i];
			int nout = kout[i];
			for (int j = 0; j < nout; j++) {
				if (outi[j] == i) {
					ok = false;
					logger.fine("Node " + i + " has loop in 'out'-connections");
				}
			}
			int[] ini = in[i];
			int nin = kin[i];
			for (int j = 0; j < nin; j++) {
				if (ini[j] == i) {
					ok = false;
					logger.fine("Node " + i + " has loop in 'in'-connections");
				}
			}
		}
		logger.fine("Self-connections check: " + (ok ? "success!" : "failed!"));
		allOk &= ok;
		ok = true;
		if (isRegular) {
			logger.fine("Checking regularity... ");
			int nout = minOut;
			int nin = minIn;
			for (int i = 0; i < size; i++) {
				if (kout[i] != nout) {
					ok = false;
					logger.fine("Node " + i + " has wrong 'out'-link count - " + kout[i] + " instead of " + nout);
				}
				if (kin[i] != nin) {
					ok = false;
					logger.fine("Node " + i + " has wrong 'in'-link count - " + kin[i] + " instead of " + nin);
				}
			}
			logger.fine("Regularity check: " + (ok ? "success!" : "failed!"));
			allOk &= ok;
			ok = true;
		}
		if (isUndirected) {
			logger.fine("Checking undirected structure... ");
			for (int i = 0; i < size; i++) {
				// each connection must go both ways
				int[] outa = out[i];
				int nouta = kout[i];
				nextout: for (int j = 0; j < nouta; j++) {
					int[] outb = out[outa[j]];
					int noutb = kout[outa[j]];
					for (int k = 0; k < noutb; k++)
						if (outb[k] == i)
							continue nextout;
					ok = false;
					logger.fine("Node " + i + " has 'out'-link to node " + outa[j] + ", but not vice versa");
				}
				int[] ina = in[i];
				int nina = kin[i];
				nextin: for (int j = 0; j < nina; j++) {
					int[] inb = in[ina[j]];
					int ninb = kin[ina[j]];
					for (int k = 0; k < ninb; k++)
						if (inb[k] == i)
							continue nextin;
					ok = false;
					logger.fine("Node " + i + " has 'in'-link to node " + ina[j] + ", but not vice versa");
				}
			}
			logger.fine("Undirected structure check: " + (ok ? "success!" : "failed!"));
			allOk &= ok;
		}
		return allOk;
	}

	/**
	 * add undirected links
	 * 
	 * @return false if addition of further links failed
	 */
	public boolean addUndirected() {
		switch (geometry) {
			// everything is already connected
			case COMPLETE:
				return false;
		}
		RNGDistribution rng = population.rng;

//		long nLinks = (long)Math.floor(-linkCount(geom)/2.0*Math.log(1.0-geom.pUndirLinks)+0.5);
//		long nLinks = (long)Math.floor(-(int)(geom.avgOut*size+0.5)/2.0*Math.log(1.0-geom.pUndirLinks)+0.5);
		// add at most the number of links already present in the system
		int nLinks = (int) Math.floor(avgOut * size * pUndirLinks / 2.0 + 0.5);
		int from, to;
		while (nLinks > 0) {
			from = rng.random0n(size);
			to = rng.random0n(size - 1);
			if (to >= from)
				to++; // avoid self-connections
			if (isNeighborOf(from, to))
				continue; // avoid double connections
			addEdgeAt(from, to);
			nLinks--;
		}
		return true;
	}

	/**
	 * rewire directed links
	 * 
	 * requirements/notes: - only an initially undirected graph is guaranteed to
	 * remain connected - rewiring preserves connectivity of all nodes (both inlinks
	 * and outlinks) - resulting graph is obviously directed (even if original was
	 * directed)
	 * 
	 * todo: - should be rewritten similar to rewireUndirected()!!!
	 * 
	 * @return false if rewiring failed
	 */
	public boolean rewireDirected() {
		switch (geometry) {
			// do not attempt to rewire star structures
			// rewiring while preserving connectivities is impossible!
			case STAR:
				return false;
			// everything is already connected
			case COMPLETE:
				return false;
		}
		RNGDistribution rng = population.rng;

// make sure the right fraction of original links is replaced!
//		long nLinks = (long)Math.floor(-linkCount()*Math.log(1.0-pDirLinks)+0.5);
// it should not matter whether we use avgOut or avgIn - check!
//		long nLinks = (long)Math.floor(-(int)(avgOut*size+0.5)*Math.log(1.0-pDirLinks)+0.5);
		// rewire at most the number of directed links present in the system
		// (corresponds to a fraction of 1-1/e (~63%) of links rewired)
		int nLinks = (int) Math.floor((int) (avgOut * size + 0.5) * Math.min(1.0, -Math.log(1.0 - pDirLinks)) + 0.5);
		int done = 0;
		int last = -1, prev, from, to = -1, len, neigh;
		isUndirected = false;
		do {
			// draw first node - avoid sources (nodes without inlinks) and fully connected
			// nodes
			do {
				last = rng.random0n(size);
				len = kin[last];
			} while (len == 0 || len == size - 1);
			neigh = len == 1 ? 0 : rng.random0n(len);
			from = in[last][neigh]; // link used to come from here
			// note that 'from' must have at least one outlink to 'last'.
			if (kout[from] == size - 1)
				continue; // already linked to everybody else
			// draw random node 'to' that is not a neighbor of 'from' (avoid double
			// connections)
			// in addition, 'to' must not be a source
			do {
				to = rng.random0n(size - 1);
				if (to >= from)
					to++;
			} while (isNeighborOf(from, to) || kin[to] == 0);
			// 'from' -> 'last' rewired to 'from' -> 'to'
			rewireLinkAt(from, to, last);
			done++;

			// rewiring is tricky if there are few highly connected hubs and many nodes with
			// few (single) connections
			// the following may still get stuck...
			while (done < nLinks) {
				// 'to' just got a new inlink -> len>1
				len = kin[to];
				// draw random neighbor of 'to' but exclude newly drawn link
				neigh = (len - 1) == 1 ? 0 : rng.random0n(len - 1);
				from = in[to][neigh]; // link used to come from here
				// is 'from' already linked to everyone else?
				if (kout[from] == size - 1) {
					// are there other feasible neighbors?
					for (int n = 0; n < len - 1; n++)
						if (kout[in[to][n]] < size - 1)
							continue; // there is hope...
					// this looks bad - try node we just came from
					if (kout[in[to][len - 1]] == size - 1) {
						throw new Error("Rewiring troubles - giving up...");
					}
					// let's go back - can this fail?
					from = in[to][len - 1];
				}
				prev = to;
				// draw random node 'to' that is not a neighbor of 'from' (avoid double
				// connections)
				// in addition, 'to' must not be a source
				do {
					to = rng.random0n(size - 1);
					if (to >= from)
						to++;
				} while (isNeighborOf(from, to) || kin[to] == 0);
				// 'from' -> 'prev' rewired to 'from' -> 'to'
				rewireLinkAt(from, to, prev);
				done++;
				if (to == last)
					break;
			}
		} while (nLinks - done > 1); // this accounts for the last link(s)

		// if 'from' happens to be the origin we are done
		if (to == last)
			return true;

		// draw last link from origin to next, if they are already neighbors,
		// then rewire additional links to preserve connectivity
		while (isNeighborOf(last, to)) {
			// 'to' just got a new inlink -> len>1
			len = kin[to];
			// draw random neighbor of 'to' but exclude newly drawn link
			neigh = (len - 1) == 1 ? 0 : rng.random0n(len - 1);
			from = in[to][neigh]; // link used to come from here
			if (kout[from] == size - 1)
				continue; // already linked to everybody else
			prev = to;
			// draw random node 'to' that is not a neighbor of 'from' (avoid double
			// connections)
			// in addition, 'to' must not be a source
			do {
				to = rng.random0n(size - 1);
				if (to >= from)
					to++;
			} while (isNeighborOf(from, to) || kin[to] == 0);
			rewireLinkAt(from, to, prev);
		}
		// 'to' just got a new inlink -> len>1
		len = kin[to];
		// draw random neighbor of 'to' but exclude last drawn link
		neigh = (len - 1) == 1 ? 0 : rng.random0n(len - 1);
		rewireLinkAt(to, last, in[to][neigh]);
		return true;
	}

	/**
	 * add directed links to network
	 *
	 * @return
	 */
	public boolean addDirected() {
		switch (geometry) {
			// everything is already connected
			case COMPLETE:
				return false;
		}
		RNGDistribution rng = population.rng;

//		long nLinks = (long)Math.floor(-linkCount(geom)*Math.log(1.0-geom.pDirLinks)+0.5);
//		long nLinks = (long)Math.floor(-(int)(geom.avgOut*size+0.5)*Math.log(1.0-geom.pDirLinks)+0.5);
		// add at most the number of directed links already present in the system
		int nLinks = (int) Math.floor(avgOut * size * pDirLinks + 0.5);
		int from, to;
		while (nLinks > 0) {
			from = rng.random0n(size);
			to = rng.random0n(size - 1);
			if (to >= from)
				to++; // avoid self-connections
			if (isNeighborOf(from, to))
				continue; // avoid double connections
			addLinkAt(from, to);
			nLinks--;
		}
		return true;
	}

	/*
	 *
	 * methods to create/edit/test population structure
	 *
	 */
	/**
	 * add edge (undirected link) from vertex <code>from</code> to vertex
	 * <code>to</code>.
	 * 
	 * @param from
	 * @param to
	 */
	public void addEdgeAt(int from, int to) {
		addLinkAt(from, to);
		addLinkAt(to, from);
	}

	/**
	 * add directed link from vertex <code>from</code> to vertex <code>to</code>.
	 * allocate new memory if required and notify listeners (required to update
	 * network).
	 * 
	 * @param from
	 * @param to
	 */
	public void addLinkAt(int from, int to) {
		int[] mem = out[from];
		int max = mem.length;
		int ko = kout[from];
		if (max <= ko) {
			int[] newmem = new int[max + 10];
			if (max > 0)
				System.arraycopy(mem, 0, newmem, 0, max);
			out[from] = newmem;
			mem = newmem;
		}
		mem[ko] = to;
		kout[from]++;
		ko++;
		if (ko > maxOut)
			maxOut = ko;

		// incoming
		mem = in[to];
		max = mem.length;
		int ki = kin[to];
		if (max <= ki) {
			int[] newmem = new int[max + 10];
			if (max > 0)
				System.arraycopy(mem, 0, newmem, 0, max);
			in[to] = newmem;
			mem = newmem;
		}
		mem[ki] = from;
		kin[to]++;
		ki++;
		if (ki > maxIn)
			maxIn = ki;
//		if( ko+ki>geom.maxTot ) geom.maxTot = ko+ki;
		maxTot = Math.max(maxTot, ko + kin[from]);
		maxTot = Math.max(maxTot, kout[to] + ki);
		evaluated = false;
	}

	/**
	 * remove edge (undirected link) from vertex <code>from</code> to vertex
	 * <code>to</code>.
	 * 
	 * notes: - does not update maxIn, maxOut or maxTot...
	 * 
	 * @param from
	 * @param to
	 */
	public void removeEdgeAt(int from, int to) {
		removeLinkAt(from, to);
		removeLinkAt(to, from);
	}

	/**
	 * remove directed link from vertex <code>from</code> to vertex <code>to</code>.
	 * 
	 * notes: - does not update maxIn, maxOut or maxTot...
	 *
	 * @param from
	 * @param to
	 */
	public void removeLinkAt(int from, int to) {
		removeInLink(from, to);
		removeOutLink(from, to);
	}

	/**
	 * remove all outgoing links from node <code>idx</code>.
	 * 
	 * @param idx
	 */
	public void clearLinksFrom(int idx) {
		// remove in-links
		int len = kout[idx];
		int[] neigh = out[idx];
		for (int i = 0; i < len; i++)
			removeInLink(idx, neigh[i]);
		// clear out-links
		kout[idx] = 0;
		minOut = 0;
		// could free some memory too...
	}

	/**
	 * remove incoming link to node <code>to</code> from node <code>from</code>.
	 * 
	 * @param from
	 * @param to
	 */
	private void removeInLink(int from, int to) {
		// find index
		int idx = -1;
		int[] mem = in[to];
		int k = kin[to];
		for (int i = 0; i < k; i++)
			if (mem[i] == from) {
				idx = i;
				break;
			}
		if (idx < 0)
			return; // not found - ignore
		// remove links - do not shrink array
		System.arraycopy(mem, idx + 1, mem, idx, k - 1 - idx);
		kin[to]--;
		if (k - 1 < minIn)
			minIn = k - 1;
		evaluated = false;
	}

	/**
	 * remove all incoming links to node <code>idx</code>.
	 * 
	 * @param idx
	 */
	public void clearLinksTo(int idx) {
		// remove out-links
		int len = kin[idx];
		int[] neigh = in[idx];
		for (int i = 0; i < len; i++)
			removeOutLink(neigh[i], idx);
		// clear in-links
		kin[idx] = 0;
		minIn = 0;
		// could free some memory too...
	}

	/**
	 * remove outgoing link from node <code>from</code> to node <code>to</code>.
	 * 
	 * @param from
	 * @param to
	 */
	private void removeOutLink(int from, int to) {
		// find index
		int idx = -1;
		int[] mem = out[from];
		int k = kout[from];
		for (int i = 0; i < k; i++)
			if (mem[i] == to) {
				idx = i;
				break;
			}
		if (idx < 0)
			return; // not found - ignore
		// remove links - do not shrink array
		System.arraycopy(mem, idx + 1, mem, idx, k - 1 - idx);
		kout[from]--;
		if (k - 1 < minOut)
			minOut = k - 1;
		evaluated = false;
	}

	/**
	 * rewire directed link from node <code>from</code> to node <code>prev</code> to
	 * node <code>to</code>.
	 * 
	 * @param from
	 * @param to
	 * @param prev
	 */
	public void rewireLinkAt(int from, int to, int prev) {
		removeLinkAt(from, prev);
		addLinkAt(from, to);
	}

	/**
	 * rewire edge (undirected link) from node <code>from</code> to node
	 * <code>prev</code> to node <code>to</code>.
	 *
	 * @param from
	 * @param to
	 * @param prev
	 */
	public void rewireEdgeAt(int from, int to, int prev) {
		rewireLinkAt(from, to, prev);
		removeLinkAt(prev, from);
		addLinkAt(to, from);
	}

	/**
	 * Check if 'focal' and 'check' are neighbors (not necessarily the other way
	 * round). For undirected networks 'focal' and 'check' can be exchanged.
	 *
	 * @param focal index of focal individual
	 * @param check index of individual to be checked
	 * @return true if 'check' is neighbor of 'focal'
	 */
	public boolean isNeighborOf(int focal, int check) {
		int[] neigh = out[focal];
		int k = kout[focal];
		for (int n = 0; n < k; n++)
			if (neigh[n] == check)
				return true;
		return false;
	}

	/**
	 * Derive interaction geometry from current (reproduction) geometry. This is
	 * only possible if interReproSame is true. Returns null otherwise. If
	 * opp==population then it is a intra-species interaction, which allows to
	 * simply return 'this' (no cloning etc required). Otherwise the geometry is
	 * cloned, the opponent set and 'self-loops' added for interactions with
	 * individuals in same location.
	 *
	 * @param opp population of interaction partners
	 * @return derived interaction geometry or null if it cannot be derived
	 */
	public Geometry deriveInteractionGeometry(Population opp) {
		// this is reproduction geometry (hence population==opponent)
		if (!interReproSame)
			return null; // impossible to derive interaction geometry
		// intra-species interactions: nothing to derive - use same geometry
		if (population == opp)
			return this;
		Geometry interaction = clone();
		interaction.opponent = opp;
		// add interactions with individual in same location
		for (int n = 0; n < size; n++)
			interaction.addLinkAt(n, n);
		interaction.evaluate();
		return interaction;
	}

	/**
	 * Derive reproduction geometry from current (interaction) geometry. This is
	 * only possible if interReproSame is true. Returns null otherwise. If
	 * opponent==population then it is a intra-species interaction, which allows to
	 * simply return 'this' (no cloning etc required). Otherwise the geometry is
	 * cloned, the opponent set and 'self-loops' removed.
	 *
	 * @return derived interaction geometry or null if it cannot be derived
	 */
	public Geometry deriveReproductionGeometry() {
		// this is interaction geometry (hence population!=opponent for inter-species
		// interactions)
		if (!interReproSame)
			return null; // impossible to derive reproduction geometry
		// intra-species interactions: nothing to derive - use same geometry
		if (population == opponent)
			return this;
		Geometry reproduction = clone();
		reproduction.opponent = population;
		// add interactions with individual in same location
		if (reproduction.geometry != MEANFIELD)
			for (int n = 0; n < size; n++)
				reproduction.removeLinkAt(n, n);
		reproduction.evaluate();
		return reproduction;
	}

	/**
	 * parse geometry specifications <code>cli</code>. check for consistency of
	 * settings (e.g. in terms of population size, connectivity) and make
	 * appropriate adjustments if applicable and possible.
	 * 
	 * @param cli
	 * @return true if reset is required
	 */
	public boolean parse(String cli) {
		boolean doReset = false;
		// NOTE: allow to provide a number for the geometry until we have a more elegant
		// solution (for now, no other arguments are allowed,
		// i.e. connectivity or the like)
		int geomtype = cli.charAt(0);
		String sub = cli.substring(1);
		if (Character.isDigit((char) geomtype)) {
			// add prevent overlap with specifying geometries by a char we add
			// 256.
			geomtype = CLOption.NUMBERED_KEY_OFFSET + Integer.parseInt(cli);
			sub = "";
		}
		// check validity of requested geomtype only later to avoid hassles with
		// variants
		boolean oldFixedBoundary = fixedBoundary;
		fixedBoundary = false;
		if (sub.length() > 0) {
			// fixed boundaries for regular lattices
			if (sub.charAt(0) == 'f' || sub.charAt(0) == 'F') {
				fixedBoundary = true;
				sub = sub.substring(1);
			}
		}
		doReset |= (oldFixedBoundary != fixedBoundary);

		int[] ivec;
		double[] dvec;
		int oldGeometry = geometry;
		double oldConnectivity = connectivity;
		geometry = geomtype;
		int oldSubGeometry = subgeometry;
		subgeometry = VOID;
		switch (geometry) {
			case MEANFIELD: // mean field
				break;
			case COMPLETE: // complete graph
				break;
			case HIERARCHY: // deme structured, hierarchical graph
				subgeometry = MEANFIELD;
				if (!Character.isDigit(sub.charAt(0))) {
					// check for geometry of hierarchies
//XXX should we allow different geometries at different levels? (e.g. well-mixed demes in spatial arrangement would make sense)
					subgeometry = sub.charAt(0);
					sub = sub.substring(1);
					// check once more for fixed boundaries
					if (sub.charAt(0) == 'f' || sub.charAt(0) == 'F') {
						fixedBoundary = true;
						sub = sub.substring(1);
					}
				}
				if (oldSubGeometry != subgeometry || oldFixedBoundary != fixedBoundary)
					doReset = true;

//XXX Geometry is capable of dealing with arbitrary square lattices but how to specify connectivity? first number?
				int[] oldRawHierarchy = rawhierarchy;
				// H[n,m[f]]<n0>[:<n1>[:<n2>[...]]]w<d> where <ni> refers to the number of units
				// in level i and
				// <d> to the weight of the linkage between subsequent levels
				int widx = sub.lastIndexOf('w');
				if (widx < 0) {
					// 'w' not found - no coupling between hierarchies (identical to isolated demes)
					hierarchyweight = 0;
					rawhierarchy = CLOParser.parseIntVector(sub);
				} else {
					hierarchyweight = CLOParser.parseDouble(sub.substring(widx + 1));
					rawhierarchy = CLOParser.parseIntVector(sub.substring(0, widx));
				}
				if (oldRawHierarchy != null)
					doReset |= (ArrayMath.norm(ArrayMath.sub(oldRawHierarchy, rawhierarchy)) > 0);
				break;
			case LINEAR: // linear
				int[] conn = CLOParser.parseIntVector(sub);
				switch (conn.length) {
					default:
						logger.warning("too many arguments for linear geometry.");
						//$FALL-THROUGH$
					case 2:
						connectivity = conn[0] + conn[1];
						linearAsymmetry = conn[0] - conn[1];
						break;
					case 1:
						connectivity = conn[0];
						//$FALL-THROUGH$
					case 0:
						connectivity = Math.max(2, connectivity);
						linearAsymmetry = 0;
				}
				break;
			case SQUARE_NEUMANN: // von neumann
				connectivity = 4;
				geometry = SQUARE;
				break;
			case SQUARE_MOORE: // moore
				connectivity = 8;
				geometry = SQUARE;
				break;
			case SQUARE: // square, larger neighborhood
				if (sub.length() < 1)
					sub = "4"; // default
				//$FALL-THROUGH$
			case CUBE: // cubic, larger neighborhood
			case HONEYCOMB: // hexagonal
				if (sub.length() < 1)
					sub = "6"; // default
				//$FALL-THROUGH$
			case TRIANGULAR: // triangular
				if (sub.length() < 1)
					sub = "3"; // default
				// allow any connectivity - check() ensures validity
				connectivity = Integer.parseInt(sub);
				break;
			case FRUCHT: // Frucht graph
			case TIETZE: // Tietze graph
			case FRANKLIN: // Franklin graph
			case HEAWOOD: // Heawood graph
			case DODEKAHEDRON: // Dodekahedron graph
			case DESARGUES: // Desargues graph
				connectivity = 3;
				break;
			case ICOSAHEDRON: // Icosahedron graph
				connectivity = 5;
				break;
			case RANDOM_REGULAR_GRAPH: // random regular graph
				connectivity = Math.max(2, Integer.parseInt(sub));
				break;
			case RANDOM_GRAPH: // random graph
				connectivity = Math.max(2, Integer.parseInt(sub));
				break;
			case RANDOM_REGULAR_GRAPH_DIRECTED: // random regular graph directed
				connectivity = Math.max(2, Integer.parseInt(sub));
				break;
			case RANDOM_GRAPH_DIRECTED: // random graph directed
				connectivity = Math.max(2, Integer.parseInt(sub));
				break;
			case STAR: // star
				petalsamplification = 2;
				break;
			case WHEEL: // wheel - cycle (k=2) with single hub (k=N-1)
				break;
			case 'P': // petals - for compatibility
				geometry = Geometry.SUPER_STAR;
				//$FALL-THROUGH$
			case SUPER_STAR: // super-star
				int oldPetalsAmplification = petalsamplification;
				int oldPetalsCount = petalscount;
				petalsamplification = 3;
				ivec = CLOParser.parseIntVector(sub);
				switch (ivec.length) {
					default:
					case 2:
						petalsamplification = ivec[1];
						//$FALL-THROUGH$
					case 1:
						petalscount = ivec[0];
						break;
					case 0:
						geometry = Geometry.STAR; // too few parameters, change to star geometry
				}
				doReset |= (oldPetalsAmplification != petalsamplification);
				doReset |= (oldPetalsCount != petalscount);
				break;
			case STRONG_AMPLIFIER: // strong amplifier
			case STRONG_SUPPRESSOR: // strong suppressor
				// known geometries but no further settings required
				break;
			case SCALEFREE_BA: // scale-free network - barabasi & albert
				connectivity = Math.max(2, Integer.parseInt(sub));
				break;
			case SCALEFREE_KLEMM: // scale-free network - klemm
				double oldPUndirLinks = pUndirLinks;
				pUndirLinks = 0.0;
				dvec = CLOParser.parseVector(sub);
				switch (dvec.length) {
					default:
					case 2:
						pUndirLinks = dvec[1];
						//$FALL-THROUGH$
					case 1:
						connectivity = Math.max(2, (int) dvec[0]);
						break;
					case 0:
						geometry = INVALID; // too few parameters, change to default geometry
				}
				doReset |= (oldPUndirLinks != pUndirLinks);
				break;
			case SCALEFREE: // scale-free network - uncorrelated, from degree distribution
				double oldSfExponent = sfExponent;
				sfExponent = 0.0;
				dvec = CLOParser.parseVector(sub);
				switch (dvec.length) {
					default:
					case 2:
						sfExponent = dvec[1];
						//$FALL-THROUGH$
					case 1:
						connectivity = Math.max(2, (int) dvec[0]);
						break;
					case 0:
						geometry = INVALID; // too few parameters, change to default geometry
				}
				doReset |= (oldSfExponent != sfExponent);
				break;
			/*
			 * not yet implemented... case 'g': // scale-free network - directed
			 * geom.geometry = Geometry.SCALEFREE_DIRECTED; geom.connectivity = Math.max(2,
			 * arg); break;
			 */
			default:
				// last resort: try engine - maybe new implementations provide new geometries
				if (!population.parseGeometry(this, cli))
					geometry = INVALID; // too few parameters, change to default geometry
				break;
		}
		// checking the validity of geometry only now saves us the hassle of dealing
		// with variants (e.g. 'n' and 'm' for SQUARE geometries)
		CLOption clo = population.cloGeometry;
		if (!clo.isValidKey(geometry)) {
			String failedArg = (geomtype > 255 ? String.valueOf(geomtype - 256) : String.valueOf((char) geomtype));
			String defaultArg = clo.getDefault();
			// try again with default argument
			parse(defaultArg);
			logger.warning("geometry '" + failedArg + "' is invalid - reverting to default '"
					+ clo.getDescriptionKey(geometry) + "' geometry.");
			isValid = false;
			return true;
		}
		doReset |= (oldGeometry != geometry);
		if (Math.abs(oldConnectivity - connectivity) > 1e-6)
			doReset = true;
		isValid &= !doReset;
		return doReset;
	}

	/**
	 * @return usage for <code>--geometry</code> command line option
	 */
	public String usage() {
		CLOption clo = population.cloGeometry;
		boolean fixedBoundariesAvailable = (clo.isValidKey(LINEAR) || clo.isValidKey(SQUARE) || clo.isValidKey(CUBE)
				|| clo.isValidKey(HONEYCOMB) || clo.isValidKey(TRIANGULAR));
		String descr = "--geometry,  -G<>         geometry - interaction==reproduction\n" + "  argument:   <g>"
				+ (fixedBoundariesAvailable ? "[f|F]" : "") + "<n>\n" + "      g: type of geometry"
				+ clo.getDescriptionKey() + "\n  further specifications"
				+ (fixedBoundariesAvailable ? "\n      f|F: fixed lattice boundaries (default periodic)" : "");
		return descr;
	}

	/**
	 * initialize available geometries for command line options.
	 */
	public static void load(CLOption clo) {
		clo.addKey(MEANFIELD, "mean-field/well-mixed population");
		clo.addKey(COMPLETE, "complete graph (k=N-1)");
		clo.addKey(HIERARCHY, "hierarchical (meta-)populations",
				"H[<g>[f]]<n1>[,<n2>[...,<nm>]]w<w> hierarchical\n"
						+ "            structure for population geometries g:\n"
						+ "            M: well-mixed (default)\n" + "            n: square lattice (von neumann)\n"
						+ "            m: square lattice (moore)\n" + "            append f for fixed boundaries\n"
						+ "            n1,...,nm number of units on each hierarchical level\n"
						+ "            total of m+1 levels with nPopulation/(n1*...*nm)\n"
						+ "            individuals in last level\n" + "            w: strength of ties between levels");
		// lattices
		clo.addKey(LINEAR, "linear lattice, 1D", "l<l>[,<r>] linear lattice (l neighbourhood,\n"
				+ "            if r>0 and r!=l asymmetric neighbourhood)");
		clo.addKey(SQUARE_NEUMANN, "square lattice (von neumann)");
		clo.addKey(SQUARE_MOORE, "square lattice (moore)");
		clo.addKey(SQUARE, "square lattice, 2D", "N<n> square lattice (n neighbours, 3x3, 5x5...)");
		clo.addKey(CUBE, "cubic lattice, 3D", "C<n> cubic lattice (n neighbours, 2+2+2, 3x3x3, 5x5x5...)");
		clo.addKey(HONEYCOMB, "honeycomb lattice (k=6)");
		clo.addKey(TRIANGULAR, "triangular lattice (k=3)");
		// named graphs
		clo.addKey(FRUCHT, "Frucht graph (N=12, k=3)");
		clo.addKey(TIETZE, "Tietze graph (N=12, k=3)");
		clo.addKey(FRANKLIN, "Franklin graph (N=12, k=3)");
		clo.addKey(HEAWOOD, "Heawood graph (N=14, k=3)");
		clo.addKey(ICOSAHEDRON, "Icosahedron graph (N=12, k=5)");
		clo.addKey(DODEKAHEDRON, "Dodekahedron graph (N=20, k=3)");
		clo.addKey(DESARGUES, "Desargues graph (N=20, k=3)");
		// amplifiers
		clo.addKey(STAR, "star (single hub)");
		clo.addKey(SUPER_STAR, "super-star (single hub, petals)",
				(char) SUPER_STAR + "<p[,k]> super-star (p petals [1], k amplification [3])");
		clo.addKey(WHEEL, "wheel (cycle, single hub)");
		clo.addKey(STRONG_AMPLIFIER, "strong (undirected) amplifier");
		// suppressors
		clo.addKey(STRONG_SUPPRESSOR, "strong (undirected) suppressor");
		// random graphs
		clo.addKey(RANDOM_REGULAR_GRAPH, "random regular graph",
				(char) RANDOM_REGULAR_GRAPH + "<d> random regular graph (d degree [2])");
		clo.addKey(RANDOM_GRAPH, "random graph", (char) RANDOM_GRAPH + "<d> random graph (d degree [2])");
		clo.addKey(RANDOM_GRAPH_DIRECTED, "random graph (directed)",
				(char) RANDOM_GRAPH_DIRECTED + "<d> directed random graph (d degree [2])");
		clo.addKey(SCALEFREE, "scale-free graph", (char) SCALEFREE + "<e> scale-free graph (e exponent [-2]");
		clo.addKey(SCALEFREE_KLEMM, "scale-free graph (Klemm)",
				(char) SCALEFREE_KLEMM + "<n[,p]> scale-free graph (n degree, p random links)");
		clo.addKey(SCALEFREE_BA, "scale-free graph (Barabasi & Albert)");
	}

	/**
	 * clone geometry
	 * 
	 * notes: - this overrides clone() in Object but conflicts with GWT's aversion
	 * to clone()ing... - when modifying method uncomment @SuppressWarnings("all")
	 * to ensure that no other issues crept in.
	 * 
	 * @return clone of geometry
	 */
//	@Override
	@SuppressWarnings("all")
	public Geometry clone() {
		Geometry clone = new Geometry(population, opponent);
		clone.name = name;
		if (kin != null)
			clone.kin = Arrays.copyOf(kin, kin.length);
		if (kout != null)
			clone.kout = Arrays.copyOf(kout, kout.length);
		if (in != null) {
			clone.in = Arrays.copyOf(in, in.length);
			for (int i = 0; i < in.length; i++)
				clone.in[i] = Arrays.copyOf(in[i], in[i].length);
		}
		if (out != null) {
			clone.out = Arrays.copyOf(out, out.length);
			for (int i = 0; i < out.length; i++)
				clone.out[i] = Arrays.copyOf(out[i], out[i].length);
		}
		if (rawhierarchy != null)
			clone.rawhierarchy = Arrays.copyOf(rawhierarchy, rawhierarchy.length);
		if (hierarchy != null)
			clone.hierarchy = Arrays.copyOf(hierarchy, hierarchy.length);
		clone.hierarchyweight = hierarchyweight;
		clone.size = size;
		clone.geometry = geometry;
		clone.fixedBoundary = fixedBoundary;
		clone.minIn = minIn;
		clone.maxIn = maxIn;
		clone.avgIn = avgIn;
		clone.minOut = minOut;
		clone.maxOut = maxOut;
		clone.avgOut = avgOut;
		clone.minTot = minTot;
		clone.maxTot = maxTot;
		clone.avgTot = avgTot;
		clone.petalscount = petalscount;
		clone.petalsamplification = petalsamplification;
		clone.sfExponent = sfExponent;
		clone.connectivity = connectivity;
		clone.pUndirLinks = pUndirLinks;
		clone.pDirLinks = pDirLinks;
		clone.addUndirLinks = addUndirLinks;
		clone.addDirLinks = addDirLinks;
		clone.isUndirected = isUndirected;
		clone.isRewired = isRewired;
		clone.interReproSame = interReproSame;
		clone.isDynamic = isDynamic;
		clone.isRegular = isRegular;
		clone.isLattice = isLattice;
		clone.isValid = isValid;
		return clone;
	}

	/**
	 * Check if this Geometry and geo refer to the same structures. Different
	 * realizations of random structures, such as random regular graphs, are
	 * considered equal as long as their characteristic parameters are the same.
	 *
	 * @param geo
	 * @return true if structures are the same
	 */
	public boolean equals(Geometry geo) {
		if (geo == this)
			return true;
		if (geo.geometry != geometry)
			return false;
		if (geo.size != size)
			return false;
		if (Math.abs(geo.connectivity - connectivity) > 1e-6)
			return false;
		return true;
	}

	/**
	 * encode geometry as a plist string fragment
	 *
	 * @return
	 */
	public String encodeGeometry() {
		StringBuilder plist = new StringBuilder();
		plist.append(EvoLudo.encodeKey("Name", population.cloGeometry.getDescriptionKey(geometry)));
		plist.append(EvoLudo.encodeKey("Code", geometry));
		// no need to explicitly encode geometries that can be easily and unambiguously
		// re-generated
		if (isUniqueGeometry()) {
			// encode geometry
			plist.append("<key>Graph</key>\n<dict>\n");
			// note: in[] and kin[] will be reconstructed on restore
			for (int n = 0; n < size; n++)
				plist.append(EvoLudo.encodeKey(Integer.toString(n), out[n], kout[n]));
			plist.append("</dict>\n");
		}
		return plist.toString();
	}

	/**
	 * decode geometry encoded in map providing array of neighbor indices for each
	 * individual index.
	 * <p>
	 * based on command line arguments the population (including its
	 * geometry/geometries) must already have been initialized. this only restores a
	 * particular (unique) geometry.
	 * </p>
	 * 
	 * @param plist
	 */
	public void decodeGeometry(Plist plist) {
		if (!isUniqueGeometry())
			return;
		// decode geometry
		Plist graph = (Plist) plist.get("Graph");
		ArrayList<List<Integer>> outlinks = new ArrayList<List<Integer>>(size);
		ArrayList<ArrayList<Integer>> inlinks = new ArrayList<ArrayList<Integer>>(size);
		List<Integer> placeholder = new ArrayList<Integer>();
		for (int n = 0; n < size; n++) {
			outlinks.add(placeholder);
			inlinks.add(new ArrayList<Integer>());
		}
		for (Iterator<String> i = graph.keySet().iterator(); i.hasNext();) {
			String idxs = i.next();
			int idx = Integer.parseInt(idxs);
			@SuppressWarnings("unchecked")
			List<Integer> neighs = (List<Integer>) graph.get(idxs);
			out[idx] = EvoLudo.list2int(neighs);
			kout[idx] = out[idx].length;
			// each outlink is someone else's inlink; process links from i to j
			for (Iterator<Integer> j = neighs.iterator(); j.hasNext();)
				inlinks.get(j.next()).add(idx);
		}
		// outlinks already in place; finish inlinks
		for (int n = 0; n < size; n++) {
			in[n] = EvoLudo.list2int(inlinks.get(n));
			kin[n] = in[n].length;
		}
		// finish
		evaluate();
	}

	/**
	 * check if current geometry unique. lattices etc are not unique because they
	 * can be identically recreated. all geometries involving random elements are
	 * unique.
	 *
	 * @return true if geometry is unique
	 */
	public boolean isUniqueGeometry() {
		return isUniqueGeometry(geometry);
	}

	/**
	 * helper method to check uniqueness of geometry. hierarchical geometries
	 * require recursive checks of uniqueness.
	 * 
	 * @param geo
	 * @return
	 */
	private boolean isUniqueGeometry(int geo) {
		switch (geo) {
			// non-unique geometries
			case MEANFIELD: // mean field
			case COMPLETE: // complete graph
			case LINEAR: // linear
			case SQUARE_NEUMANN: // von neumann
			case SQUARE_MOORE: // moore
			case SQUARE: // square, larger neighborhood
			case CUBE: // cubic, larger neighborhood
			case HONEYCOMB: // hexagonal
			case TRIANGULAR: // triangular
			case FRUCHT: // Frucht graph
			case TIETZE: // Tietze graph
			case FRANKLIN: // Franklin graph
			case HEAWOOD: // Heawood graph
			case DODEKAHEDRON: // Dodekahedron graph
			case DESARGUES: // Desargues graph
			case ICOSAHEDRON: // Icosahedron graph
				// some suppressors are non-unique
				// some amplifiers are non-unique
			case STAR: // star
			case WHEEL: // wheel - cycle (k=2) with single hub (k=N-1)
			case 'P': // petals - for compatibility
			case SUPER_STAR: // super-star
				return false;

			// hierarchies of random regular graphs or similar would be unique
			case HIERARCHY: // deme structured, hierarchical graph
				return isUniqueGeometry(subgeometry);

			// unique graphs
			case RANDOM_REGULAR_GRAPH: // random regular graph
			case RANDOM_GRAPH: // random graph
			case RANDOM_REGULAR_GRAPH_DIRECTED: // random regular graph directed
			case RANDOM_GRAPH_DIRECTED: // random graph directed
			case STRONG_AMPLIFIER: // strong amplifier
			case STRONG_SUPPRESSOR: // strong suppressor
			case SCALEFREE_BA: // scale-free network - barabasi & albert
			case SCALEFREE_KLEMM: // scale-free network - klemm
			case SCALEFREE: // scale-free network - uncorrelated, from degree distribution
				// for unknown graphs simply assume is unique
			default:
				return true;
		}
	}
}
