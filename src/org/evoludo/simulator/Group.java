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

import org.evoludo.util.RNGDistribution;

/**
 *
 * @author Christoph Hauert
 */
public class Group {
	/**
	 *
	 */
	public static final int SAMPLING_NONE = -1;

	/**
	 *
	 */
	public static final int SAMPLING_ALL = 0;

	/**
	 *
	 */
	public static final int SAMPLING_COUNT = 1;

	private static int[] loner = new int[0];
	private int[] mem;
	int[]	group;
	int		focal;
	int		defaultsize, size;
	int		samplingType = SAMPLING_ALL;
	protected RNGDistribution rng;

	/**
	 * random number generator must be supplied. it is recommended that the (thread safe) random number
	 * generator is shared. this is important for debugging and the reproducibility of simulations for 
	 * fixed seeds. 
	 * (note, default (empty) constructor does not need to be declared private in order to prevent
	 * instantiation. the compiler does not generate a default constructor if another one exists.)
	 *  
	 * @param rng
	 */
	public Group(RNGDistribution rng) {
		this.rng = rng;
	}

	/**
	 *
	 * @param maxsize
	 */
	public void alloc(int maxsize) {
		if( mem==null || mem.length!=maxsize ) {
			mem = new int[maxsize];
			group = mem;
			// defaults are from earlier times
			defaultsize = 1;
			size = 1;
		}
	}

