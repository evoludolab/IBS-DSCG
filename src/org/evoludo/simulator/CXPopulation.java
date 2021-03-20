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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.evoludo.util.CLOParser;
import org.evoludo.util.CLOption;
import org.evoludo.util.CLOption.CLODelegate;
import org.evoludo.util.Formatter;
import org.evoludo.util.ArrayMath;
import org.evoludo.util.Plist;

/**
 *
 * @author Christoph Hauert
 */
public abstract class CXPopulation extends Population {

	protected CXPopulation(EvoLudo engine, String key) {
		super(engine, key);
	}

	protected CXPopulation(Population partner) {
		super(partner);
	}

	@Override
	public void load() {
		super.load();

		isContinuous = true;

		// best-reply is not an acceptable update rule for continuous strategies - exclude Population.PLAYER_UPDATE_BEST_REPLY
		cloPlayerUpdate.removeKey(PlayerUpdateType.BEST_REPLY);

		// initialize cost function 
		cloCostFunction.addKey(PAYOFF_COST_ME_LINEAR, "C(x,y)=c0*x");
		cloCostFunction.addKey(PAYOFF_COST_ME_QUAD, "C(x,y)=c0*x+c1*x^2");
		cloCostFunction.addKey(PAYOFF_COST_ME_SQRT, "C(x,y)=c0*sqrt(x)");
		cloCostFunction.addKey(PAYOFF_COST_ME_LOG, "C(x,y)=c0*ln(c1*x+1)");
		cloCostFunction.addKey(PAYOFF_COST_ME_EXP, "C(x,y)=c0*(1-exp(-c1*x))");

		cloCostFunction.addKey(PAYOFF_COST_WE_LINEAR, "C(x,y)=c0*(x+y)");
		cloCostFunction.addKey(PAYOFF_COST_WE_QUAD, "C(x,y)=c0*(x+y)+c1*(x+y)^2");
		cloCostFunction.addKey(PAYOFF_COST_WE_QUBIC, "C(x,y)=c0*(x+y)+c1*(x+y)^2+c2*(x+y)^3");
		cloCostFunction.addKey(PAYOFF_COST_WE_QUARTIC, "C(x,y)=c0*(x+y)+c1*(x+y)^2+c2*(x+y)^3+c3*(x+y)^4");

		cloCostFunction.addKey(PAYOFF_COST_MEYOU_LINEAR, "C(x,y)=c0*x+c1*y+c2*x*y");

		// initialize cost function validator
		cloBenefitFunction.addKey(PAYOFF_BENEFIT_YOU_LINEAR, "B(x,y)=b0*y");
		cloBenefitFunction.addKey(PAYOFF_BENEFIT_YOU_QUADR, "B(x,y)=b0*y+b1*y^2");
		cloBenefitFunction.addKey(PAYOFF_BENEFIT_YOU_SQRT, "B(x,y)=b0*sqrt(y)");
		cloBenefitFunction.addKey(PAYOFF_BENEFIT_YOU_LOG, "B(x,y)=b0*ln(b1*y+1)");
		cloBenefitFunction.addKey(PAYOFF_BENEFIT_YOU_EXP, "B(x,y)=b0*(1-exp(-b1*y))");

		cloBenefitFunction.addKey(PAYOFF_BENEFIT_WE_LINEAR, "B(x,y)=b0*(x+y)");
		cloBenefitFunction.addKey(PAYOFF_BENEFIT_WE_QUAD, "B(x,y)=b0*(x+y)+b1*(x+y)^2");	// default
		cloBenefitFunction.addKey(PAYOFF_BENEFIT_WE_SQRT, "B(x,y)=b0*sqrt(x+y)");
		cloBenefitFunction.addKey(PAYOFF_BENEFIT_WE_LOG, "B(x,y)=b0*ln(b1*(x+y)+1)");
		cloBenefitFunction.addKey(PAYOFF_BENEFIT_WE_EXP, "B(x,y)=b0*(1-exp(-b1*(x+y)))");

		cloBenefitFunction.addKey(PAYOFF_BENEFIT_MEYOU_LINEAR, "B(x,y)=b0*x+b1*y+b2*x*y");

		cloBenefitFunction.addKey(PAYOFF_BENEFIT_ME_LINEAR, "B(x,y)=b0*x");
		cloBenefitFunction.addKey(PAYOFF_BENEFIT_ME_QUADR, "B(x,y)=b0*x+b1*x^2");
		cloBenefitFunction.addKey(PAYOFF_BENEFIT_ME_QUBIC, "B(x,y)=b0*x+b1*x^2+b2*x^3");

		// initialize mutation types
		cloMutationType.addKey(MUTATION_UNIFORM, "uniform mutations");
		cloMutationType.addKey(MUTATION_GAUSSIAN, "gaussian mutations around mean");
	}

	@Override
	public void unload() {
		// free resources
		super.unload();
		cloCostFunction.clearKeys();
		cloBenefitFunction.clearKeys();
		cloMutationType.clearKeys();
		benefitFcnType = null;
		costFcnType = null;
		bi = null;
		ci = null;
		traitMin = null;
		traitMax = null;
		initMean = null;
		initSdev = null;
		mutSdev = null;
	}

	@Override
	public boolean hasConverged() {
		// takes more than just the absence of mutations
		return false;
	}

	/**
	 * @param me
	 * @param group
	 * @param len
	 * @param payoffs
	 * @return
	 */
	public double pairScores(double[] me, double[] group, int len, double[] payoffs) {
		throw new Error("pairScores not implemented - this is an error.");
	}

	/**
	 * @param me
	 * @param group
	 * @param len
	 * @param payoffs
	 * @return
	 */
	public double groupScores(double[] me, double[] group, int len, double[] payoffs) {
		if( len==1 ) return pairScores(me, group, len, payoffs);
		throw new Error("groupScores not implemented - this is an error.");
	}

	/*
	 * payoff functions 
	 */

	/**
	 *
	 */
	protected int[] benefitFcnType;	// default is quadratic benefits and costs

	/**
	 *
	 */
	protected int[] costFcnType;	// default is quadratic benefits and costs

	static final int MAX_PARAMS = 10;

	/**
	 *
	 */
	protected double[][]	bi;

	/**
	 *
	 */
	protected double[][]	ci;

	/**
	 *
	 */
	public static final int PAYOFF_COST_ME_LINEAR = CLOption.NUMBERED_KEY_OFFSET + 0;

	/**
	 *
	 */
	public static final int PAYOFF_COST_ME_QUAD = CLOption.NUMBERED_KEY_OFFSET + 1; // default

	/**
	 *
	 */
	public static final int PAYOFF_COST_ME_SQRT = CLOption.NUMBERED_KEY_OFFSET + 2;

	/**
	 *
	 */
	public static final int PAYOFF_COST_ME_LOG = CLOption.NUMBERED_KEY_OFFSET + 3;

	/**
	 *
	 */
	public static final int PAYOFF_COST_ME_EXP = CLOption.NUMBERED_KEY_OFFSET + 4;

	/**
	 *
	 */
	public static final int PAYOFF_COST_WE_LINEAR = CLOption.NUMBERED_KEY_OFFSET + 10;

	/**
	 *
	 */
	public static final int PAYOFF_COST_WE_QUAD = CLOption.NUMBERED_KEY_OFFSET + 11;

	/**
	 *
	 */
	public static final int PAYOFF_COST_WE_QUBIC = CLOption.NUMBERED_KEY_OFFSET + 12;

	/**
	 *
	 */
	public static final int PAYOFF_COST_WE_QUARTIC = CLOption.NUMBERED_KEY_OFFSET + 13;

	/**
	 *
	 */
	public static final int PAYOFF_COST_MEYOU_LINEAR = CLOption.NUMBERED_KEY_OFFSET + 20;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_YOU_LINEAR = CLOption.NUMBERED_KEY_OFFSET + 0;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_YOU_QUADR = CLOption.NUMBERED_KEY_OFFSET + 1;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_YOU_SQRT = CLOption.NUMBERED_KEY_OFFSET + 2;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_YOU_LOG = CLOption.NUMBERED_KEY_OFFSET + 3;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_YOU_EXP = CLOption.NUMBERED_KEY_OFFSET + 4;
	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_WE_LINEAR = CLOption.NUMBERED_KEY_OFFSET + 10;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_WE_QUAD = CLOption.NUMBERED_KEY_OFFSET + 11; // default

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_WE_SQRT = CLOption.NUMBERED_KEY_OFFSET + 12;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_WE_LOG = CLOption.NUMBERED_KEY_OFFSET + 13;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_WE_EXP = CLOption.NUMBERED_KEY_OFFSET + 14;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_MEYOU_LINEAR = CLOption.NUMBERED_KEY_OFFSET + 20;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_ME_LINEAR = CLOption.NUMBERED_KEY_OFFSET + 30;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_ME_QUADR = CLOption.NUMBERED_KEY_OFFSET + 31;

