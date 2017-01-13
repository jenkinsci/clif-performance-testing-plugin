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

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.ow2.clif.jenkins.parser.clif.Messages;

/**
 * Quantile Distribution Chart.<br>
 * This chart displays the percentage of requests whose response time is less
 * than some thresholds. The number of thresholds is set by a ChartConfiguration
 * object. Thresholds range from zero to the maximum response time.
 * For example, such a chart makes it possible to visualize that 90% of response
 * times are less than some computed value.
 *
 * @author Julien Coste
 */
public class QuantileDistributionChart
		extends AbstractChart {

	protected final DefaultCategoryDataset data;

	public QuantileDistributionChart(
		String testplan,
		String bladeId,
		String event,
		ChartConfiguration chartConfiguration)
	{
		super("QuantileDistribution", bladeId, testplan, event, chartConfiguration);
		this.data = new DefaultCategoryDataset();
	}

	public void addData(DescriptiveStatistics stat)
	{
		for (int i = 5; i <= 100; i += 5)
		{
			this.data.addValue(stat.getPercentile(i), this.chartId.getEvent(), "" + i);
		}
	}

	@Override
	protected JFreeChart createChart()
	{
		JFreeChart chart = ChartFactory.createBarChart(
			getBasicTitle(),
			Messages.QuantileDistributionChart_PercentageOfRequests(),
			Messages.QuantileDistributionChart_ResponseTime(),
			data,
			PlotOrientation.VERTICAL,
			true,
			true,
			false);
		chart.getCategoryPlot().setRangeGridlinesVisible(true);
		chart.getCategoryPlot().setDomainGridlinesVisible(true);
		return chart;
	}
}
