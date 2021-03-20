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
import java.util.Arrays;

//import org.evoludo.simulator.models.PDEReactionDiffusion;
import org.evoludo.util.ArrayMath;

/**
 * Interface for mapping data to colors.
 * <p>
 * Colors are handled very differently in JRE and GWT as well as for HTML
 * <code>canvas</code>'es and WebGL. This class provides a unified interface,
 * which hides the implementation details.
 * <p>
 * <strong>Note:</strong>
 * <ol>
 * <li>the interface provides a number of useful methods to blend colors.</li>
 * <li>the implementation of all methods is optional because not all may be
 * adequate for every data-to-color mapping.</li>
 * <li>the <code>translate(...)</code> methods fill in arrays of type
 * <code>Object[]</code> or return <code>Object</code>'s, respectively. This
 * ambiguity is required to render the interface agnostic to the different
 * implementations.</li>
 * <li>Color is emulated in GWT and can be used to set and manipulate colors but
 * needs to be converted to to CSS (for <code>canvas</code>) or to Material (for
 * WebGL), respectively, before applying to graphics or the GUI.</li>
 * </ol>
 * <p>
 * <strong>See also:</strong> GWT emulation of Color in
 * org.evoludo.gwt.emulate.java.awt.Color
 * 
 * @author Christoph Hauert
 */
public abstract class ColorMap<T extends Object> {

	/**
	 * Assigns a Color to a <code>value</code>.
	 * 
	 * @param value the value which corresponds to the <code>color</code>
	 * @param color the color which represents the <code>value</code>
	 */
	public void setColor(double value, Color color) {
		throw new Error("ColorMap.setColor(double, Color) not implemented!");
	}

	/**
	 * Assigns a Color to a multi-dimensional <code>value</code>.
	 * 
	 * @param value the multi-dimensional value which corresponds to the
	 *              <code>color</code>
	 * @param color the color which represents the <code>value</code>
	 */
	public void setColor(double[] value, Color color) {
		throw new Error("ColorMap.setColor(double[], Color) not implemented!");
	}

	/**
	 * Set the range of data values that are mapped onto gradient colors. For
	 * ColorMap's dealing with multiple traits, the range for each trait is set.
	 * 
	 * @param min the minimum value
	 * @param max the maximum value
	 * 
	 * @throws IllegalArgumentException if <code>max&le;min</code>
	 */
	public void setRange(double min, double max) {
		throw new Error("ColorMap.setRange(double, double) not implemented!");
	}

	/**
	 * Set the range of data values that are mapped onto gradient colors for each
	 * trait separately.
	 * 
	 * @param min an array with the minimum values
	 * @param max an array with the maximum values
	 */
	public void setRange(double[] min, double[] max) {
		throw new Error("ColorMap.setRange(double[], double[]) not implemented!");
	}

	/**
	 * Set the range of data values that are mapped onto gradient colors for trait
	 * with index <code>idx</code>.
	 * 
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param idx the index of the trait whose range is set
	 */
	public void setRange(double min, double max, int idx) {
		throw new Error("ColorMap.setRange(double, double, int) not implemented!");
	}

	/**
	 * Translate the <code>int</code> value <code>data</code> to a color. The type
	 * of object returned depends on the implementation.
	 * 
	 * @param data the <code>int</code> value to convert to a color
	 * @return the color object
	 */
	public T translate(int data) {
		throw new Error("ColorMap.translate(int) not implemented!");
	}

	/**
	 * Translate the <code>double</code> value <code>data</code> to a color
	 * gradient. The type of object returned depends on the implementation.
	 * 
	 * @param data the <code>double</code> value to convert to a color
	 * @return the color object
	 */
	public T translate(double data) {
		throw new Error("ColorMap.translate(double) not implemented!");
	}

	/**
	 * Translate the multi-trait <code>double[]</code> array <code>data</code> to a
	 * color. The type of object returned depends on the implementation.
	 * 
	 * @param data the <code>double[]</code> array to convert to a color
	 * @return the color object
	 */
	public T translate(double[] data) {
		throw new Error("ColorMap.translate(double) not implemented!");
	}

	/**
	 * Translate the <code>data</code> array of <code>int</code> values to colors
	 * and store the results in the <code>color</code> array. The type of the
	 * <code>color</code> array depends on the implementation.
	 * 
	 * @param data  the <code>int[]</code> array to convert to colors
	 * @param color the array for the resulting colors
	 * @return <code>true</code> if translation successful
	 */
	public boolean translate(int[] data, T[] color) {
		throw new Error("ColorMap.translate(int[], Object[]) not implemented!");
	}

	/**
	 * Translate the <code>data</code> array of <code>double</code> values to colors
	 * and store the results in the <code>color</code> array. The type of the
	 * <code>color</code> array depends on the implementation.
	 * <p>
	 * <strong>Note:</strong> whether <code>data</code> refers to a single or
	 * multiple traits is up to the implementation to decide. For example,
	 * {@link org.evoludo.simulator.CXPopulation CXPopulation} stores multiple
	 * traits in a linear array.
	 * 
	 * @param data  the <code>double[]</code> array to convert to colors
	 * @param color the array for the resulting colors
	 * @return <code>true</code> if translation successful
	 */
	public boolean translate(double[] data, T[] color) {
		throw new Error("ColorMap.translate(double[], Object[]) not implemented!");
	}

