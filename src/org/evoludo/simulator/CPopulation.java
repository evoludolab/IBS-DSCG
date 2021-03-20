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

import java.util.Arrays;

/**
 *
 * @author Christoph Hauert
 */
public abstract class CPopulation extends CXPopulation {

	protected CPopulation(EvoLudo engine, String key) {
		super(engine, key);
	}

	protected CPopulation(Population partner) {
		super(partner);
	}

	/* simpler variants of the methods defined in CXPopulation */
	/**
	 *
	 * @param me
	 * @param strat
	 * @param len
	 * @param payoffs
	 * @return my payoff
	 */
	public double pairScores(double me, double[] strat, int len, double[] payoffs) {
		throw new Error("pairScores not implemented - this is an error.");
	}

	/**
	 * @param me
	 * @param group
	 * @param len
	 * @param payoffs
	 * @return
	 */
	public double groupScores(double me, double[] group, int len, double[] payoffs) {
		if( len==1 ) return pairScores(me, group, len, payoffs);
		throw new Error("groupScores not implemented - this is an error.");
	}

	@Override
	public final double pairScores(double[] me, double[] group, int len, double[] payoffs) {
		throw new Error("pairScores: use single trait implementation.");
	}

	@Override
	public final double groupScores(double[] me, double[] group, int len, double[] payoffs) {
		throw new Error("groupScores: use single trait implementation.");
	}

	/**
	 * Calculate my payoff for using strategy <code>me</code> against an opponent with strategy <code>you</code>.
	 * <p>
	 * <b>Note:</b> in order to avoid passing the trait index <code>0</code> (as well as dealing with
	 * an unnecessary overhead for a single trait) the cost/benefit functions would need to be defined 
	 * again.
	 * </p>
	 * 
	 * @param me my strategy
	 * @param you opponent's strategy
	 * @return my payoff
	 */
	protected double payoff(double me, double you) {
		return benefits(me, you, 0)-costs(me, you, 0);
	}

	// to avoid this, cost/benefit functions would need to be defined again in CPopulation
	/**
	 * Calculate my costs for using strategy <code>me</code> against an opponent with strategy <code>you</code>.
	 * <p>
	 * <b>Note:</b> in order to avoid passing the trait index <code>0</code> (as well as dealing with
	 * an unnecessary overhead for a single trait) the cost/benefit functions would need to be defined 
	 * again.
	 * </p>
	 * 
	 * @param me my strategy
	 * @param you opponent's strategy
	 * @return my costs
	 */
	protected double costs(double me, double you) {
		return costs(me, you, 0);
	}

	/**
	 * Calculate my benefits for using strategy <code>me</code> against an opponent with strategy <code>you</code>.
	 * <p>
	 * <b>Note:</b> in order to avoid passing the trait index <code>0</code> (as well as dealing with
	 * an unnecessary overhead for a single trait) the cost/benefit functions would need to be defined 
	 * again.
	 * </p>
	 * 
	 * @param me my strategy
	 * @param you opponent's strategy
	 * @return my benefits
	 */
	protected double benefits(double me, double you) {
		return benefits(me, you, 0);
	}

	/**
	 *
	 * @param trait
	 * @return
	 */
	public double getMonoGameScore(double trait) {
		throw new Error("getMonoGameScore not implemented - this is an error.");
	}

	/**
	 * NOTE: this hides vector-valued variables of the same name in Population
	 * and CXPopulation. This allows to deal with a single trait more efficiently.
	 */
	@SuppressWarnings("hiding")
	protected String traitName;	// defined as String[] in Population
	@SuppressWarnings("hiding")
	double	mutSdev = -1.0;	// defined as double[] in CXPopulation
	@SuppressWarnings("hiding")
	double	traitMin = -1.0;	// defined as double[] in CXPopulation
	@SuppressWarnings("hiding")
	double	traitMax = -1.0;	// defined as double[] in CXPopulation

	@Override
	protected boolean haveSameStrategy(int a, int b) {
		return (Math.abs(strategies[a]-strategies[b])<1e-8);
	}

	@Override
	protected boolean isSameStrategy(int a) {
		return (Math.abs(strategies[a]-strategiesScratch[a])<1e-8);
	}

	@Override
	protected void swapStrategies(int a, int b) {
		strategiesScratch[a] = strategies[b];
		strategiesScratch[b] = strategies[a];
	}

