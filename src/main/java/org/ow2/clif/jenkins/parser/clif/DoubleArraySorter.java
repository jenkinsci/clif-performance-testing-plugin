/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: clif@ow2.org
 */
package org.ow2.clif.jenkins.parser.clif;

/**
 * This class contains various methods for manipulating arrays (such as
 * sorting and searching).  This class also contains a static factory
 * that allows arrays to be viewed as lists.
 * <br>
 * All methods in this class throw a <code>NullPointerException</code> if
 * the specified array reference is null, except where noted.
 * <br>
 * The documentation for the methods contained in this class includes
 * briefs description of the <i>implementations</i>.  Such descriptions should
 * be regarded as <i>implementation notes</i>, rather than parts of the
 * <i>specification</i>.  Implementors should feel free to substitute other
 * algorithms, so long as the specification itself is adhered to.  (For
 * example, the algorithm used by <code>sort(Object[])</code> does not have to be
 * a mergesort, but it does have to be <i>stable</i>.)
 * <br>
 * This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Josh Bloch
 * @author Neal Gafter
 * @author John Rose
 * @version 1.71, 04/21/06
 * @since 1.2
 */

public class DoubleArraySorter {
	// Suppresses default constructor, ensuring non-instantiability.
	private DoubleArraySorter() {
	}

	// Sorting

	/**
	 * Sorts the specified arrays of doubles into ascending numerical order.
	 * Only the first array is used to sort. The second array is sorted using
	 * the same order as the first one
	 * <br>
	 * The <code>&lt;</code> relation does not provide a total order on
	 * all floating-point values; although they are distinct numbers
	 * <code>-0.0 == 0.0</code> is <code>true</code> and a NaN value
	 * compares neither less than, greater than, nor equal to any
	 * floating-point value, even itself.  To allow the sort to
	 * proceed, instead of using the <code>&lt;</code> relation to
	 * determine ascending numerical order, this method uses the total
	 * order imposed by {@link Double#compareTo}.  This ordering
	 * differs from the <code>&lt;</code> relation in that
	 * <code>-0.0</code> is treated as less than <code>0.0</code> and
	 * NaN is considered greater than any other floating-point value.
	 * For the purposes of sorting, all NaN values are considered
	 * equivalent and equal.
	 * <br>
	 * The sorting algorithm is a tuned quicksort, adapted from Jon
	 * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
	 * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
	 * 1993).  This algorithm offers n*log(n) performance on many data sets
	 * that cause other quicksorts to degrade to quadratic performance.
	 *
	 * @param first  the array to be sorted
	 * @param second second array to be sorted
	 */
	public static void sort(double[] first, double[] second) {
		sort2(first, second, 0, first.length);
	}

	/**
	 * Sorts the specified range of the specified arrays of doubles into
	 * ascending numerical order.
	 * Sorts the specified arrays of doubles into ascending numerical order.
	 * Only the first array is used to sort. The second array is sorted using
	 * the same order as the first one
	 * The range to be sorted extends from index
	 * <code>fromIndex</code>, inclusive, to index <code>toIndex</code>, exclusive.
	 * (If <code>fromIndex==toIndex</code>, the range to be sorted is empty.)
	 * <br>
	 * The <code>&lt;</code> relation does not provide a total order on
	 * all floating-point values; although they are distinct numbers
	 * <code>-0.0 == 0.0</code> is <code>true</code> and a NaN value
	 * compares neither less than, greater than, nor equal to any
	 * floating-point value, even itself.  To allow the sort to
	 * proceed, instead of using the <code>&lt;</code> relation to
	 * determine ascending numerical order, this method uses the total
	 * order imposed by {@link Double#compareTo}.  This ordering
	 * differs from the <code>&lt;</code> relation in that
	 * <code>-0.0</code> is treated as less than <code>0.0</code> and
	 * NaN is considered greater than any other floating-point value.
	 * For the purposes of sorting, all NaN values are considered
	 * equivalent and equal.
	 * <br>
	 * The sorting algorithm is a tuned quicksort, adapted from Jon
	 * L. Bentley and M. Douglas McIlroy's "Engineering a Sort Function",
	 * Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
	 * 1993).  This algorithm offers n*log(n) performance on many data sets
	 * that cause other quicksorts to degrade to quadratic performance.
	 *
	 * @param first     the array to be sorted
	 * @param second    second array to be sorted
	 * @param fromIndex the index of the first element (inclusive) to be
	 *                  sorted
	 * @param toIndex   the index of the last element (exclusive) to be sorted
	 * @throws IllegalArgumentException       if <code>fromIndex &gt; toIndex</code>
	 * @throws ArrayIndexOutOfBoundsException if <code>fromIndex &lt; 0</code> or
	 *                                        <code>toIndex &gt; a.length</code>
	 */
	public static void sort(double[] first, double[] second, int fromIndex, int toIndex) {
		rangeCheck(first.length, fromIndex, toIndex);
		sort2(first, second, fromIndex, toIndex);
	}