	/**
	 *
	 * @param size
	 */
	public void setSize(int size) {
		defaultsize = size;
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	/**
	 *
	 * @param type
	 */
	public void setSampling(int type) {
		samplingType = type;
	}

	public int getSampling() {
		return samplingType;
	}

	public boolean isSampling(int type) {
		return (type==samplingType);
	}

	/**
	 *
	 * @param focal
	 * @param group
	 * @param size
	 */
	public void setGroupAt(int focal, int[] group, int size) {
		this.focal = focal;
		if( group==null ) {
			this.group = loner;
			this.size = 0;
			return;
		}
		this.group = group;
		this.size = size;
	}

	public int[] getGroup() {
		return group;
	}

	public int getFocal() {
		return focal;
	}

	/**
	 * @param me
	 * @param geom
	 * @param out
	 * @return
	 */
	public int[] pickAt(int me, Geometry geom, boolean out) {
		focal = me;
		// IMPORTANT: setting group=src saves copying of 'src' but requires that 'group' is NEVER manipulated
		switch( samplingType ) {
			case SAMPLING_NONE:	// speeds things up e.g. for best-reply in well-mixed populations
//				size = 0;
// if size==0 then updatePlayerAt aborts because no references found... pretend we have one.
				size = 1;
				return null;

			case SAMPLING_ALL:
				if( out ) {
					group = geom.out[focal];
					size = geom.kout[focal];
					return group;
				}
				group = geom.in[focal];
				size = geom.kin[focal];
				return group;

			case SAMPLING_COUNT:
				switch( geom.geometry ) {
					case Geometry.MEANFIELD:
						pickRandom(geom.size);
						return group;

					case Geometry.HIERARCHY:
						if( defaultsize!=1 ) {
							throw new Error("sampling of groups (≥2) in hierarchical structures not (yet) implemented!");
						}
						size = 1;

						int level = 0;
						int maxLevel = geom.hierarchy.length-1;
						int unitSize = geom.hierarchy[maxLevel];
						int levelSize = 1;
						int exclSize = 1;
						double prob = geom.hierarchyweight;
						if( prob>0.0 ) {
							if( (int)Math.rint(geom.connectivity)==unitSize-1 ) {
								// if individuals are connected to all other members of the unit one hierarchy level is lost.
								// this applies to well-mixed units as well as e.g. square lattices with moore neighbourhood and 3x3 units.
								maxLevel--;
								levelSize = unitSize;
							}
							double rand = rng.random01();
							while( rand<prob && level<=maxLevel ) {
								exclSize = levelSize;
								levelSize *= geom.hierarchy[maxLevel-level];
								level++;
								prob *= geom.hierarchyweight;
							}
						}
						group = mem;
						int model;
						int levelStart, exclStart;
						switch( geom.subgeometry ) {
							case Geometry.MEANFIELD:
								if( level==0 ) {
									// pick random neighbour
									if( out )
										group[0] = geom.out[focal][rng.random0n(geom.kout[focal])];
									else
										group[0] = geom.in[focal][rng.random0n(geom.kin[focal])];
									return group;
								}
								// with zero hierarchyweight levelSize is still 1 instead of unitSize
								levelSize = Math.max(levelSize, unitSize);
								// determine start of level
								levelStart = (focal/levelSize)*levelSize;
								// determine start of exclude level
								exclStart = (focal/exclSize)*exclSize;	// relative to level
								// pick random individual in level, excluding focal unit
								model = levelStart+rng.random0n(levelSize-exclSize);
								if( model>=exclStart ) model += exclSize;
								group[0] = model;
								return group;

							case Geometry.SQUARE:
								if( level==0 ) {
									// pick random neighbour
									if( out )
										group[0] = geom.out[focal][rng.random0n(geom.kout[focal])];
									else
										group[0] = geom.in[focal][rng.random0n(geom.kin[focal])];
									return group;
								}
								// determine start of focal level
								int side = (int)Math.sqrt(geom.size);
								int levelSide = (int)Math.sqrt(levelSize);
								int levelX = ((focal%side)/levelSide)*levelSide;
								int levelY = ((focal/side)/levelSide)*levelSide;
								levelStart = levelY*side+levelX;
								// determine start of excluded level (relative to focal level)
								int exclSide = (int)Math.sqrt(exclSize);
								int exclX = ((focal%side)/exclSide)*exclSide;
								int exclY = ((focal/side)/exclSide)*exclSide;
								exclStart = (exclY-levelY)*levelSide+exclX-levelX;
								// draw random individual in focal level, excluding lower level
								model = rng.random0n(levelSize-exclSize);
								for( int i=0; i<exclSide; i++ ) {
									if( model<exclStart ) break;
									model += exclSide;
									exclStart += levelSide;
								}
								// model now relative to levelStart. transform to population level
								int modelX = model%levelSide;
								int modelY = model/levelSide;
								model = levelStart+modelY*side+modelX;
								group[0] = model;
								return group;

							default:
								throw new Error("hierachy geometry '"+(char)geom.subgeometry+"' not supported");
						}

					default:
						int[] src = (out?geom.out[focal]:geom.in[focal]);
						int len = (out?geom.kout[focal]:geom.kin[focal]);
						if( len<=defaultsize ) {
							group = src;
							size = len;
							return group;
						}
						group = mem;
						size = defaultsize;
						if( size==1 ) {
							// optimization: single reference is commonly used and saves copying of all neighbors.
							group[0] = src[rng.random0n(len)];
							return group;
						}
						System.arraycopy(src, 0, group, 0, len);
						if( size>len/2 ) {
							for( int n=0; n<len-size; n++ ) {
								int aRand = rng.random0n(len-n);
								group[aRand] = group[len-n-1];
							}
							return group;
						}
						for( int n=0; n<size; n++ ) {
							int aRand = rng.random0n(len-n)+n;
							int swap = group[n];
							group[n] = group[aRand];
							group[aRand] = swap;
						}
						return group;
				}
			default:
				throw new Error("Unknown group sampling (type: "+samplingType+")!");
		}
	}

	/**
	 *
	 * @param idx
	 * @param geom
	 */
//	public void pickFitAt(int idx, Geometry geom) {
//		pickFitAt(idx, geom, useOut);
//	}

	/**
	 *
	 * @param focal
	 * @param geom
	 * @param out
	 */
//	public void pickFitAt(int me, Geometry geom, boolean out) {
//		focal = me;
//		// IMPORTANT: setting group=src saves copying of 'src' but requires that 'group' is NEVER manipulated
//		switch( samplingType ) {
//			case SAMPLING_NONE:	// speeds things up e.g. for best-reply in well-mixed populations
////				size = 0;
////if size==0 then updatePlayerAt aborts because no references found... pretend we have one.
//size = 1;
//				return;
//
//			case SAMPLING_ALL:
//				if( out ) {
//					group = geom.out[focal];
//					size = geom.kout[focal];
//					return;
//				}
//				group = geom.in[focal];
//				size = geom.kin[focal];
//				return;
//
//			case SAMPLING_COUNT:
//				switch( geom.geometry ) {
//					case Geometry.MEANFIELD:
//						pickRandom(focal, geom.size);
//						break;
//
//					case Geometry.HIERARCHY:
//						if( defaultsize!=1 ) {
//							throw new Error("sampling of groups (≥2) in hierarchical structures not (yet) implemented!");
//						}
//
//						int level = 0;
//						int maxLevel = geom.hierarchy.length-1;
//						int unitSize = geom.hierarchy[maxLevel];
//						int levelSize = 1;
//						int exclSize = 1;
//						double prob = geom.hierarchyweight;
//						if( prob>0.0 ) {
//							if( (int)Math.rint(geom.connectivity)==unitSize-1 ) {
//								// if individuals are connected to all other members of the unit one hierarchy level is lost.
//								// this applies to well-mixed units as well as e.g. square lattices with moore neighbourhood and 3x3 units.
//								maxLevel--;
//								levelSize = unitSize;
//							}
//							double rand = rng.random01();
//							while( rand<prob && level<=maxLevel ) {
//								exclSize = levelSize;
//								levelSize *= geom.hierarchy[maxLevel-level];
//								level++;
//								prob *= geom.hierarchyweight;
//							}
//						}
//						group = mem;
//						int model;
//						int levelStart, exclStart;
//						switch( geom.subgeometry ) {
//							case Geometry.MEANFIELD:
//								// determine start of level
//								levelStart = (focal/levelSize)*levelSize;
//								// determine start of exclude level
//								exclStart = (focal/exclSize)*exclSize;	// relative to level
//								// pick random individual in level, excluding focal unit
//								model = levelStart+rng.random0n(levelSize-exclSize);
//								if( model>=exclStart ) model += exclSize;
//								group[0] = model;
//								return;
//
//							case Geometry.SQUARE:
//								if( level==0 ) {
//									// pick random neighbour
//									if( out )
//										group[0] = geom.out[focal][rng.random0n(geom.kout[focal])];
//									else
//										group[0] = geom.in[focal][rng.random0n(geom.kin[focal])];
//									return;
//								}
//								// determine start of focal level
//								int side = (int)Math.sqrt(geom.size);
//								int levelSide = (int)Math.sqrt(levelSize);
//								int levelX = ((focal%side)/levelSide)*levelSide;
//								int levelY = ((focal/side)/levelSide)*levelSide;
//								levelStart = levelY*side+levelX;
//								// determine start of excluded level (relative to focal level)
//								int exclSide = (int)Math.sqrt(exclSize);
//								int exclX = ((focal%side)/exclSide)*exclSide;
//								int exclY = ((focal/side)/exclSide)*exclSide;
//								exclStart = (exclY-levelY)*levelSide+exclX-levelX;
//								// draw random individual in focal level, excluding lower level
//								model = rng.random0n(levelSize-exclSize);
//								for( int i=0; i<exclSide; i++ ) {
//									if( model<exclStart ) break;
//									model += exclSide;
//									exclStart += levelSide;
//								}
//								// model now relative to levelStart. transform to population level
//								int modelX = model%levelSide;
//								int modelY = model/levelSide;
//								model = levelStart+modelY*side+modelX;
//								group[0] = model;
//								return;
//
//							default:
//								throw new Error("hierachy geometry '"+(char)geom.subgeometry+"' not supported");
//						}
//
//					default:
//						int[] src = (out?geom.out[focal]:geom.in[focal]);
//						int len = (out?geom.kout[focal]:geom.kin[focal]);
//						if( len<=defaultsize ) {
//							group = src;
//							size = len;
//							return;
//						}
//						group = mem;
//						size = defaultsize;
//						if( size==1 ) {
//							// optimization: single reference is commonly used and saves copying of all neighbors.
//							group[0] = src[rng.random0n(len)];
//							return;
//						}
//						System.arraycopy(src, 0, group, 0, len);
//						if( size>len/2 ) {
//							for( int n=0; n<len-size; n++ ) {
//								int aRand = rng.random0n(len-n);
//								group[aRand] = group[len-n-1];
//							}
//							return;
//						}
//						for( int n=0; n<size; n++ ) {
//							int aRand = rng.random0n(len-n)+n;
//							int swap = group[n];
//							group[n] = group[aRand];
//							group[aRand] = swap;
//						}
//				}
//				return;
//			default:
//				throw new Error("Unknown group sampling (type: "+samplingType+")!");
//		}
//	}

	/**
	 * pick group of <code>size</code> random individuals with indices ranging from <code>0</code> to 
	 * <code>max-1</code>. exclude focal individual if <code>exclFocal</code> is true. the picked group 
	 * is stored in <code>group</code>.
	 * 
	 * @param max
	 * @param exclFocal
	 */
	private void pickRandom(int max, boolean exclFocal) {
		group = mem;
		size = defaultsize;

		if( size==1 ) {
			group[0] = pick(max, exclFocal);
			return;
		}

		int n = 0;
	nextpick:
		while( n<size ) {
			int aPick = pick(max, exclFocal);
			for( int i=0; i<n; i++ )
				if( group[i]==aPick ) continue nextpick;
			group[n++] = aPick;
		}
	}

	/**
	 * pick random individual with index <code>0</code> through <code>max-1</code>. exclude focal individual if 
	 * <code>exclFocal</code> is true.
	 * 
	 * @param max
	 * @param exclFocal
	 * @return index of randomly picked individual
	 */
	private int pick(int max, boolean exclFocal) {
		if( exclFocal ) {
			// exclude focal individual
			int aPick = rng.random0n(max-1);
			if( aPick>=focal ) aPick++;
			return aPick;
		}
		return rng.random0n(max);
	}

	/**
	 * pick random individual with index <code>0</code> through <code>max-1</code>, including focal individual 
	 * (if applicable, i.e. focal index with  <code>0&le;focal&le;max-1</code>
	 * <code>exclFocal</code> is true.
	 * 
	 * @param max
	 * @param exclFocal
	 * @return index of randomly picked individual
	 */
	private void pickRandom(int max) {
		pickRandom(max, false);
	}
}
