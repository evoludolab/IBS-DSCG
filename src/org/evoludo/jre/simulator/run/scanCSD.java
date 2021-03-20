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

package org.evoludo.jre.simulator.run;

import java.io.File;
import java.io.PrintStream;

//import org.evoludo.jre.graphics.AbstractGraph;
//import org.evoludo.jre.simulator.MVPop2D;
import org.evoludo.simulator.EvoLudo;
import org.evoludo.simulator.lab.CSD;
import org.evoludo.util.CLOParser;
import org.evoludo.util.CLOption;
import org.evoludo.util.CLOption.CLODelegate;
import org.evoludo.util.Formatter;
import org.evoludo.util.ArrayMath;
import org.evoludo.util.Distributions;

/**
 *
 * @author Christoph Hauert
 */
public class scanCSD extends CSD {

	/* additional parameters */
	double	b1Start = -Double.MAX_VALUE, b1End = -Double.MAX_VALUE, b1Incr = Double.MAX_VALUE;
	boolean	b1Log = false;
	double	b2Start = -Double.MAX_VALUE, b2End = -Double.MAX_VALUE, b2Incr = Double.MAX_VALUE;
	boolean	b2Log = false;
	double	c1Start = -Double.MAX_VALUE, c1End = -Double.MAX_VALUE, c1Incr = Double.MAX_VALUE;
	boolean	c1Log = false;
	double	c2Start = -Double.MAX_VALUE, c2End = -Double.MAX_VALUE, c2Incr = Double.MAX_VALUE;
	boolean	c2Log = false;
	int		nBins = 100;
//	int		timeWindow = 100;
//	int		relaxation = 5000;
	int relaxation = 0;
//	boolean classify = false;
	boolean	printDistr = false;
//	int snapinterval = 0;
	boolean	progress = false;

	public scanCSD(EvoLudo engine) {
		super(engine);
	}
	