	private static void sort2(double first[], double[] second, int fromIndex, int toIndex) {
		final long NEG_ZERO_BITS = Double.doubleToLongBits(-0.0d);
		/*
				 * The sort is done in three phases to avoid the expense of using
				 * NaN and -0.0 aware comparisons during the main sort.
				 */

		/*
				 * Preprocessing phase:  Move any NaN's to end of array, count the
				 * number of -0.0's, and turn them into 0.0's.
				 */
		int numNegZeros = 0;
		int i = fromIndex, n = toIndex;
		while (i < n) {
			if (first[i] != first[i]) {
				double swap = first[i];
				double swap2 = second[i];
				first[i] = first[--n];
				second[i] = second[n + 1];
				first[n] = swap;
				second[n] = swap2;
			}
			else {
				if (first[i] == 0 && Double.doubleToLongBits(first[i]) == NEG_ZERO_BITS) {
					first[i] = 0.0d;
					numNegZeros++;
				}
				i++;
			}
		}

		// Main sort phase: quicksort everything but the NaN's
		sort1(first, second, fromIndex, n - fromIndex);

		// Postprocessing phase: change 0.0's to -0.0's as required
		if (numNegZeros != 0) {
			int j = binarySearch0(first, fromIndex, n, 0.0d); // posn of ANY zero
			do {
				j--;
			}
			while (j >= 0 && first[j] == 0.0d);

			// j is now one less than the index of the FIRST zero
			for (int k = 0; k < numNegZeros; k++) {
				first[++j] = -0.0d;
			}
		}
	}

	/**
	 * Sorts the specified sub-array of doubles into ascending order.
	 */
	private static void sort1(double[] first, double[] second, int off, int len) {
		// Insertion sort on smallest arrays
		if (len < 7) {
			for (int i = off; i < len + off; i++) {
				for (int j = i; j > off && first[j - 1] > first[j]; j--) {
					swap(first, j, j - 1);
					swap(second, j, j - 1);
				}
			}
			return;
		}

		// Choose a partition element, v
		int m = off + (len >> 1);       // Small arrays, middle element
		if (len > 7) {
			int l = off;
			int n = off + len - 1;
			if (len > 40) {        // Big arrays, pseudomedian of 9
				int s = len / 8;
				l = med3(first, l, l + s, l + 2 * s);
				m = med3(first, m - s, m, m + s);
				n = med3(first, n - 2 * s, n - s, n);
			}
			m = med3(first, l, m, n); // Mid-size, med of 3
		}
		double v = first[m];

		// Establish Invariant: v* (<v)* (>v)* v*
		int a = off, b = a, c = off + len - 1, d = c;
		while (true) {
			while (b <= c && first[b] <= v) {
				if (first[b] == v) {
					swap(first, a++, b);
					swap(second, a - 1, b);
				}
				b++;
			}
			while (c >= b && first[c] >= v) {
				if (first[c] == v) {
					swap(first, c, d--);
					swap(second, c, d + 1);
				}
				c--;
			}
			if (b > c) {
				break;
			}
			swap(first, b++, c--);
			swap(second, b - 1, c + 1);
		}

		// Swap partition elements back to middle
		int s, n = off + len;
		s = Math.min(a - off, b - a);
		vecswap(first, off, b - s, s);
		vecswap(second, off, b - s, s);
		s = Math.min(d - c, n - d - 1);
		vecswap(first, b, n - s, s);
		vecswap(second, b, n - s, s);

		// Recursively sort non-partition-elements
		if ((s = b - a) > 1) {
			sort1(first, second, off, s);
		}
		if ((s = d - c) > 1) {
			sort1(first, second, n - s, s);
		}
	}

	/**
	 * Swaps x[a] with x[b].
	 */
	private static void swap(double x[], int a, int b) {
		double t = x[a];
		x[a] = x[b];
		x[b] = t;
	}

	/**
	 * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
	 */
	private static void vecswap(double x[], int a, int b, int n) {
		for (int i = 0; i < n; i++, a++, b++) {
			swap(x, a, b);
		}
	}

	/**
	 * Returns the index of the median of the three indexed doubles.
	 */
	private static int med3(double x[], int a, int b, int c) {
		return (x[a] < x[b] ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a) : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
	}

	/**
	 * Check that fromIndex and toIndex are in range, and throw an
	 * appropriate exception if they aren't.
	 */
	private static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
		if (fromIndex > toIndex) {
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		}
		if (fromIndex < 0) {
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		}
		if (toIndex > arrayLen) {
			throw new ArrayIndexOutOfBoundsException(toIndex);
		}
	}

	// Searching

	// Like public version, but without range checks.
	private static int binarySearch0(double[] a, int fromIndex, int toIndex, double key) {
		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			double midVal = a[mid];

			int cmp;
			if (midVal < key) {
				cmp = -1;   // Neither val is NaN, thisVal is smaller
			}
			else if (midVal > key) {
				cmp = 1;    // Neither val is NaN, thisVal is larger
			}
			else {
				long midBits = Double.doubleToLongBits(midVal);
				long keyBits = Double.doubleToLongBits(key);
				cmp = (midBits == keyBits ? 0 : // Values are equal
						(midBits < keyBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
								1));                     // (0.0, -0.0) or (NaN, !NaN)
			}

			if (cmp < 0) {
				low = mid + 1;
			}
			else if (cmp > 0) {
				high = mid - 1;
			}
			else {
				return mid; // key found
			}
		}
		return -(low + 1);  // key not found.
	}


}