	/**
	 *
	 */
	public static final int PAYOFF_BENEFIT_ME_QUBIC = CLOption.NUMBERED_KEY_OFFSET + 32;

	/*
	 * mutations 
	 */
	/**
	 *
	 */
	public static final int MUTATION_UNIFORM = 'u';		// was 0

	/**
	 *
	 */
	public static final int MUTATION_GAUSSIAN = 'g';	// was 1

	/**
	 *
	 */
	protected int mutationType = MUTATION_GAUSSIAN;

	/**
	 *
	 * @param me
	 * @param you
	 * @return
	 */
	protected double costs(double[] me, double[] you) {
		double totcosts = 0.0;
		for( int n=0; n<nTraits; n++ )
			totcosts += costs(me[n], you[n], n);
		return totcosts;
	}

	/**
	 *
	 * @param me
	 * @param you
	 * @param trait
	 * @return
	 */
	protected double costs(double me, double you, int trait) {
		double shift = traitMin[trait];
		double scale = traitMax[trait]-shift;
		double myinv = me*scale+shift;
		double yourinv = you*scale+shift;
		double ourinv = myinv+yourinv;
		double[] c = ci[trait];

		switch( costFcnType[trait] ) {
			case PAYOFF_COST_ME_LINEAR:
				return c[0]*myinv;
			case PAYOFF_COST_ME_QUAD: // default
				return myinv*(c[1]*myinv+c[0]);
			case PAYOFF_COST_ME_SQRT:
				return c[0]*Math.sqrt(myinv);
			case PAYOFF_COST_ME_LOG:
				return c[0]*Math.log(c[1]*myinv+1.0);
			case PAYOFF_COST_ME_EXP:
				return c[0]*(1.0-Math.exp(-c[1]*myinv));

			case PAYOFF_COST_WE_LINEAR:
				return c[0]*ourinv;
			case PAYOFF_COST_WE_QUAD:
				return (c[1]*ourinv+c[0])*ourinv;
			case PAYOFF_COST_WE_QUBIC:
				return ((c[2]*ourinv+c[1])*ourinv+c[0])*ourinv;
			case PAYOFF_COST_WE_QUARTIC:
				return (((c[3]*ourinv+c[2])*ourinv+c[1])*ourinv+c[0])*ourinv;

			case PAYOFF_COST_MEYOU_LINEAR:
				return c[0]*myinv+c[1]*yourinv+c[2]*myinv*yourinv;

			default:	// this is bad
				throw new Error("Unknown cost function type ("+costFcnType[trait]+")");
		}
	}

	/**
	 *
	 * @param me
	 * @param you
	 * @return
	 */
	protected double benefits(double[] me, double[] you) {
		double totbenefits = 0.0;
		for( int n=0; n<nTraits; n++ )
			totbenefits += benefits(me[n], you[n], n);
		return totbenefits;
	}

	/**
	 *
	 * @param me
	 * @param you
	 * @param trait
	 * @return
	 */
	protected double benefits(double me, double you, int trait) {
		double shift = traitMin[trait];
		double scale = traitMax[trait]-shift;
		double myinv = me*scale+shift;
		double yourinv = you*scale+shift;
		double ourinv = myinv+yourinv;
		double[] b = bi[trait];

		switch( benefitFcnType[trait] ) {

			// benefit depending solely on the 'me' investment
			case PAYOFF_BENEFIT_ME_LINEAR:
				return b[0]*myinv;
			case PAYOFF_BENEFIT_ME_QUADR:
				return (b[1]*myinv+b[0])*myinv;
			case PAYOFF_BENEFIT_ME_QUBIC:
				return ((b[2]*myinv+b[1])*myinv+b[0])*myinv;

			// benefit depending solely on the 'you' investment
			case PAYOFF_BENEFIT_YOU_LINEAR:
				return b[0]*yourinv;
			case PAYOFF_BENEFIT_YOU_QUADR:
				return (b[1]*yourinv+b[0])*yourinv;
			case PAYOFF_BENEFIT_YOU_SQRT:
				return b[0]*Math.sqrt(yourinv);
			case PAYOFF_BENEFIT_YOU_LOG:
				return b[0]*Math.log(b[1]*yourinv+1.0);
			case PAYOFF_BENEFIT_YOU_EXP:
				return b[0]*(1.0-Math.exp(-b[1]*yourinv));

			// benefit depending on the sum of 'me' and 'you' investments
			case PAYOFF_BENEFIT_WE_LINEAR:	// was 2
				return b[0]*ourinv;
			case PAYOFF_BENEFIT_WE_QUAD: // default
				return (b[1]*ourinv+b[0])*ourinv;
			case PAYOFF_BENEFIT_WE_SQRT:
				return b[0]*Math.sqrt(ourinv);
			case PAYOFF_BENEFIT_WE_LOG:
				return b[0]*Math.log(b[1]*ourinv+1.0);
			case PAYOFF_BENEFIT_WE_EXP:
				return b[0]*(1.0-Math.exp(-b[1]*ourinv));

			// benefit depending on 'me' and 'you' investments individually
			case PAYOFF_BENEFIT_MEYOU_LINEAR:
				return b[0]*myinv+b[1]*yourinv+b[2]*myinv*yourinv;

			default:	// this is bad
				throw new Error("Unknown benefit function type ("+benefitFcnType[trait]+")");
		}
	}

	/**
	 *
	 * @param me
	 * @param you
	 * @return
	 */
	protected double payoff(double[] me, double[] you) {
		// this assumes that benefits and costs can be decomposed into the different traits
		return benefits(me, you)-costs(me, you);
	}

	@Override
	public double getMinGameScore() {
		if( !extremalScoresSet ) setExtremalScores();
		return cxMinScore;
	}

	@Override
	public double getMaxGameScore() {
		if( !extremalScoresSet ) setExtremalScores();
		return cxMaxScore;
	}

	@Override
	public double getMinMonoGameScore() {
		if( !extremalScoresSet ) setExtremalScores();
		return cxMinMonoScore;
	}

	@Override
	public double getMaxMonoGameScore() {
		if( !extremalScoresSet ) setExtremalScores();
		return cxMaxMonoScore;
	}

	/**
	 *
	 * @param trait
	 * @return
	 */
	public double	getMonoGameScore(double[] trait) {
		throw new Error("getMonoGameScore not implemented - this is an error.");
	}

	/*
	 * trait space 
	 */
	/**
	 *
	 */
	protected	double[] traitMin;

	/**
	 *
	 */
	protected	double[] traitMax;

	/**
	 *
	 */
	protected	double[] initMean;

	/**
	 *
	 */
	protected	double[] initSdev;

	/**
	 *
	 */
	protected	double[] mutSdev;

	@Override
	public void setNTraits(int nTraits) {
		super.setNTraits(nTraits);
		if( traitMin==null || traitMin.length!=nTraits ) {
			traitMin = new double[nTraits];
			Arrays.fill(traitMin, 0.0);
		}
		if( traitMax==null || traitMax.length!=nTraits ) {
			traitMax = new double[nTraits];
			Arrays.fill(traitMax, 1.0);
		}
		if( initMean==null || initMean.length!=nTraits ) {
			initMean = new double[nTraits];
			Arrays.fill(initMean, 0.1);
		}
		if( initSdev==null || initSdev.length!=nTraits ) {
			initSdev = new double[nTraits];
			Arrays.fill(initSdev, 0.1);
		}
		if( mutSdev==null || mutSdev.length!=nTraits ) {
			mutSdev = new double[nTraits];
			Arrays.fill(mutSdev, 0.01);
		}
		if( costFcnType==null || costFcnType.length!=nTraits ) {
			costFcnType = new int[nTraits];
			Arrays.fill(costFcnType, PAYOFF_COST_ME_QUAD);
			ci = new double[nTraits][MAX_PARAMS];
		}
		if( benefitFcnType==null || benefitFcnType.length!=nTraits ) {
			benefitFcnType = new int[nTraits];
			Arrays.fill(benefitFcnType, PAYOFF_BENEFIT_WE_QUAD);
			bi = new double[nTraits][MAX_PARAMS];
		}
	}

	/**
	 * store strategies sequentially first all traits of first player 
	 * then all traits of second player etc.
	 */
	protected   double[] strategies;

	/**
	 *
	 */
	protected   double[] strategiesScratch;

	/**
	 *
	 */
	protected   double[] strategiesSwap;

	/*
	 * methods referring to continuous strategies 
	 */
	@Override
	protected void updateFromModelAt(int index, int modelPlayer) {
		super.updateFromModelAt(index, modelPlayer);	// deal with tags
		System.arraycopy(strategies, modelPlayer*nTraits, strategiesScratch, index*nTraits, nTraits);
	}

	@Override
	protected boolean haveSameStrategy(int a, int b) {
		if( nTraits==1 ) return (Math.abs(strategies[a]-strategies[b])<1e-8);
		int idxa = a*nTraits;
		int idxb = b*nTraits;
		for( int i=0; i<nTraits; i++ )
			if( Math.abs(strategies[idxa+i]-strategies[idxb+i])>1e-8 ) return false;
		return true;
	}

