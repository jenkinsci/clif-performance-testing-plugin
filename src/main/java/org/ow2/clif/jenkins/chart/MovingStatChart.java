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
package org.ow2.clif.jenkins.chart;

import java.awt.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.ow2.clif.jenkins.chart.movingstatistics.*;
import org.ow2.clif.jenkins.parser.clif.Messages;
import static org.ow2.clif.jenkins.parser.clif.Messages.*;

/**
 * Class used to generate and save a chart attach to a build
 *
 * @author Julien Coste
 */
public class MovingStatChart
		extends AbstractChart {

	protected final XYSeries eventSerie;

	public MovingStatChart(String testplan, String bladeId, String event, ChartConfiguration chartConfiguration) {
		super("movingStat", bladeId, testplan, event, chartConfiguration);
		this.eventSerie = new XYSeries(event);
	}


	public void addData(double x, double y) {
		this.eventSerie.add(x, y);
	}

	@Override
	protected JFreeChart createChart() {
		XYSeriesCollection coreDataset = new XYSeriesCollection();
		coreDataset.addSeries(this.eventSerie);

		long periodMs = this.chartConfiguration.getStatisticalPeriod() * 1000L;

		XYSeriesCollection movingDataset = calculateMovingDataset(coreDataset, periodMs);
		XYSeriesCollection throughputDataset = calculateThroughputDataset(coreDataset, periodMs);

		JFreeChart chart;
		chart = ChartFactory.createXYLineChart(
				getBasicTitle() + " " +
						Messages.MovingChart_StatisticalPeriod(this.chartConfiguration.getStatisticalPeriod()),
				// chart title
				Messages.MovingChart_Time(), // x axis label
				Messages.MovingChart_ResponseTime(), // y axis label
				movingDataset, // data
				PlotOrientation.VERTICAL, true, // include legend
				true, // tooltips
				false // urls
		);

		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		XYPlot plot = (XYPlot) chart.getPlot();
		configureBasicPlotProperties(plot);

		// Force the 0 on vertical axis
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(true);
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// Force the 0 on horizontal axis
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(true);

		attachThroughputDatasetToDedicatedAxis(throughputDataset, plot);

		// Global renderer for moving stats
		plot.setRenderer(getGlobalRenderer());

		// Dedicated Throughput renderer
		plot.setRenderer(1, getThroughputRenderer());

		return chart;
	}

	private XYSeriesCollection calculateThroughputDataset(XYSeriesCollection coreDataset, long periodMs) {
		MovingThroughputStat throughputStat = new MovingThroughputStat(periodMs);
		XYSeries throughputSeries =
				throughputStat.calculateMovingStat(coreDataset, 0, MovingChart_MovingThroughput(), periodMs, 0);
		XYSeriesCollection throughputDataset = new XYSeriesCollection();
		throughputDataset.addSeries(throughputSeries);
		return throughputDataset;
	}

	private XYSeriesCollection calculateMovingDataset(XYSeriesCollection coreDataset, long periodMs) {
		MovingAverageStat averageStat = new MovingAverageStat();
		XYSeries movingSeries =
				averageStat.calculateMovingStat(coreDataset, 0, MovingChart_MovingAverage(), periodMs, 0);
		MovingMaxStat maxStat = new MovingMaxStat();
		XYSeries maxSeries = maxStat.calculateMovingStat(coreDataset, 0, MovingChart_MovingMax(), periodMs, 0);
		MovingMinStat minStat = new MovingMinStat();
		XYSeries minSeries = minStat.calculateMovingStat(coreDataset, 0, MovingChart_MovingMin(), periodMs, 0);
		MovingMedianStat medianStat = new MovingMedianStat();
		XYSeries medianSeries = medianStat.calculateMovingStat(coreDataset, 0, MovingChart_MovingMedian(), periodMs, 0);
		MovingStdDevStat stdDevStat = new MovingStdDevStat();
		XYSeries stdDevSeries = stdDevStat.calculateMovingStat(coreDataset, 0, MovingChart_MovingStdDev(), periodMs, 0);

		XYSeriesCollection movingDataset = new XYSeriesCollection();
		movingDataset.addSeries(movingSeries);
		movingDataset.addSeries(maxSeries);
		movingDataset.addSeries(minSeries);
		movingDataset.addSeries(medianSeries);
		movingDataset.addSeries(stdDevSeries);
		return movingDataset;
	}

	private void configureBasicPlotProperties(XYPlot plot) {
		plot.setBackgroundPaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
	}

	private void attachThroughputDatasetToDedicatedAxis(XYSeriesCollection throughputDataset, XYPlot plot) {
		NumberAxis throughputAxis = new NumberAxis(Messages.MovingChart_Throughput());
		plot.setRangeAxis(1, throughputAxis);
		plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		plot.setDataset(1, throughputDataset);
		plot.mapDatasetToDomainAxis(1, 0);
		plot.mapDatasetToRangeAxis(1, 1);
	}

	private XYLineAndShapeRenderer getThroughputRenderer() {
		final XYLineAndShapeRenderer rendererThroughput = new XYLineAndShapeRenderer();
		rendererThroughput.setSeriesShapesVisible(0, false);
		rendererThroughput.setSeriesPaint(0, Color.MAGENTA);
		rendererThroughput.setSeriesStroke(0, new BasicStroke(1));
		return rendererThroughput;
	}

	private XYLineAndShapeRenderer getGlobalRenderer() {
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesShapesVisible(0, false);
		renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setSeriesStroke(0, new BasicStroke(1));
		renderer.setSeriesShapesVisible(1, false);
		renderer.setSeriesPaint(1, Color.RED);
		renderer.setSeriesStroke(1, new BasicStroke(1));
		renderer.setSeriesShapesVisible(2, false);
		renderer.setSeriesPaint(2, Color.GREEN);
		renderer.setSeriesStroke(2, new BasicStroke(1));
		renderer.setSeriesShapesVisible(3, false);
		renderer.setSeriesPaint(3, Color.YELLOW);
		renderer.setSeriesStroke(3, new BasicStroke(1));
		renderer.setSeriesShapesVisible(4, false);
		renderer.setSeriesPaint(4, Color.ORANGE);
		renderer.setSeriesStroke(4, new BasicStroke(1));
		return renderer;
	}


}
