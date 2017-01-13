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

/**
 * This class holds all configuration parmaeters to genertae charts
 */
public class ChartConfiguration {

	private int chartWidth = 1200;

	private int chartHeight = 600;

	private int distributionSliceSize = 20;

	private int distributionSliceNumber = 20;

	private int statisticalPeriod = 5;

	public ChartConfiguration(int chartHeight, int chartWidth, int distributionSliceNumber, int distributionSliceSize,
	                          int statisticalPeriod) {
		this.chartHeight = chartHeight;
		this.chartWidth = chartWidth;
		this.distributionSliceNumber = distributionSliceNumber;
		this.distributionSliceSize = distributionSliceSize;
		this.statisticalPeriod = statisticalPeriod;
	}

	public int getChartHeight() {
		return chartHeight;
	}

	public void setChartHeight(int chartHeight) {
		this.chartHeight = chartHeight;
	}

	public int getChartWidth() {
		return chartWidth;
	}

	public void setChartWidth(int chartWidth) {
		this.chartWidth = chartWidth;
	}

	public int getDistributionSliceNumber() {
		return distributionSliceNumber;
	}

	public void setDistributionSliceNumber(int distributionSliceNumber) {
		this.distributionSliceNumber = distributionSliceNumber;
	}

	public int getDistributionSliceSize() {
		return distributionSliceSize;
	}

	public void setDistributionSliceSize(int distributionSliceSize) {
		this.distributionSliceSize = distributionSliceSize;
	}

	public int getStatisticalPeriod() {
		return statisticalPeriod;
	}

	public void setStatisticalPeriod(int statisticalPeriod) {
		this.statisticalPeriod = statisticalPeriod;
	}
}