	@Override
	protected boolean isSameStrategy(int a) {
		if( nTraits==1 ) return (Math.abs(strategies[a]-strategiesScratch[a])<1e-8);
		int idxa = a*nTraits;
		for( int i=0; i<nTraits; i++ )
			if( Math.abs(strategies[idxa+i]-strategiesScratch[idxa+i])>1e-8 ) return false;
		return true;
	}

	@Override
	protected void swapStrategies(int a, int b) {
		if( nTraits==1 ) {
			strategiesScratch[a] = strategies[b];
			strategiesScratch[b] = strategies[a];
			return;
		}
		int idxa = a*nTraits;
		int idxb = b*nTraits;
		System.arraycopy(strategies, idxb, strategiesScratch, idxa, nTraits);
		System.arraycopy(strategies, idxa, strategiesScratch, idxb, nTraits);
	}

	/**
	 * mutate randomly chosen trait of individual at <code>index</code>. override method to implement 
	 * custom mutations. if <code>changed==true</code> the trait has already been updated and the 
	 * reference is in <code>strategiesScratch</code> rather than <code>strategies</code>.
	 * <p>
	 * <strong>Note:</strong> traits are always scaled to fall into the interval <code>[0, 1]</code>.
	 * </p>
	 * @param index
	 * @param changed
	 */
	@Override
	protected void mutateStrategyAt(int index, boolean changed) {
		int idx = index;
		int loc = 0;
		if( nTraits>1 ) {
			idx *= nTraits;
			loc = random0n(nTraits);
			// if strategy has not yet been updated, all traits need to be copied to 
			// strategiesScratch otherwise commitStrategy commits the wrong unmutated traits
			if (!changed)
				System.arraycopy(strategies, idx, strategiesScratch, idx, nTraits);
		}
		switch( mutationType ) {
			case MUTATION_UNIFORM:
				strategiesScratch[idx+loc] = random01();
				return;
			case MUTATION_GAUSSIAN:
				double mean = changed?strategiesScratch[idx+loc]:strategies[idx+loc];
				double sdev = mutSdev[loc];
				// draw mutants until we find viable one...
				// not very elegant but avoids emphasis of interval boundaries.
				double mut;
				do {
					mut = randomGaussian(mean, sdev);
				}
				while( mut<0.0 || mut>1.0 );
// alternative approach - use reflective boundaries (John Fairfield)
// note: this is much more elegant than the above - is there a biological motivation for 'reflective mutations'? is such a justification necessary?
//				double mut = randomGaussian(orig, mutSdev[loc]);
//				mut = Math.abs(mut);
//				if( mut>1.0 ) mut = 2-mut;

				strategiesScratch[idx+loc] = mut;
				return;
			default:
				throw new Error("Unknown mutation type ("+mutationType+")");
		}
	}

	/**
	 * best-reply makes little sense for continuous strategies - should never get here.
	 * this is excluded by adjusting validPlayerUpdates in CXPopulationLab
	 * 
	 * @param me
	 * @param group
	 * @param size
	 * @return
	 */
	@Override
	protected boolean updatePlayerBestReply(int me, int[] group, int size) {
		throw new Error("Best-reply dynamics ill defined for continuous strategies!");
	}

	/**
	 * for deterministic updating we always need to be sure which strategy is the preferred one.
	 * here we introduce the convention that the one closer to the original is the preferred one.
	 * 
	 * @param me
	 * @param best
	 * @param sample
	 * @return
	 */
	@Override
	protected boolean preferredPlayerBest(int me, int best, int sample) {
		double distmesample = deltaStrategies(me, sample);
		if( distmesample<1e-8 ) return true;
		double distmebest = deltaStrategies(me, best);
		return (distmesample<distmebest);
	}

	/**
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	protected double deltaStrategies(int a, int b) {
		if( nTraits==1 ) return Math.abs(strategies[a]-strategies[b]);
		int idxa = a*nTraits;
		int idxb = b*nTraits;
		double dist = 0.0;
		for( int i=0; i<nTraits; i++ ) {
			double d = strategies[idxa+i]-strategies[idxb+i];
			dist += d*d;
		}
		return Math.sqrt(dist);
	}

	private void gatherPlayers(Group group) {
		CXPopulation opponent = (CXPopulation)interaction.opponent;
		double[] oppstrategies = opponent.strategies;
		int oppntraits = opponent.getNTraits();
		for( int i=0; i<group.size; i++ )
			System.arraycopy(oppstrategies, group.group[i]*oppntraits, groupStrat, i*oppntraits, oppntraits);
		System.arraycopy(strategies, group.focal*nTraits, myType, 0, nTraits);
	}

	// scratch variable to calculate payoffs - CPopulation needs access to these
	/**
	 *
	 */
	protected	double[] groupStrat;

	/**
	 *
	 */
	protected	double[] myType;

	/**
	 *
	 */
	protected	double[] smallStrat;

	@Override
	protected void playGameAt(Group group) {
		if( group.size<=0 ) {
			updateScoreAt(group.focal, 0.0);
			return;
		}

		double myScore;
		int me = group.focal;
		gatherPlayers(group);
		CXPopulation opponent = (CXPopulation)interaction.opponent;

		switch( group.samplingType ) {
			// interact with all neighbors - interact repeatedly if nGroup<groupSize+1
			case Group.SAMPLING_ALL:
				if( pairwise ) {
					myScore = pairScores(myType, groupStrat, group.size, groupScores);
					updateScoreAt(me, myScore, group.size);
					for( int i=0; i<group.size; i++ )
						opponent.updateScoreAt(group.group[i], groupScores[i]);
					return;
				}
				if( nGroup<group.size+1 ) {	// interact with part of group sequentially
					myScore = 0.0;
					Arrays.fill(smallScores, 0, group.size, 0.0);
					for( int n=0; n<group.size; n++ ) {
						for( int i=0; i<nGroup-1; i++ )
							System.arraycopy(groupStrat, ((n+i)%group.size)*nTraits, smallStrat, i*nTraits, nTraits);
						myScore += groupScores(myType, smallStrat, nGroup-1, groupScores);
						for( int i=0; i<nGroup-1; i++ )
							smallScores[(n+i)%group.size] += groupScores[i];
					}
					updateScoreAt(me, myScore, group.size);
					for( int i=0; i<group.size; i++ )
						opponent.updateScoreAt(group.group[i], smallScores[i], nGroup-1);
					return;
				}
				// interact with full group (random graphs)
				myScore = groupScores(myType, groupStrat, group.size, groupScores);
				updateScoreAt(me, myScore);
				for( int i=0; i<group.size; i++ )
					opponent.updateScoreAt(group.group[i], groupScores[i]);
				return;

			// interact only with the randomly chosen group
			case Group.SAMPLING_COUNT:
				if( pairwise ) {	// interact pairwise
					myScore = pairScores(myType, groupStrat, group.size, groupScores);
					updateScoreAt(me, myScore, group.size);
				}
				else {
					myScore = groupScores(myType, groupStrat, group.size, groupScores);
					updateScoreAt(me, myScore);
				}
				for( int i=0; i<group.size; i++ )
					opponent.updateScoreAt(group.group[i], groupScores[i]);
				return;

			default:
				throw new Error("Unknown interaction type ("+getInteractionType()+")");
		}
	}

	@Override
	protected void yalpPairGameAt(Group group) {
		gatherPlayers(group);

		CXPopulation opponent = (CXPopulation)interaction.opponent;
		// note: focal score is not needed as it is simply reset (could be used for debugging though)
		pairScores(myType, groupStrat, group.size, groupScores);
		resetScoreAt(group.focal);
		if( interaction.isUndirected ) {
			// this is an optimization for undirected graphs that saves us one call to this routine
			for( int i=0; i<group.size; i++ )
				opponent.removeScoreAt(group.group[i], 2.0*groupScores[i], 2);
			return;
		}
		for( int i=0; i<group.size; i++ )
			opponent.removeScoreAt(group.group[i], groupScores[i]);
	}

	@Override
	protected void yalpGroupGameAt(Group group) {
		double myScore;
		gatherPlayers(group);

		CXPopulation opponent = (CXPopulation)interaction.opponent;
		if( nGroup<group.size+1 ) {	// interact with part of group sequentially
			myScore = 0.0;
			Arrays.fill(smallScores, 0, group.size, 0.0);
			for( int n=0; n<group.size; n++ ) {
				for( int i=0; i<nGroup-1; i++ )
					System.arraycopy(groupStrat, ((n+i)%group.size)*nTraits, smallStrat, i*nTraits, nTraits);
				myScore += groupScores(myType, smallStrat, nGroup-1, groupScores);
				for( int i=0; i<nGroup-1; i++ )
					smallScores[(n+i)%group.size] += groupScores[i];
			}
			removeScoreAt(group.focal, myScore, group.size);
			for( int i=0; i<group.size; i++ )
				opponent.removeScoreAt(group.group[i], smallScores[i], nGroup-1);
			return;
		}
		// interact with full group (random graphs)
		myScore = groupScores(myType, groupStrat, group.size, groupScores);
		removeScoreAt(group.focal, myScore);
		for( int i=0; i<group.size; i++ )
			opponent.removeScoreAt(group.group[i], groupScores[i]);
	}

