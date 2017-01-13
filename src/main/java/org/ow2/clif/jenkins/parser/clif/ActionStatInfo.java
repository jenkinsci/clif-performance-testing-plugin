/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012 France Telecom R&D
 * Copyright (C) 2016 Orange SA
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

import java.io.File;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.util.ResizableDoubleArray;
import org.ow2.clif.jenkins.chart.*;

/**
 * @author Julien Coste
 * @author Bruno Dillenseger
 */
public class ActionStatInfo {

	protected static final int MIN_SIZE_OF_STATISTICAL_DATA = 30;

	// Used to compute statistics (Mean, Sum, Min,  Max)
	protected final SummaryStatistics onTheFlyStat;

	protected final ParsingContext context;

	private final ChartConfiguration chartConfiguration;

	protected ResizableDoubleArray values = new ResizableDoubleArray();
	protected ResizableDoubleArray dates = new ResizableDoubleArray();

	protected double[] valuesArray;
	protected double[] datesArray;

	private long errors;

	protected boolean statsAvailable;

	private DescriptiveStatistics stat;

	private CallChart callChart;

	private MovingStatChart movingStatChart;

	private FixedSliceNumberDistributionChart fixedSliceNumberDistributionChart;

	private FixedSliceSizeDistributionChart fixedSliceSizeDistributionChart;

	private QuantileDistributionChart quantileDistributionChart;

	private static long firstCall = Long.MAX_VALUE;

	private static long lastCall = Long.MIN_VALUE;

	static public synchronized void resetTime()
	{
		firstCall = Long.MAX_VALUE;
		lastCall = Long.MIN_VALUE;
	}

	static public synchronized void addCallTime(long date)
	{
		// Calculate test period
		if (date < firstCall) {
			firstCall = date;
		}
		if (date > lastCall) {
			lastCall = date;
		}
	}

	public ActionStatInfo(final ParsingContext context, final ChartConfiguration chartConfiguration) {
		this.context = new ParsingContext(context);
		this.onTheFlyStat = new SummaryStatistics();
		this.chartConfiguration = chartConfiguration;
	}


	public void addStat(final long date, final double value) {
		// Store values
		values.addElement(value);
		dates.addElement(date);

		onTheFlyStat.addValue(value);
		addCallTime(date);
	}

	public void incrementErrors() {
		errors++;
	}


	public long getErrors() {
		return errors;
	}


	public double getThroughput() {
		checkState();
		if (lastCall == firstCall) {
			return -1;
		}
		return ((double) (stat.getN() * 1000)) / (lastCall - firstCall);
	}


	public double getMean() {
		checkState();
		return stat.getMean();
	}

	public double getStandardDeviation() {
		checkState();
		return stat.getStandardDeviation();
	}

	public double getMax() {
		checkState();
		return stat.getMax();
	}

	public double getMin() {
		checkState();
		return stat.getMin();
	}

	public long getN() {
		checkState();
		return stat.getN();
	}

	public double getPercentile(double p) {
		checkState();
		return stat.getPercentile(p);
	}

	/**
	 * Compute statistics and build associated graphs
	 */
	public void compute() {
		// get double arrays
		valuesArray = values.getElements();
		datesArray = dates.getElements();

		if (context.dataCleanup && (context.getBlade() == null || context.getBlade().isInjector())) {
			dataCleanup();
		}

		// Build stat objet
		stat = new DescriptiveStatistics();
		for (double aValuesArray : valuesArray) {
			stat.addValue(aValuesArray);
		}

		// Build detailled graph
		callChart = createCallChart();
		if (context.getBlade() == null || context.getBlade().isInjector()) {
			movingStatChart = createMovingStatChart();
			// Build distribution graph
			fixedSliceNumberDistributionChart = createFixedSliceNumberDistributionChart();
			fixedSliceSizeDistributionChart = createFixedSliceSizeDistributionChart();
			quantileDistributionChart = createQuantileDistributionChart();
		}
		statsAvailable = true;
	}

	/**
	 * Throw an {@link IllegalStateException} if stats have not been computed.
	 */
	private void checkState() {
		if (!this.statsAvailable) {
			throw new IllegalStateException("Statistics have not been computed");
		}
	}

