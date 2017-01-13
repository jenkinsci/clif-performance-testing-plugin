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
package org.ow2.clif.jenkins.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Class that holds statistics for a measure
 *
 * @author Julien Coste
 * @author Bruno Dillenseger
 */
public class Measure {

	private transient NumberFormat percentFormat;
	private transient NumberFormat dataFormat;
	private transient NumberFormat doubleFormat;

	private long size;
	private long countErrors;
	private long average;
	private long median;
	private long min;
	private long max;
	private double stdDev;
	private double throughput;
	private String name;

	public Measure() {
		percentFormat = new DecimalFormat("#.##%");
		doubleFormat = new DecimalFormat("#.##");
		dataFormat = new DecimalFormat("#,###");
	}

	public Measure(String name, long size, long average, long median, long min, long max, double stdDev,
	               double throughput, long countErrors) {
		this();
		this.size = size;
		this.countErrors = countErrors;
		this.average = average;
		this.median = median;
		this.min = min;
		this.max = max;
		this.stdDev = stdDev;
		this.throughput = throughput;
		this.name = name;
	}

	private Object readResolve() {
		percentFormat = new DecimalFormat("#.##%");
		doubleFormat = new DecimalFormat("#.##");
		dataFormat = new DecimalFormat("#,###");
		return this;
	}

	public long countActions()
	{
		return countErrors + size;
	}

	public long countErrors() {
		return countErrors;
	}

	public double errorPercent() {
		return ((double) countErrors) / (size + countErrors);
	}

	public String errorPercentFormated() {
		return percentFormat.format(errorPercent());
		//return MessageFormat.format("{0,number,#.##%}", errorPercent());
	}

	public long getAverage() {
		return average;
	}

	public String getAverageFormated() {
		return dataFormat.format(getAverage());
	}

	public void setAverage(long average) {
		this.average = average;
	}

	public long getMedian() {
		return median;
	}

	public String getMedianFormated() {
		return dataFormat.format(getMedian());
	}

	public void setMedian(long median) {
		this.median = median;
	}

	public long getMax() {
		return max;
	}

	public String getMaxFormated() {
		return dataFormat.format(getMax());
	}

	public void setMax(long max) {
		this.max = max;
	}

	public long getMin() {
		return min;
	}

	public String getMinFormated() {
		return dataFormat.format(getMin());
	}

	public void setMin(long min) {
		this.min = min;
	}

	public long getSize() {
		return size;
	}

	public String getSizeFormated() {
		return dataFormat.format(getSize());
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setCountErrors(long countErrors) {
		this.countErrors = countErrors;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getStdDev() {
		return stdDev;
	}

	public String getStdDevFormated() {
		return doubleFormat.format(stdDev);
	}

	public void setStdDev(double stdDev) {
		this.stdDev = stdDev;
	}

	public double getThroughput() {
		return throughput;
	}

	public String getThroughputFormated() {
		return doubleFormat.format(throughput);
	}

	public void setThroughput(double throughput) {
		this.throughput = throughput;
	}
}