	@Override
	public void exec() {
		double[] bparams = getBenefitParams(0);
		double[] cparams = getCostParams(0);

		// initialize
		double iMean = initMean[0];
		boolean initHigh = false;

		// print header
		engine.dumpParameters();

		// print result legend
		out.println("# tend		b1		b2		c1		c2		mean	sdev	type");

		long prev = progress?System.currentTimeMillis():0L;
		final int SAMPLES = 10; 
		nGenerations -= (SAMPLES-1)*reportFreq;
		double lowMonoThreshold = 2.0*getMutationSdev(0);
		double highMonoThreshold = getTraitMax(0)-lowMonoThreshold;
		double lowmean = -1.0, lowstdev = -1.0;
		double[] lowstatistics = null;
		double b1 = b1Start;
		while( Math.abs(b1)-Math.abs(b1End)<1e-4 ) {
			double b2 = b2Start;
			while( Math.abs(b2)-Math.abs(b2End)<1e-4 ) {
				double c1 = c1Start;
				while( Math.abs(c1)-Math.abs(c1End)<1e-4 ) {
					double c2 = c2Start;
					while( Math.abs(c2)-Math.abs(c2End)<1e-4 ) {
						// set parameters
						bparams[0] = b1;
						bparams[1] = b2;
						cparams[0] = c1;
						cparams[1] = c2;
						setBenefitParams(bparams);
						setCostParams(cparams);
						// initialize population
						if( initHigh ) setInitMean(getTraitMax(0)-iMean);
						else setInitMean(iMean);
						engine.modelReset();

						// evolve population
						if( relaxation>0 ) {
							double repfreq = reportFreq;
							reportFreq = relaxation;
							engine.modelNext();
							reportFreq = repfreq;
						}
						while( generation<nGenerations ) {
							engine.modelNext();
							int g = (int)generation;
//							if( snapinterval>0 && g%snapinterval==0 ) {
//								// save snapshot
//								saveSnapshot();
//							}
							if( progress ) {
								long now = System.currentTimeMillis();
								if( now-prev>1000 || g%1000==0 ) {
									prev = now;
									logProgress(g+"/"+(nGenerations+(SAMPLES-1)*reportFreq)+" done");
								}
							}
							// to speed things up, check every 1000 generations whether trait minimum or maximum has been reached
							// more precisely, whether mean trait <lowMonoThreshold or >highMonoThreshold
							if( g%1000==0 ) {
								double mean = Distributions.mean(strategies);
								double tmin = getTraitMin(0);
								double tmax = getTraitMax(0);
								if( mean<lowMonoThreshold && ArrayMath.max(strategies)<tmin+0.1*(tmax-tmin) ) break;
								if( mean>highMonoThreshold && ArrayMath.min(strategies)<tmax-0.1*(tmax-tmin) ) break;
							}
						}
						// analyze population
						String msg;
						// - create histogram (potentially after averaging over several generations)
						// - calculate statistical quantities (potentially over several generations)
						double[] statistics = new double[SAMPLES*nPopulation];
						System.arraycopy(strategies, 0, statistics, 0, nPopulation);
						for( int n=1; n<SAMPLES; n++ ) {
							engine.modelNext();
							System.arraycopy(strategies, 0, statistics, n*nPopulation, nPopulation);
						}
						double mean = Distributions.mean(statistics);
						double stdev = Distributions.stdev(statistics, mean);
//						if( snapinterval<0 ) saveSnapshot();
						if( mean<lowMonoThreshold ) {
							// investments converged to zero
							if( !initHigh ) {
								// check for bi-stability by starting with high investment levels
								initHigh = true;
								lowmean = mean;
								lowstdev = stdev;
								lowstatistics = statistics;
								continue;
							}
							// monomorphic state, minimum investments
							msg = Formatter.formatFix(mean, 6)+"\t"+Formatter.formatFix(stdev, 6)+"\tmonomorphic";
						}
						else if ( mean>highMonoThreshold ) {
							if( initHigh ) {
								// must be bistable, otherwise initHigh would be false
								msg = "-2\t-1\tbistable";
//								if( snapinterval<0 ) saveSnapshot();
							}
							else {
								// monomorphic state, maximum investments
								msg = Formatter.formatFix(mean, 6)+"\t"+Formatter.formatFix(stdev, 6)+"\tmonomorphic";
							}
						}
						else {
							if( initHigh ) {
								// low initial investments converged to zero; high initial investments did not stay high
								// hence likely insufficient time to converge to low investments 
								// note: argument only works for linear or quadratic cost/benefit functions where traits
								// always converge to extreme values except for ESS
								// report results for first run because more reliable
								msg = Formatter.formatFix(lowmean, 6)+"\t"+Formatter.formatFix(lowstdev, 6)+"\tmonomorphic";
								statistics = lowstatistics;
							}
							else {
								// intermediate investment levels
								double bimodal = Distributions.bimodality(statistics, mean);
								if( bimodal>5.0/9.0 ) {
									// likely bi-modal distribution
									msg = Formatter.formatFix(-mean, 6)+"\t"+Formatter.formatFix(stdev, 6)+
											"\tbranching ("+Formatter.format(bimodal, 6)+")";
								}
								else {
									// likely monomorphic distribution
									msg = Formatter.formatFix(mean, 6)+"\t"+Formatter.formatFix(stdev, 6)+
											"\tmonomorphic ("+Formatter.format(bimodal, 6)+")";
								}
							}
						}

						// print results
						// "# b1     b2	c1	c2		mean	sdev	type"
						out.println((int)generation+"\t"+Formatter.format(bparams[0], 4)+"\t"+Formatter.format(bparams[1], 4)+"\t"+
								Formatter.format(cparams[0], 4)+"\t"+Formatter.format(cparams[1], 4)+"\t"+
								msg);

						if( printDistr ) printState(statistics);
//						if( snapinterval<0 ) saveSnapshot();
//DEBUG
//double n = strategies.length;
//double tot = ChHMath.norm(strategies);
//double m1 = ChHMath.centralMoment(strategies, 1);
//double m2 = ChHMath.centralMoment(strategies, mean, 2);
//double m3 = ChHMath.centralMoment(strategies, mean, 3);
//double m4 = ChHMath.centralMoment(strategies, mean, 4);
//double sdev = Math.sqrt(m2);
//double skew = m3/ChHMath.pow(sdev, 3);
//double ekurt = m4/(m2*m2)-3.0;
//double bimod = (skew*skew+1)/(ekurt+3*(n-1)*(n-1)/((n-2)*(n-3)));
//msg =  "tot:  "+ChHFormatter.format(tot, 6)+"\n";
//msg += "n:   "+ChHFormatter.format(n, 0)+"\n";
//msg += "m1:    "+ChHFormatter.format(m1, 6)+"\n";
//msg += "m2:    "+ChHFormatter.format(m2, 6)+"\n";
//msg += "m3:    "+ChHFormatter.format(m3, 6)+"\n";
//msg += "m4:    "+ChHFormatter.format(m4, 6)+"\n";
//msg += "mean:  "+ChHFormatter.format(mean, 6)+"\n";
//msg += "stdev: "+ChHFormatter.format(sdev, 6)+"\t("+ChHFormatter.format(ChHMath.stdev(strategies), 6)+")\n";
//msg += "skew:  "+ChHFormatter.format(skew, 6)+"\t("+ChHFormatter.format(ChHMath.skewness(strategies), 6)+")\n";
//msg += "ekurt: "+ChHFormatter.format(ekurt, 6)+"\t("+ChHFormatter.format(ChHMath.kurtosis(strategies)-3.0, 6)+")\n";
//msg += "bimod: "+ChHFormatter.format(bimod, 6)+"\t("+ChHFormatter.format(ChHMath.bimodality(strategies), 6)+")\n";
//msg += "{ "+ChHFormatter.format(strategies[0], 6);
//for( int i=1; i<nPopulation; i++ ) msg += ", "+strategies[i];
//msg += " }";
//output.println(msg);
//ENDDEBUG
						// reset stuff
						initHigh = false;
						statistics = null;
						lowmean = -1;
						lowstdev = -1;
						lowstatistics = null;
						// next parameter set
						if( Double.isNaN(c2Incr) ) break;
						c2 = (c2Log?c2*c2Incr:c2+c2Incr);
					}
					if( Double.isNaN(c1Incr) ) break;
					c1 = (c1Log?c1*c1Incr:c1+c1Incr);
				}
				if( Double.isNaN(b2Incr) ) break;
				b2 = (b2Log?b2*b2Incr:b2+b2Incr);
			}
			if( Double.isNaN(b1Incr) ) break;
			b1 = (b1Log?b1*b1Incr:b1+b1Incr);
		}
	}

