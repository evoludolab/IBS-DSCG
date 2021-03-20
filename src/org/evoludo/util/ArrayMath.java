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
 * Collection of convenience methods for mathematical operations, including
 * array manipulations and statistics.
 * 
 * @author Christoph Hauert
 */
public class ArrayMath {

	/**
	 * Ensure non-instantiability with private default constructor
	 */
	private ArrayMath() {
	}

	/**
	 * Find minimum element in boolean array/vector <code>a</code>.
	 * 
	 * @param a the <code>boolean[]</code> array
	 * @return <code>true</code> if all elements are <code>true</code> and
	 *         <code>false</code> if at least one element is <code>false</code>
	 */
	public static boolean min(boolean[] a) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			if (!a[n])
				return false;
		return true;
	}

	/**
	 * Find minimum element in integer array/vector <code>a</code>.
	 * 
	 * @param a array <code>int[]</code>
	 * @return minimum element
	 */
	public static int min(int[] a) {
		int dim = a.length;
		int min = Integer.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			min = Math.min(min, a[n]);
		return min;
	}

	/**
	 * Find minimum element in long array/vector <code>a</code>.
	 * 
	 * @param a array <code>long[]</code>
	 * @return minimum element
	 */
	public static long min(long[] a) {
		int dim = a.length;
		long min = Long.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			min = Math.min(min, a[n]);
		return min;
	}

	/**
	 * Find minimum element in float array/vector <code>a</code>.
	 * 
	 * @param a array <code>float[]</code>
	 * @return minimum element
	 */
	public static float min(float[] a) {
		int dim = a.length;
		float min = Float.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			min = Math.min(min, a[n]);
		return min;
	}

	/**
	 * Find minimum element in double array/vector <code>a</code>.
	 * 
	 * @param a array <code>double[]</code>
	 * @return minimum element
	 */
	public static double min(double[] a) {
		int dim = a.length;
		double min = Double.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			min = Math.min(min, a[n]);
		return min;
	}

	/**
	 * Find minimum element in integer array/matrix <code>a</code>.
	 * 
	 * @param a array <code>int[][]</code>
	 * @return minimum element
	 */
	public static int min(int[][] a) {
		int dim = a.length;
		int min = Integer.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			min = Math.min(min, min(a[n]));
		return min;
	}

	/**
	 * Find minimum element in long array/matrix <code>a</code>.
	 * 
	 * @param a array <code>long[][]</code>
	 * @return minimum element
	 */
	public static long min(long[][] a) {
		int dim = a.length;
		long min = Long.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			min = Math.min(min, min(a[n]));
		return min;
	}

	/**
	 * Find minimum element in float array/matrix <code>a</code>.
	 * 
	 * @param a array <code>float[][]</code>
	 * @return minimum element
	 */
	public static float min(float[][] a) {
		int dim = a.length;
		float min = Float.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			min = Math.min(min, min(a[n]));
		return min;
	}

	/**
	 * Find minimum element in double array/matrix <code>a</code>.
	 * 
	 * @param a array <code>double[][]</code>
	 * @return minimum element
	 */
	public static double min(double[][] a) {
		int dim = a.length;
		double min = Double.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			min = Math.min(min, min(a[n]));
		return min;
	}

	/**
	 * Find index of minimum element in integer vector.
	 * 
	 * @param a array <code>int[]</code>
	 * @return index of minimum element
	 */
	public static int minIndex(int[] a) {
		int dim = a.length;
		int min = Integer.MAX_VALUE;
		int idx = -1;
		for (int n = 0; n < dim; n++) {
			if (min < a[n])
				continue;
			min = a[n];
			idx = n;
		}
		return idx;
	}

	/**
	 * Find index of minimum element in long vector.
	 * 
	 * @param a array <code>long[]</code>
	 * @return index of minimum element
	 */
	public static int minIndex(long[] a) {
		int dim = a.length;
		long min = Long.MAX_VALUE;
		int idx = -1;
		for (int n = 0; n < dim; n++) {
			if (min < a[n])
				continue;
			min = a[n];
			idx = n;
		}
		return idx;
	}

	/**
	 * Find index of minimum element in float vector.
	 * 
	 * @param a array <code>float[]</code>
	 * @return index of minimum element
	 */
	public static int minIndex(float[] a) {
		int dim = a.length;
		float min = Float.MAX_VALUE;
		int idx = -1;
		for (int n = 0; n < dim; n++) {
			float an = a[n];
			if (min > an) {
				min = an;
				idx = n;
			}
		}
		return idx;
	}

	/**
	 * Find index of minimum element in double vector.
	 * 
	 * @param a array <code>double[]</code>
	 * @return index of minimum element
	 */
	public static int minIndex(double[] a) {
		int dim = a.length;
		double min = Double.MAX_VALUE;
		int idx = -1;
		for (int n = 0; n < dim; n++) {
			double an = a[n];
			if (min > an) {
				min = an;
				idx = n;
			}
		}
		return idx;
	}

	/**
	 * Find minimum element in two float vectors <code>a</code> and <code>b</code>.
	 * 
	 * @param a first vector
	 * @param b second vector
	 * @return minimum element in <code>a</code> and <code>b</code>
	 */
	public static float[] min(float[] a, float[] b) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			a[n] = Math.min(a[n], b[n]);
		return a;
	}

	/**
	 * Find minimum element in two double vectors <code>a</code> and <code>b</code>.
	 * 
	 * @param a first vector
	 * @param b second vector
	 * @return minimum element in <code>a</code> and <code>b</code>
	 */
	public static double[] min(double[] a, double[] b) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			a[n] = Math.min(a[n], b[n]);
		return a;
	}

	/**
	 * Find minimum element in double vector <code>a</code> among
	 * <code>active</code> elements. More precisely, disregard elements
	 * <code>a[i]</code> with <code>active[i]==false</code>.
	 * 
	 * @param a      array <code>double[]</code>
	 * @param active array <code>boolean[]</code> indicating whether element should
	 *               be skipped (<code>false</code>) or considered
	 *               (<code>true</code>).
	 * @return minimum active element
	 */
	public static double min(double[] a, boolean[] active) {
		int dim = a.length;
		double min = Double.MAX_VALUE;
		for (int n = 0; n < dim; n++) {
			if (!active[n])
				continue;
			min = Math.min(min, a[n]);
		}
		return min;
	}

	/**
	 * Find maximum element in boolean array/vector <code>a</code>.
	 * 
	 * @param a the <code>boolean[]</code> array
	 * @return <code>false</code> if all elements are <code>false</code> and
	 *         <code>true</code> if at least one element is <code>true</code>
	 */
	public static boolean max(boolean[] a) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			if (!a[n])
				return false;
		return true;
	}

	/**
	 * Find maximum element in int array/vector <code>a</code>.
	 * 
	 * @param a array <code>int[]</code>
	 * @return maximum element
	 */
	public static int max(int[] a) {
		int dim = a.length;
		int max = -Integer.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			max = Math.max(max, a[n]);
		return max;
	}

	/**
	 * Find maximum element in long array/vector <code>a</code>.
	 * 
	 * @param a array <code>long[]</code>
	 * @return maximum element
	 */
	public static long max(long[] a) {
		int dim = a.length;
		long max = -Long.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			max = Math.max(max, a[n]);
		return max;
	}

	/**
	 * Find maximum element in float array/vector <code>a</code>.
	 * 
	 * @param a array <code>float[]</code>
	 * @return maximum element
	 */
	public static float max(float[] a) {
		int dim = a.length;
		float max = -Float.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			max = Math.max(max, a[n]);
		return max;
	}

	/**
	 * Find maximum element in double array/vector <code>a</code>.
	 * 
	 * @param a array <code>double[]</code>
	 * @return maximum element
	 */
	public static double max(double[] a) {
		int dim = a.length;
		double max = -Double.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			max = Math.max(max, a[n]);
		return max;
	}

	/**
	 * Find maximum element in int array/matrix <code>a</code>.
	 * 
	 * @param a array <code>int[][]</code>
	 * @return maximum element
	 */
	public static int max(int[][] a) {
		int dim = a.length;
		int max = -Integer.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			max = Math.max(max, max(a[n]));
		return max;
	}

	/**
	 * Find maximum element in long array/matrix <code>a</code>.
	 * 
	 * @param a array <code>long[][]</code>
	 * @return maximum element
	 */
	public static long max(long[][] a) {
		int dim = a.length;
		long max = -Long.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			max = Math.max(max, max(a[n]));
		return max;
	}

	/**
	 * Find maximum element in float array/matrix <code>a</code>.
	 * 
	 * @param a array <code>float[][]</code>
	 * @return maximum element
	 */
	public static float max(float[][] a) {
		int dim = a.length;
		float max = -Float.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			max = Math.max(max, max(a[n]));
		return max;
	}

	/**
	 * Find maximum element in double array/matrix <code>a</code>.
	 * 
	 * @param a array <code>double[][]</code>
	 * @return maximum element
	 */
	public static double max(double[][] a) {
		int dim = a.length;
		double max = -Double.MAX_VALUE;
		for (int n = 0; n < dim; n++)
			max = Math.max(max, max(a[n]));
		return max;
	}

	/**
	 * Find index of maximum element in integer vector.
	 * 
	 * @param a array <code>int[]</code>
	 * @return index of maximum element
	 */
	public static int maxIndex(int[] a) {
		int dim = a.length;
		int max = -Integer.MAX_VALUE;
		int idx = -1;
		for (int n = 0; n < dim; n++) {
			if (max > a[n])
				continue;
			max = a[n];
			idx = n;
		}
		return idx;
	}

	/**
	 * Find index of maximum element in long vector.
	 * 
	 * @param a array <code>long[]</code>
	 * @return index of maximum element
	 */
	public static int maxIndex(long[] a) {
		int dim = a.length;
		long max = -Long.MAX_VALUE;
		int idx = -1;
		for (int n = 0; n < dim; n++) {
			if (max > a[n])
				continue;
			max = a[n];
			idx = n;
		}
		return idx;
	}

	/**
	 * Find index of maximum element in float vector.
	 * 
	 * @param a array <code>float[]</code>
	 * @return index of maximum element
	 */
	public static int maxIndex(float[] a) {
		int dim = a.length;
		float max = -Float.MAX_VALUE;
		int idx = -1;
		for (int n = 0; n < dim; n++) {
			float an = a[n];
			if (max < an) {
				max = an;
				idx = n;
			}
		}
		return idx;
	}

	/**
	 * Find index of maximum element in double vector.
	 * 
	 * @param a array <code>double[]</code>
	 * @return index of maximum element
	 */
	public static int maxIndex(double[] a) {
		int dim = a.length;
		double max = -Double.MAX_VALUE;
		int idx = -1;
		for (int n = 0; n < dim; n++) {
			double an = a[n];
			if (max < an) {
				max = an;
				idx = n;
			}
		}
		return idx;
	}

	/**
	 * Find maximum element in two float vectors <code>a</code> and <code>b</code>.
	 * 
	 * @param a first vector
	 * @param b second vector
	 * @return maximum element in <code>a</code> and <code>b</code>
	 */
	public static float[] max(float[] a, float[] b) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			a[n] = Math.max(a[n], b[n]);
		return a;
	}

	/**
	 * Find maximum element in two double vectors <code>a</code> and <code>b</code>.
	 * 
	 * @param a first vector
	 * @param b second vector
	 * @return maximum element in <code>a</code> and <code>b</code>
	 */
	public static double[] max(double[] a, double[] b) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			a[n] = Math.max(a[n], b[n]);
		return a;
	}

	/**
	 * Find maximum element in double vector <code>a</code> among
	 * <code>active</code> elements. More precisely, disregard elements
	 * <code>a[i]</code> with <code>active[i]==false</code>.
	 * 
	 * @param a      array <code>double[]</code>
	 * @param active array <code>boolean[]</code> indicating whether element should
	 *               be skipped (<code>false</code>) or considered
	 *               (<code>true</code>).
	 * @return maximum active element
	 */
	public static double max(double[] a, boolean[] active) {
		int dim = a.length;
		double max = -Double.MAX_VALUE;
		for (int n = 0; n < dim; n++) {
			if (!active[n])
				continue;
			max = Math.max(max, a[n]);
		}
		return max;
	}

	/**
	 * Norm of integer vector <code>a</code>.
	 * 
	 * @param a array <code>int[]</code>
	 * @return sum of all elements <code>a[i]</code>
	 */
	public static int norm(int[] a) {
		int dim = a.length;
		int norm = 0;
		for (int n = 0; n < dim; n++)
			norm += a[n];
		return norm;
	}

	/**
	 * Norm of long vector <code>a</code>.
	 * 
	 * @param a array <code>long[]</code>
	 * @return sum of all elements <code>a[i]</code>
	 */
	public static long norm(long[] a) {
		int dim = a.length;
		long norm = 0L;
		for (int n = 0; n < dim; n++)
			norm += a[n];
		return norm;
	}

	/**
	 * Norm of float vector <code>a</code>.
	 * 
	 * @param a array <code>float[]</code>
	 * @return sum of all elements <code>a[i]</code>
	 */
	public static float norm(float[] a) {
		int dim = a.length;
		float norm = 0f;
		for (int n = 0; n < dim; n++)
			norm += a[n];
		return norm;
	}

	/**
	 * Norm of double vector <code>a</code>.
	 * 
	 * @param a array <code>double[]</code>
	 * @return sum of all elements <code>a[i]</code>
	 */
	public static double norm(double[] a) {
		int dim = a.length;
		double norm = 0.0;
		for (int n = 0; n < dim; n++)
			norm += a[n];
		return norm;
	}

	/**
	 * Normalize float vector <code>a</code>. Scales elements such that the sum over
	 * all elements <code>a[i]</code> adds up to <code>1</code>. Normalization
	 * usually only makes sense if the sign of all elements is the same but this is
	 * not checked. Elements of <code>a</code> are overwritten with new values.
	 * 
	 * @param a array <code>float[]</code>
	 * @return normalized vector <code>a</code>
	 */
	public static float[] normalize(float[] a) {
		return normalize(a, 0, a.length);
	}

	/**
	 * Normalize elements ranging from index <code>from</code> to index
	 * <coe>to</code> in float vector <code>a</code>. Scales elements such that the
	 * sum over elements <code>a[i]</code> with <code>i=from,...,to</code> add up to
	 * <code>1</code>. Normalization usually only makes sense if the sign of all
	 * elements is the same but this is not checked. Elements of <code>a</code> are
	 * overwritten with new values.
	 * 
	 * @param a    the <code>float[]</code> array to normalize
	 * @param from the start index of the section to normalize
	 * @param to   the end index of the section to normalize
	 * @return vector <code>a</code> with normalized section
	 * 
	 * @see #normalize(double[])
	 */
	public static float[] normalize(float[] a, int from, int to) {
		float norm = 0f;
		for (int n = from; n < to; n++)
			norm += a[n];
		norm = 1f / norm;
		for (int n = from; n < to; n++)
			a[n] *= norm;
		return a;
	}

	/**
	 * Normalize double vector <code>a</code>. Scales elements such that the sum
	 * over all elements <code>a[i]</code> adds up to <code>1</code>. Normalization
	 * usually only makes sense if the sign of all elements is the same but this is
	 * not checked. Elements of <code>a</code> are overwritten with new values.
	 * 
	 * @param a the <code>double[]</code> array to normalize
	 * @return normalized vector <code>a</code>
	 * 
	 * @see #normalize(double[], int, int)
	 */
	public static double[] normalize(double[] a) {
		return normalize(a, 0, a.length);
	}

	/**
	 * Normalize elements ranging from index <code>from</code> to index
	 * <coe>to</code> in double vector <code>a</code>. Scales elements such that the
	 * sum over elements <code>a[i]</code> with <code>i=from,...,to</code> add up to
	 * <code>1</code>. Normalization usually only makes sense if the sign of all
	 * elements is the same but this is not checked. Elements of <code>a</code> are
	 * overwritten with new values.
	 * 
	 * @param a    the <code>double[]</code> array to normalize
	 * @param from the start index of the section to normalize
	 * @param to   the end index of the section to normalize
	 * @return vector <code>a</code> with normalized section
	 * 
	 * @see #normalize(double[])
	 */
	public static double[] normalize(double[] a, int from, int to) {
		double norm = 0.0;
		for (int n = from; n < to; n++)
			norm += a[n];
		norm = 1.0 / norm;
		for (int n = from; n < to; n++)
			a[n] *= norm;
		return a;
	}

	/**
	 * Element-wise copy of integer array/vector <code>src</code> to double
	 * array/vector <code>dst</code>.
	 * 
	 * @param src the <code>int[]</code> source array
	 * @param dst the <code>double[]</code> destination array
	 * @return array <code>dst</code>
	 */
	public static double[] copy(int[] src, double[] dst) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] += src[n];
		return dst;
	}

	/**
	 * Add <code>scalar</code> value to each element of integer array/vector
	 * <code>dst</code>. Elements of <code>dst</code> are overwritten with new
	 * values.
	 * 
	 * @param dst    array <code>int[]</code>
	 * @param scalar value to add to each element of <code>dst</code>
	 * @return modified array <code>dst</code>
	 */
	public static int[] add(int[] dst, int scalar) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] += scalar;
		return dst;
	}

	/**
	 * Add <code>scalar</code> value to each element of long array/vector
	 * <code>dst</code>. Elements of <code>dst</code> are overwritten with new
	 * values.
	 * 
	 * @param dst    array <code>long[]</code>
	 * @param scalar value to add to each element of <code>dst</code>
	 * @return modified array <code>dst</code>
	 */
	public static long[] add(long[] dst, long scalar) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] += scalar;
		return dst;
	}

	/**
	 * Add <code>scalar</code> value to each element of float array/vector
	 * <code>dst</code>. Elements of <code>dst</code> are overwritten with new
	 * values.
	 * 
	 * @param dst    array <code>float[]</code>
	 * @param scalar value to add to each element of <code>dst</code>
	 * @return modified array <code>dst</code>
	 */
	public static float[] add(float[] dst, float scalar) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] += scalar;
		return dst;
	}

	/**
	 * Add <code>scalar</code> value to each element of double array/vector
	 * <code>dst</code>. Elements of <code>dst</code> are overwritten with new
	 * values.
	 * 
	 * @param dst    array <code>double[]</code>
	 * @param scalar value to add to each element of <code>dst</code>
	 * @return modified array <code>dst</code>
	 */
	public static double[] add(double[] dst, double scalar) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] += scalar;
		return dst;
	}

	/**
	 * Add integer vectors <code>dst</code> and <code>add</code>. Place result in
	 * <code>dst</code> (vector <code>add</code> remains unchanged). Vectors
	 * <code>dst</code> and <code>add</code> can be of different lengths as long as
	 * <code>dst.length&lt;add.length</code>. If <code>add</code> is longer,
	 * additional elements are ignored.
	 * 
	 * @param dst destination vector
	 * @param add vector to add to <code>dst</code>
	 * @return modified array <code>dst</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>add.length&lt;dst.length</code>
	 */
	public static int[] add(int[] dst, int[] add) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] += add[n];
		return dst;
	}

	/**
	 * Add long vectors <code>dst</code> and <code>add</code>. Place result in
	 * <code>dst</code> (vector <code>add</code> remains unchanged). Vectors
	 * <code>dst</code> and <code>add</code> can be of different lengths as long as
	 * <code>dst.length&lt;add.length</code>. If <code>add</code> is longer,
	 * additional elements are ignored.
	 * 
	 * @param dst destination vector
	 * @param add vector to add to <code>dst</code>
	 * @return modified array <code>dst</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>add.length&lt;dst.length</code>
	 */
	public static long[] add(long[] dst, long[] add) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] += add[n];
		return dst;
	}

	/**
	 * Add float vectors <code>dst</code> and <code>add</code>. Place result in
	 * <code>dst</code> (vector <code>add</code> remains unchanged). Vectors
	 * <code>dst</code> and <code>add</code> can be of different lengths as long as
	 * <code>dst.length&lt;add.length</code>. If <code>add</code> is longer,
	 * additional elements are ignored.
	 * 
	 * @param dst destination vector
	 * @param add vector to add to <code>dst</code>
	 * @return modified array <code>dst</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>add.length&lt;dst.length</code>
	 */
	public static float[] add(float[] dst, float[] add) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] += add[n];
		return dst;
	}

	/**
	 * Add double vectors <code>dst</code> and <code>add</code>. Place result in
	 * <code>dst</code> (vector <code>add</code> remains unchanged). Vectors
	 * <code>dst</code> and <code>add</code> can be of different lengths as long as
	 * <code>dst.length&lt;add.length</code>. If <code>add</code> is longer,
	 * additional elements are ignored.
	 * 
	 * @param dst destination vector
	 * @param add vector to add to <code>dst</code>
	 * @return modified array <code>dst</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>add.length&lt;dst.length</code>
	 */
	public static double[] add(double[] dst, double[] add) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] += add[n];
		return dst;
	}

	/**
	 * Non-destructive vector addition. Add int vectors <code>a</code> and
	 * <code>b</code> and place result in <code>dst</code> (vectors <code>a</code>
	 * and <code>b</code> remain unchanged). Vectors <code>a</code>, <code>b</code>
	 * and <code>dst</code> can be of different lengths as long as
	 * <code>a.length&le;b.length&le;dst.length</code>. If <code>b</code> or
	 * <code>dst</code> is longer than <code>a</code>, additional elements are
	 * ignored.
	 * 
	 * @param a   first vector
	 * @param b   second vector
	 * @param dst result vector
	 * @return modified array <code>dst=a+b</code>
	 * @throws ArrayIndexOutOfBoundsException if <code>dst.length&lt;a.length</code>
	 *                                        or <code>b.length&lt;a.length</code>
	 */
	public static int[] add(int[] a, int[] b, int[] dst) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			dst[n] = a[n] + b[n];
		return dst;
	}

	/**
	 * Non-destructive vector addition. Add long vectors <code>a</code> and
	 * <code>b</code> and place result in <code>dst</code> (vectors <code>a</code>
	 * and <code>b</code> remain unchanged). Vectors <code>a</code>, <code>b</code>
	 * and <code>dst</code> can be of different lengths as long as
	 * <code>a.length&le;b.length&le;dst.length</code>. If <code>b</code> or
	 * <code>dst</code> is longer than <code>a</code>, additional elements are
	 * ignored.
	 * 
	 * @param a   first vector
	 * @param b   second vector
	 * @param dst result vector
	 * @return modified array <code>dst=a+b</code>
	 * @throws ArrayIndexOutOfBoundsException if <code>dst.length&lt;a.length</code>
	 *                                        or <code>b.length&lt;a.length</code>
	 */
	public static long[] add(long[] a, long[] b, long[] dst) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			dst[n] = a[n] + b[n];
		return dst;
	}

	/**
	 * Non-destructive vector addition. Add float vectors <code>a</code> and
	 * <code>b</code> and place result in <code>dst</code> (vectors <code>a</code>
	 * and <code>b</code> remain unchanged). Vectors <code>a</code>, <code>b</code>
	 * and <code>dst</code> can be of different lengths as long as
	 * <code>a.length&le;b.length&le;dst.length</code>. If <code>b</code> or
	 * <code>dst</code> is longer than <code>a</code>, additional elements are
	 * ignored.
	 * 
	 * @param a   first vector
	 * @param b   second vector
	 * @param dst result vector
	 * @return modified array <code>dst=a+b</code>
	 * @throws ArrayIndexOutOfBoundsException if <code>dst.length&lt;a.length</code>
	 *                                        or <code>b.length&lt;a.length</code>
	 */
	public static float[] add(float[] a, float[] b, float[] dst) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			dst[n] = a[n] + b[n];
		return dst;
	}

	/**
	 * Non-destructive vector addition. Add double vectors <code>a</code> and
	 * <code>b</code> and place result in <code>dst</code> (vectors <code>a</code>
	 * and <code>b</code> remain unchanged). Vectors <code>a</code>, <code>b</code>
	 * and <code>dst</code> can be of different lengths as long as
	 * <code>a.length&le;b.length&le;dst.length</code>. If <code>b</code> or
	 * <code>dst</code> is longer than <code>a</code>, additional elements are
	 * ignored.
	 * 
	 * @param a   first vector
	 * @param b   second vector
	 * @param dst result vector
	 * @return modified array <code>dst=a+b</code>
	 * @throws ArrayIndexOutOfBoundsException if <code>dst.length&lt;a.length</code>
	 *                                        or <code>b.length&lt;a.length</code>
	 */
	public static double[] add(double[] a, double[] b, double[] dst) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			dst[n] = a[n] + b[n];
		return dst;
	}

	/**
	 * Non-destructive scalar multiplication and vector addition. Multiply double
	 * vector <code>b</code> by scalar value <code>s</code>, add the result to
	 * vector <code>a</code> and store result in vector <code>dst</code> (vectors
	 * <code>a</code> and <code>b</code> remain unchanged). Vectors <code>a</code>,
	 * <code>b</code> and <code>dst</code> can be of different lengths as long as
	 * <code>a.length&le;b.length&le;dst.length</code>. If <code>b</code> or
	 * <code>dst</code> are longer than <code>a</code>, additional elements are
	 * ignored.
	 * 
	 * @param a   first vector
	 * @param b   second vector
	 * @param dst result vector
	 * @param s   scalar multiplier
	 * @return modified array <code>dst=a+s*b</code>
	 * @throws ArrayIndexOutOfBoundsException if <code>a.length&lt;dst.length</code>
	 *                                        or <code>b.length&lt;dst.length</code>
	 */
	public static double[] addscale(double[] a, double[] b, double s, double[] dst) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			dst[n] = a[n] + s * b[n];
		return dst;
	}

	/**
	 * Subtract integer vector <code>sub</code> from vector <code>dst</code>. Place
	 * result in <code>dst</code> (vector <code>sub</code> remains unchanged).
	 * Vectors <code>dst</code> and <code>sub</code> can be of different lengths as
	 * long as <code>dst.length&lt;sub.length</code>. If <code>sub</code> is longer,
	 * additional elements are ignored.
	 * 
	 * @param dst destination vector
	 * @param sub vector to subtract from <code>dst</code>
	 * @return modified array <code>dst</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>sub.length&lt;dst.length</code>
	 */
	public static int[] sub(int[] dst, int[] sub) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] -= sub[n];
		return dst;
	}

	/**
	 * Subtract long vector <code>sub</code> from vector <code>dst</code>. Place
	 * result in <code>dst</code> (vector <code>sub</code> remains unchanged).
	 * Vectors <code>dst</code> and <code>sub</code> can be of different lengths as
	 * long as <code>dst.length&lt;sub.length</code>. If <code>sub</code> is longer,
	 * additional elements are ignored.
	 * 
	 * @param dst destination vector
	 * @param sub vector to subtract from <code>dst</code>
	 * @return modified array <code>dst</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>sub.length&lt;dst.length</code>
	 */
	public static long[] sub(long[] dst, long[] sub) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] -= sub[n];
		return dst;
	}

	/**
	 * Subtract float vector <code>sub</code> from vector <code>dst</code>. Place
	 * result in <code>dst</code> (vector <code>sub</code> remains unchanged).
	 * Vectors <code>dst</code> and <code>sub</code> can be of different lengths as
	 * long as <code>dst.length&lt;sub.length</code>. If <code>sub</code> is longer,
	 * additional elements are ignored.
	 * 
	 * @param dst destination vector
	 * @param sub vector to subtract from <code>dst</code>
	 * @return modified array <code>dst</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>sub.length&lt;dst.length</code>
	 */
	public static float[] sub(float[] dst, float[] sub) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] -= sub[n];
		return dst;
	}

	/**
	 * Subtract double vector <code>sub</code> from vector <code>dst</code>. Place
	 * result in <code>dst</code> (vector <code>sub</code> remains unchanged).
	 * Vectors <code>dst</code> and <code>sub</code> can be of different lengths as
	 * long as <code>dst.length&lt;sub.length</code>. If <code>sub</code> is longer,
	 * additional elements are ignored.
	 * 
	 * @param dst destination vector
	 * @param sub vector to subtract from <code>dst</code>
	 * @return modified array <code>dst</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>sub.length&lt;dst.length</code>
	 */
	public static double[] sub(double[] dst, double[] sub) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] -= sub[n];
		return dst;
	}

	/**
	 * Non-destructive vector subtraction. Subtract integer vector <code>sub</code>
	 * from vector <code>orig</code> and place result in <code>dst</code> (vectors
	 * <code>orig</code> and <code>sub</code> remain unchanged). Vectors
	 * <code>orig</code>, <code>sub</code> and <code>dst</code> can be of different
	 * lengths as long as <code>orig.length&le;sub.length&le;dst.length</code>. If
	 * <code>dst</code> or <code>sub</code> are longer, additional elements are
	 * ignored.
	 * 
	 * @param orig first vector
	 * @param sub  second vector to subtract from <code>orig</code>
	 * @param dst  result vector
	 * @return modified array <code>dst=orig-sub</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>dst.length&lt;orig.length</code>
	 *                                        or
	 *                                        <code>sub.length&lt;orig.length</code>
	 */
	public static int[] sub(int[] orig, int[] sub, int[] dst) {
		int dim = orig.length;
		for (int n = 0; n < dim; n++)
			dst[n] = orig[n] - sub[n];
		return dst;
	}

	/**
	 * Non-destructive vector subtraction. Subtract long vector <code>sub</code>
	 * from vector <code>orig</code> and place result in <code>dst</code> (vectors
	 * <code>orig</code> and <code>sub</code> remain unchanged). Vectors
	 * <code>orig</code>, <code>sub</code> and <code>dst</code> can be of different
	 * lengths as long as <code>orig.length&le;sub.length&le;dst.length</code>. If
	 * <code>dst</code> or <code>sub</code> are longer, additional elements are
	 * ignored.
	 * 
	 * @param orig first vector
	 * @param sub  second vector to subtract from <code>orig</code>
	 * @param dst  result vector
	 * @return modified array <code>dst=orig-sub</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>dst.length&lt;orig.length</code>
	 *                                        or
	 *                                        <code>sub.length&lt;orig.length</code>
	 */
	public static long[] sub(long[] orig, long[] sub, long[] dst) {
		int dim = orig.length;
		for (int n = 0; n < dim; n++)
			dst[n] = orig[n] - sub[n];
		return dst;
	}

	/**
	 * Non-destructive vector subtraction. Subtract float vector <code>sub</code>
	 * from vector <code>orig</code> and place result in <code>dst</code> (vectors
	 * <code>orig</code> and <code>sub</code> remain unchanged). Vectors
	 * <code>orig</code>, <code>sub</code> and <code>dst</code> can be of different
	 * lengths as long as <code>orig.length&le;sub.length&le;dst.length</code>. If
	 * <code>dst</code> or <code>sub</code> are longer, additional elements are
	 * ignored.
	 * 
	 * @param orig first vector
	 * @param sub  second vector to subtract from <code>orig</code>
	 * @param dst  result vector
	 * @return modified array <code>dst=orig-sub</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>dst.length&lt;orig.length</code>
	 *                                        or
	 *                                        <code>sub.length&lt;orig.length</code>
	 */
	public static float[] sub(float[] orig, float[] sub, float[] dst) {
		int dim = orig.length;
		for (int n = 0; n < dim; n++)
			dst[n] = orig[n] - sub[n];
		return dst;
	}

	/**
	 * Non-destructive vector subtraction. Subtract double vector <code>sub</code>
	 * from vector <code>orig</code> and place result in <code>dst</code> (vectors
	 * <code>orig</code> and <code>sub</code> remain unchanged). Vectors
	 * <code>orig</code>, <code>sub</code> and <code>dst</code> can be of different
	 * lengths as long as <code>orig.length&le;sub.length&le;dst.length</code>. If
	 * <code>dst</code> or <code>sub</code> are longer, additional elements are
	 * ignored.
	 * 
	 * @param orig first vector
	 * @param sub  second vector to subtract from <code>orig</code>
	 * @param dst  result vector
	 * @return modified array <code>dst=orig-sub</code>
	 * @throws ArrayIndexOutOfBoundsException if
	 *                                        <code>dst.length&lt;orig.length</code>
	 *                                        or
	 *                                        <code>sub.length&lt;orig.length</code>
	 */
	public static double[] sub(double[] orig, double[] sub, double[] dst) {
		int dim = orig.length;
		for (int n = 0; n < dim; n++)
			dst[n] = orig[n] - sub[n];
		return dst;
	}

	/**
	 * Squared distance of two integer vectors <code>a</code> and <code>b</code>
	 * given by the sum over <code>(a[i]-b[i])^2</code>. Vectors <code>a</code> and
	 * <code>b</code> are preserved.
	 *
	 * @param a first vector
	 * @param b second vector
	 * @return distance between <code>a</code> and <code>b</code> squared
	 */
	public static int distSq(int[] a, int[] b) {
		int dim = a.length;
		int dist2 = 0;
		for (int n = 0; n < dim; n++) {
			int d = a[n] - b[n];
			dist2 += d * d;
		}
		return dist2;
	}

	/**
	 * Squared distance of two double vectors <code>a</code> and <code>b</code>
	 * given by the sum over <code>(a[i]-b[i])^2</code>. Vectors <code>a</code> and
	 * <code>b</code> are preserved.
	 *
	 * @param a first vector
	 * @param b second vector
	 * @return distance between <code>a</code> and <code>b</code> squared
	 */
	public static double distSq(double[] a, double[] b) {
		int dim = a.length;
		double dist2 = 0.0;
		for (int n = 0; n < dim; n++) {
			double d = a[n] - b[n];
			dist2 += d * d;
		}
		return dist2;
	}

	/**
	 * Distance of two integer vectors <code>a</code> and <code>b</code> given by
	 * the square root of the sum over <code>(a[i]-b[i])^2</code>. Vectors
	 * <code>a</code> and <code>b</code> are preserved.
	 *
	 * @param a first vector
	 * @param b second vector
	 * @return distance between <code>a</code> and <code>b</code>
	 */
	public static double dist(int[] a, int[] b) {
		return Math.sqrt(distSq(a, b));
	}

	/**
	 * Distance of two double vectors <code>a</code> and <code>b</code> given by the
	 * square root of the sum over <code>(a[i]-b[i])^2</code>. Vectors
	 * <code>a</code> and <code>b</code> are preserved.
	 *
	 * @param a first vector
	 * @param b second vector
	 * @return distance between <code>a</code> and <code>b</code>
	 */
	public static double dist(double[] a, double[] b) {
		return Math.sqrt(distSq(a, b));
	}

	/**
	 * Dot product of two integer vectors <code>a</code> and <code>b</code> given by
	 * the sum over <code>a[i]*b[i]</code>. Vectors <code>a</code> and
	 * <code>b</code> are preserved.
	 * 
	 * @param a first vector
	 * @param b second vector
	 * @return dot product <code>a.b</code>
	 */
	public static int dot(int[] a, int[] b) {
		int dim = a.length;
		int dot = 0;
		for (int n = 0; n < dim; n++)
			dot += a[n] * b[n];
		return dot;
	}

	/**
	 * Dot product of two long vectors <code>a</code> and <code>b</code> given by
	 * the sum over <code>a[i]*b[i]</code>. Vectors <code>a</code> and
	 * <code>b</code> are preserved.
	 * 
	 * @param a first vector
	 * @param b second vector
	 * @return dot product <code>a.b</code>
	 */
	public static long dot(long[] a, long[] b) {
		int dim = a.length;
		long dot = 0;
		for (int n = 0; n < dim; n++)
			dot += a[n] * b[n];
		return dot;
	}

	/**
	 * Dot product of two float vectors <code>a</code> and <code>b</code> given by
	 * the sum over <code>a[i]*b[i]</code>. Vectors <code>a</code> and
	 * <code>b</code> are preserved.
	 * 
	 * @param a first vector
	 * @param b second vector
	 * @return dot product <code>a.b</code>
	 */
	public static float dot(float[] a, float[] b) {
		int dim = a.length;
		float dot = 0;
		for (int n = 0; n < dim; n++)
			dot += a[n] * b[n];
		return dot;
	}

	/**
	 * Dot product of two double vectors <code>a</code> and <code>b</code> given by
	 * the sum over <code>a[i]*b[i]</code>. Vectors <code>a</code> and
	 * <code>b</code> are preserved.
	 * 
	 * @param a first vector
	 * @param b second vector
	 * @return dot product <code>a.b</code>
	 */
	public static double dot(double[] a, double[] b) {
		int dim = a.length;
		double dot = 0;
		for (int n = 0; n < dim; n++)
			dot += a[n] * b[n];
		return dot;
	}

	/**
	 * Scalar multiplication of integer vector <code>dst</code> by
	 * <code>scalar</code>.
	 * 
	 * @param dst    vector
	 * @param scalar multiplier
	 * @return modified array <code>dst</code>
	 */
	public static int[] multiply(int[] dst, int scalar) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] *= scalar;
		return dst;
	}

	/**
	 * Scalar multiplication of long vector <code>dst</code> by <code>scalar</code>.
	 * 
	 * @param dst    vector
	 * @param scalar multiplier
	 * @return modified array <code>dst</code>
	 */
	public static long[] multiply(long[] dst, long scalar) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] *= scalar;
		return dst;
	}

	/**
	 * Scalar multiplication of float vector <code>dst</code> by
	 * <code>scalar</code>.
	 * 
	 * @param dst    vector
	 * @param scalar multiplier
	 * @return modified array <code>dst</code>
	 */
	public static float[] multiply(float[] dst, float scalar) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] *= scalar;
		return dst;
	}

	/**
	 * Scalar multiplication of double vector <code>dst</code> by
	 * <code>scalar</code>.
	 * 
	 * @param dst    vector
	 * @param scalar multiplier
	 * @return modified array <code>dst</code>
	 */
	public static double[] multiply(double[] dst, double scalar) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] *= scalar;
		return dst;
	}

	/**
	 * Element-wise multiplication of integer vectors <code>a</code> and
	 * <code>dst</code>. The result is placed in <code>dst</code> (vector
	 * <code>a</code> is preserved).
	 * 
	 * @param dst first vector
	 * @param a   second vector
	 * @return modified array <code>dst</code>
	 */
	public static int[] multiply(int[] dst, int[] a) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] *= a[n];
		return dst;
	}

	/**
	 * Element-wise multiplication of long vectors <code>a</code> and
	 * <code>dst</code>. The result is placed in <code>dst</code> (vector
	 * <code>a</code> is preserved).
	 * 
	 * @param dst first vector
	 * @param a   second vector
	 * @return modified array <code>dst</code>
	 */
	public static long[] multiply(long[] dst, long[] a) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] *= a[n];
		return dst;
	}

	/**
	 * Element-wise multiplication of float vectors <code>a</code> and
	 * <code>dst</code>. The result is placed in <code>dst</code> (vector
	 * <code>a</code> is preserved).
	 * 
	 * @param dst first vector
	 * @param a   second vector
	 * @return modified array <code>dst</code>
	 */
	public static float[] multiply(float[] dst, float[] a) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] *= a[n];
		return dst;
	}

	/**
	 * Element-wise multiplication of double vectors <code>a</code> and
	 * <code>dst</code>. The result is placed in <code>dst</code> (vector
	 * <code>a</code> is preserved).
	 * 
	 * @param dst first vector
	 * @param a   second vector
	 * @return modified array <code>dst</code>
	 */
	public static double[] multiply(double[] dst, double[] a) {
		int dim = dst.length;
		for (int n = 0; n < dim; n++)
			dst[n] *= a[n];
		return dst;
	}

	/**
	 * Non-destructive, element-wise multiplication of integer vectors
	 * <code>a</code> and <code>b</code>. The result is placed in <code>dst</code>
	 * (vectors <code>a</code> and <code>b</code> are preserved).
	 * 
	 * @param a   first vector
	 * @param b   second vector
	 * @param dst result vector
	 * @return modified array <code>dst[i]=a[i]*b[i]</code>
	 */
	public static int[] multiply(int[] a, int[] b, int[] dst) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			dst[n] = a[n] * b[n];
		return a;
	}

	/**
	 * Non-destructive, element-wise multiplication of long vectors <code>a</code>
	 * and <code>b</code>. The result is placed in <code>dst</code> (vectors
	 * <code>a</code> and <code>b</code> are preserved).
	 * 
	 * @param a   first vector
	 * @param b   second vector
	 * @param dst result vector
	 * @return modified array <code>dst[i]=a[i]*b[i]</code>
	 */
	public static long[] multiply(long[] a, long[] b, long[] dst) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			dst[n] = a[n] * b[n];
		return a;
	}

	/**
	 * Non-destructive, element-wise multiplication of float vectors <code>a</code>
	 * and <code>b</code>. The result is placed in <code>dst</code> (vectors
	 * <code>a</code> and <code>b</code> are preserved).
	 * 
	 * @param a   first vector
	 * @param b   second vector
	 * @param dst result vector
	 * @return modified array <code>dst[i]=a[i]*b[i]</code>
	 */
	public static float[] multiply(float[] a, float[] b, float[] dst) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			dst[n] = a[n] * b[n];
		return a;
	}

	/**
	 * Non-destructive, element-wise multiplication of double vectors <code>a</code>
	 * and <code>b</code>. The result is placed in <code>dst</code> (vectors
	 * <code>a</code> and <code>b</code> are preserved).
	 * 
	 * @param a   first vector
	 * @param b   second vector
	 * @param dst result vector
	 * @return modified array <code>dst[i]=a[i]*b[i]</code>
	 */
	public static double[] multiply(double[] a, double[] b, double[] dst) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			dst[n] = a[n] * b[n];
		return a;
	}

	/**
	 * Non-destructive integer matrix multiplication. Matrix <code>mat</code>
	 * multiplied by vector <code>vec</code> and the result stored in
	 * <code>dst</code> (right multiplication). Matrix <code>mat</code> and vector
	 * <code>vec</code> preserved.
	 * 
	 * @param mat matrix
	 * @param vec vector
	 * @param dst result vector
	 * @return modified array <code>dst=mat * vec</code>
	 */
	public static int[] multiply(int[][] mat, int[] vec, int[] dst) {
		int n = mat.length;
		for (int i = 0; i < n; i++) {
			dst[i] = 0;
			int[] row = mat[i];
			for (int j = 0; j < n; j++) {
				dst[i] += row[j] * vec[j];
			}
		}
		return dst;
	}

	/**
	 * Non-destructive long matrix multiplication. Matrix <code>mat</code>
	 * multiplied by vector <code>vec</code> and the result stored in
	 * <code>dst</code> (right multiplication). Matrix <code>mat</code> and vector
	 * <code>vec</code> preserved.
	 * 
	 * @param mat matrix
	 * @param vec vector
	 * @param dst result vector
	 * @return modified array <code>dst=mat * vec</code>
	 */
	public static long[] multiply(long[][] mat, long[] vec, long[] dst) {
		int n = mat.length;
		for (int i = 0; i < n; i++) {
			dst[i] = 0L;
			long[] row = mat[i];
			for (int j = 0; j < n; j++) {
				dst[i] += row[j] * vec[j];
			}
		}
		return dst;
	}

	/**
	 * Non-destructive float matrix multiplication. Matrix <code>mat</code>
	 * multiplied by vector <code>vec</code> and the result stored in
	 * <code>dst</code> (right multiplication). Matrix <code>mat</code> and vector
	 * <code>vec</code> preserved.
	 * 
	 * @param mat matrix
	 * @param vec vector
	 * @param dst result vector
	 * @return modified array <code>dst=mat * vec</code>
	 */
	public static float[] multiply(float[][] mat, float[] vec, float[] dst) {
		int n = mat.length;
		for (int i = 0; i < n; i++) {
			dst[i] = 0f;
			float[] row = mat[i];
			for (int j = 0; j < n; j++) {
				dst[i] += row[j] * vec[j];
			}
		}
		return dst;
	}

	/**
	 * Non-destructive double matrix multiplication. Matrix <code>mat</code>
	 * multiplied by vector <code>vec</code> and the result stored in
	 * <code>dst</code> (right multiplication). Matrix <code>mat</code> and vector
	 * <code>vec</code> preserved.
	 * 
	 * @param mat matrix
	 * @param vec vector
	 * @param dst result vector
	 * @return modified array <code>dst=mat * vec</code>
	 */
	public static double[] multiply(double[][] mat, double[] vec, double[] dst) {
		int n = mat.length;
		for (int i = 0; i < n; i++) {
			dst[i] = 0.0;
			double[] row = mat[i];
			for (int j = 0; j < n; j++) {
				dst[i] += row[j] * vec[j];
			}
		}
		return dst;
	}

	/**
	 * Element-wise logarithm of float vector <code>a</code> (base 10).
	 * 
	 * @param a array to apply logarithm to
	 * @return modified array <code>a</code>
	 */
	public static float[] log10(float[] a) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			a[n] = (float) Math.log10(a[n]);
		return a;
	}

	/**
	 * Element-wise logarithm of double vector <code>a</code> (base 10).
	 * 
	 * @param a array to apply logarithm to
	 * @return modified array <code>a</code>
	 */
	public static double[] log10(double[] a) {
		int dim = a.length;
		for (int n = 0; n < dim; n++)
			a[n] = Math.log10(a[n]);
		return a;
	}
}
