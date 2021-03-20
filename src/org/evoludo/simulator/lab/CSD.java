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

package org.evoludo.simulator.lab;

import java.awt.Color;

import org.evoludo.simulator.CPopulation;
import org.evoludo.simulator.EvoLudo;
import org.evoludo.simulator.Species.IBSDelegate;
//import org.evoludo.simulator.views.HasConsole;
//import org.evoludo.simulator.views.HasDistribution;
//import org.evoludo.simulator.views.HasHistogram;
//import org.evoludo.simulator.views.HasMean;
//import org.evoludo.simulator.views.HasPop2D;
//import org.evoludo.simulator.views.HasPop3D;

/**
 *
 * @author Christoph Hauert
 */
public class CSD extends CPopulation implements IBSDelegate /*,
										HasPop2D.Strategy, HasPop3D.Strategy, HasMean.Strategy, 
										HasHistogram.Strategy, HasDistribution.Strategy,
										HasPop2D.Fitness, HasPop3D.Fitness, HasMean.Fitness, 
										HasHistogram.Fitness, HasHistogram.Degree, HasConsole*/ {

	public CSD(EvoLudo engine) {
		super(engine, "cSD");
	}

	@Override
	public void load() {
		super.load();
		// initialize
		pairwiseOnly = true;
		setTraitNames(new String[] { "Investment" });
		setTraitColors(new Color[] { Color.BLACK });
		// set defaults
		setCostFunctionType(PAYOFF_COST_ME_QUAD);
		cloCostParams.setDefault("4.56,-1.6");
		setBenefitFunctionType(PAYOFF_BENEFIT_WE_QUAD);
		cloBenefitParams.setDefault("6.0,-1.4");
	}

	@Override
	public String getInfo() {
		return "Title: "+getTitle()+"\nAuthor: Christoph Hauert\n"+
			   "Time evolution of continuous cooperative investments in different population structures.";
	}

	@Override
	public String getTitle() {
		return "Continuous Snowdrift";
	}

	@Override
	public String getVersion() {
		return org.evoludo.simulator.EvoLudo.COPYRIGHT
				+", v1.2 July 2005 ("+super.getVersion()+")";
	}

	@Override
	public double pairScores(double me, double[] group, int len, double[] payoffs) {
		double yourInvest;
		double myScore = 0.0;
		
		for( int n=0; n<len; n++ ) {
			yourInvest = group[n];
			myScore += payoff(me, yourInvest);
			payoffs[n] = payoff(yourInvest, me);
		}
		return myScore;
	}
}