	@Override
	protected void playGameAt(Group group) {
		if( group.size<=0 ) return;

		double myScore;
		int me = group.focal;
		double myTrait = strategies[me];
		CPopulation opponent = (CPopulation)interaction.opponent;
		double[] oppstrategies = opponent.strategies;
		for( int i=0; i<group.size; i++ )
			groupStrat[i] = oppstrategies[group.group[i]];

		switch( group.samplingType ) {
			// interact with all neighbors - interact repeatedly if nGroup<groupSize+1
			case Group.SAMPLING_ALL:
				if( pairwise ) {
					myScore = pairScores(myTrait, groupStrat, group.size, groupScores);
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
							smallStrat[i] = groupStrat[(n+i)%group.size];
						myScore += groupScores(myTrait, smallStrat, nGroup-1, groupScores);
						for( int i=0; i<nGroup-1; i++ )
							smallScores[(n+i)%group.size] += groupScores[i];
					}
					updateScoreAt(me, myScore, group.size);
					for( int i=0; i<group.size; i++ )
						opponent.updateScoreAt(group.group[i], smallScores[i], nGroup-1);
					return;
				}
				// interact with full group (random graphs)
				myScore = groupScores(myTrait, groupStrat, group.size, groupScores);
				updateScoreAt(me, myScore);
				for( int i=0; i<group.size; i++ )
					opponent.updateScoreAt(group.group[i], groupScores[i]);
				return;

			// interact only with the randomly chosen group
			case Group.SAMPLING_COUNT:
				if( pairwise ) {	// interact pairwise
					myScore = pairScores(myTrait, groupStrat, group.size, groupScores);
					updateScoreAt(me, myScore, group.size);
				}
				else {
					myScore = groupScores(myTrait, groupStrat, group.size, groupScores);
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
		if( group.size<=0 ) return;

		CPopulation opponent = (CPopulation)interaction.opponent;
		double[] oppstrategies = opponent.strategies;
		for( int i=0; i<group.size; i++ )
			groupStrat[i] = oppstrategies[group.group[i]];

		// note: focal score is not needed as it is simply reset (could be used for debugging though)
		pairScores(strategies[group.focal], groupStrat, group.size, groupScores);
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
		if( group.size<=0 ) return;

		double myScore;
		double myTrait = strategies[group.focal];
		CPopulation opponent = (CPopulation)interaction.opponent;
		double[] oppstrategies = opponent.strategies;
		for( int i=0; i<group.size; i++ )
			groupStrat[i] = oppstrategies[group.group[i]];

		if( nGroup<group.size+1 ) {	// interact with part of group sequentially
			myScore = 0.0;
			Arrays.fill(smallScores, 0, group.size, 0.0);
			for( int n=0; n<group.size; n++ ) {
				for( int i=0; i<nGroup-1; i++ )
					smallStrat[i] = groupStrat[(n+i)%group.size];
				myScore += groupScores(myTrait, smallStrat, nGroup-1, groupScores);
				for( int i=0; i<nGroup-1; i++ )
					smallScores[(n+i)%group.size] += groupScores[i];
			}
			removeScoreAt(group.focal, myScore, group.size);
			for( int i=0; i<group.size; i++ )
				opponent.removeScoreAt(group.group[i], smallScores[i], nGroup-1);
			return;
		}
		// interact with full group (random graphs)
		myScore = groupScores(myTrait, groupStrat, group.size, groupScores);
		removeScoreAt(group.focal, myScore);

		for( int i=0; i<group.size; i++ )
			opponent.removeScoreAt(group.group[i], groupScores[i]);
	}

	@Override
	public boolean check() {
		boolean doReset = super.check();
		traitMax = super.traitMax[0];
		traitMin = super.traitMin[0];
		mutSdev = (super.getMutationSdev())[0];
		traitName = (super.getTraitNames())[0];
		return doReset;
	}

	@Override
	public void prepareStrategies() {
		System.arraycopy(strategies, 0, strategiesScratch, 0, nPopulation);
	}

	@Override
	public void commitStrategyAt(int me) {
		strategies[me] = strategiesScratch[me];
	}

	/**
	 *
	 * @param aVector
	 */
	public void setCostParams(double[] aVector) {
		super.setCostParams(aVector, 0);
	}

	/**
	 *
	 * @param aVector
	 */
	public void setBenefitParams(double[] aVector) {
		super.setBenefitParams(aVector, 0);
	}
}