	@Override
	public void prepareStrategies() {
		System.arraycopy(strategies, 0, strategiesScratch, 0, nPopulation*nTraits);
	}

	@Override
	public void commitStrategies() {
		double[] swap = strategies;
		strategies = strategiesScratch;
		strategiesScratch = swap;
	}

	@Override
	public void commitStrategyAt(int me) {
		int idx = me*nTraits;
		System.arraycopy(strategiesScratch, idx, strategies, idx, nTraits);
	}

	/**
	 *
	 * @param allstrat
	 */
	public void getStrategies(double[] allstrat) {
		System.arraycopy(strategies, 0, allstrat, 0, nPopulation*nTraits);
	}

	@Override
	public synchronized <T> void getTraitData(T[] colors, ColorMap<T> colorMap) {
		// only MODEL_SIMUALTION available at this time		
		colorMap.translate(strategies, colors);
	}

	/**
	 *
	 * @param bins
	 */
	public void getTraitHistogramData(double[][] bins) {
		// clear bins
		for( int n=0; n<nTraits; n++ ) Arrays.fill(bins[n], 0.0);
		int nBins = bins[0].length;
		double scale = (nBins-1);
		double norm = 1.0/nPopulation;
		// fill bins
		for( int d=0; d<nTraits; d++ ) {
			for( int n=d; n<nPopulation*nTraits; n+=nTraits )
				// continuous strategies are stored in normalized form
				bins[d][(int)(strategies[n]*scale+0.5)]++;
			for( int n=0; n<nBins; n++ ) bins[d][n] *= norm;
		}
	}

	/**
	 *
	 * @param bins
	 */
	public void getTraitHistogramData(double[] bins) {
		// clear bins
		Arrays.fill(bins, 0.0);
		int nBins = bins.length/nTraits;
		double scale = (nBins-1);
		double incr = 1.0/nPopulation;
		int shift = 0;
		// fill bins
		for( int d=0; d<nTraits; d++ ) {
			for( int n=d; n<nPopulation*nTraits; n+=nTraits )
				// continuous strategies are stored in normalized form
				bins[shift+(int)(strategies[n]*scale+0.5)] += incr;
			shift += nBins;
		}
	}

	/**
	 * create 2D histogram for trait1 and trait2
	 *
	 * @param bins
	 * @param trait1
	 * @param trait2
	 */
	public void getTraitHistogramData(double[] bins, int trait1, int trait2) {
		// clear bins
		Arrays.fill(bins, 0.0);
		int size = (int)Math.sqrt(bins.length);
		double scale = (size-1);
		double incr = 1.0/nPopulation;
		for( int n=0; n<nPopulation*nTraits; n+=nTraits )
			bins[(int)(strategies[n+trait2]*scale+0.5)*size+(int)(strategies[n+trait1]*scale+0.5)] += incr;
	}

	/**
	 *
	 * @param colors
	 * @param cMap
	 * @param bins
	 * @param auto
	 */
	public synchronized <T> void getTraitDensityData(T[] colors, ColorMap<T> cMap, double[] bins, boolean auto) {
		int dim = (int)(Math.pow(colors.length, 1.0/nTraits)+0.5);
		Arrays.fill(bins, 0.0);
		double ddim1 = (dim-1);
		int idx = 0;
		for( int n=0; n<nPopulation; n++ ) {
			int bin = (int)(strategies[idx++]*ddim1+0.5);
			for( int i=1; i<nTraits; i++ ) {
				bin *= dim;
				bin += (int)(strategies[idx++]*ddim1+0.5);
			}
			bins[bin]++;
		}
		if( auto ) cMap.setRange(0.0, ArrayMath.max(bins));
		cMap.translate(bins, colors);
	}

	@Override
	public String getTraitNameAt(int index) {
		String aName = "";
		int idx = index*nTraits;
		for( int i=0; i<(nTraits-1); i++ ) {
			aName += getTraitName(i)+" → "+Formatter.format(strategies[idx+i]*(traitMax[i]-traitMin[i])+traitMin[i], 3)+", ";
		}
		aName += getTraitName(nTraits-1)+" → "+Formatter.format(strategies[idx+nTraits-1]*(traitMax[nTraits-1]-traitMin[nTraits-1])+traitMin[nTraits-1], 3);
		return aName;
	}

	@Override
	public boolean getMeanTrait(double[] mean) {
		int midx = 0; 
		for( int i=0; i<nTraits; i++ ) {
			int idx = i;
			double avg = 0.0, var = 0.0;
			for( int n=1; n<=nPopulation; n++ ) {
				double aStrat = strategies[idx];
				double delta = aStrat-avg;
				avg += delta/n;
				var += delta*(aStrat-avg);
				idx += nTraits;
			}
			double scale = traitMax[i]-traitMin[i];
			double shift = traitMin[i];
			mean[midx++] = avg*scale+shift;
			mean[midx++] = Math.sqrt(var/(nPopulation-1))*scale;
		}
		return true;
	}

	@Override
	public boolean getMeanFitness(double[] mean) {
		double avg = 0.0, var = 0.0;
		for( int n=0; n<nPopulation; n++ ) {
			double aScore = scores[n];
			double delta = aScore-avg;
			avg += delta/(n+1);
			var += delta*(aScore-avg);
		}
		mean[0] = avg;
		mean[1] = Math.sqrt(var/(nPopulation-1));
		return true;
	}

	double[] statusInfo = new double[3];
	private double[] meantrait;

	@Override
	protected String _getStatus() {
		getMeanTrait(meantrait);
		String status = getTraitName(0)+" mean: "+Formatter.formatFix(meantrait[0], 3)+
				" ± "+Formatter.formatFix(meantrait[1], 3);
		for( int i=1; i<nTraits; i++ )
			status += "; "+getTraitName(i)+" mean: "+Formatter.formatFix(meantrait[2*i], 3)+
					" ± "+Formatter.formatFix(meantrait[2*i+1], 3);
		return status;
	}

	@Override
	public boolean check() {
		boolean doReset = false;
		// minimum and maximum game scores will be determined by super.checkParams()
		// verify trait minimums and maximums
		for( int s=0; s<nTraits; s++ ) {
			if( traitMax[s]<=traitMin[s] ) {
				// set to default
				logger.warning("invalid trait range ["+Formatter.format(traitMin[s], 4)+", "+
						Formatter.format(traitMax[s], 4)+"] for trait "+s+" - reset to [0, 1]!");
				setTraitRange(0.0, 1.0, s);
				doReset = true;
			}
			if( initMean[s]>traitMax[s] || initMean[s]<traitMin[s] ) {
				double newmean = Math.min(traitMax[s], Math.max(initMean[s], traitMin[s]));
				logger.warning("initial mean ("+Formatter.format(initMean[s], 4)+") not in trait range ["+Formatter.format(traitMin[s], 4)+", "+
						Formatter.format(traitMax[s], 4)+"] for trait "+s+" - changed to "+Formatter.format(newmean, 4)+"!");
				setInitMean(newmean, s);
			}
		}
		doReset |= super.check();

		// optimizations currently not feasible for continuous strategies
		if (optimizeHomo || optimizeMoran) {
			logger.warning("optimizations currently only for discrete strategies available - disabled.");
			doReset = true;
		}
		optimizeHomo = false;
		optimizeMoran = false;

		// check interaction geometry
		if( interaction.geometry==Geometry.MEANFIELD && interactionGroup.samplingType==Group.SAMPLING_ALL ) {
			// interacting with everyone in mean-field simulations is not feasible - except for discrete strategies
			logger.warning("interaction type ("+getInteractionType()+") unfeasible in well-mixed populations!");
			setInteractionType(Group.SAMPLING_COUNT);
			// change of sampling may affect whether scores can be adjusted
			adjustScores = doAdjustScores();
		}
		return doReset;
	}

	/**
	 *
	 * @param mean
	 */
	public void setInitMean(double[] mean) {
		System.arraycopy(mean, 0, initMean, 0, nTraits);
	}

	/**
	 *
	 * @param mean
	 */
	public void setInitMean(double mean) {
		Arrays.fill(initMean, mean);
	}

	/**
	 *
	 * @param mean
	 * @param trait
	 */
	public void setInitMean(double mean, int trait) {
		initMean[trait] = mean;
	}

	/**
	 *
	 * @return
	 */
	public double[] getInitMean() {
		return initMean;
	}