	/**
	 * Translate the <code>data</code> array of <code>double[]</code> multi-trait
	 * values to colors and store the results in the <code>color</code> array. The
	 * type of the <code>color</code> array depends on the implementation.
	 * 
	 * @param data  the <code>double[]</code> array to convert to colors
	 * @param color the array for the resulting colors
	 * @return <code>true</code> if translation successful
	 */
	public boolean translate(double[][] data, T[] color) {
		throw new Error("ColorMap.translate(double[][], Object[]) not implemented!");
	}

	/**
	 * Translate the <code>data1</code> and <code>data2</code> arrays of
	 * <code>double[]</code> multi-trait values to colors and store the results in
	 * the <code>color</code> array. The type of the <code>color</code> array
	 * depends on the implementation.
	 * <p>
	 * For example, frequencies and fitnesses, say <code>data1</code> and
	 * <code>data2</code>, respectively, yield the average fitness as
	 * <code>data1&middot;data2</code> (dot product), which then gets converted to a
	 * color.
	 * 
	 * @param data1 the first <code>double[]</code> array to convert to colors
	 * @param data2 the second <code>double[]</code> array to convert to colors
	 * @param color the array for the resulting colors
	 * @return <code>true</code> if translation successful
	 */
	public boolean translate(double[][] data1, double[][] data2, T[] color) {
		throw new Error("ColorMap.translate(double[][], double[][], Object[]) not implemented!");
	}

	public abstract T getColorTemplate();

	/*
	 * dealing with alpha is not really needed - performance should be checked
	 * private static void interpolateColors(Color[] gradient, Color start, int
	 * first, Color end, int last) { int[] s = new int[] { start.getRed(),
	 * start.getGreen(), start.getBlue(), start.getAlpha() }; int[] e = new int[] {
	 * end.getRed(), end.getGreen(), end.getBlue(), end.getAlpha() }; for( int
	 * n=first; n<=last; n++ ) { double x = (double)(n-first)/(double)(last-first);
	 * gradient[n] = new Color((int)((1.0-x)*s[0]+x*e[0]+0.5),
	 * (int)((1.0-x)*s[1]+x*e[1]+0.5), (int)((1.0-x)*s[2]+x*e[2]+0.5),
	 * (int)((1.0-x)*s[3]+x*e[3]+0.5)); } }
	 */

	/**
	 * Utility method for adding the <span style="color:red;">red</span>,
	 * <span style="color:green;">green</span>,
	 * <span style="color:blue;">blue</span> and alpha components of two colors.
	 * 
	 * @param one the first color
	 * @param two the second color
	 * @return component-wise addition of the two colors
	 */
	public static Color addColors(Color one, Color two) {
		return new Color(Math.min(one.getRed() + two.getRed(), 255), Math.min(one.getGreen() + two.getGreen(), 255),
				Math.min(one.getBlue() + two.getBlue(), 255), Math.min(one.getAlpha() + two.getAlpha(), 255));
	}

	/**
	 * Utility method for the smooth, component-wise blending of two colors, where
	 * color <code>one</code> has weight <code>w1</code> (and color <code>two</code>
	 * has weight <code>(1-w1)</code>) and includes the alpha-channel.
	 * 
	 * @param one the first color
	 * @param two the second color
	 * @param w1  the weight of color <code>one</code>
	 * @return component-wise addition of the two colors
	 */
	public static Color blendColors(Color one, Color two, double w1) {
		double norm = 0.003921568627451; // 1/255
		double w2 = 1.0 - w1;
		return new Color((float) ((w1 * one.getRed() + w2 * two.getRed()) * norm),
				(float) ((w1 * one.getGreen() + w2 * two.getGreen()) * norm),
				(float) ((w1 * one.getBlue() + w2 * two.getBlue()) * norm),
				(float) ((w1 * one.getAlpha() + w2 * two.getAlpha()) * norm));
	}

	/**
	 * Utility method for the smooth, component-wise blending of multiple
	 * <code>colors</code> with respective <code>weights</code> (includes
	 * alpha-channel).
	 * 
	 * @param colors  the colors for blending
	 * @param weights the weights of each color
	 * @return component-wise blending of <code>colors</code>
	 */
	public static Color blendColors(Color[] colors, double[] weights) {
		int n = weights.length;
		if (colors.length < n)
			return Color.WHITE;
		// assume that weights are normalized to avoid overhead
		double red = 0.0, green = 0.0, blue = 0.0, alpha = 0.0;
		for (int i = 0; i < n; i++) {
			double w = weights[i];
			Color ci = colors[i];
			red += w * ci.getRed();
			green += w * ci.getGreen();
			blue += w * ci.getBlue();
			alpha += w * ci.getAlpha();
		}
		double norm = 0.00390625; // 1/256
		return new Color((float) (red * norm), (float) (green * norm), (float) (blue * norm), (float) (alpha * norm));
	}

