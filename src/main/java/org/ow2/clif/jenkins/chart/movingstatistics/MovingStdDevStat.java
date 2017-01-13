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

import org.apache.commons.math.stat.descriptive.DescriptiveStatisticsImpl;
import org.jfree.data.xy.XYSeries;


/**
 * Calculate the moving stabdard deviation of time series data.
 */
public class MovingStdDevStat extends AbstractMovingStat {

	DescriptiveStatisticsImpl stat = new DescriptiveStatisticsImpl();
	int n = 0;


	@Override
	public void resetMovingStat() {
		this.stat = new DescriptiveStatisticsImpl();
		this.n = 0;
	}

	@Override
	protected void calculateMovingStatInPeriod(double xx, double yy) {
		stat.addValue(yy);
		n = n + 1;
	}

	@Override
	protected void addMovingStatForPeriod(XYSeries result, double x) {
		if (n > 0) {
			result.add(x, stat.getStandardDeviation());
		}
		else {
			result.add(x, null);
		}
	}

}
