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
package org.ow2.clif.jenkins.chart.movingstatistics;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * Abtract class to calculate moving statistics.
 */
public abstract class AbstractMovingStat {

	/**
	 * Check that these arguments are valid
	 *
	 * @param source DataSet that contain the XYSeries
	 * @param period Period of time to use
	 * @param skip   the length of the initial skip period
	 */
	private static void checkArguments(XYDataset source, double period, double skip) {
		// check arguments
		if (source == null) {
			throw new IllegalArgumentException("Null source (XYDataset).");
		}

		if (period < 0.0) {
			throw new IllegalArgumentException("statisticalPeriod must be positive.");

		}

		if (skip < 0.0) {
			throw new IllegalArgumentException("skip must be >= 0.0.");

		}
	}


	/**
	 * Creates a new {@link org.jfree.data.xy.XYSeries} containing the moving statistic of one
	 * series in the <code>source</code> dataset.
	 *
	 * @param source the source dataset.
	 * @param series the series index (zero based).
	 * @param name   the name for the new series.
	 * @param period the period.
	 * @param skip   the length of the initial skip period.
	 * @return The dataset.
	 */
	public XYSeries calculateMovingStat(XYDataset source, int series, String name, double period, double skip) {
		checkArguments(source, period, skip);

		XYSeries result = new XYSeries(name);
		if (source.getItemCount(series) > 0) {
			// if the initial averaging period is to be excluded, then
			// calculate the lowest x-value to have an average calculated...
			double first = source.getXValue(series, 0) + skip;
			double lastXEndPeriod = source.getXValue(series, source.getItemCount(series) - 1);
			for (int i = source.getItemCount(series) - 1; i >= 0; i--) {
				// get the current data item...
				double x = source.getXValue(series, i);
				while (lastXEndPeriod >= x + period) {
					lastXEndPeriod -= period;
				}
				if (x >= first) {
					// work out the average for the earlier values...
					resetMovingStat();
					double limit = x - period;
					int offset = 0;
					boolean finished = false;

					while (!finished) {
						if ((i - offset) >= 0) {
							double xx = source.getXValue(series, i - offset);
							Number yy = source.getY(series, i - offset);
							if (xx > limit) {
								if (yy != null) {
									calculateMovingStatInPeriod(xx, yy.doubleValue());
									offset = offset + 1;
								}
							}
							else {
								finished = true;
							}
						}
						else {
							finished = true;
						}
					}
					addMovingStatForPeriod(result, lastXEndPeriod - (period / 2));
					i -= offset - 1;
				}
			}
		}
		return result;
	}

	/**
	 * Reset the movinf statistic just before calculating a new one
	 */
	public abstract void resetMovingStat();

	/**
	 * Calculate the current moving statistic with a new point
	 *
	 * @param xx x value of the point
	 * @param yy Y value of the point
	 */
	protected abstract void calculateMovingStatInPeriod(double xx, double yy);

	/**
	 * Add the moving statistic to the resulting XYSeries
	 *
	 * @param result Resulting XYSeries
	 * @param x      X value where to add the moving statistic
	 */
	protected abstract void addMovingStatForPeriod(XYSeries result, double x);
}