	protected void dataCleanup() {
		int count;
		int statNb = (int) onTheFlyStat.getN();
		int minStatIndex = 0;
		int maxStatIndex = statNb - 1;
		if (statNb < 3) {
			return;
		}

		// apply statistical rejection only if the number of values is
		// greater than statisticalSortPercentage % of initial data number and
		// in respect of MIN_SIZE_OF_STATISTICAL_DATA
		int minSize = (int) Math.ceil((context.getKeepPercentage() * statNb / 100));
		if (minSize < MIN_SIZE_OF_STATISTICAL_DATA) {
			minSize = MIN_SIZE_OF_STATISTICAL_DATA;
		}
		// ready to reject value(s) out of statistical range
		double statMean = onTheFlyStat.getMean();
		double statStd = onTheFlyStat.getStandardDeviation();
		double statSum = onTheFlyStat.getSum();
		double statDev = onTheFlyStat.getSumsq();

		DoubleArraySorter.sort(valuesArray, datesArray);

		double[] data = valuesArray;
		while (statNb > minSize) {
			double minVal = data[minStatIndex];
			double maxVal = data[maxStatIndex];
			double lowerOutOfRange = Math.round(Math.ceil(statMean - context.getKeepFactor() * statStd)) - minVal;
			double upperOutOfRange = maxVal - Math.round(Math.floor(statMean + context.getKeepFactor() * statStd));
			// checks if statistical rejection must be applied
			if (lowerOutOfRange > upperOutOfRange) {
				if (lowerOutOfRange > 0) {
					// statistical rejection can be applied
					// try to include all-same value in the current removing
					for (count = 1, minStatIndex++; data[minStatIndex] == minVal; minStatIndex++) {
						count++;
					}
					// check if count doesn't pass the min size
					if (count > (statNb - minSize)) {
						minStatIndex -= count + minSize - statNb;
						count = statNb - minSize;
					}
					statSum -= count * minVal;
					statDev -= count * minVal * minVal;
					statNb -= count;
				}
				else {
					break;
				}
			}
			else {
				if (upperOutOfRange > 0) {
					// statistical rejection can be applied
					// try to include all-same value in the current removing
					for (count = 1, maxStatIndex--; data[maxStatIndex] == maxVal; maxStatIndex--) {
						count++;
					}
					// check if count doesn't pass the min size
					if (count > (statNb - minSize)) {
						maxStatIndex += count + minSize - statNb;
						count = statNb - minSize;
					}
					statSum -= count * maxVal;
					statDev -= count * maxVal * maxVal;
					statNb -= count;
				}
				else {
					break;
				}
			}
			// then (if no break) compute new statistical values for next loop
			statMean = statSum / statNb;
			statStd = (statDev - (statSum * statSum / statNb)) / (statNb - 1);
			statStd = Math.sqrt(statStd);
		}

		int newLength = maxStatIndex - minStatIndex + 1;
		double[] newValuesArray = new double[newLength];
		double[] newDatesArray = new double[newLength];

		System.arraycopy(valuesArray, minStatIndex, newValuesArray, 0, newLength);
		System.arraycopy(datesArray, minStatIndex, newDatesArray, 0, newLength);

		valuesArray = newValuesArray;
		datesArray = newDatesArray;
	}


	/**
	 * Create a {@link MovingStatChart} and populate the chart
	 *
	 * @return Chart build from the context and collected values
	 */
	private MovingStatChart createMovingStatChart() {
		MovingStatChart chart =
				new MovingStatChart(context.getTestPlanShortName(), getBladeId(context), context.getEventType(),
				                    chartConfiguration);

		for (int i = 0; i < valuesArray.length; i++) {
			chart.addData(datesArray[i], valuesArray[i]);
		}
		return chart;
	}

	private String getBladeId(final ParsingContext context) {
		if (context.getBlade() == null) {
			return "aggregated";
		}
		return context.getBlade().getId();
	}

	/**
	 * Create a {@link CallChart} and populate the chart
	 *
	 * @return Chart build from the context and collected values
	 */
	private CallChart createCallChart() {
		final CallChart chart =
				new CallChart(context.getTestPlanShortName(), getBladeId(context), context.getEventType(),
				              this.chartConfiguration);
		if (context.getBlade() != null && context.getBlade().isInjector()) {
			chart.setScatterPlot(true);
		}

		for (int i = 0; i < valuesArray.length; i++) {
			chart.addData(datesArray[i], valuesArray[i]);
		}
		return chart;
	}

	/**
	 * Create a {@link FixedSliceNumberDistributionChart} and populate the chart
	 *
	 * @return Chart build from the context and collected values
	 */
	private FixedSliceNumberDistributionChart createFixedSliceNumberDistributionChart() {

		final FixedSliceNumberDistributionChart chart =
				new FixedSliceNumberDistributionChart(context.getTestPlanShortName(), getBladeId(context),
				                                      context.getEventType(), chartConfiguration);
		chart.addData(valuesArray);
		return chart;
	}

	/**
	 * Create a {@link FixedSliceSizeDistributionChart} and populate the chart
	 *
	 * @return Chart build from the context and collected values
	 */
	private FixedSliceSizeDistributionChart createFixedSliceSizeDistributionChart() {

		final FixedSliceSizeDistributionChart chart =
				new FixedSliceSizeDistributionChart(context.getTestPlanShortName(), getBladeId(context),
				                                    context.getEventType(), chartConfiguration);
		chart.addData(valuesArray, stat.getMin(), stat.getMax());
		return chart;
	}

	/**
	 * Create a {@link QuantileDistributionChart} and populate the chart
	 *
	 * @return Chart build from the context and collected values
	 */
	private QuantileDistributionChart createQuantileDistributionChart() {
		final QuantileDistributionChart chart =
				new QuantileDistributionChart(context.getTestPlanShortName(), getBladeId(context),
				                              context.getEventType(), this.chartConfiguration);

		chart.addData(stat);
		return chart;
	}

	public void generateCharts(final File rootDir) {
		callChart.createChart(rootDir);
		if (movingStatChart != null) {
			movingStatChart.createChart(rootDir);
		}
		if (fixedSliceNumberDistributionChart != null) {
			fixedSliceNumberDistributionChart.createChart(rootDir);
		}
		if (fixedSliceSizeDistributionChart != null) {
			fixedSliceSizeDistributionChart.createChart(rootDir);
		}
		if (quantileDistributionChart != null) {
			quantileDistributionChart.createChart(rootDir);
		}
	}

}