	/**
	 * Utility method for creating a smooth gradient ranging from color
	 * <code>start</code> to color <code>end</code> in <code>last-first</code>
	 * steps. The color gradient is stored in the array <code>gradient</code> from
	 * elements <code>first</code> to <code>last</code> (inclusive).
	 * 
	 * @param gradient the array for storing the color gradient
	 * @param start    the starting color
	 * @param first    the index in <code>gradient</code> for the <code>start</code>
	 *                 color
	 * @param end      the end color
	 * @param last     the index in <code>gradient</code> for the <code>end</code>
	 *                 color
	 */
	public static void interpolateColors(Color[] gradient, Color start, int first, Color end, int last) {
		for (int n = first; n <= last; n++) {
			double x = (double) (n - first) / (double) (last - first);
			gradient[n] = ColorMap.blendColors(end, start, x);
		}
	}

	/**
	 * Associates integer indices with colors.
	 */
	public static class Index<G> extends ColorMap<G> {

		/**
		 * Array of generic colors <code>G</code>.
		 */
		protected G[] colors;

		/**
		 * Number of colors
		 */
		protected final int nColors;

		/**
		 * Construct a new Index color map.
		 * 
		 * @param colors the array of colors to assign to indices
		 */
		@SuppressWarnings("unchecked")
		public Index(Color[] colors) {
			this(colors.length);
			this.colors = (G[])Arrays.copyOf(colors, nColors);
		}

		protected Index(int size) {
			nColors = size;
		}

		@Override
		public G getColorTemplate() {
			return colors[0];
		}

		/**
		 * Assign a new <code>color</code> to <code>index</code>.
		 * 
		 * @param index the index of the <code>color</code>
		 * @param color the new color for <code>index</code>
		 */
		@SuppressWarnings("unchecked")
		public void setColor(int index, Color color) {
			if (index < 0 || index >= nColors)
				return;
			colors[index] = (G) color;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * <strong>Note:</strong> for performance reasons no validity checks are
		 * performed on the data. If any data entry is negative or
		 * <code>&gt;nColors</code> an {@linkplain ArrayIndexOutOfBoundsException} is
		 * thrown.
		 * 
		 * @return the color corresponding to <code>index</code>
		 */
		@Override
		public G translate(int index) {
			return colors[index];
		}

		/**
		 * Translate the <code>data</code> array of <code>int</code> values to colors
		 * and store the results in the <code>color</code> array. The type of the
		 * <code>color</code> array depends on the implementation.
		 * <p>
		 * <strong>Note:</strong> for performance reasons no validity checks are
		 * performed on the data. If any data entry is negative or
		 * <code>&gt;nColors</code> an {@linkplain ArrayIndexOutOfBoundsException} is
		 * thrown.
		 * 
		 * @param data  the <code>int[]</code> array to convert to colors
		 * @param color the array for the resulting colors
		 * @return <code>true</code> if translation successful
		 */
		@Override
		public boolean translate(int[] data, G[] color) {
			int len = data.length;
			for (int n = 0; n < len; n++)
				color[n] = colors[data[n]];
			return true;
		}
	}

	/**
	 * Color gradient following the hue.
	 * <p>
	 * The actual data-to-color translation is performed in the super class.
	 * 
	 * @see Gradient1D
	 */
	public static class Hue<G> extends Gradient1D<G> {

		/**
		 * Construct a new Hue color map, starting at a hue of <code>0.0</code> (red) up
		 * to hue <code>1.0</code> (red, again) and interpolate with
		 * <code>steps-1</code> intermediate colors. The resulting gradient spans
		 * <code>steps+1</code> colors. The default range for mapping data values onto
		 * the color gradient is <code>[0.0, 1.0]</code>.
		 * 
		 * @param steps the number of intermediate colors
		 * 
		 * @see #setRange(double, double)
		 */
		public Hue(int steps) {
			this(0.0, 1.0, steps);
		}

		/**
		 * Construct a new Hue color map, starting at hue <code>start</code> up to hue
		 * <code>end</code> (both in <code>[0.0, 1.0]</code>) and interpolate with
		 * <code>steps-1</code> intermediate colors. The resulting gradient spans
		 * <code>steps+1</code> colors. The default range for mapping data values onto
		 * the color gradient is <code>[0.0, 1.0]</code>.
		 * <p>
		 * <strong>Notes:</strong>
		 * <ol>
		 * <li><code>end&le;start</code> is acceptable and simply wraps around through
		 * red.</li>
		 * <li>with <code>steps&lt;0</code> the hue is interpolated in reverse.</li>
		 * <li>the saturation and brightness of the gradient are both maximal.</li>
		 * </ol>
		 * 
		 * @param start the starting hue
		 * @param end   the ending hue
		 * @param steps the number of intermediate colors
		 * 
		 * @see #setRange(double, double)
		 */
		@SuppressWarnings("unchecked")
		public Hue(double start, double end, int steps) {
			super(Math.abs(steps));
			double huevalue = start;
			double hueincr = Math.signum(steps) * ((end > start) ? (end - start) : (end + 1.0 - start)) / nGradient;
			gradient = (G[]) new Color[nGradient + 1];
			for (int c = 0; c < nGradient; c++) {
				gradient[c] = (G) new Color(Color.HSBtoRGB((float) huevalue, 1f, 1f));
				huevalue += hueincr;
			}
		}
	}

	/**
	 * One dimensional color gradient spanning two or more colors
	 */
	public static class Gradient1D<G> extends ColorMap<G> {