	public void printState(double[] statistics) {
		double[] bins = new double[nBins+1];
		double scale = nBins;
		int nSamples = statistics.length;
		double incr = 1.0/nSamples;
		for( int i=0; i<nSamples; i++ ) {
			bins[(int)(statistics[i]*scale+0.5)] += incr;
		}
		String msg = "# "+(int)generation+":";
		for( int n=0; n<=nBins; n++ ) msg += "\t"+bins[n];
		out.println(msg);
	}

//	public void printState() {
//		double[] bins = new double[nBins+1];
//		getTraitHistogramData(bins);
//		String msg = "# "+(int)generation+":";
//		for( int n=0; n<=nBins; n++ ) msg += "\t"+bins[n];
//		output.println(msg);
//	}

	/*
	 * command line parsing stuff
	 */
	protected final CLOption cloC1 = new CLOption("c1", CLOption.Argument.REQUIRED, "4.56",
			"--c1<l>,<u>[,<i>[l]]  range of c1 cost parameter [l,u], increment i, append l for logarithmic increments",
			new CLODelegate() {
		    	@Override
		    	public boolean parse(String arg) {
					String[] argv = arg.trim().split(CLOParser.VECTOR_DELIMITER);
					switch( argv.length ) {
						case 1:
							c1Start = CLOParser.parseDouble(argv[0]);
							c1End = Double.MAX_VALUE;
							c1Incr = Double.NaN;
							return true;
						case 2:
							c1Start = CLOParser.parseDouble(argv[0]);
							c1End = CLOParser.parseDouble(argv[1]);
							c1Incr = (c1End-c1Start)*0.1;
							return true;
						default:
							logger.warning("too many arguments for c1 ("+arg+") - ignored.");
							return false;
						case 3:
							double start = CLOParser.parseDouble(argv[0]);
							double end = CLOParser.parseDouble(argv[1]);
							double incr;
							boolean log = false;
							if( argv[2].endsWith("l") ) {
								log = true;
								// ignore sign (<0 is meaningless)
								incr = Math.abs(CLOParser.parseDouble(argv[2].substring(0, argv[2].length()-1)));
								if( (Math.abs(end)<Math.abs(start) && incr>1.0) || (Math.abs(end)>Math.abs(start) && incr<1.0) ) {
									logger.warning("invalid logarithmic increment for c1-range ["+Formatter.format(start, 3)+", "+
											Formatter.format(end, 3)+"] ("+incr+(incr<1.0?">":"<")+"1 should hold) - ignored.");
									return false;
								}
							}
							else {
								// ignore specified sign; set sign of increment based on start and end
								incr = Math.abs(CLOParser.parseDouble(argv[2]));
								if( end<start ) incr = -incr;
							}
							c1Start = start;
							c1End = end;
							c1Incr = incr;
							c1Log = log;
							return true;
					}
		    	}
		    	@Override
		    	public void report(PrintStream output) {
		    		output.println((Double.isNaN(c1Incr)?
		    			"# c1:                   "+Formatter.format(c1Start, 4):
		    			"# c1range:              "+Formatter.format(c1Start, 4)+" - "+Formatter.format(c1End, 4)+
		    				": "+Formatter.format(c1Incr, 4)+(c1Log?" (logarithmic)":"")));
		    	}
		    });
	protected final CLOption cloC2 = new CLOption("c2", CLOption.Argument.REQUIRED, "-1.6",
			"--c2<l>,<u>[,<i>[l]]  range of c2 cost parameter [l,u], increment i, append l for logarithmic increments",
			new CLODelegate() {
		    	@Override
		    	public boolean parse(String arg) {
					String[] argv = arg.trim().split(CLOParser.VECTOR_DELIMITER);
					switch( argv.length ) {
						case 1:
							c2Start = CLOParser.parseDouble(argv[0]);
							c2End = Double.MAX_VALUE;
							c2Incr = Double.NaN;
							return true;
						case 2:
							c2Start = CLOParser.parseDouble(argv[0]);
							c2End = CLOParser.parseDouble(argv[1]);
							c2Incr = (c2End-c2Start)*0.1;
							return true;
						default:
							logger.warning("too many arguments for c2 ("+arg+") - ignored.");
							return false;
						case 3:
							double start = CLOParser.parseDouble(argv[0]);
							double end = CLOParser.parseDouble(argv[1]);
							double incr;
							boolean log = false;
							if( argv[2].endsWith("l") ) {
								log = true;
								// ignore sign (<0 is meaningless)
								incr = Math.abs(CLOParser.parseDouble(argv[2].substring(0, argv[2].length()-1)));
								if( (Math.abs(end)<Math.abs(start) && incr>1.0) || (Math.abs(end)>Math.abs(start) && incr<1.0) ) {
									logger.warning("invalid logarithmic increment for c2-range ["+Formatter.format(start, 3)+", "+
											Formatter.format(end, 3)+"] ("+incr+(incr<1.0?">":"<")+"1 should hold) - ignored.");
									return false;
								}
							}
							else {
								// ignore specified sign; set sign of increment based on start and end
								incr = Math.abs(CLOParser.parseDouble(argv[2]));
								if( end<start ) incr = -incr;
							}
							c2Start = start;
							c2End = end;
							c2Incr = incr;
							c2Log = log;
							return true;
					}
		    	}
		    	@Override
		    	public void report(PrintStream output) {
		    		output.println((Double.isNaN(c2Incr)?
		    			"# c2:                   "+Formatter.format(c2Start, 4):
		    			"# c2range:              "+Formatter.format(c2Start, 4)+" - "+Formatter.format(c2End, 4)+
		    				": "+Formatter.format(c2Incr, 4)+(c2Log?" (logarithmic)":"")));
		    	}
		    });