	/**
	 *
	 * @param trait
	 * @return
	 */
	public double getInitMean(int trait) {
		return initMean[trait];
	}

	/**
	 *
	 * @param sdev
	 */
	public void setInitSdev(double[] sdev) {
		System.arraycopy(sdev, 0, initSdev, 0, nTraits);
	}

	/**
	 *
	 * @param sdev
	 */
	public void setInitSdev(double sdev) {
		Arrays.fill(initSdev, sdev);
	}

	/**
	 *
	 * @param sdev
	 * @param trait
	 */
	public void setInitSdev(double sdev, int trait) {
		initSdev[trait] = sdev;
	}

	/**
	 *
	 * @return
	 */
	public double[] getInitSdev() {
		return initSdev;
	}

	/**
	 *
	 * @param trait
	 * @return
	 */
	public double getInitSdev(int trait) {
		return initSdev[trait];
	}

	/**
	 *
	 * @param type
	 */
	public void setMutationType(int type) {
		if( type==mutationType ) return;
		if( cloMutationType.isValidKey(type) ) {
			mutationType = type;
			return;
		}
		// type not found... ignore
		logger.warning("mutation type '"+type+"' is invalid - ignored, using '"+mutationType+"'.");
	}

	/**
	 *
	 * @return
	 */
	public int getMutationType() {
		return mutationType;
	}

	/**
	 *
	 * @param sdev
	 */
	public void setMutationSdev(double[] sdev) {
		for( int s=0; s<nTraits; s++ ) mutSdev[s] = sdev[s]/(traitMax[s]-traitMin[s]);
	}

	/**
	 *
	 * @param sdev
	 */
	public void setMutationSdev(double sdev) {
		for( int s=0; s<nTraits; s++ ) mutSdev[s] = sdev/(traitMax[s]-traitMin[s]);
	}

	/**
	 *
	 * @return
	 */
	public double[] getMutationSdev() {
		double[] scaledsdev = new double[nTraits];
		for( int s=0; s<nTraits; s++ ) scaledsdev[s] = mutSdev[s]*(traitMax[s]-traitMin[s]);
		return scaledsdev;
	}

	/**
	 *
	 * @param trait
	 * @return
	 */
	public double getMutationSdev(int trait) {
		return mutSdev[trait]*(traitMax[trait]-traitMin[trait]);
	}

	/**
	 *
	 * @param max
	 */
	public void setTraitMax(double max) {
		for( int s=0; s<nTraits; s++ ) mutSdev[s] *= (traitMax[s]-traitMin[s])/(max-traitMin[s]);
		Arrays.fill(traitMax, max);
		extremalScoresSet = false;	// update extremal scores
	}

	/**
	 *
	 * @param max
	 */
	public void setTraitMax(double[] max) {
		for( int s=0; s<nTraits; s++ ) mutSdev[s] *= (traitMax[s]-traitMin[s])/(max[s]-traitMin[s]);
		System.arraycopy(max, 0, traitMax, 0, nTraits);
		extremalScoresSet = false;	// update extremal scores
	}

	/**
	 *
	 * @return
	 */
	public double[] getTraitMax() {
		return traitMax;
	}

	/**
	 *
	 * @param trait
	 * @return
	 */
	public double getTraitMax(int trait) {
		return traitMax[trait];
	}

	/**
	 *
	 * @param min
	 */
	public void setTraitMin(double min) {
		for( int s=0; s<nTraits; s++ ) mutSdev[s] *= (traitMax[s]-traitMin[s])/(traitMax[s]-min);
		Arrays.fill(traitMin, min);
		extremalScoresSet = false;	// update extremal scores
	}

	/**
	 *
	 * @param min
	 */
	public void setTraitMin(double[] min) {
		for( int s=0; s<nTraits; s++ ) mutSdev[s] *= (traitMax[s]-traitMin[s])/(traitMax[s]-min[s]);
		System.arraycopy(min, 0, traitMin, 0, nTraits);
		extremalScoresSet = false;	// update extremal scores
	}

	/**
	 *
	 * @return
	 */
	public double[] getTraitMin() {
		return traitMin;
	}

	/**
	 *
	 * @param trait
	 * @return
	 */
	public double getTraitMin(int trait) {
		return traitMin[trait];
	}

	/**
	 *
	 * @param min
	 * @param max
	 * @param trait
	 */
	public void setTraitRange(double min, double max, int trait) {
		mutSdev[trait] *= (traitMax[trait]-traitMin[trait])/(max-min);
		traitMax[trait] = max;
		traitMin[trait] = min;
		extremalScoresSet = false;	// update extremal scores
	}

	/**
	 * @param costfcn
	 * @param trait
	 */
	public void setCostFunctionType(int costfcn, int trait) {
		if( trait<0 || trait>=nTraits ) {
			logger.warning("invalid trait for cost function type.");
			return;
		}
		if( costFcnType[trait]!=costfcn ) {
			extremalScoresSet = false;	// update extremal scores
			costFcnType[trait] = costfcn;
		}
	}

	/**
	 * @param costfcn
	 */
	public void setCostFunctionType(int costfcn) {
		for( int s=0; s<nTraits; s++ ) {
			extremalScoresSet &= costFcnType[s]!=costfcn;
			costFcnType[s] = costfcn;
		}
	}

	/**
	 *
	 * @return
	 */
	public int[] getCostFunctionType() {
		return costFcnType;
	}

	/**
	 *
	 * @param trait
	 * @return
	 */
	public int getCostFunctionType(int trait) {
		return costFcnType[trait];
	}

	/**
	 *
	 * @param aVector
	 * @param trait
	 */
	public void setCostParams(double[] aVector, int trait) {
		int len = Math.min(aVector.length, MAX_PARAMS);
		for( int i=0; i<len; i++ ) {
			if( Math.abs(aVector[i]-ci[trait][i])<1e-8 ) continue;
			ci[trait][i] = aVector[i];
			extremalScoresSet = false;	// update extremal scores
		}
		if( len<MAX_PARAMS ) Arrays.fill(ci[trait], len, MAX_PARAMS, 0.0);
	}

	/**
	 *
	 * @param trait
	 * @return
	 */
	public double[] getCostParams(int trait) {
		return clone(ci[trait]);
	}

	/**
	 *
	 * @param aMat
	 */
	public void setCostParams(double[][] aMat) {
		for( int n=0; n<nTraits; n++ ) setCostParams(aMat[n], n);
	}

	/**
	 *
	 * @return
	 */
	public double[][] getCostParams() {
		return clone(ci);
	}

	/**
	 * @param benefitfcn
	 * @param trait
	 */
	public void setBenefitFunctionType(int benefitfcn, int trait) {
		if( trait<0 || trait>=nTraits ) {
			logger.warning("invalid trait for benefit function type.");
			return;
		}
		if( benefitFcnType[trait]!=benefitfcn ) {
			extremalScoresSet = false;	// update extremal scores
			benefitFcnType[trait] = benefitfcn;
		}
	}

	/**
	 * @param benefitfcn
	 */
	public void setBenefitFunctionType(int benefitfcn) {
		for( int s=0; s<nTraits; s++ ) {
			extremalScoresSet &= benefitFcnType[s]!=benefitfcn;
			benefitFcnType[s] = benefitfcn;
		}
	}

	/**
	 *
	 * @return
	 */
	public int[] getBenefitFunctionType() {
		return benefitFcnType;
	}

	/**
	 *
	 * @param trait
	 * @return
	 */
	public int getBenefitFunctionType(int trait) {
		return benefitFcnType[trait];
	}

	/**
	 *
	 * @param aVector
	 * @param trait
	 */
	public void setBenefitParams(double[] aVector, int trait) {
		int len = Math.min(aVector.length, MAX_PARAMS);
		for( int i=0; i<len; i++ ) {
			if( Math.abs(aVector[i]-bi[trait][i])<1e-8 ) continue;
			bi[trait][i] = aVector[i];
			extremalScoresSet = false;	// update extremal scores
		}
		if( len<MAX_PARAMS ) Arrays.fill(bi[trait], len, MAX_PARAMS, 0.0);
	}

	/**
	 *
	 * @param trait
	 * @return
	 */
	public double[] getBenefitParams(int trait) {
		return clone(bi[trait]);
	}

	/**
	 *
	 * @param aMat
	 */
	public void setBenefitParams(double[][] aMat) {
		for( int n=0; n<nTraits; n++ ) setBenefitParams(aMat[n], n);
	}

	/**
	 *
	 * @return
	 */
	public double[][] getBenefitParams() {
		return clone(bi);
	}

