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
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;
import org.ow2.clif.jenkins.parser.clif.Messages;

/**
 * Class used to generate and save a chart attach to a build
 *
 * @author Julien Coste
 */
public class CallChart
		extends AbstractChart {

	protected boolean scatterPlot;

	protected final XYSeries eventSerie;

	public CallChart(String testplan, String bladeId, String event, ChartConfiguration chartConfiguration) {
		super("Call", bladeId, testplan, event, chartConfiguration);
		this.eventSerie = new XYSeries(event);
	}

	public void setScatterPlot(boolean scatterPlot) {
		this.scatterPlot = scatterPlot;
	}

	public void addData(double x, double y) {
		this.eventSerie.add(x, y);
	}

	@Override
	protected JFreeChart createChart() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(this.eventSerie);

		JFreeChart chart;
		if (this.scatterPlot) {
			chart = ChartFactory.createScatterPlot(getBasicTitle(),
			                                       // chart title
			                                       Messages.CallChart_Time(), // x axis label
			                                       Messages.CallChart_ResponseTime(), // y axis label
			                                       dataset, // data
			                                       PlotOrientation.VERTICAL, true, // include legend
			                                       true, // tooltips
			                                       false // urls
			);
		}
		else {
			chart = ChartFactory.createXYLineChart(getBasicTitle(),
			                                       // chart title
			                                       Messages.CallChart_Time(), // x axis label
			                                       this.chartId.getEvent(), // y axis label
			                                       dataset, // data
			                                       PlotOrientation.VERTICAL, false, // include legend
			                                       true, // tooltips
			                                       false // urls
			);
		}

		chart.setBackgroundPaint(Color.white);
		// get a reference to the plot for further customisation...
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		Shape cross = ShapeUtilities.createDiamond(3);

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setBaseShape(cross);
		renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesShape(0, cross);
		plot.setRenderer(renderer);

		// Force the 0 on vertical axis
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(true);
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// Force the 0 on horizontal axis
		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(true);
		return chart;
	}


}