	protected final CLOption cloB1 = new CLOption("b1", CLOption.Argument.REQUIRED, "6.0",
			"--b1<l>,<u>[,<i>[l]]  range of b1 benefit parameter [l,u], increment i, append l for logarithmic increments",
			new CLODelegate() {
		    	@Override
		    	public boolean parse(String arg) {
					String[] argv = arg.trim().split(CLOParser.VECTOR_DELIMITER);
					switch( argv.length ) {
						case 1:
							b1Start = CLOParser.parseDouble(argv[0]);
							b1End = Double.MAX_VALUE;
							b1Incr = Double.NaN;
							return true;
						case 2:
							b1Start = CLOParser.parseDouble(argv[0]);
							b1End = CLOParser.parseDouble(argv[1]);
							b1Incr = (b1End-b1Start)*0.1;
							return true;
						default:
							logger.warning("too many arguments for b1 ("+arg+") - ignored.");
							return false;
						case 3:
							double start = CLOParser.parseDouble(argv[0]);
							double end = CLOParser.parseDouble(argv[1]);
							double incr;
							boolean log = false;
							if( argv[2].endsWith("l") ) {
								log = true;
								// ignore sign (<0 is meaningless)
								incr = Math.abs(CLOParser.parseDouble(argv[2].substring(0, argv[2].length()-1)));
								if( (Math.abs(end)<Math.abs(start) && incr>1.0) || (Math.abs(end)>Math.abs(start) && incr<1.0) ) {
									logger.warning("invalid logarithmic increment for b1-range ["+Formatter.format(start, 3)+", "+
											Formatter.format(end, 3)+"] ("+incr+(incr<1.0?">":"<")+"1 should hold) - ignored.");
									return false;
								}
							}
							else {
								// ignore specified sign; set sign of increment based on start and end
								incr = Math.abs(CLOParser.parseDouble(argv[2]));
								if( end<start ) incr = -incr;
							}
							b1Start = start;
							b1End = end;
							b1Incr = incr;
							b1Log = log;
							return true;
					}
		    	}
		    	@Override
		    	public void report(PrintStream output) {
		    		output.println((Double.isNaN(b1Incr)?
		    			"# b1:                   "+Formatter.format(b1Start, 4):
		    			"# b1range:              "+Formatter.format(b1Start, 4)+" - "+Formatter.format(b1End, 4)+
		    				": "+Formatter.format(b1Incr, 4)+(b1Log?" (logarithmic)":"")));
		    	}
		    });
	protected final CLOption cloB2 = new CLOption("b2", CLOption.Argument.REQUIRED, "-1.4",
			"--b2<l>,<u>[,<i>[l]]  range of cost parameter [l,u], increment i, append l for logarithmic increments",
			new CLODelegate() {
		    	@Override
		    	public boolean parse(String arg) {
					String[] argv = arg.trim().split(CLOParser.VECTOR_DELIMITER);
					switch( argv.length ) {
						case 1:
							b2Start = CLOParser.parseDouble(argv[0]);
							b2End = Double.MAX_VALUE;
							b2Incr = Double.NaN;
							return true;
						case 2:
							b2Start = CLOParser.parseDouble(argv[0]);
							b2End = CLOParser.parseDouble(argv[1]);
							b2Incr = (b2End-b2Start)*0.1;
							return true;
						default:
							logger.warning("too many arguments for b2 ("+arg+") - ignored.");
							return false;
						case 3:
							double start = CLOParser.parseDouble(argv[0]);
							double end = CLOParser.parseDouble(argv[1]);
							double incr;
							boolean log = false;
							if( argv[2].endsWith("l") ) {
								log = true;
								// ignore sign (<0 is meaningless)
								incr = Math.abs(CLOParser.parseDouble(argv[2].substring(0, argv[2].length()-1)));
								if( (Math.abs(end)<Math.abs(start) && incr>1.0) || (Math.abs(end)>Math.abs(start) && incr<1.0) ) {
									logger.warning("invalid logarithmic increment for b2-range ["+Formatter.format(start, 3)+", "+
											Formatter.format(end, 3)+"] ("+incr+(incr<1.0?">":"<")+"1 should hold) - ignored.");
									return false;
								}
							}
							else {
								// ignore specified sign; set sign of increment based on start and end
								incr = Math.abs(CLOParser.parseDouble(argv[2]));
								if( end<start ) incr = -incr;
							}
							b2Start = start;
							b2End = end;
							b2Incr = incr;
							b2Log = log;
							return true;
					}
		    	}
		    	@Override
		    	public void report(PrintStream output) {
		    		output.println((Double.isNaN(b2Incr)?
		    			"# b2:                   "+Formatter.format(b2Start, 4):
		    			"# b2range:              "+Formatter.format(b2Start, 4)+" - "+Formatter.format(b2End, 4)+
		    				": "+Formatter.format(b2Incr, 4)+(b2Log?" (logarithmic)":"")));
		    	}
		    });
	protected final CLOption cloRelaxation = new CLOption("relaxation", CLOption.Argument.REQUIRED, "0",
			"--relaxation <n>     relaxation time in MC steps",
    		new CLODelegate() {
		    	@Override
		    	public boolean parse(String arg) {
					relaxation = CLOParser.parseInteger(arg);
					return true;
		    	}
		    	@Override
		    	public void report(PrintStream output) {
		    		output.println("# relaxation:           "+relaxation);
		    	}
		    });
//	protected final CLOption cloTimeWindow = new CLOption("timewindow", 'T', CLOption.Argument.REQUIRED, "100",
//			"--timewindow, -T <t>  number of generations for moving window",
//  		new CLOSetter() {
//		    	@Override
//		    	public void parse(String arg) {
//					timeWindow = CLOParser.parseInteger(arg);
//		    	}
//		    	@Override
//		    	public void report() {
//		    		output.println("# timewindow:           "+timeWindow);
//		    	}
//		    });
	protected final CLOption cloBins = new CLOption("bins", CLOption.Argument.REQUIRED, "100",
			"--bins <b>            number of bins for histogram",
  		new CLODelegate() {
		    	@Override
		    	public boolean parse(String arg) {
					nBins = CLOParser.parseInteger(arg);
					return true;
		    	}
		    	@Override
		    	public void report(PrintStream output) {
		    		output.println("# bins:                 "+nBins);
		    	}
		    });
//	protected final CLOption cloClassify = new CLOption("classify", 'y', CLOption.Argument.REQUIRED, "noclassify",
//			"--classify, -y        classify type of dynamics",
//  		new CLOSetter() {
//		    	@Override
//		    	public void parse(String arg) {
//					classify = cloClassify.isSet();
//		    	}
//		    	@Override
//		    	public void report() {
//		    		// nothing to report - setting affects only GUI
//		    	}
//		    });
	protected final CLOption cloDistribution = new CLOption("distribution", CLOption.Argument.NONE, "quiet",
			  "--distribution       print distributions",
		new CLODelegate() {
		    	@Override
		    	public boolean parse(String arg) {
					printDistr = cloDistribution.isSet();
					return true;
		    	}
		    });
//    protected final CLOption cloSnapInterval = new CLOption("snapinterval", CLOption.Argument.REQUIRED, "0",
//			  "--snapinterval <n>   save snapshot every n generations (-1 only at end)",
//  		new CLODelegate() {
//		    	@Override
//		    	public boolean parse(String arg) {
//					snapinterval = CLOParser.parseInteger(arg);
//					return true;
//		    	}
//		    });
    protected final CLOption cloProgress = new CLOption("progress", CLOption.Argument.NONE, "noprogress",
			  "--progress           make noise about progress",
  		new CLODelegate() {
		    	@Override
		    	public boolean parse(String arg) {
					progress = cloProgress.isSet();
					return true;
		    	}
		    });