	@Override
	public boolean alloc() {
		boolean changed = super.alloc();
		if( strategies==null || strategies.length!=nPopulation*nTraits )
			strategies = new double[nPopulation*nTraits];
		if( strategiesScratch==null || strategiesScratch.length!=nPopulation*nTraits )
			strategiesScratch = new double[nPopulation*nTraits];
		if( strategiesSwap==null || strategiesSwap.length!=nTraits )
			strategiesSwap = new double[nTraits];
		if( myType==null || myType.length!=nTraits )
			myType = new double[nTraits];
		// groupScores have the same maximum length
		int maxGroup = groupScores.length;
		if( groupStrat==null || groupStrat.length!=maxGroup*nTraits )
			groupStrat = new double[maxGroup*nTraits];
		if( smallStrat==null || smallStrat.length!=maxGroup*nTraits )
			smallStrat = new double[maxGroup*nTraits];
		if( meantrait==null || meantrait.length!=2*nTraits )
			meantrait = new double[2*nTraits];
		return changed;
	}

	@Override
	public void dealloc() {
		super.dealloc();
		strategies = null;
		strategiesScratch = null;
		strategiesSwap = null;
		myType = null;
		groupStrat = null;
		smallStrat = null;
		meantrait = null;
	}

	@Override
	public void init() {
		super.init();
		// initialize each trait
		for( int s=0; s<nTraits; s++ ) {
			double scaledmean = (initMean[s]-traitMin[s])/(traitMax[s]-traitMin[s]);
			if( initSdev[s]<0.0 ) {
				for( int n=s; n<nPopulation*nTraits; n += nTraits )
					strategies[n] = random01();
			}
			else {
				double scaledsdev = initSdev[s]/(traitMax[s]-traitMin[s]);
				for( int n=s; n<nPopulation*nTraits; n += nTraits )
					strategies[n] = Math.min(1.0, Math.max(0.0, randomGaussian(scaledmean, scaledsdev)));
			}
		}
	}


	@Override
	public void encodeStrategies(StringBuilder plist) {
		plist.append(EvoLudo.encodeKey("Configuration", strategies));
	}

	@Override
	public boolean restoreStrategies(Plist plist) {
		@SuppressWarnings("unchecked")
		List<Double> strat = (List<Double>)plist.get("Configuration");
		int size = nPopulation*nTraits;
		if( strat==null || strat.size()!=size )
			return false;
		for( int n=0; n<size; n++ )
			strategies[n] = strat.get(n);
		return true;
	}