		/**
		 * Reference to pre-allocated gradient colors.
		 */
		protected G[] gradient;

		/**
		 * Number of colors in the gradient: <code>nGradient+1</code>.
		 */
		protected final int nGradient;

		/**
		 * Minimum data value. This value is mapped onto color <code>gradient[0]</code>.
		 */
		protected double min;

		/**
		 * Helper variable to make the mapping from a range of data values to indices of
		 * the gradient array more efficient: <code>map=nBins/(max-min)</code>, where
		 * <code>max</code> maximum data value to be mapped.
		 */
		protected double map;

		/**
		 * Flag indicating whether data is normalized. <code>true</code> if
		 * <code>min==0</code> and <code>max==1</code>. Allows for minor coding
		 * optimizations.
		 */
		private boolean isNormalized;

		/**
		 * Construct color gradient ranging from color <code>start</code> to color
		 * <code>end</code> and interpolate with <code>steps-1</code> intermediate
		 * colors. The resulting gradient spans <code>steps+1</code> colors. The default
		 * range for mapping data values onto the color gradient is
		 * <code>[0.0, 1.0]</code>.
		 * 
		 * @param start the starting color
		 * @param end   the ending color
		 * @param steps the number of intermediate colors
		 * 
		 * @see #setRange(double, double)
		 */
		public Gradient1D(Color start, Color end, int steps) {
			this(new Color[] { start, end }, steps);
		}

		/**
		 * Construct color gradient running through all the colors in the array
		 * <code>colors</code>. The number of interpolated intermediate colors between
		 * two subsequent entries in <code>colors</code> is
		 * <code>(steps-1)/(N-1)</code>, where <code>N</code> is the number of colors in
		 * <code>colors</code>. The resulting gradient spans <code>steps+1</code>
		 * colors. The default range for mapping data values onto the color gradient is
		 * <code>[0.0, 1.0]</code>.
		 * 
		 * @param colors the equally spaced reference colors of the gradient
		 * @param steps  the number of intermediate, gradient colors
		 * 
		 * @see #setRange(double, double)
		 */
		@SuppressWarnings("unchecked")
		public Gradient1D(Color[] colors, int steps) {
			this(steps);
			gradient = (G[]) new Color[nGradient + 1];
			int parts = colors.length - 1;
			for (int n = 0; n < parts; n++)
				interpolateColors((Color[])gradient, colors[n], n * nGradient / parts, colors[n + 1], (n + 1) * nGradient / parts);
		}

		/**
		 * For internal use only. Allocates an array of length <code>steps+1</code> for
		 * storing the color gradient. The maximum size of the gradient is
		 * <code>1000</code>. <code>steps&gt;1000</code> are ignored. The default range
		 * for mapping data values onto the color gradient is <code>[0.0, 1.0]</code>.
		 * 
		 * @param steps the number of gradient colors
		 * 
		 * @see #setRange(double, double)
		 */
		protected Gradient1D(int steps) {
			nGradient = Math.min(steps, 1000);
			setRange(0.0, 1.0);
		}

