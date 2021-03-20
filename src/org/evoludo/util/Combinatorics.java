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
package org.evoludo.util;

/**
 * Collection of convenience methods for mathematical operations dealing with
 * combinatorics.
 * 
 * @author Christoph Hauert
 */
public class Combinatorics {

	/**
	 * Ensure non-instantiability with private default constructor
	 */
	public Combinatorics() {
	}

	/**
	 * Calculate <code>x^n</code> for integer <code>x</code> and <code>n</code>.
	 * This is an optimization for the CPU intense {@link Math#pow(double, double)}.
	 *
	 * @param x basis
	 * @param n exponent
	 * @return <code>x^n</code>
	 */
	public static int pow(int x, int n) {
		int x2, x4;
		if (n < 0)
			return 0;
	
		switch (n) {
			case 10:
				x2 = x * x;
				x4 = x2 * x2;
				return x2 * x4 * x4;
			case 9:
				x2 = x * x * x;
				return x2 * x2 * x2;
			case 8:
				x2 = x * x;
				x2 *= x2;
				return x2 * x2;
			case 7:
				x2 = x * x * x;
				return x * x2 * x2;
			case 6:
				x2 = x * x * x;
				return x2 * x2;
			case 5:
				x2 = x * x;
				return x * x2 * x2;
			case 4:
				x2 = x * x;
				return x2 * x2;
			case 3:
				return x * x * x;
			case 2:
				return x * x;
			case 1:
				return x;
			case 0:
				return 1;
			default:
				x2 = x * x;
				x4 = x2 * x2;
				int x10 = x2 * x4 * x4;
				int xn = x10;
				int exp = 10;
				while (n - exp > 10) {
					xn *= x10;
					exp += 10;
				}
				return xn * pow(x, n - exp);
		}
	}

	/**
	 * Calculate <code>x^n</code> for double <code>x</code> and integer
	 * <code>n</code>. This is an optimization for the CPU intense
	 * {@link Math#pow(double, double)}.
	 *
	 * @param x basis
	 * @param n exponent
	 * @return <code>x^n</code>
	 */
	public static double pow(double x, int n) {
		double x2, x3, x4, x6, x8;
		if (n < 0) {
			x = 1.0 / x;
			n = -n;
		}
		switch (n) {
			case 16:
				x2 = x * x;
				x4 = x2 * x2;
				x8 = x4 * x4;
				return x8 * x8;
			case 15:
				x3 = x * x * x;
				x6 = x3 * x3;
				return x3 * x6 * x6;
			case 14:
				x2 = x * x;
				x6 = x2 * x2 * x2;
				return x2 * x6 * x6;
			case 13:
				x3 = x * x * x;
				x6 = x3 * x3;
				return x * x6 * x6;
			case 12:
				x2 = x * x;
				x4 = x2 * x2;
				return x4 * x4 * x4;
			case 11:
				x2 = x * x;
				x4 = x2 * x2;
				return x * x2 * x4 * x4;
			case 10:
				x2 = x * x;
				x4 = x2 * x2;
				return x2 * x4 * x4;
			case 9:
				x3 = x * x * x;
				return x3 * x3 * x3;
			case 8:
				x2 = x * x;
				x4 = x2 * x2;
				return x4 * x4;
			case 7:
				x3 = x * x * x;
				return x * x3 * x3;
			case 6:
				x3 = x * x * x;
				return x3 * x3;
			case 5:
				x2 = x * x;
				return x * x2 * x2;
			case 4:
				x2 = x * x;
				return x2 * x2;
			case 3:
				return x * x * x;
			case 2:
				return x * x;
			case 1:
				return x;
			case 0:
				return 1.0;
			default:
				x2 = x * x;
				x4 = x2 * x2;
				x8 = x4 * x4;
				double x16 = x8 * x8;
				double xn = x16;
				int exp = 16;
				while (n - exp > 16) {
					xn *= x16;
					exp += 16;
				}
				return xn * pow(x, n - exp);
		}
	}

	/**
	 * Combinations: number of ways to draw <code>k</code> elements from pool of
	 * size <code>n</code>.
	 * <p>
	 * <em>Mathematica:</em> <code>Binomial[n,k] = n!/(k!(n-k)!)</code>
	 * </p>
	 * 
	 * @param n pool size
	 * @param k number of samples
	 * @return <code>Binomial[n,k]</code>
	 * @throws ArithmeticException if result <code>&gt;Integer.MAX_VALUE</code>
	 */
	public static int combinations(int n, int k) {
		if (k < 0 || n < k)
			return 0;
		if (n == 0 || k == 0 || n == k)
			return 1;
		if (k == 1 || n == k + 1)
			return n;
		double comb = (double) n / (double) k;
		for (int i = 1; i < k; i++)
			comb *= (double) (n - i) / (double) i;
		if (comb > Integer.MAX_VALUE)
			throw new ArithmeticException("result exceeds max int");
		return (int) Math.floor(comb + 0.5);
	}

	/**
	 * Combinations: number of ways to draw <code>k</code> elements from pool of
	 * size <code>n</code>.
	 * <p>
	 * <em>Mathematica:</em> <code>Binomial[n,k] = n!/(k!(n-k)!)</code>
	 * </p>
	 * 
	 * @param n pool size
	 * @param k number of samples
	 * @return <code>Binomial[n,k]</code>
	 * @throws ArithmeticException if result <code>&gt;Long.MAX_VALUE</code>
	 */
	public static long combinations(long n, long k) {
		if (k < 0 || n < k)
			return 0;
		if (n == 0 || k == 0 || n == k)
			return 1;
		if (k == 1 || n == k + 1)
			return n;
		double comb = (double) n / (double) k;
		for (int i = 1; i < k; i++)
			comb *= (double) (n - i) / (double) i;
		if (comb > Long.MAX_VALUE)
			throw new ArithmeticException("result exceeds max long");
		return (long) Math.floor(comb + 0.5);
	}

	/**
	 * Hypergeometric probability distribution <code>H<sub>2</sub>(X,x,Y,y)</code>
	 * for sampling <code>x</code> individuals from pool of size <code>X</code> and
	 * <code>y</code> individuals from pool of size <code>Y</code>.
	 * <p>
	 * <em>Mathematica:</em>
	 * <code>H_2[X, x, Y, y] = Binomial[X,x] Binomial[Y,y] / Binomial[X+Y,x+y]</code>
	 * </p>
	 * 
	 * @param X size of first pool
	 * @param x number samples from first pool
	 * @param Y size of second pool
	 * @param y number samples from second pool
	 * @return probability of drawing x from X and y from Y
	 */
	public static double H2(int X, int x, int Y, int y) {
		if (x < 0 || x > X || y < 0 || y > Y)
			return 0.0;
		if (x < y) {
			int swap = x;
			x = y;
			y = swap;
			swap = X;
			X = Y;
			Y = swap;
		}
		double num = X;
		double XY = X + Y;
		double frac = 1.0;
		for (int n = 0; n < x; n++)
			frac *= (num--) / (XY--);
		num = Y;
		double xy = x + y;
		int terms = y;
		for (int n = 0; n < terms; n++)
			frac *= ((num--) * (xy--)) / ((XY--) * (y--));
		return frac;
	}

}