    // override this method in subclasses to add further command line options
	// subclasses must make sure that they include a call to super
	@Override
	public void collectCLO(CLOParser parser) {
		parser.addCLO(cloC1);
		parser.addCLO(cloC2);
		parser.addCLO(cloB1);
		parser.addCLO(cloB2);
		parser.addCLO(cloRelaxation);
//		parser.addCLO(cloTimeWindow);
		parser.addCLO(cloBins);
//		parser.addCLO(cloClassify);
		parser.addCLO(cloDistribution);
//		parser.addCLO(cloSnapInterval);
		parser.addCLO(cloProgress);

		cloGenerations.setDefault("10000");

		super.collectCLO(parser);
		// remove options that do not make sense in present context
		parser.removeCLO(new String[] { "costparams", "benefitparams" });
	}

//	public void saveSnapshot() {
//		File snapfile;
//		int side = Math.max(16*(int)Math.sqrt(nPopulation), 1000);
//
//		int format = getGeometry().isLattice?AbstractGraph.SNAPSHOT_PNG:AbstractGraph.SNAPSHOT_SVG;
//		switch( format ) {
//			case AbstractGraph.SNAPSHOT_PNG:
//				snapfile = openSnapshot("png");
//				MVPop2D.exportSnapshotPNG(this, side, side, snapfile);
//				return;
//
//			case AbstractGraph.SNAPSHOT_SVG:
//				boolean compressed = true;
//				snapfile = openSnapshot(compressed?"svgz":"svg");
//				MVPop2D.exportSnapshotSVG(this, side, side, snapfile, compressed);
//				return;
//
//			default:
//				logger.warning("unknown file format ("+format+")");
//		}
//	}

//	protected File openSnapshot(String ext) {
//		// use name of out file as prefix for snapshot - not easily accessible, postponed
//		String pre = "snap-g"+(char)(getGeometry().geometry)+(int)(getGeometry().connectivity)+"-t"+Formatter.format(engine.getModel().getTime(), 2)+
//					 "-n"+Formatter.format(getThermalNoise(), 3);
//		File snapfile = new File(pre+"."+ext);
//		int counter = 0;
//		while( snapfile.exists() )
//			snapfile = new File(pre+"-"+(counter++)+"."+ext);
//		return snapfile;
//	}
}