		@Override
		public G getColorTemplate() {
			return gradient[0];
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Invalid <code>value</code>'s are ignored. This includes any
		 * <code>value</code> outside the range for mapping data values.
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void setColor(double value, Color color) {
			if (!Double.isFinite(value)) // protect against NaN
				return;
			int bin = (int) ((value - min) * map + 0.5);
			if (bin < 0 || bin > nGradient)
				return;
			gradient[bin] = (G) color;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * <strong>Gradient1D:</strong> the value <code>min</code> is mapped onto the
		 * start color, <code>gradient[0]</code>, and <code>max</code> onto the end
		 * color, <code>gradient[steps]</code>.
		 */
		@Override
		public void setRange(double min, double max) {
			assert max > min;
			this.min = min;
			map = nGradient / (max - min);
			isNormalized = (Math.abs(min) < 1e-10 && Math.abs(max - 1.0) < 1e-10);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @return the gradient color corresponding to <code>data</code>
		 */
		@Override
		public G translate(double data) {
			int bin = (int) ((data - min) * map + 0.5);
			return gradient[bin];
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * <strong>Gradient1D:</strong> Each entry in <code>data</code> is converted to
		 * the corresponding gradient color and returned in the <code>color</code>
		 * array.
		 * <p>
		 * <strong>Important:</strong> For performance reasons no validity checks on
		 * <code>data</code>. In particular, all data entries must lie inside the range
		 * for mapping data values.
		 */
		@Override
		public boolean translate(double[] data, G[] color) {
			int len = data.length;
			if (isNormalized) {
				for (int n = 0; n < len; n++)
					color[n] = gradient[(int) (data[n] * nGradient + 0.5)];
				return true;
			}
			for (int n = 0; n < len; n++)
				color[n] = gradient[(int) ((data[n] - min) * map + 0.5)];
			return true;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see PDEReactionDiffusion
		 */
//		@Override
//		public boolean translate(double[][] data1, double[][] data2, G[] color) {
//			int len = color.length;
//			for (int n = 0; n < len; n++)
//				color[n] = translate(ArrayMath.dot(data1[n], data2[n]));
//			return true;
//		}
	}

	/**
	 * Two dimensional color gradient with one color for each dimension.
	 */
	public static class Gradient2D<G> extends ColorMap<G> {

		/**
		 * Reference to pre-allocated gradient colors.
		 */
		protected G[][] gradient;

		/**
		 * Number of colors in the gradient: <code>nGradient+1</code>.
		 */
		protected final int nGradient;

		/**
		 * Number of traits in the gradient: <code>nTraits=2</code>. This is only for
		 * convenience and readability of the code.
		 */
		protected final int nTraits;

		/**
		 * Minimum data value. This value is mapped onto color <code>gradient[0]</code>.
		 */
		protected double[] min;

		/**
		 * Helper variable to make the mapping from a range of data values to indices of
		 * the gradient array more efficient: <code>map=nBins/(max-min)</code>, where
		 * <code>max</code> maximum data value to be mapped.
		 */
		protected double[] map;

		/**
		 * Flag indicating whether data is normalized. <code>true</code> if
		 * <code>min==0</code> and <code>max==1</code> in both dimensions. Allows for
		 * minor coding optimizations.
		 */
		protected boolean isNormalized;

		/**
		 * Construct two dimensional color gradient to represent two dimensional data
		 * values. The first dimension spans colors ranging from black to
		 * <code>colors[0]</code> and the second dimension from black to
		 * <code>colors[1]</code>. Each dimension is interpolated with
		 * <code>steps-1</code> intermediate colors. The resulting gradient spans
		 * <code>(steps+1)<sup>2</sup></code> colors.
		 * <p>
		 * The default range for mapping data values onto the color gradient is
		 * <code>[0.0, 1.0]</code> in both dimensions.
		 * 
		 * @param colors the two colors for each dimension
		 * @param steps  the number of intermediate colors per dimension
		 * 
		 * @see #setRange(double, double)
		 * @see #setRange(double[], double[])
		 */
		public Gradient2D(Color[] colors, int steps) {
			this(colors[0], colors[1], steps);
		}

		/**
		 * Construct two dimensional color gradient to represent <em>three</em>
		 * dimensional data values where one data value is dependent on the other two.
		 * This applies, for example, to data based on the replicator equation and
		 * dynamics that unfold on the simplex <code>S<sub>N</sub></code>.
		 * <p>
		 * The index of the dependent trait is <code>idx</code>. It is at its maximum if
		 * both other traits are at their minimum. Thus, the color gradient in the first
		 * dimension ranges from <code>colors[idx]</code> to <code>colors[0]</code> and
		 * from <code>colors[idx]</code> to <code>colors[1]</code> in the second
		 * dimension, assuming that <code>idx=2</code> for the above illustration.
		 * <p>
		 * Each dimension is interpolated with <code>steps-1</code> intermediate colors.
		 * The resulting gradient spans <code>(steps+1)<sup>2</sup></code> colors.
		 * <p>
		 * The default range for mapping data values onto the color gradient is
		 * <code>[0.0, 1.0]</code> in all dimensions.
		 *
		 * @param colors the colors for the three dimensions
		 * @param idx    the index of the dependent trait
		 * @param steps  the number of intermediate colors per dimension
		 * 
		 * @see #setRange(double, double)
		 * @see #setRange(double[], double[])
		 */
		public Gradient2D(Color[] colors, int idx, int steps) {
			this(colors[idx == 0 ? 1 : 0], colors[idx == 0 || idx == 1 ? 2 : 1],
					idx >= 0 && idx < colors.length ? colors[idx] : Color.BLACK, steps);
		}

		/**
		 * Construct two dimensional color gradient to represent two dimensional data
		 * values. The first dimension spans colors ranging from black to
		 * <code>colors[0]</code> and the second dimension from black to
		 * <code>colors[1]</code>. Each dimension is interpolated with
		 * <code>steps-1</code> intermediate colors. The resulting gradient spans
		 * <code>(steps+1)<sup>2</sup></code> colors.
		 * <p>
		 * The default range for mapping data values onto the color gradient is
		 * <code>[0.0, 1.0]</code> in both dimensions.
		 * 
		 * @param trait1 the color representing the first trait
		 * @param trait2 the color representing the second trait
		 * @param steps  the number of intermediate colors per dimension
		 * 
		 * @see #setRange(double, double)
		 * @see #setRange(double[], double[])
		 */
		public Gradient2D(Color trait1, Color trait2, int steps) {
			this(trait1, trait2, Color.BLACK, steps);
		}

		/**
		 * Construct two dimensional color gradient to represent <em>two</em>
		 * dimensional data with a background color other than black, or, to represent
		 * <em>three</em> dimensional data values where one data value is dependent on
		 * the other two.
		 * <p>
		 * The color gradient in the first dimension ranges from <code>bg</code> to
		 * <code>trait1</code> and from <code>bg</code> to <code>trait2</code> in the
		 * second dimension.
		 * <p>
		 * Each dimension is interpolated with <code>steps-1</code> intermediate colors.
		 * The resulting gradient spans <code>(steps+1)<sup>2</sup></code> colors.
		 * <p>
		 * The default range for mapping data values onto the color gradient is
		 * <code>[0.0, 1.0]</code> in both (all) dimensions.
		 *
		 * @param trait1 the color representing the first trait
		 * @param trait2 the color representing the second trait
		 * @param bg     the color representing the background (or a third, dependent
		 *               trait)
		 * @param steps  the number of intermediate colors per dimension
		 * 
		 * @see #Gradient2D(Color[], int)
		 * @see #Gradient2D(Color, Color, int)
		 * @see #Gradient2D(Color[], int, int)
		 * @see #setRange(double, double)
		 * @see #setRange(double[], double[])
		 */
		@SuppressWarnings("unchecked")
		public Gradient2D(Color trait1, Color trait2, Color bg, int steps) {
			this(steps);
			gradient = (G[][]) new Color[nGradient + 1][nGradient + 1];
			double weight1 = 0.0;
			double wincr = 1.0 / nGradient;
			for (int i = 0; i <= nGradient; i++) {
				// trait 1
				Color start = ColorMap.blendColors(trait1, bg, weight1);
				Color end = ColorMap.addColors(start, trait2);
				interpolateColors((Color[])gradient[i], start, 0, end, nGradient);
				weight1 += wincr;
			}
		}

		/**
		 * For internal use only. Allocates a two dimensional
		 * <code>(steps+1)&times;(steps+1)</code> array for storing the color gradient.
		 * The maximum size of the gradient is capped at <code>100&times;100</code>.
		 * <code>steps&gt;100</code> are ignored. The default range for mapping data
		 * values onto the color gradient is <code>[0.0, 1.0]</code> in each dimension.
		 * 
		 * @param steps the number of gradient colors
		 * 
		 * @see #setRange(double, double)
		 */
		protected Gradient2D(int steps) {
			nTraits = 2;
			nGradient = Math.min(steps, 100);
			min = new double[nTraits];
			map = new double[nTraits];
			setRange(0.0, 1.0);
		}

		@Override
		public G getColorTemplate() {
			return gradient[0][0];
		}

		@SuppressWarnings("unchecked")
		@Override
		public void setColor(double[] value, Color color) {
			gradient[(int) ((value[0] - min[0]) * map[0] + 0.5)][(int) ((value[1] - min[1]) * map[1] + 0.5)] = (G) color;
		}

		@Override
		public void setRange(double min, double max) {
			this.min = new double[nTraits];
			Arrays.fill(this.min, min);
			Arrays.fill(this.map, nGradient / (max - min));
			isNormalized = (Math.abs(min) < 1e-10 && Math.abs(max - 1.0) < 1e-10);
		}

		@Override
		public void setRange(double[] min, double[] max) {
			setRange(min, max, -1);
		}

		/**
		 * Set the range of data values for <em>three</em> dimensional data with
		 * dependent trait. The index of the dependent trait is <code>idx</code>. The
		 * data range for the first trait is <code>[min[0], max[0]]</code> and for the
		 * second trait <code>[min[1], max[1]</code>, assuming that <code>idx=2</code>
		 * for the above illustration.
		 * 
		 * @param min an array with the minimum values
		 * @param max an array with the maximum values
		 * @param idx the index of the dependent trait
		 * 
		 * @see #Gradient2D(Color[], int, int)
		 */
		public void setRange(double[] min, double[] max, int idx) {
			this.min = new double[nTraits];
			isNormalized = true;
			int trait = 0;
			for (int n = 0; n < nTraits; n++) {
				if (n == idx)
					continue;
				double minn = min[n];
				double maxn = max[n];
				this.min[trait] = minn;
				map[trait] = nGradient / (maxn - minn);
				isNormalized &= (Math.abs(minn) < 1e-10 && Math.abs(maxn - 1.0) < 1e-10);
				trait++;
			}
		}

		@Override
		public void setRange(double min, double max, int idx) {
			this.min[idx] = min;
			map[idx] = nGradient / (max - min);
			isNormalized = true;
			for (int n = 0; n < nTraits; n++)
				isNormalized &= (Math.abs(this.min[n]) < 1e-10 && Math.abs(map[n]) < 1e-10);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @return the gradient color corresponding to <code>data</code>
		 */
		@Override
		public G translate(double[] data) {
			return translate(data[0], data[1]);
		}

		/**
		 * Helper method to translate the trait values <code>trait1</code> and
		 * <code>trait2</code> into color gradient.
		 * 
		 * @param trait1 the value of the first trait
		 * @param trait2 the value of the second trait
		 * @return the gradient Color based on <code>trait1</code> and
		 *         <code>trait2</code>
		 */
		private G translate(double trait1, double trait2) {
			if (isNormalized)
				return gradient[(int) (trait1 * nGradient + 0.5)][(int) (trait2 * nGradient + 0.5)];
			return gradient[(int) ((trait1 - min[0]) * map[0] + 0.5)][(int) ((trait2 - min[1]) * map[1] + 0.5)];
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * <strong>Gradient2D:</strong> Two subsequent entries, <code>data[n]</code> and
		 * <code>data[n+1]</code> are converted to the corresponding gradient colors.
		 * This applies, for example, to data from continuous games with multiple
		 * traits. The gradient colors are returned in the <code>color</code> array.
		 * <p>
		 * <strong>Important:</strong> For performance reasons no validity checks on
		 * <code>data</code>. In particular, all data entries must lie inside the range
		 * for mapping data values.
		 * 
		 * @see CXPopulation
		 */
		@Override
		public boolean translate(double[] data, G[] color) {
			int len = color.length;
			int idx = 0;
			if (isNormalized) {
				for (int n = 0; n < len; n++) {
					color[n] = gradient[(int) (data[idx] * nGradient + 0.5)][(int) (data[idx + 1] * nGradient + 0.5)];
					idx += nTraits;
				}
				return true;
			}
			double min0 = min[0], min1 = min[1];
			double map0 = map[0], map1 = map[1];
			for (int n = 0; n < len; n++) {
				color[n] = gradient[(int) ((data[idx] - min0) * map0 + 0.5)][(int) ((data[idx + 1] - min1) * map1
						+ 0.5)];
				idx += nTraits;
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * <strong>Gradient2D:</strong> Each entry in <code>data</code> represents a
		 * multi-dimensional trait value, which is converted to the corresponding
		 * gradient color and returned in the <code>color</code> array. An example are
		 * trait densities in PDE models.
		 * <p>
		 * <strong>Important:</strong> For performance reasons no validity checks on
		 * <code>data</code>. In particular, all data entries must lie inside the range
		 * for mapping data values.
		 * 
		 * @see PDEReactionDiffusion
		 */
		@Override
		public boolean translate(double[][] data, G[] color) {
//			return translate(data, -1, color);
			int len = color.length;
			if (isNormalized) {
				for (int n = 0; n < len; n++) {
					double[] datan = data[n];
					color[n] = gradient[(int) (datan[0] * nGradient + 0.5)][(int) (datan[1] * nGradient + 0.5)];
				}
				return true;
			}
			double min0 = min[0], min1 = min[1];
			double map0 = map[0], map1 = map[1];
			for (int n = 0; n < len; n++) {
				double[] datan = data[n];
				color[n] = gradient[(int) ((datan[0] - min0) * map0 + 0.5)][(int) ((datan[1] - min1) * map1 + 0.5)];
			}
			return true;
		}
	}

	/**
	 * <code>N</code> dimensional color gradient with one color for each dimension.
	 */
	public static class GradientND<G> extends ColorMap<G> {

		/**
		 * Reference to trait colors that provide the basis to generate gradient colors.
		 */
		protected Color[] traits;

		/**
		 * Reference to background color. The default is black.
		 */
		Color bg = Color.BLACK;

		/**
		 * Number of traits in the gradient.
		 */
		protected final int nTraits;

		/**
		 * Minimum data value.
		 */
		protected double[] min;

		/**
		 * Helper variable to make the mapping from a range of data values to gradients
		 * more efficient: <code>map=1.0/(max-min)</code>, where <code>min</code> and
		 * <code>max</code> represent the minimum and maximum data value to be mapped.
		 */
		protected double[] map;

		/**
		 * Flag indicating whether data is normalized. <code>true</code> if
		 * <code>min==0</code> and <code>max==1</code> in each dimension. Allows for
		 * minor coding optimizations.
		 */
		protected boolean isNormalized;

		/**
		 * Temporary variable to store the weights of each color component.
		 */
		protected double[] weights;

		/**
		 * Construct <code>N</code> dimensional color gradient to represent
		 * <code>N</code> dimensional data values. Each dimension <code>i</code> spans
		 * colors ranging from black to <code>colors[i]</code>.
		 * <p>
		 * The default range for mapping data values onto the color gradient is
		 * <code>[0.0, 1.0]</code> in all dimensions.
		 * 
		 * @param colors the colors for each dimension
		 * 
		 * @see #setRange(double, double)
		 * @see #setRange(double[], double[])
		 */
		public GradientND(Color[] colors) {
			this(colors.length);
			traits = Arrays.copyOf(colors, nTraits);
		}

		/**
		 * Construct <code>N</code> dimensional color gradient to represent
		 * <code>N</code> dimensional data with a background color other than black, or,
		 * to represent <code>N+1</code> dimensional data values where one data value is
		 * dependent on the other <code>N</code>.
		 * <p>
		 * The color gradient in each dimension <code>i</code> ranges from
		 * <code>bg</code> to <code>colors[i]</code>.
		 * <p>
		 * The default range for mapping data values onto the color gradient is
		 * <code>[0.0, 1.0]</code> in all dimensions.
		 *
		 * @param colors the colors for the <code>N</code> dimensions
		 * @param bg     the color representing the background (or an <code>N+1</code>,
		 *               dependent trait)
		 * 
		 * @see #setRange(double, double)
		 * @see #setRange(double[], double[])
		 */
		public GradientND(Color[] colors, Color bg) {
			this(colors);
			if (bg != null)
				this.bg = bg;
		}

		/**
		 * Construct <code>N</code> dimensional color gradient to represent
		 * <code>N+1</code> dimensional data values where one data value is dependent on
		 * the other two. This applies, for example, to data based on the replicator
		 * equation and dynamics that unfold on the simplex <code>S<sub>N</sub></code>.
		 * <p>
		 * The index of the dependent trait is <code>idx</code>. It is at its maximum if
		 * all other traits are at their minimum. The color gradient in each dimension
		 * <code>i</code> ranges from <code>colors[idx]</code> to <code>colors[i]</code>
		 * for <code>idx&ne;i</code>.
		 * <p>
		 * The default range for mapping data values onto the color gradient is
		 * <code>[0.0, 1.0]</code> in all dimensions.
		 *
		 * @param colors the colors for the <code>N</code> dimensions
		 * @param idx    the index of the dependent trait
		 * 
		 * @see #setRange(double, double)
		 * @see #setRange(double[], double[])
		 */
		public GradientND(Color[] colors, int idx) {
			this(idx < 0 || idx >= colors.length ? colors.length : colors.length - 1);
			traits = new Color[nTraits];
			int trait = 0;
			for (int n = 0; n < colors.length; n++) {
				if (n == idx)
					continue;
				traits[trait++] = colors[n];
			}
			if (idx >= 0 && idx < colors.length)
				bg = colors[idx];
		}

		/**
		 * For internal use only. Allocates memory and sets the default range for
		 * mapping data values onto the color gradient to <code>[0.0, 1.0]</code> in
		 * every dimension.
		 * 
		 * @param dim the dimension of the gradient colors
		 * 
		 * @see #setRange(double, double)
		 */
		protected GradientND(int dim) {
			nTraits = dim;
			weights = new double[nTraits];
			min = new double[nTraits];
			map = new double[nTraits];
			setRange(0.0, 1.0);
		}

		@Override
		@SuppressWarnings("unchecked")
		public G getColorTemplate() {
			return (G)Color.BLACK;
		}

		@Override
		public void setRange(double min, double max) {
			this.min = new double[nTraits];
			Arrays.fill(this.min, min);
			Arrays.fill(this.map, 1.0 / (max - min));
			isNormalized = (Math.abs(min) < 1e-10 && Math.abs(max - 1.0) < 1e-10);
		}

		@Override
		public void setRange(double min, double max, int idx) {
			this.min[idx] = min;
			map[idx] = 1.0 / (map[idx] * (max - min));
			isNormalized = true;
			for (int n = 0; n < nTraits; n++) {
				isNormalized &= (Math.abs(this.min[n]) < 1e-10 && Math.abs(map[n] - 1.0) < 1e-10);
			}
		}

		@Override
		public void setRange(double[] min, double[] max) {
			setRange(min, max, -1);
		}

		/**
		 * Set the range of data values for <em>three</em> dimensional data with
		 * dependent trait. The index of the dependent trait is <code>idx</code>. The
		 * data range for the first trait is <code>[min[0], max[0]]</code> and for the
		 * second trait <code>[min[1], max[1]</code>, assuming that <code>idx=2</code>
		 * for the above illustration.
		 * 
		 * @param min an array with the minimum values
		 * @param max an array with the maximum values
		 * @param idx the index of the dependent trait
		 * 
		 * @see Gradient2D#Gradient2D(Color[], int, int)
		 */
		public void setRange(double[] min, double[] max, int idx) {
			isNormalized = true;
			int trait = 0;
			for (int n = 0; n < nTraits; n++) {
				if (idx == n)
					continue;
				double minn = min[n];
				double maxn = max[n];
				this.min[trait] = minn;
				map[trait] = 1.0 / (maxn - minn);
				isNormalized &= (Math.abs(minn) < 1e-10 && Math.abs(maxn - 1.0) < 1e-10);
			}
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * <strong>GradientND:</strong> color gradients are generated on the fly.
		 * 
		 * @return the corresponding gradient color
		 */
		@SuppressWarnings("unchecked")
		@Override
		public G translate(double[] data) {
			if (isNormalized)
				return (G) blendColors(traits, data);
			for (int n = 0; n < nTraits; n++)
				weights[n] = (data[n] - min[n]) * map[n];
			return (G) blendColors(traits, weights);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * <strong>GradientND:</strong> <code>N</code> subsequent entries,
		 * <code>data[n]</code> through <code>data[n+N]</code>, are converted to
		 * gradient colors on the fly. This applies, for example, to data from
		 * continuous games with <code>N</code> traits. The gradient colors are returned
		 * in the <code>color</code> array.
		 * <p>
		 * <strong>Important:</strong> For performance reasons no validity checks on
		 * <code>data</code>. In particular, all data entries must lie inside the range
		 * for mapping data values.
		 * 
		 * @see CXPopulation
		 */
		@Override
		public boolean translate(double[] data, G[] color) {
			double[] datan = new double[nTraits];
			int len = color.length;
			int idx = 0;
			for (int n = 0; n < len; n++) {
				System.arraycopy(data, idx, datan, 0, nTraits);
				color[n] = translate(datan);
				idx += nTraits;
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * <strong>GradientND:</strong> Each entry in <code>data</code> represents a
		 * <code>N</code>-dimensional trait value, which is converted to gradient colors
		 * on the fly and returned in the <code>color</code> array. An example are trait
		 * densities in PDE models.
		 * <p>
		 * <strong>Important:</strong> For performance reasons no validity checks on
		 * <code>data</code>. In particular, all data entries must lie inside the range
		 * for mapping data values.
		 * 
		 * @see PDEReactionDiffusion
		 */
		@Override
		public boolean translate(double[][] data, G[] color) {
			int len = color.length;
			for (int n = 0; n < len; n++)
				color[n] = translate(data[n]);
			return true;
		}
	}
}