	/*
	 * command line parsing stuff
	 */
	protected final CLOption cloMutationType = new CLOption("mutationtype", CLOption.Argument.REQUIRED, "g",
			"--mutationtype <t>        mutation type (u: uniform, g: gaussian)",
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseMutationType(arg, CXPopulation.this);
		}
		@Override
		public void report(PrintStream output) {
			output.println("# mutationtype:         "+cloMutationType.getDescriptionKey(mutationType));
		}
	});
	protected final CLOption cloMutationSdev = new CLOption("mutationsdev", CLOption.Argument.REQUIRED, "0.01", null,
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseMutationSdev(arg, CXPopulation.this);
		}
		@Override
		public void report(PrintStream output) {
			double[] fvec = getMutationSdev();
			String msg = "# mutationsdev:         "+Formatter.format(fvec[0], 4);
			for( int n=1; n<nTraits; n++ )
				msg += ":"+Formatter.format(fvec[n], 4);
			output.println(msg);
		}
	});
	protected final CLOption cloTraitMin = new CLOption("traitmin", CLOption.Argument.REQUIRED, "0", null,
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseTraitMin(arg, CXPopulation.this);
		}
		@Override
		public void report(PrintStream output) {
			double[] fvec = getTraitMin();
			String msg = "# traitmin:             "+Formatter.format(fvec[0], 4);
			for( int n=1; n<nTraits; n++ )
				msg += ":"+Formatter.format(fvec[n], 4);
			output.println(msg);
		}
	});
	protected final CLOption cloTraitMax = new CLOption("traitmax", CLOption.Argument.REQUIRED, "1", null,
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseTraitMax(arg, CXPopulation.this);
		}
		@Override
		public void report(PrintStream output) {
			double[] fvec = getTraitMax();
			String msg = "# traitmax:             "+Formatter.format(fvec[0], 4);
			for( int n=1; n<nTraits; n++ )
				msg += ":"+Formatter.format(fvec[n], 4);
			output.println(msg);
		}
	});
	protected final CLOption cloInitMean = new CLOption("initmean", CLOption.Argument.REQUIRED, "0.1", null,
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseInitMean(arg, CXPopulation.this);
		}
		@Override
		public void report(PrintStream output) {
			double[] fvec = getInitMean();
			String msg = "# initmean:             "+Formatter.format(fvec[0], 4);
			for( int n=1; n<nTraits; n++ )
				msg += ":"+Formatter.format(fvec[n], 4);
			output.println(msg);
		}
	});
	protected final CLOption cloInitSdev = new CLOption("initsdev", CLOption.Argument.REQUIRED, "0.1", null,
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseInitSdev(arg, CXPopulation.this);
		}
		@Override
		public void report(PrintStream output) {
			double[] fvec = getInitSdev();
			String msg = "# initsdev:             "+Formatter.format(fvec[0], 4);
			for( int n=1; n<nTraits; n++ )
				msg += ":"+Formatter.format(fvec[n], 4);
			output.println(msg);
		}
	});
	protected final CLOption cloCostFunction = new CLOption("costfcn", 'C', CLOption.Argument.REQUIRED, "1", null,
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseCostFcnType(arg, CXPopulation.this);
		}
		@Override
		public void report(PrintStream output) {
			int[] ivec = getCostFunctionType();
			String msg = "# costfunction:         "+cloCostFunction.getDescriptionKey(ivec[0]);
			for( int n=1; n<nTraits; n++ )
				msg += ":"+cloCostFunction.getDescriptionKey(ivec[n]);
			output.println(msg);
		}
	});
	protected final CLOption cloCostParams = new CLOption("costparams", 'c', CLOption.Argument.REQUIRED, "0",
			"--costparams, -c <c0>,<c1>,...<cn>     parameters for cost function\n"+
					"                  different traits separated by ';' (max 10 per trait)",
					new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseCostParams(arg, CXPopulation.this);
		}
		@Override
		public void report(PrintStream output) {
			double[] dvec = getCostParams(0);
			String msg = "# costparams:           "+Formatter.format(dvec, 6);
			for( int n=1; n<nTraits; n++ ) {
				dvec = getCostParams(n);
				msg += ";"+Formatter.format(dvec, 6);
			}
			output.println(msg);
		}
	});
	protected final CLOption cloBenefitFunction = new CLOption("benefitfcn", 'B', CLOption.Argument.REQUIRED, "11", null,
			new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseBenefitFcnType(arg, CXPopulation.this);
		}
		@Override
		public void report(PrintStream output) {
			int[] ivec = getBenefitFunctionType();
			String msg = "# benefitfunction:      "+cloBenefitFunction.getDescriptionKey(ivec[0]);
			for( int n=1; n<nTraits; n++ ) 
				msg += ":"+cloBenefitFunction.getDescriptionKey(ivec[n]);
			output.println(msg);
		}
	});
	protected final CLOption cloBenefitParams = new CLOption("benefitparams", 'b', CLOption.Argument.REQUIRED, "0",
			"--benefitparams, -b <b0>,<b1>,...<bn>  parameters for benefit function\n"+
					"                  different traits separated by ';', max 10 per trait",
					new CLODelegate() {
		@Override
		public boolean parse(String arg) {
			return parseBenefitParams(arg, CXPopulation.this);
		}
		@Override
		public void report(PrintStream output) {
			double[] dvec = getBenefitParams(0);
			String msg = "# benefitparams:        "+Formatter.format(dvec, 6);
			for( int n=1; n<nTraits; n++ ) {
				dvec = getBenefitParams(n);
				msg += ";"+Formatter.format(dvec, 6);
			}
			output.println(msg);
		}
	});

	// override this method in subclasses to add further command line options
	// subclasses must make sure that they include a call to super
	@Override
	public void collectCLO(CLOParser parser) {
		// prepare command line options
		String descr;
		switch( nTraits ) {
			case 1:
				cloMutationSdev.setDescription("--mutationsdev <s>   sdev of mutations in trait "+getTraitName(0));
				cloTraitMin.setDescription("--traitmin <m>       minimum of trait "+getTraitName(0));
				cloTraitMax.setDescription("--traitmax <m>       maximum of trait "+getTraitName(0));
				cloInitMean.setDescription("--initmean <m>       initial mean of trait "+getTraitName(0));
				cloInitSdev.setDescription("--initsdev <s>       initial sdev of trait "+getTraitName(0));
				cloCostFunction.setDescription("--costfcn, -C <s>    cost function of trait "+getTraitName(0)+"\n"+
						"  cost functions: <s>\n"+cloCostFunction.getDescriptionKey());
				cloBenefitFunction.setDescription("--benefitfcn, -B <s> benefit function of trait "+getTraitName(0)+"\n"+
						"  benefit functions: <s>\n"+cloBenefitFunction.getDescriptionKey());
				break;
			case 2:
				cloMutationSdev.setDescription("--mutationsdev <s0>,<s1> sdev of mutations in each trait, with\n"+
						"      0: "+getTraitName(0)+"\n"+
						"      1: "+getTraitName(1));
				cloTraitMin.setDescription("--traitmin <m0>,<m1> minimum of each trait, with\n"+
						"      0: "+getTraitName(0)+"\n"+
						"      1: "+getTraitName(1));
				cloTraitMax.setDescription("--traitmax <m0>,<m1> maximum of each trait, with\n"+
						"      0: "+getTraitName(0)+"\n"+
						"      1: "+getTraitName(1));
				cloInitMean.setDescription("--initmean <m0>,<m1> initial mean of each trait, with\n"+
						"      0: "+getTraitName(0)+"\n"+
						"      1: "+getTraitName(1));
				cloInitSdev.setDescription("--initsdev <m0>,<m1> initial sdev of each trait, with\n"+
						"      0: "+getTraitName(0)+"\n"+
						"      1: "+getTraitName(1));
				cloCostFunction.setDescription("--costfcn, -C <s0>,<s1> cost function of traits\n"+
						"      0: "+getTraitName(0)+"\n"+
						"      1: "+getTraitName(1)+"\n"+
						"  cost functions: <s>\n"+cloCostFunction.getDescriptionKey());
				cloBenefitFunction.setDescription("--benefitfcn, -B <s0>,<s1> benefit function of traits\n"+
						"      0: "+getTraitName(0)+"\n"+
						"      1: "+getTraitName(1)+"\n"+
						"  benefit functions: <s>\n"+cloBenefitFunction.getDescriptionKey());
				break;
			default:
				descr = "--mutationsdev <s0>,...,<s"+(nTraits-1)+">  sdev of mutations in each trait, with\n";
				for( int n=0; n<nTraits-1; n++ )
					descr += "      "+n+": "+getTraitName(n)+"\n";
				descr += "      "+(nTraits-1)+": "+getTraitName(nTraits-1);
				cloMutationSdev.setDescription(descr);
				descr = "--traitmin <m0>,...,<m"+(nTraits-1)+">  minimum of each trait, with\n";
				for( int n=0; n<nTraits-1; n++ )
					descr += "      "+n+": "+getTraitName(n)+"\n";
				descr += "      "+(nTraits-1)+": "+getTraitName(nTraits-1);
				cloTraitMin.setDescription(descr);
				descr = "--traitmax <m0>,...,<m"+(nTraits-1)+">  maximum of each trait, with\n";
				for( int n=0; n<nTraits-1; n++ )
					descr += "      "+n+": "+getTraitName(n)+"\n";
				descr += "      "+(nTraits-1)+": "+getTraitName(nTraits-1);
				cloTraitMax.setDescription(descr);
				descr = "--initmean <m0>,...,<m"+(nTraits-1)+">  initial mean of each trait, with\n";
				for( int n=0; n<nTraits-1; n++ )
					descr += "      "+n+": "+getTraitName(n)+"\n";
				descr += "      "+(nTraits-1)+": "+getTraitName(nTraits-1);
				cloInitMean.setDescription(descr);
				descr = "--initsdev <m0>,...,<m"+(nTraits-1)+">  initial sdev of each trait, with\n";
				for( int n=0; n<nTraits-1; n++ )
					descr += "      "+n+": "+getTraitName(n)+"\n";
				descr += "      "+(nTraits-1)+": "+getTraitName(nTraits-1)+
						"      (set to <0 for uniform distribution)";
				cloInitSdev.setDescription(descr);
				descr = "--costfcn, -C <s0>,...,<s"+(nTraits-1)+">  cost function of traits\n";
				for( int n=0; n<nTraits-1; n++ )
					descr += "      "+n+": "+getTraitName(n)+"\n";
				descr += "      "+(nTraits-1)+": "+getTraitName(nTraits-1);
				descr += "\n  cost functions: <s>\n"+cloCostFunction.getDescriptionKey();
				cloCostFunction.setDescription(descr);
				descr = "--benefitfcn, -B <s0>,...,<s"+(nTraits-1)+">  benefit function of traits\n";
				for( int n=0; n<nTraits-1; n++ )
					descr += "      "+n+": "+getTraitName(n)+"\n";
				descr += "      "+(nTraits-1)+": "+getTraitName(nTraits-1);
				descr += "\n  benefit functions: <s>\n"+cloBenefitFunction.getDescriptionKey();
				cloBenefitFunction.setDescription(descr);
		}

		parser.addCLO(cloMutationType);
		parser.addCLO(cloMutationSdev);
		parser.addCLO(cloTraitMin);
		parser.addCLO(cloTraitMax);
		parser.addCLO(cloInitMean);
		parser.addCLO(cloInitSdev);
		parser.addCLO(cloCostFunction);
		parser.addCLO(cloCostParams);
		parser.addCLO(cloBenefitFunction);
		parser.addCLO(cloBenefitParams);
		super.collectCLO(parser);
	}

	// CXPopulation parser methods

	static boolean parseMutationType(String arg, CXPopulation pop) {
		switch( arg.charAt(0) ) {
			case 'g':
				pop.setMutationType(MUTATION_GAUSSIAN);
				return true;
			case 'u':
				pop.setMutationType(MUTATION_UNIFORM);
				return true;
		}
		return false;
	}

	// treat string vectors
	private static double[] treatVector(String arg, CXPopulation pop) {
		double[] vec = CLOParser.parseVector(arg);
		int nTrait = pop.getNTraits();
		if( vec.length == 1 && nTrait > 1 ) {
			double val = vec[0];
			vec = new double[nTrait];
			Arrays.fill(vec, val);
		}
		return vec;
	}

	static boolean parseMutationSdev(String arg, CXPopulation pop) {
		double[] sdev = treatVector(arg, pop);
		if( sdev.length==1 ) {
			pop.setMutationSdev(sdev[0]);
			return true;
		}
		if( sdev.length>=pop.getNTraits() ) {
			pop.setMutationSdev(sdev);
			return true;
		}
		// we could return false to signal parsing problems		pop.setMutationSdev(sdev);
		pop.setMutationSdev(0.01*(pop.getTraitMax(0)-pop.getTraitMin(0)));
		return true;
	}

	static boolean parseTraitMin(String arg, CXPopulation pop) {
		double[] min = treatVector(arg, pop);
		if( min.length==1 ) {
			pop.setTraitMin(min[0]);
			return true;
		}
		if( min.length==pop.getNTraits() ) {
			pop.setTraitMin(min);
			return true;
		}
		// we could return false to signal parsing problems
		pop.setTraitMin(0.0);
		return true;
	}

	static boolean parseTraitMax(String arg, CXPopulation pop) {
		double[] max = treatVector(arg, pop);
		if( max.length==1 ) {
			pop.setTraitMax(max[0]);
			return true;
		}
		if( max.length==pop.getNTraits() ) {
			pop.setTraitMax(max);
			return true;
		}
		// we could return false to signal parsing problems
		pop.setTraitMax(1.0);
		return true;
	}

	static boolean parseInitMean(String arg, CXPopulation pop) {
		double[] mean = treatVector(arg, pop);
		if( mean.length == 0 )
			return false;
		pop.setInitMean(mean);
		return true;
	}

	static boolean parseInitSdev(String arg, CXPopulation pop) {
		double[] sdev = treatVector(arg, pop);
		if( sdev.length == 0 )
			return false;
		pop.setInitSdev(sdev);
		return true;
	}

	static boolean parseCostFcnType(String arg, CXPopulation pop) {
		String[] cstf = arg.split(CLOParser.VECTOR_DELIMITER);
		int ncstf = cstf.length;
		Logger logger = pop.getLogger();
		if( ncstf<1 ) {
			logger.warning("failed to parse cost function type '"+arg+"' - ignored.");
			return false;
		}
		int nTraits = pop.getNTraits();
		boolean success = true;
		for( int s=0; s<nTraits; s++ ) {
			String type = cstf[s % ncstf];
			if( pop.cloCostFunction.isValidKey(type) ) {
				pop.setCostFunctionType(CLOption.NUMBERED_KEY_OFFSET+Integer.parseInt(type), s);
				continue;
			}
			// type not found... ignore
			logger.warning("cost function type '"+type+"' of trait "+s+" is invalid - ignored.");
			success = false;
		}
		return success;
	}

	static boolean parseCostParams(String arg, CXPopulation pop) {
		double[][] cparams = CLOParser.parseMatrix(arg);
		if( cparams.length == 0 )
			return false;
		int nTraits = pop.getNTraits();
		// if only one set of parameters is specified use it for all traits
		if( cparams.length == 1 && nTraits > 1 ) {
			double[] params = cparams[0];
			int len = params.length;
			cparams = new double[nTraits][len];
			for( int n=0; n<nTraits; n++ )
				System.arraycopy(params, 0, cparams[n], 0, len);
		}
		if( cparams.length != nTraits )
			return false;
		pop.setCostParams(cparams);
		return true;
	}

	static boolean parseBenefitFcnType(String arg, CXPopulation pop) {
		String[] cstf = arg.split(CLOParser.VECTOR_DELIMITER);
		int ncstf = cstf.length;
		Logger logger = pop.getLogger();
		if( ncstf<1 ) {
			logger.warning("failed to parse benefit function type '"+arg+"' - ignored.");
			return false;
		}
		int nTraits = pop.getNTraits();
		boolean success = true;
		for( int s=0; s<nTraits; s++ ) {
			String type = cstf[s % ncstf];
			if( pop.cloBenefitFunction.isValidKey(type) ) {
				pop.setBenefitFunctionType(CLOption.NUMBERED_KEY_OFFSET+Integer.parseInt(type), s);
				continue;
			}
			// type not found... ignore
			logger.warning("benefit function type '"+type+"' of trait "+s+" is invalid - ignored.");
			success = false;
		}
		return success;
	}

	static boolean parseBenefitParams(String arg, CXPopulation pop) {
		double[][] bparams = CLOParser.parseMatrix(arg);
		if( bparams.length == 0 )
			return false;
		int nTraits = pop.getNTraits();
		// if only one set of parameters is specified use it for all traits
		if( bparams.length == 1 && nTraits > 1 ) {
			double[] params = bparams[0];
			int len = params.length;
			bparams = new double[nTraits][len];
			for( int n=0; n<nTraits; n++ )
				System.arraycopy(params, 0, bparams[n], 0, len);
		}
		if( bparams.length != nTraits )
			return false;
		pop.setBenefitParams(bparams);
		return true;
	}

	/* 
	 * private methods 
	 */

	private double cxMinScore = Double.MAX_VALUE;
	private double cxMaxScore = -Double.MAX_VALUE;
	private double cxMinMonoScore = Double.MAX_VALUE;
	private double cxMaxMonoScore = -Double.MAX_VALUE;
	private boolean extremalScoresSet = false;

	private void setExtremalScores() {
		cxMinScore = findExtremalScore(false);
		cxMaxScore = findExtremalScore(true);
		cxMinMonoScore = findExtremalMonoScore(false);
		cxMaxMonoScore = findExtremalMonoScore(true);
		extremalScoresSet = true;
	}

	/* brute force methods to determine minimum and maximum scores for 
	 * (a) two competing traits and
	 * (b) monomorphic populations
	 * if the boolean argument is true, the maximum is returned
	 */
	static final int MINMAX_STEPS = 10;
	static final int MINMAX_ITER = 5;

	private double findExtremalScore(boolean maximum) {
		double[][] resInterval = new double[nTraits][2];
		double[][] mutInterval = new double[nTraits][2];
		double[] resScale = new double[nTraits];
		double[] mutScale = new double[nTraits];
		double[] resTrait = new double[nTraits];
		double[] mutTrait = new double[nTraits];
		int[]	resIdx = new int[nTraits];
		int[]	mutIdx = new int[nTraits];
		int[]	resMax = new int[nTraits];
		int[]	mutMax = new int[nTraits];
		double	minmax = maximum?1.0:-1.0;
		double  scoreMax = -Double.MAX_VALUE;

		// initialize trait intervals
		for( int n=0; n<nTraits; n++ ) {
			resInterval[n][0] = 0;
			resInterval[n][1] = 1;
			mutInterval[n][0] = 0;
			mutInterval[n][1] = 1;
		}

		for( int i=0; i<MINMAX_ITER; i++ ) {
			for( int n=0; n<nTraits; n++ ) {
				resScale[n] = (resInterval[n][1]-resInterval[n][0])/MINMAX_STEPS;
				mutScale[n] = (mutInterval[n][1]-mutInterval[n][0])/MINMAX_STEPS;
			}
			Arrays.fill(resMax, -1);
			Arrays.fill(mutMax, -1);
			scoreMax = Math.max(scoreMax, 
					findExtrema(resTrait, mutTrait, resIdx, mutIdx, 
							resInterval, mutInterval, resScale, mutScale, 
							resMax, mutMax, -Double.MAX_VALUE, nTraits-1, minmax));
			// determine new intervals and scales
			for( int n=0; n<nTraits; n++ ) {
				switch( resIdx[n] ) {
					case 0:
						resInterval[n][1] = resInterval[n][0]+resScale[n];
						break;
					case MINMAX_STEPS:
						resInterval[n][0] += (MINMAX_STEPS-1)*resScale[n];
						break;
					default:
						resInterval[n][0] += (resIdx[n]-1)*resScale[n];
						resInterval[n][1] = resInterval[n][0]+2.0*resScale[n];
						break;
				}
				switch( mutIdx[n] ) {
					case 0:
						mutInterval[n][1] = mutInterval[n][0]+mutScale[n];
						break;
					case MINMAX_STEPS:
						mutInterval[n][0] += (MINMAX_STEPS-1)*mutScale[n];
						break;
					default:
						mutInterval[n][0] += (mutIdx[n]-1)*mutScale[n];
						mutInterval[n][1] = mutInterval[n][0]+2.0*mutScale[n];
						break;
				}
			}
		}
		return minmax*scoreMax;
	}

	private double findExtrema(
			double[] resTrait, double[] mutTrait,
			int[] resIdx, int[] mutIdx,
			double[][] resInterval, double[][] mutInterval,
			double[] resScale, double[] mutScale, 
			int[] resMax, int[] mutMax, 
			double scoreMax, int trait, double minmax) {

		for( int r=0; r<=MINMAX_STEPS; r++ ) {
			resIdx[trait] = r;
			resTrait[trait] = resInterval[trait][0]+r*resScale[trait];
			for( int m=0; m<=MINMAX_STEPS; m++ ) {
				mutIdx[trait] = m;
				mutTrait[trait] = mutInterval[trait][0]+m*mutScale[trait];
				if( trait > 0 ) {
					scoreMax = Math.max(scoreMax, 
							findExtrema(resTrait, mutTrait, resIdx, mutIdx, 
									resInterval, mutInterval, resScale, mutScale, 
									resMax, mutMax, scoreMax, trait-1, minmax));
					continue;
				}
				double traitScore = minmax*payoff(resTrait, mutTrait);
				if( traitScore>scoreMax ) {
					scoreMax = traitScore;
					int len = resIdx.length;	// nTraits
					System.arraycopy(resIdx, 0, resMax, 0, len);
					System.arraycopy(mutIdx, 0, mutMax, 0, len);
				}
			}
		}
		return scoreMax;
	}

	private double findExtremalMonoScore(boolean maximum) {
		double[][] resInterval = new double[nTraits][2];
		double[] resScale = new double[nTraits];
		double[] resTrait = new double[nTraits];
		int[]	resIdx = new int[nTraits];
		int[]	resMax = new int[nTraits];
		double	minmax = maximum?1.0:-1.0;
		double  scoreMax = -Double.MAX_VALUE;

		// initialize trait intervals
		for( int n=0; n<nTraits; n++ ) {
			resInterval[n][0] = 0;
			resInterval[n][1] = 1;
		}

		for( int i=0; i<MINMAX_ITER; i++ ) {
			for( int n=0; n<nTraits; n++ ) {
				resScale[n] = (resInterval[n][1]-resInterval[n][0])/MINMAX_STEPS;
			}
			Arrays.fill(resMax, -1);
			scoreMax = Math.max(scoreMax, findExtrema(resTrait, resIdx, resInterval, resScale, resMax, -Double.MAX_VALUE, nTraits-1, minmax));
			// determine new intervals and scales
			for( int n=0; n<nTraits; n++ ) {
				switch( resIdx[n] ) {
					case 0:
						resInterval[n][1] = resInterval[n][0]+resScale[n];
						break;
					case MINMAX_STEPS:
						resInterval[n][0] += (MINMAX_STEPS-1)*resScale[n];
						break;
					default:
						resInterval[n][0] += (resIdx[n]-1)*resScale[n];
						resInterval[n][1] = resInterval[n][0]+2.0*resScale[n];
						break;
				}
			}
		}
		return minmax*scoreMax;
	}

	private double findExtrema(
			double[] resTrait,
			int[] resIdx,
			double[][] resInterval,
			double[] resScale, 
			int[] resMax, 
			double scoreMax, int trait, double minmax) {

		for( int r=0; r<=MINMAX_STEPS; r++ ) {
			resIdx[trait] = r;
			resTrait[trait] = resInterval[trait][0]+r*resScale[trait];
			if( trait > 0 ) {
				scoreMax = Math.max(scoreMax, findExtrema(resTrait, resIdx, resInterval, resScale, resMax, scoreMax, trait-1, minmax));
				continue;
			}
			double traitScore = minmax*payoff(resTrait, resTrait);
			if( traitScore>scoreMax ) {
				scoreMax = traitScore;
				int len = resIdx.length;	// nTraits
				System.arraycopy(resIdx, 0, resMax, 0, len);
			}
		}
		return scoreMax;
	}
}
